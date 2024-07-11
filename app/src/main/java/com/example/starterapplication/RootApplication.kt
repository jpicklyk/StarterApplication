package com.example.starterapplication

import android.app.Application
import com.example.starterapplication.core.common.di.CoreCommonModule
import com.example.starterapplication.core.knox.di.KnoxFeatureModule
import com.example.starterapplication.di.AppModule
import com.example.starterapplication.feature.deviceadmin.di.deviceAdminModule
import com.example.starterapplication.knox_standard.di.knoxStandardModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.*


class RootApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RootApplication)
            modules(
                AppModule().module,
                deviceAdminModule,
                CoreCommonModule().module,
                KnoxFeatureModule().module,
                knoxStandardModule()
            )
        }
    }
}