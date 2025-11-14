package uk.ac.tees.mad.payclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainViewModel(application: Application) : AndroidViewModel(application) {

    fun logout() {
        Firebase.auth.signOut()
    }
}
