package ai.p2ach.p2achandroidvision


interface Const {

    interface REST_API{

        interface RETROFIT{
            companion object{
                const val TIME_OUT=1000L
            }

            interface PRE_SIGN{
               interface ACTION{
                   companion object{
                       const val UPLOAD="upload"
                   }
               }
            }
        }

    }

    interface LOCAL{

        interface FILE{

            interface IMAGE{
                companion object{
                    const val CAPTURE_FILE_DIR ="captures"
                    const val COMPRESS_QUALITY = 90
                    const val DOT="."
                    const val FORMAT = "jpg"
                    const val PREFIX = "capture"
                }
            }


        }

    }


    interface Service{

        companion object{
            const val CHANNEL_ID = "pe2ch_camera_service_channel"
            const val CHNNEL_NAME="P2ach Camera Service Channel"
        }
    }


    interface DB{
        companion object{
            const val NAME = "p2achvision.db"
        }
    }


    interface MDM{




            interface SETTING{

                interface REMOTE{

                    interface KEY{
                        companion object{

                            const val MDM_ENTITY = "MDMEntity"
                            const val DEVICE_NAME = "deviceName"
                            const val HW_TYPE = "hwType"
                            const val DEVICE_UUID = "deviceUuid"
                            const val RTSP_TIMEOUT_MS = "rtspTimeoutMs"
                            const val RTSP_URL = "rtspUrl"
                            const val API_URL = "apiUrl"
                            const val WEBVIEW_URL = "webviewUrl"
                            const val MIDDLEWARE_URL = "middlewareUrl"
                            const val APP_MODE = "appMode"
                            const val DEMO_VERSION = "demo_version"
                            const val BROADCAST_VERSION = "broadcast_version"
                            const val USE_SMART_SIGN_SERVICE = "useSmartSignService"
                            const val HIDE_BUTTONS = "hide_buttons"
                            const val DRAW_GRID = "drawGrid"
                            const val ROTATION = "rotation"
                            const val AUTO_ROTATION = "autoRotation"
                            const val DATA_SENDING_INTERVAL = "dataSendingInterval"
                            const val DATA_COLLECTION_INTERVAL = "dataCollectionInterval"
                            const val USE_GZIP = "useGzip"
                            const val USE_OTA = "use_ota"
                            const val USE_REID = "use_reid"
                            const val USE_AGE_GENDER_NPU_MODEL = "use_ageGender_NpuModel"
                            const val USE_VIDEO_FILE = "useVideofile"
                            const val VIDEO_FILE_PATHS = "videofilepaths"
                            const val VIDEO_FILE_URIS = "videofileUris"
                            const val USE_POSE = "use_pose"
                            const val USE_HEAD_POSE = "use_headpose"
                            const val USE_YOLO = "use_yolo"
                            const val USE_PAR = "use_par"
                            const val USE_DEEP_SORT = "use_deepsort"
                            const val USE_FACE = "use_face"
                            const val USE_4SPLIT = "use_4split"
                            const val CONTENTS_MODE = "contents_mode"
                            const val FLIP = "flip"
                            const val TRACK_FRMS = "track_frms"
                            const val AGE_MODE = "ageMode"
                            const val DEV_MODE = "devMode"
                            const val GENDER_THR = "genderThr"
                            const val USE_AGE_COMP = "use_age_comp"
                            const val USE_DRAW_LIMIT = "use_draw_limit"

                            // ROI
                            const val ROI = "roi"
                            const val ROI_TOP = "top"
                            const val ROI_LEFT = "left"
                            const val ROI_WIDTH = "width"
                            const val ROI_HEIGHT = "height"

                            // CamParam
                            const val CAM_PARAM = "camParam"
                            const val FOCAL_X = "focal_x"
                            const val FOCAL_Y = "focal_y"
                            const val ROT_X = "rot_x"
                            const val ROT_Y = "rot_y"
                            const val ROT_Z = "rot_z"
                            const val DID_W = "did_w"
                            const val DID_H = "did_h"
                            const val DID_L = "did_l"
                            const val DID_T = "did_t"
                            const val SCALE1 = "scale1"
                            const val SCALE2 = "scale2"

                            // QuadrangleRegion
                            const val CORRIDOR_REGION = "corridorRegion"
                            const val JUNCTION_REGION = "junctionRegion"
                            const val LEFT_LINE = "leftLine"
                            const val RIGHT_LINE = "rightLine"

                            const val START_X = "startX"
                            const val START_Y = "startY"
                            const val END_X = "endX"
                            const val END_Y = "endY"

                            // TV size
                            const val TV_WIDTH = "tvWidth"
                            const val TV_HEIGHT = "tvHeight"

                            // Camera autostart
                            const val AUTO_START_CAMERA_ACTIVITY = "autoStartCameraActivity"

                            // Google Analytics
                            const val GA_API_URL = "gaApiUrl"
                            const val GA_API_SECRET = "gaApiSecret"
                            const val GA_MEASUREMENT_ID = "gaMeasurementId"

                            const val P5 = "p5"
                            const val P95 = "p95"
                            const val CAMERA_TYPE = "cameraType"

                            const val CAPTURE_REPORT="captureReport"

                        }
                    }
                }


