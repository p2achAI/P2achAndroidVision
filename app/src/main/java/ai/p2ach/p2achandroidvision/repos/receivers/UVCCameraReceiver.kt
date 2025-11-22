package ai.p2ach.p2achandroidvision.repos.receivers

import ai.p2ach.p2achandroidvision.utils.DeviceUtils.isCameraDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat

class UVCCameraReceiver(
    private val context: Context,
    private val onAttached: (UsbDevice) -> Unit,
    private val onDetached: (UsbDevice) -> Unit
) {

    private var receiver: BroadcastReceiver? = null

    fun register() {
        if (receiver != null) return

        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {

                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(UsbManager.EXTRA_DEVICE,UsbDevice::class.java) ?: return
                } else {
                    intent?.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE) ?: return
                }

                if (!device.isCameraDevice()) return

                when (intent.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> onAttached(device)
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> onDetached(device)
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun unregister() {
        receiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: Exception) {
            }
        }
        receiver = null
    }
}