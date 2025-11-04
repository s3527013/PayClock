package uk.ac.tees.mad.payclock.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // IMPORTANT: Replace this with your actual Django API base URL
    private const val BASE_URL = "https://your-django-api-domain.com/"

    // Configure the OkHttpClient with interceptors
    private val client = OkHttpClient.Builder()
        // 1. Logging Interceptor: For debugging API requests/responses. Set level to NONE for release.
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        // 2. Auth Interceptor: Adds the X-Session-Token header to all authenticated requests.
        .addInterceptor(AuthInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // The Retrofit instance
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Lazy-initialized instance of the DjangoApiService interface.
     * Use this object to call your API methods (e.g., RetrofitClient.apiService.getJobs()).
     */
    val apiService: DjangoApiService by lazy {
        retrofit.create(DjangoApiService::class.java)
    }
}