package ai.p2ach.p2achandroidvision.viewmodels

import ai.p2ach.p2achandroidvision.base.viewmodel.BaseViewModel
import ai.p2ach.p2achandroidvision.repos.camera.CaptureReportEntity
import ai.p2ach.p2achandroidvision.repos.camera.CaptureReportRepo
import ai.p2ach.p2achandroidvision.repos.camera.CaptureReportStatus

class CaptureReportViewModel(private val captureReportRepo: CaptureReportRepo) : BaseViewModel<List<CaptureReportEntity>,
        CaptureReportRepo>(captureReportRepo) {
     val captureReportStatus = captureReportRepo.captureReportStatuses
}