package com.example.starterapplication.knox_standard

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.example.starterapplication.core.common.di.CoreCommonModule
import com.example.starterapplication.knox_standard.di.knoxStandardModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.ksp.generated.module


class KoinTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TestApplication)
            modules(
                CoreCommonModule().module,
                knoxStandardModule()
            )
        }
    }

    override fun onTerminate() {
        stopKoin()
        super.onTerminate()
    }
}

