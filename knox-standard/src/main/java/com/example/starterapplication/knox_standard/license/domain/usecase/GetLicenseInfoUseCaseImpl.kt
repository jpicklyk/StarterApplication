package com.example.starterapplication.knox_standard.license.domain.usecase

import android.content.Context
import com.example.starterapplication.knox_standard.license.presentation.LicenseState
import com.samsung.android.knox.license.ActivationInfo
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager
import org.koin.core.annotation.Single
import javax.inject.Inject

@Single
internal class GetLicenseInfoUseCaseImpl @Inject constructor(
    private val context: Context
) : GetLicenseInfoUseCase {
    override suspend operator fun invoke(): LicenseState {
        return try {
            val activationInfo = getKnoxManager().licenseActivationInfo

            when (activationInfo?.state) {
                null -> LicenseState.NotActivated
                ActivationInfo.State.ACTIVE -> LicenseState.Activated(
                    message = "Activation Date: ${activationInfo.activationDate}"
                )
                ActivationInfo.State.EXPIRED -> LicenseState.Expired
                ActivationInfo.State.TERMINATED -> LicenseState.Terminated
            }
        } catch (e: Exception) {
            LicenseState.Error("Failed to get license info: ${e.message}")
        }
    }

    private fun getKnoxManager(): KnoxEnterpriseLicenseManager {
        return KnoxEnterpriseLicenseManager.getInstance(context)
    }
}