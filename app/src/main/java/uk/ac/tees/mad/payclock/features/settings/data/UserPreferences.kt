package uk.ac.tees.mad.payclock.features.settings.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class UserPreferences(
    @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String = "",
    @get:PropertyName("theme_choice") @set:PropertyName("theme_choice") var themeChoice: String = "SYSTEM",
    @get:PropertyName("use_dynamic_color") @set:PropertyName("use_dynamic_color") var useDynamicColor: Boolean = true,
    @get:PropertyName("display_name") @set:PropertyName("display_name") var displayName: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("last_updated") @set:PropertyName("last_updated") var lastUpdated: Timestamp = Timestamp.now(),
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Timestamp = Timestamp.now()
) {
    companion object {
        const val COLLECTION_NAME = "user_preferences"
    }

    fun toMap(): Map<String, Any> = mapOf(
        "user_id" to userId,
        "theme_choice" to themeChoice,
        "use_dynamic_color" to useDynamicColor,
        "display_name" to displayName,
        "email" to email,
        "last_updated" to Timestamp.now(),
        "created_at" to createdAt
    )
}