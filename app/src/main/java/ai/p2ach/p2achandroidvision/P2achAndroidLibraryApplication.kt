package ai.p2ach.p2achandroidvision

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger


class P2achAndroidVisionApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
    }

}