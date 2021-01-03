package com.japan.paypay.di

import android.content.Context
import com.japan.paypay.utils.ExchangeRatesWorker
import com.japan.paypay.view.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RepoModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(exchangeRateWorker: ExchangeRatesWorker)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bindContext(context: Context): Builder
        fun build(): AppComponent
    }
}