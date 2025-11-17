package uk.ac.tees.mad.payclock.features.auth.data

/**
 * Represents a user object received from the Django REST Framework backend.
 *
 * This data class is optimized for compatibility with a standard Django User model
 * and the `django-allauth` package. It uses `kotlinx.serialization` for JSON
 * parsing and is `Parcelable` to be passed between Android components.
 *
 * Note: This class represents a user *received from* the server. For actions like
 * user registration or login, it's better practice to use separate data classes
 * that include fields like a plain-text password.
 */
data class User(
    /**
     * The unique integer ID for the user, as provided by the Django backend.
     */
    val id: Int,

    /**
     * The user's chosen username.
     */
    val username: String,

    /**
     * The user's email address.
     */
    val email: String,

    /**
     * The user's first name. Maps to the `first_name` field in Django's User model.
     * Can be null if not provided.
     */
    val firstName: String?,

    /**
     * The user's last name. Maps to the `last_name` field in Django's User model.
     * Can be null if not provided.
     */
    val lastName: String?,

    ) {
    companion object {
        fun create(
            id: Int,
            username: String,
            email: String,
            firstName: String?,
            lastName: String?,
        ): User {
            return User(id, username, email, firstName, lastName)
        }
    }
}