package com.example.starterapplication.feature.deviceadmin.di

import com.example.starterapplication.feature.deviceadmin.domain.DeviceAdminManager
import com.example.starterapplication.feature.deviceadmin.presentation.viewmodel.DeviceAdminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val deviceAdminModule = module {
    single { DeviceAdminManager(get()) }
    viewModel { DeviceAdminViewModel(get()) }
}