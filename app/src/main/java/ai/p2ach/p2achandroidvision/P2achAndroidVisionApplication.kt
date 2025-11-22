package ai.p2ach.p2achandroidvision


import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.mdm.MDMHandlers
import ai.p2ach.p2achandroidvision.repos.mdm.MDMRepo
import ai.p2ach.p2achandroidvision.repos.camera.CameraServiceRepo
import ai.p2ach.p2achandroidvision.repos.camera.handlers.UVCCameraHandler
import ai.p2ach.p2achandroidvision.viewmodels.CameraViewModel
import ai.p2ach.p2achandroidvision.viewmodels.MdmViewModel
import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraManager
import android.hardware.usb.UsbManager
import androidx.room.Room
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module


class P2achAndroidVisionApplication : Application() {


    val dbModule = module {
        single {
            Room.databaseBuilder(androidContext(), AppDataBase::class.java, Const.DB.NAME)
                .fallbackToDestructiveMigration(true)
                .build()
        }
        single { get<AppDataBase>().MDMDao() }

    }




    val repoModule = module {

        single { MDMRepo(androidContext(),get(), get()) }
        single { CameraServiceRepo(get()) }
    }

    val vmModule = module {
       viewModel {
           MdmViewModel(get ())
           CameraViewModel(get())

       }
    }

    val mdmModule = module {
        single { MDMHandlers(get(),get()) }
        single { UVCCameraHandler(get()) }
    }


    val systemModule = module {

        single<CameraManager> {
            androidContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        }
        single<UsbManager>{
            androidContext().getSystemService(Context.USB_SERVICE) as UsbManager
        }
    }

    val managerModule = module{


    }










    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("opencv_java4")

        startKoin {
//            /*Android System Logger*/
//            androidLogger(org.koin.core.logger.Level.DEBUG)
            androidContext(this@P2achAndroidVisionApplication)
            modules(dbModule,repoModule,vmModule, mdmModule, systemModule, managerModule)
        }
        Logger.addLogAdapter(AndroidLogAdapter())
    }

}