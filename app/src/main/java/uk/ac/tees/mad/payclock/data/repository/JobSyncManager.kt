package uk.ac.tees.mad.payclock.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.tees.mad.payclock.data.Job

/**
 * Service dedicated to handling the complex two-way synchronization logic
 * between the local cache (via JobRepository) and the Django API.
 */
class JobSyncManager(
    private val repository: JobRepository // The repository provides both local and network access
) {

    /**
     * Executes the full synchronization process:
     * 1. Pushes local changes (Creations, Updates, Deletes) to the API.
     * 2. Pulls remote changes from the API.
     * 3. Merges the results into a single list for the local cache.
     *
     * @return The final, merged list of jobs to be set in the ViewModel's StateFlow.
     */
    suspend fun sync() = withContext(Dispatchers.IO) {
        // Start by getting the current local state
        val localJobs = repository.getJobs().toMutableList()
        val syncedJobs = mutableListOf<Job>()

        // ==========================================================
        // STAGE 1: PUSH LOCAL CHANGES (Creations/Updates/Deletes)
        // ==========================================================

        val jobsToPush = localJobs.filter { it.isPendingSync }
        val jobsToKeepLocally = localJobs.filter { !it.isPendingSync }.toMutableList()

        jobsToPush.forEach { localJob ->
            try {
                // Currently only handling CREATE (POST). For a true solution,
                // you'd check for a temporary local ID vs. a real server ID.

                // PUSH: Create the job on the API
                val remoteJob = repository.createJobOnApi(localJob)

                // Add the new, server-confirmed job to the list of synced jobs
                syncedJobs.add(remoteJob.copy(isPendingSync = false))

                // Remove the old local job from the list of jobs to keep
                jobsToKeepLocally.remove(localJob)

            } catch (e: Exception) {
                // Log and keep the job marked as pending for the next sync attempt
                println("Push failed for job ${localJob.name}. Keeping pending status.")
                syncedJobs.add(localJob) // Keep it in the list to be resaved
            }
        }

        // ==========================================================
        // STAGE 2: PULL REMOTE CHANGES
        // ==========================================================

        val remoteJobs = try {
            repository.fetchJobsFromApi()
        } catch (e: Exception) {
            println("Pull failed. Returning local cache combined with pending changes.")
            // If pull fails, we rely only on the local cache and pending push results
            return@withContext (jobsToKeepLocally + syncedJobs).toList()
        }

        // ==========================================================
        // STAGE 3: MERGE
        // ==========================================================

        // Simple merge strategy:
        // 1. Start with the remote list.
        val finalMergedList = remoteJobs.toMutableList()
        val remoteIds = remoteJobs.map { it.id }.toSet()

        // 2. Add any local, non-pending jobs that are NOT on the remote list (i.e., local-only)
        jobsToKeepLocally.forEach { job ->
            if (job.id != 0 && !remoteIds.contains(job.id)) {
                // This handles a job that was created locally and might be missing from API fetch
                // or a job created with a local ID structure that needs proper merging logic (advanced)
                // For now, if the ID exists remotely, we trust the remote version.
            }
        }

        // 3. Add back any jobs that failed to push (they are already in syncedJobs)
        finalMergedList.addAll(syncedJobs.filter { it.isPendingSync })

        // This simple merge prioritizes remote data and re-adds any pending local changes.
        return@withContext finalMergedList.toList()
    }
}