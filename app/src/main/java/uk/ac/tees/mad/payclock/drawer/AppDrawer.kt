package uk.ac.tees.mad.payclock.drawer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.R
import uk.ac.tees.mad.payclock.features.auth.AuthViewModel
import java.io.File
import java.io.FileOutputStream

// Helper function to save Bitmap to cache
fun saveBitmapToCache(ctx: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(ctx.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        Log.e("AppDrawer", "Failed to save bitmap to cache", e)
        null
    }
}

@Composable
fun AppDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val user by authViewModel.currentUser.collectAsState()
    // Local state for the profile picture URL
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }

    // Effect to initialize profilePictureUrl when user data changes
    LaunchedEffect(user?.photoUrl) {
        user?.photoUrl?.let {
            profilePictureUrl = it.toString()
        }
    }

    val localScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Image picker launcher (gallery)
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Update local state immediately for instant UI feedback
                profilePictureUrl = it.toString()
                authViewModel.updateProfilePicture(it.toString()) { result -> // Convert Uri to String here
                    localScope.launch {
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Profile picture updated")
                        } else {
                            handleUploadError(result.exceptionOrNull(), snackbarHostState)
                            // Optionally reset profilePictureUrl if upload fails, to reflect the last known good state
                            // For simplicity, we are not resetting here, but you could store the previous valid URL.
                        }
                    }
                }
            }
        }
    )

    // Camera launcher: TakePicturePreview returns a Bitmap
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = {
            // This callback receives a Bitmap directly
            it?.let { bitmap ->
                val tempUri = saveBitmapToCache(context, bitmap)
                // Update local state with the cached image URI as a String
                profilePictureUrl = tempUri?.toString()

                tempUri?.let { uri ->
                    authViewModel.updateProfilePicture(uri.toString()) { result -> // Convert Uri to String here
                        localScope.launch {
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Profile picture updated")
                            } else {
                                handleUploadError(result.exceptionOrNull(), snackbarHostState)
                                // Optionally reset profilePictureUrl if upload fails
                            }
                        }
                    }
                }
                    ?: localScope.launch { snackbarHostState.showSnackbar("Failed to prepare captured image") }
            }
        }
    )

    // Permission launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, launch camera
                cameraLauncher.launch(null)
            } else {
                // Permission denied
                localScope.launch {
                    snackbarHostState.showSnackbar("Camera permission is required to take photos.")
                }
            }
        }
    )

    ModalDrawerSheet {
        SnackbarHost(hostState = snackbarHostState)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                // Use the local profilePictureUrl state
                model = profilePictureUrl ?: user?.photoUrl,
                contentDescription = "Profile Picture",
                placeholder = painterResource(id = R.drawable.ic_user_placeholder),
                error = painterResource(id = R.drawable.ic_user_placeholder),
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cameraPermissionStatus = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    )
                    if (cameraPermissionStatus == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(null) // Launch camera directly if permission granted
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA) // Request permission
                    }
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Take photo")
                }
                IconButton(onClick = { pickImageLauncher.launch("image/*") }) {
                    Icon(
                        Icons.Default.Photo,
                        contentDescription = "Choose from gallery"
                    ) // Using Material 3 Photo icon
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user?.displayName ?: "Guest",
                style = MaterialTheme.typography.titleMedium
            )
        }
        HorizontalDivider()

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                navController.navigate("settings") // Navigate to the settings route
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
            label = { Text("Logout") },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                authViewModel.signOut() // Call the signlogout()
                navController.navigate("login") {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

// Helper function to handle upload errors
private fun handleUploadError(exception: Throwable?, snackbarHostState: SnackbarHostState) {
    // Note: LocalContext.current cannot be used directly here as it's not a Composable.
    // You might need to pass context or a CoroutineScope if you intend to show Snackbars from here.
    exception?.let {
        val msg = it.message ?: "Unknown error"
        val permissionIssue = msg.contains("permission", ignoreCase = true)
                || msg.contains("Permission denied", ignoreCase = true)
                || msg.contains("does not have permission to access object", ignoreCase = true)
                || it is com.google.firebase.storage.StorageException

        val errorMessage = if (permissionIssue) {
            "Upload failed: You don't have permission to access that Storage object. Check your Firebase Storage security rules."
        } else {
            "Failed to update profile picture: $msg"
        }
        Log.e("AppDrawer", errorMessage, it)
        // To show a Snackbar, you would need a CoroutineScope from the calling composable.
        // Example: scope.launch { snackbarHostState.showSnackbar(errorMessage) }
    }
}

// Dummy placeholder for a gallery icon if you don't have one.
// You should replace this with your actual drawable resource.
@Composable
fun IconGalleryPlaceholder() {
    Icon(
        painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your actual gallery icon
        contentDescription = "Gallery Icon",
        modifier = Modifier.size(24.dp) // Adjust size as needed
    )
}
