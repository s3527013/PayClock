package uk.ac.tees.mad.payclock.data


object AuthManager {
    // This value should be loaded from EncryptedSharedPreferences on app start
    // and saved whenever it changes.
    var sessionToken: String? = null
}