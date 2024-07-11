package com.example.starterapplication.knox_standard.license.domain.usecase

import com.example.starterapplication.knox_standard.license.presentation.LicenseState

interface GetLicenseInfoUseCase {
    suspend operator fun invoke(): LicenseState
}