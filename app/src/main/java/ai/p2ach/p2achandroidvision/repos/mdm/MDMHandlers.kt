package ai.p2ach.p2achandroidvision.repos.mdm

import ai.p2ach.p2achandroidlibrary.utils.Log
import android.content.Context
import com.hmdm.MDMPushHandler
import com.hmdm.MDMPushMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MDMHandlers(private val context: Context, private val mdmRepo: MDMRepo) {

    private val mdmMessageConfigUpdated = "configUpdated"
    private val mdmMessageAppConfigUpdated = "appConfigUpdated"
    private val mdmMessageAppReboot = "visionAppReboot"
    private val mdmMessageCheckCameraView = "checkCameraView"

    private val eventChannels = arrayOf(mdmMessageConfigUpdated,mdmMessageAppConfigUpdated,mdmMessageAppReboot,mdmMessageCheckCameraView)


    private val mdmPushHandler = object : MDMPushHandler() {
        override fun onMessageReceived(mdmPushMessage: MDMPushMessage?) {



            when (mdmPushMessage?.type) {
                mdmMessageConfigUpdated, mdmMessageAppConfigUpdated -> {
                    Log.d("onMessageReceived ${mdmPushMessage?.type}")
                    CoroutineScope(Dispatchers.IO).launch {
                        mdmRepo.syncMDMInfo()
                    }

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
            Log.d("Handler register success  $messageType")
        }

        override fun unregister(context: Context?) {
            super.unregister(context)
            Log.d("Handler unregister")
        }

        override fun register(
            messageTypes: Array<out String?>?,
            context: Context?
        ) {
            super.register(messageTypes, context)
        }
    }

    fun init(){
//        Log.d("mdmPushHandler init()")
        mdmPushHandler.register(eventChannels,context)
    }






}