package ai.p2ach.p2achandroidvision.utils

import ai.p2ach.p2achandroidvision.repos.camera.UploadPendingCaptureReportsWorker
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object WorkerManagerUtil {


    private const val WORK_NAME_UPLOAD_CAPTURE = "upload_capture_pending"

    fun enqueueUploadPendingCaptures(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<UploadPendingCaptureReportsWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                WORK_NAME_UPLOAD_CAPTURE,
                ExistingWorkPolicy.KEEP,
                request
            )
    }


}