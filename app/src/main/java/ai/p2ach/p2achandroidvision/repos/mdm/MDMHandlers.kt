package ai.p2ach.p2achandroidvision.repos.mdm

import ai.p2ach.p2achandroidlibrary.utils.Log
import android.content.Context
import com.hmdm.MDMPushHandler
import com.hmdm.MDMPushMessage

class MDMHandlers(private val context: Context) {

    private val mdmMessageConfigUpdated = "configUpdated"
    private val mdmMessageAppConfigUpdated = "appConfigUpdated"
    private val mdmMessageAppReboot = "visionAppReboot"
    private val mdmMessageCheckCameraView = "checkCameraView"

    private val eventChannels = arrayOf(mdmMessageConfigUpdated,mdmMessageAppConfigUpdated,mdmMessageAppReboot,mdmMessageCheckCameraView)


    private val mdmPushHandler = object : MDMPushHandler() {
        override fun onMessageReceived(mdmPushMessage: MDMPushMessage?) {

            when (mdmPushMessage?.type) {
                mdmMessageConfigUpdated, mdmMessageAppConfigUpdated -> {
//                        ConfigSync.refreshFromMdm()
                    Log.d("MDM Handler mdmMessageConfigUpdated or mdmMessageAppConfigUpdated message.")
                }
                mdmMessageAppReboot -> {
                    // TODO: restart
                }
                else -> {

                    Log.e("MDM Handler Unsupported mdm message type: ${mdmPushMessage?.type}")

                }
            }
        }

        override fun register(messageType: String?, context: Context?) {
            super.register(messageType, context)
            Log.d("MDM Handler register  $messageType")
        }

        override fun unregister(context: Context?) {
            super.unregister(context)
            Log.d("MDM Handler unregister")
        }
    }

    fun init(){
        mdmPushHandler.register(eventChannels,context)
    }






}