                interface DEFAULT{

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
                        const val DEFAULT_USE_DEEP_SORT = false
                        const val DEFAULT_USE_FACE = true
                        const val DEFAULT_USE_4SPLIT = false
                        const val DEFAULT_CONTENTS_MODE = false
                        const val DEFAULT_USE_OTA = false
                        const val DEFAULT_USE_GZIP = false
                        const val DEFAULT_USE_REID = false
                        const val DEFAULT_DRAW_GRID = false
                        const val DEFAULT_USE_AGE_COMP = false
                        const val DEFAULT_USE_DRAW_LIMIT = false
                        const val DEFAULT_USE_VIDEO_FILE = false
                        const val DEFAULT_ROTATION = 0
                        const val DEFAULT_FLIP = false
                        const val DEFAULT_TRACK_FRMS :Int = 300
                        const val DEFAULT_API_URL = BuildConfig.API_URL     //"https://k50o0i0a90.execute-api.ap-northeast-2.amazonaws.com/"
                        const val DEFAULT_API_KEY = BuildConfig.API_KEY
                        const val DEFAULT_GA_API_URL = "https://www.google-analytics.com"
                        const val DEFAULT_RTSP_URL = "rtsp://admin:password123!@192.168.50.100/ISAPI/streaming/channels/101"
                        const val DEFAULT_MIDDLEWARE_URL = "http://192.168.21.131:8000"
                        const val DEFAULT_WEBVIEW_URL = "https://demo.p2ach.io"

                        const val DEFAULT_LOCAL_WEBVIEW_URL = "http://localhost:3000"
                        const val DEFAULT_DEMO_VERSION = "0.0.1"
                        const val DEFAULT_BROADCAST_VERSION = "0.0.1"
                        const val DEFAULT_RTSP_TIMEOUT_MS : Long = 5000L
                        const val DEFAULT_DATA_SENDING_INTERVAL : Int = 300
                        const val DEFAULT_DATA_COLLECTION_INTERVAL : Int =  0
                        const val DEFAULT_FOCAL_LENGTH = 1800F
                        const val DEFAULT_FOCAL_X = DEFAULT_FOCAL_LENGTH
                        const val DEFAULT_FOCAL_Y = DEFAULT_FOCAL_LENGTH
                        const val DEFAULT_DID_W = 480F
                        const val DEFAULT_DID_H = 270F
                        const val DEFAULT_DID_L = 240F
                        const val DEFAULT_DID_T = 10F
                        const val DEFAULT_SCALE1 = 400F
                        const val DEFAULT_SCALE2 = 4F
                        const val DEFAULT_AGE_MODE : Int = 0
                        const val DEFAULT_GENDER_THR = 0.5F
                        const val DEFAULT_TV_WIDTH = 400
                        const val DEFAULT_TV_HEIGHT = 200
                        const val DEFAULT_AUTO_START_CAMERA_ACTIVITY = true  // LoadingActivity에서 CameraActivity 자동 실행 여부

                        const val DEFAULT_AUTO_ROTATION = false

                        const val DEFAULT_USE_AGE_GENDER_NPU_MODEL = false

                        const val DEFAULT_CAMERA_TYPE="UVC"



                    }

                }

            }
        }


    interface CAMERA_TYPE{
        companion object{
            const val UVC="UVC"
            const val INTERNAL="INTERNAL"
            const val RTSP="RTSP"

        }
    }



    interface BUNDLE{
        interface KEY{
            companion object{ const val CAMERA_TYPE="CAMERA_TYPE"}
        }

    }

}