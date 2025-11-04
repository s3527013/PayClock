package uk.ac.tees.mad.payclock.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import uk.ac.tees.mad.payclock.data.Job

/**
 * Retrofit interface defining the API endpoints for the Django server.
 */
interface ApiService {

    @GET("api/jobs/")
    suspend fun getJobs(): List<Job>

    @POST("api/jobs/")
    suspend fun addJob(@Body job: Job): Job

    @DELETE("api/jobs/{id}/")
    suspend fun deleteJob(@Path("id") jobId: String): Response<Unit> // Response<Unit> for empty responses
}
