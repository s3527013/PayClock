package uk.ac.tees.mad.payclock

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import uk.ac.tees.mad.payclock.core.Graph

/**
 * The main [Application] class for the PayClock app.
 *
 * This class is the entry point of the application and is responsible for
 * initializing global application state, such as dependency injection and Firebase services.
 *
 * It is annotated with [@HiltAndroidApp] to enable dependency injection with Hilt.
 */
@HiltAndroidApp
class PayClockApplication : Application() {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     *
     * This method initializes the following:
     * - The [Graph] for service locator pattern.
     * - Firebase services through [FirebaseApp.initializeApp].
     * - Firebase App Check with Play Integrity to protect backend resources.
     */
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
        FirebaseApp.initializeApp(this) // Initialize Firebase
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
}