package ai.p2ach.p2achandroidvision.utils



import ai.p2ach.p2achandroidvision.utils.Log
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class UsbPermissionAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "UsbPermissionAccessibilityService"
    }
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        if (p0?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            p0?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            val rootNode = rootInActiveWindow ?: return

            val allowButtons = findNodesByText(rootNode, listOf("Allow", "OK", "허용", "확인"))

            for (node in allowButtons) {
                if(node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d( "USB 권한 팝업 자동 승인 클릭")
                }
            }
        }
    }

    override fun onInterrupt() { }

    private fun findNodesByText(node: AccessibilityNodeInfo, texts: List<String>): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        for (text in texts) {
            node.findAccessibilityNodeInfosByText(text)?.let { results.addAll(it) }
        }
        return results
    }
}