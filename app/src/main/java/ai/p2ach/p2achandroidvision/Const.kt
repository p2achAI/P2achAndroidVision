package ai.p2ach.p2achandroidvision

interface Const {


    interface DB{
        companion object{
            const val NAME = "p2achvision.db"
        }
    }


    interface MDM{

            interface SETTING{
                companion object{
                    const val DEFAULT_HW_TYPE = "rk3588"
                    const val DEFAULT_DEVICE_NAME = "UnknownDevice"
                    const val DEFAULT_HIDE_BUTTONS = false
                    const val DEFAULT_APP_MODE = "base"
                    const val DEFAULT_DEV_MODE = false
                    const val DEFAULT_USE_SMART_SIGN_SERVICE = false
                    const val DEFAULT_USE_POSE = true
                    const val DEFAULT_USE_HEADPOSE = true
                    const val DEFAULT_USE_YOLO = false
                    const val DEFAULT_USE_PAR = true
                    const val DEFAULT_USE_DEEPSORT = false
                    const val DEFAULT_USE_FACE = true
                    const val DEFAULT_USE_4SPLIT = false
                    const val DEFAULT_CONTENTS_MODE = false
                    const val DEFAULT_USE_OTA = false
                    const val DEFAULT_USE_GZIP = false
                    const val DEFAULT_USE_REID = false
                    const val DEFAULT_DRAW_GRID = false
                    const val DEFAULT_USE_AGE_COMP = false
                    const val DEFAULT_USE_DRAW_LIMIT = false
                    const val DEFAULT_USE_VIDEOFILE = false
                    const val DEFAULT_ROTATION = 0
                    const val DEFAULT_FLIP = false
                    const val DEFAULT_TRACK_FRMS = 300
                    const val DEFAULT_API_URL = BuildConfig.API_URL     //"https://k50o0i0a90.execute-api.ap-northeast-2.amazonaws.com/"
                    const val DEFAULT_API_KEY = BuildConfig.API_KEY
                    const val DEFAULT_GA_API_URL = "https://www.google-analytics.com"
                    const val DEFAULT_RTSP_URL = "rtsp://admin:password123!@192.168.50.100/ISAPI/streaming/channels/101"
                    const val DEFAULT_MIDDLEWARE_URL = "http://192.168.21.131:8000"
                    const val DEFAULT_WEBVIEW_URL = "https://demo.p2ach.io"
                    const val DEFAULT_LOCAL_WEBVIEW_URL = "http://localhost:3000"
                    const val DEFAULT_DEMO_VERSION = "0.0.1"
                    const val DEFAULT_BROADCAST_VERSION = "0.0.1"
                    const val DEFAULT_RTSP_TIMEOUT_MS = 5_000L
                    const val DEFAULT_DATA_SENDING_INTERVAL : Long = 300
                    const val DEFAULT_DATA_COLLECTION_INTERVAL = 0
                    const val DEFAULT_FOCAL_LENGTH = 1800F
                    const val DEFAULT_FOCAL_X = DEFAULT_FOCAL_LENGTH
                    const val DEFAULT_FOCAL_Y = DEFAULT_FOCAL_LENGTH
                    const val DEFAULT_DID_W = 480F
                    const val DEFAULT_DID_H = 270F
                    const val DEFAULT_DID_L = 240F
                    const val DEFAULT_DID_T = 10F
                    const val DEFAULT_SCALE1 = 400F
                    const val DEFAULT_SCALE2 = 4F
                    const val DEFAULT_AGE_MODE = 0
                    const val DEFAULT_GENDER_THR = 0.5F
                    const val DEFAULT_TV_WIDTH = 400
                    const val DEFAULT_TV_HEIGHT = 200
                    const val DEFAULT_AUTO_START_CAMERA_ACTIVITY = true  // LoadingActivity에서 CameraActivity 자동 실행 여부
                }

            }
        }




}