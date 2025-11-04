package uk.ac.tees.mad.payclock.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import uk.ac.tees.mad.payclock.data.Job
import uk.ac.tees.mad.payclock.api.RetrofitClient
import uk.ac.tees.mad.payclock.api.DjangoApiService

/**
 * Repository for managing both local persistence and network synchronization
 * of Job profiles with the Django API.
 */
class JobRepository(context: Context) {

    // --- Local Persistence (SharedPreferences Cache) ---
    private val sharedPreferences = context.getSharedPreferences(
        "job_preferences",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val JOB_LIST_KEY = "job_list_json"

    // --- Network API Service ---
    private val apiService: DjangoApiService = RetrofitClient.apiService


    // =========================================================================
    // LOCAL CACHE IMPLEMENTATION (Used by ViewModel's initialization/observation)
    // =========================================================================

    /**
     * Retrieves the list of jobs from the local cache (SharedPreferences).
     * @return A list of [Job] objects. Returns an empty list if no data is found.
     */
    fun getJobs(): List<Job> {
        val json = sharedPreferences.getString(JOB_LIST_KEY, null)
        return if (json == null) {
            emptyList()
        } else {
            val type = object : TypeToken<List<Job>>() {}.type
            gson.fromJson(json, type)
        }
    }

    /**
     * Saves the current list of jobs to the local cache (SharedPreferences).
     * This is called by the ViewModel whenever the local state changes.
     */
    fun saveJobs(jobList: List<Job>) {
        val json = gson.toJson(jobList)
        sharedPreferences.edit()
            .putString(JOB_LIST_KEY, json)
            .apply()
    }

    // =========================================================================
    // NETWORK SYNCHRONIZATION IMPLEMENTATION
    // =========================================================================

    /**
     * Fetches the latest list of jobs from the Django API.
     * @return The list of [Job] objects from the server.
     * @throws Exception if the network request fails or returns an error.
     */
    suspend fun fetchJobsFromApi(): List<Job> {
        val response = apiService.getJobs()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        } else {
            // Log error, throw exception, or handle specific HTTP error codes
            throw Exception("Failed to fetch jobs from API: ${response.code()}")
        }
    }

    /**
     * Pushes a new job to the Django API.
     * @param job The [Job] to be created on the server.
     * @return The created [Job] object, usually with a new server-assigned ID.
     * @throws Exception if the network request fails.
     */
    suspend fun createJobOnApi(job: Job): Job {
        val response = apiService.createJob(job)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        } else {
            throw Exception("Failed to create job on API: ${response.code()}")
        }
    }

    // You would add updateJobOnApi and deleteJobOnApi here...
}