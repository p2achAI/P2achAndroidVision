package ai.p2ach.p2achandroidvision



import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.database.Migration
import ai.p2ach.p2achandroidvision.repos.ai.AiModelRepo
import ai.p2ach.p2achandroidvision.repos.camera.CameraService
import ai.p2ach.p2achandroidvision.repos.mdm.MDMHandlers
import ai.p2ach.p2achandroidvision.repos.mdm.MDMRepo
import ai.p2ach.p2achandroidvision.repos.camera.CameraServiceRepo
import ai.p2ach.p2achandroidvision.repos.camera.CaptureReportRepo
import ai.p2ach.p2achandroidvision.repos.camera.UploadPendingCaptureReportsWorker
import ai.p2ach.p2achandroidvision.repos.camera.handlers.InternalCameraHandler
import ai.p2ach.p2achandroidvision.repos.camera.handlers.RTSPCameraHandler
import ai.p2ach.p2achandroidvision.repos.camera.handlers.UVCCameraHandler
import ai.p2ach.p2achandroidvision.repos.monitoring.MonitoringRepo
import ai.p2ach.p2achandroidvision.repos.presign.PreSignRepo
import ai.p2ach.p2achandroidvision.repos.receivers.watchdog.WatchdogScheduler
import ai.p2ach.p2achandroidvision.utils.Log


import ai.p2ach.p2achandroidvision.viewmodels.CameraViewModel
import ai.p2ach.p2achandroidvision.viewmodels.CaptureReportViewModel
import ai.p2ach.p2achandroidvision.viewmodels.MdmViewModel
import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraManager
import android.hardware.usb.UsbManager
import android.net.ConnectivityManager
import androidx.room.Room
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.context.startKoin
import org.koin.dsl.module


class P2achAndroidVisionApplication : Application() {

    val dbModule = module {
        single {
            Room.databaseBuilder(androidContext(), AppDataBase::class.java, Const.DB.NAME)
              /*  .addMigrations(Migration.MIGRATION_1_2)
                .addMigrations(Migration.MIGRATION_2_3)*/
                .fallbackToDestructiveMigration(true)
                .build()
        }
        single { get<AppDataBase>().MDMDao() }
        single { get<AppDataBase>().CaptureDao() }
        single { get<AppDataBase>().AiModelDao() }
    }


    val repoModule = module {

        single { MDMRepo(androidContext(),get(), get()) }
        single { CameraServiceRepo(get()) }
        single { CaptureReportRepo(get(),get(),get()) }
        single { PreSignRepo() }
        single { MonitoringRepo(get()) }
        single { AiModelRepo(get(),get()) }
    }

    val vmModule = module {
       viewModel {
           MdmViewModel(get())


       }
        viewModel{
            CameraViewModel(get())
        }

        viewModel{
            CaptureReportViewModel(get())
        }

    }

    val mdmModule = module {
        single { MDMHandlers(get(),get()) }
        single { UVCCameraHandler(get()) }
        single { RTSPCameraHandler(get()) }
        single { InternalCameraHandler(get()) }
    }


    val systemModule = module {

        single<CameraManager> {
            androidContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        }
        single<UsbManager>{
            androidContext().getSystemService(Context.USB_SERVICE) as UsbManager
        }

        single<ConnectivityManager>{
            androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }

    }

    val workerModule = module{
            worker {
                UploadPendingCaptureReportsWorker(get (),get() )
            }

    }










    override fun onCreate() {
        super.onCreate()


        try {
            System.loadLibrary("opencv_java4")
            System.loadLibrary("p2ach-vision")
        }catch (e: Exception){
            Log.d("onCreate ${e.message}")
        }


        startKoin {
//            /*Android System Logger*/
//            androidLogger(org.koin.core.logger.Level.DEBUG)
            androidContext(this@P2achAndroidVisionApplication)
            modules(dbModule,repoModule,vmModule, mdmModule, systemModule, workerModule)
        }
        Logger.addLogAdapter(AndroidLogAdapter())
        WatchdogScheduler.start(this)
    }

}