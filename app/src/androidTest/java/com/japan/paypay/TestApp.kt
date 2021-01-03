package com.japan.paypay

import com.japan.paypay.dagger.DaggerTestAppComponent
import com.japan.paypay.dagger.TestAppComponent

class TestApp : App() {
    private lateinit var daggerComponent: TestAppComponent

    override fun onCreate() {
        super.onCreate()
        daggerComponent = DaggerTestAppComponent.builder()
            .bindContext(this)
            .build()
    }

    override fun daggerComponent(): TestAppComponent = daggerComponent
}