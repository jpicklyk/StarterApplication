package com.example.starterapplication.knox_standard.license

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.starterapplication.knox_standard.BuildConfig
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImportLicenseTest {

    @Test
    fun readLicenseKeyFromBuildConfig() {
        // Access the KNOX_LICENSE_KEY from the main BuildConfig
        val licenseKey = BuildConfig.KNOX_LICENSE_KEY
        assert(licenseKey != "KNOX_LICENSE_KEY_NOT_FOUND") { licenseKey }
    }
}