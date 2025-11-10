package ai.p2ach.p2achandroidvision


import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.mdm.MDMRepo
import ai.p2ach.p2achandroidvision.viewmodels.MdmViewModel
import android.app.Application
import androidx.room.Room
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
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
    }

    val vmModule = module {
       viewModel { MdmViewModel(get ()) }
    }



    override fun onCreate() {
        super.onCreate()
        startKoin {
//            /*Android System Logger*/
//            androidLogger(org.koin.core.logger.Level.DEBUG)
            androidContext(this@P2achAndroidVisionApplication)
            modules(dbModule,repoModule,vmModule)
        }
        Logger.addLogAdapter(AndroidLogAdapter())
    }

}