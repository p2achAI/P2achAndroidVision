package ai.p2ach.p2achandroidvision

import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.MDMRepo
import android.app.Application
import androidx.room.Room
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module


class P2achAndroidVisionApplication : Application() {


    val dbModule = module {
        single {
            Room.databaseBuilder(androidContext(), AppDataBase::class.java, Const.DB.NAME)
                .build()
        }
        single { get<AppDataBase>().mdmSettingDao() }

    }

    val repoModule = module {
        single { MDMRepo(get()) }
    }

    val vmModule = module {
//        viewModel {  }
    }



    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        startKoin {
            androidContext(this@P2achAndroidVisionApplication)
            modules(dbModule,repoModule,vmModule)
        }
    }

}