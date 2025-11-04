package uk.ac.tees.mad.payclock.api


import okhttp3.Interceptor
import okhttp3.Response
import uk.ac.tees.mad.payclock.data.AuthManager

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = AuthManager.sessionToken

        // Exclude the header if the token is null or empty
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Add the X-Session-Token header required by allauth.headless
        val newRequest = originalRequest.newBuilder()
            .header("X-Session-Token", token)
            .build()

        return chain.proceed(newRequest)
    }
}