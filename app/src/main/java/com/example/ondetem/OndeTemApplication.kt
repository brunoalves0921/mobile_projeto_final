package com.example.ondetem

import android.app.Application
import android.content.pm.PackageManager
import com.google.android.libraries.places.api.Places

class OndeTemApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializa o SDK do Google Places uma única vez quando o app é criado.
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY")

            if (apiKey != null && !Places.isInitialized()) {
                Places.initialize(applicationContext, apiKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}