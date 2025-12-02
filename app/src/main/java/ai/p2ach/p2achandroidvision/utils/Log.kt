package ai.p2ach.p2achandroidvision.utils


import com.orhanobut.logger.Logger
import com.orhanobut.logger.Printer
import org.json.JSONObject
import kotlin.ranges.until
import kotlin.toString

object Log {

    fun getPrinter(logTitle: String?): Printer {
        return Logger.t(logTitle)
    }

    fun f(logTitle: String? = null) {
        getPrinter(logTitle).d("")
    }

    fun d(vararg objs: Any?) {
        if (objs.size == 1) {
            var jsonObject = objs[0] as? JSONObject
            if (jsonObject != null) getPrinter("").json(jsonObject.toString())
            else getPrinter("").d(objs[0])
        } else {
            if (objs[0] is String) {
                for (index in 1 until objs.size) {
                    getPrinter(objs[0] as String).d(objs[index])
                }
            } else {
                for (element in objs) {
                    getPrinter("").d(element)
                }
            }
        }
    }

    fun o(obj: Any? , title: String = "") {
        var jsonObject = obj as? JSONObject
        if (jsonObject != null) getPrinter("").json(jsonObject.toString())
        else getPrinter(title).d(obj)
    }

    fun i(s: String, logTitle: String? = null) {
        getPrinter(logTitle).i(s)
    }

    fun v(s: String, logTitle: String? = null) {
        getPrinter(logTitle).v(s)
    }

    fun w(s: String, logTitle: String? = null) {
        getPrinter(logTitle).w(s)
    }

    fun e(exception: Exception, logTitle: String? = null) {
        getPrinter(logTitle).e(exception.toString())
    }

    fun e(message: String, logTitle: String? = null) {
        getPrinter(logTitle).e(message)
    }

    fun e(logTitle: String, message: String, exception: Exception) {
        getPrinter(logTitle).e(message + " " + exception.toString())
    }
}