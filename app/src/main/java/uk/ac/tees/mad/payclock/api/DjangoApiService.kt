package uk.ac.tees.mad.payclock.api


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import uk.ac.tees.mad.payclock.data.AuthResponse
import uk.ac.tees.mad.payclock.data.Credentials
import uk.ac.tees.mad.payclock.data.Job

interface DjangoApiService {

    // --- Authentication Endpoints (no token required) ---
    @POST("/_allauth/app/v1/auth/login")
    suspend fun login(@Body credentials: Credentials): AuthResponse<Any>

    // --- Job Endpoints (token required via interceptor) ---

    // Pull: Fetch all jobs (or implement ?modified_since filtering later)
    @GET("api/jobs/")
    suspend fun getJobs(): Response<List<Job>>

    // Push: Create a new job
    @POST("api/jobs/")
    suspend fun createJob(@Body job: Job): Response<Job>

    // Push: Update an existing job
    @PUT("api/jobs/{id}/")
    suspend fun updateJob(@Path("id") id: Int, @Body job: Job): Response<Job>

    // Push: Delete a job
    @DELETE("api/jobs/{id}/")
    suspend fun deleteJob(@Path("id") id: Int): Response<Unit>
}