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
import androidx.compose.ui.tooling.preview.Preview
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

/**
 * Data class to hold user information for the drawer, decoupled from the ViewModel.
 *
 * @param displayName The name of the user.
 * @param photoUrl The URL of the user's profile picture.
 */
private data class DrawerData(val displayName: String?, val photoUrl: String?)

/**
 * Saves a bitmap to the cache and returns its URI.
 *
 * @param ctx The context.
 * @param bitmap The bitmap to save.
 * @return The URI of the saved bitmap, or null if an error occurred.
 */
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

/**
 * A composable that displays the application drawer. This component is stateful and
 * responsible for handling user authentication state and actions.
 *
 * @param drawerState The state of the drawer.
 * @param scope The coroutine scope.
 * @param navController The navigation controller.
 * @param authViewModel The view model for authentication.
 */
@Composable
fun AppDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authUser by authViewModel.currentUser.collectAsState()
    val user = authUser?.let { DrawerData(it.displayName, it.photoUrl?.toString()) }

    var profilePictureUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user?.photoUrl) {
        user?.photoUrl?.let {
            profilePictureUrl = it
        }
    }

    val localScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                profilePictureUrl = it.toString()
                authViewModel.updateProfilePicture(it.toString()) { result ->
                    localScope.launch {
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Profile picture updated")
                        } else {
                            handleUploadError(result.exceptionOrNull(), snackbarHostState)
                        }
                    }
                }
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                val tempUri = saveBitmapToCache(context, it)
                profilePictureUrl = tempUri?.toString()

                tempUri?.let { uri ->
                    authViewModel.updateProfilePicture(uri.toString()) { result ->
                        localScope.launch {
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Profile picture updated")
                            } else {
                                handleUploadError(result.exceptionOrNull(), snackbarHostState)
                            }
                        }
                    }
                }
                    ?: localScope.launch { snackbarHostState.showSnackbar("Failed to prepare captured image") }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                cameraLauncher.launch(null)
            } else {
                localScope.launch {
                    snackbarHostState.showSnackbar("Camera permission is required to take photos.")
                }
            }
        }
    )

    ModalDrawerSheet {
        AppDrawerContent(
            user = user,
            profilePictureUrl = profilePictureUrl,
            snackbarHostState = snackbarHostState,
            onTakePhotoClick = {
                val cameraPermissionStatus =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (cameraPermissionStatus == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onChooseFromGalleryClick = { pickImageLauncher.launch("image/*") },
            onSettingsClick = {
                scope.launch { drawerState.close() }
                navController.navigate("settings")
            },
            onLogoutClick = {
                scope.launch { drawerState.close() }
                authViewModel.signOut()
                navController.navigate("login") {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        )
    }
}

/**
 * A stateless composable that displays the content of the application drawer.
 *
 * @param user The user data to display.
 * @param profilePictureUrl The URL of the profile picture to display.
 * @param snackbarHostState The state for showing snackbars.
 * @param onTakePhotoClick The action to perform when the take photo button is clicked.
 * @param onChooseFromGalleryClick The action to perform when the choose from gallery button is clicked.
 * @param onSettingsClick The action to perform when the settings item is clicked.
 * @param onLogoutClick The action to perform when the logout item is clicked.
 */
@Composable
private fun AppDrawerContent(
    user: DrawerData?,
    profilePictureUrl: String?,
    snackbarHostState: SnackbarHostState,
    onTakePhotoClick: () -> Unit,
    onChooseFromGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    SnackbarHost(hostState = snackbarHostState)

    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
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
                IconButton(onClick = onTakePhotoClick) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Take photo")
                }
                IconButton(onClick = onChooseFromGalleryClick) {
                    Icon(Icons.Default.Photo, contentDescription = "Choose from gallery")
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
            onClick = onSettingsClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
            label = { Text("Logout") },
            selected = false,
            onClick = onLogoutClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

/**
 * Handles an error that occurred during a profile picture upload.
 *
 * @param exception The exception that occurred.
 * @param snackbarHostState The snackbar host state.
 */
private fun handleUploadError(exception: Throwable?, snackbarHostState: SnackbarHostState) {
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
    }
}

/**
 * A composable that displays a placeholder for a gallery icon.
 */
@Composable
fun IconGalleryPlaceholder() {
    Icon(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "Gallery Icon",
        modifier = Modifier.size(24.dp)
    )
}

@Preview(showBackground = true, name = "App Drawer Preview")
@Composable
fun AppDrawerContentPreview() {
    MaterialTheme {
        AppDrawerContent(
            user = DrawerData("John Doe", null),
            profilePictureUrl = null,
            snackbarHostState = remember { SnackbarHostState() },
            onTakePhotoClick = {},
            onChooseFromGalleryClick = {},
            onSettingsClick = {},
            onLogoutClick = {}
        )
    }
}