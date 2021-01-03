package com.japan.paypay.dagger

import android.content.Context
import com.japan.paypay.di.AppComponent
import com.japan.paypay.di.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [TestRepoModule::class, ViewModelModule::class])
interface TestAppComponent : AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bindContext(context: Context): Builder
        fun build(): TestAppComponent
    }
}