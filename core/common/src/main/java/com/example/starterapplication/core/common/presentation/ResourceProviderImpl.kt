package com.example.starterapplication.core.common.presentation

import android.content.Context
import androidx.annotation.StringRes
import org.koin.core.annotation.Single

@Single
internal class ResourceProviderImpl (
    private val context: Context
) : ResourceProvider {
    override fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}