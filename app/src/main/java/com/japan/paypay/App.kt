package com.japan.paypay

import android.app.Application
import com.japan.paypay.di.AppComponent
import com.japan.paypay.di.DaggerAppComponent

open class App : Application() {
    private lateinit var daggerComponent: AppComponent
    override fun onCreate() {
        super.onCreate()
        daggerComponent = DaggerAppComponent
            .builder()
            .bindContext(this)
            .build()
    }

    open fun daggerComponent(): AppComponent = daggerComponent
}