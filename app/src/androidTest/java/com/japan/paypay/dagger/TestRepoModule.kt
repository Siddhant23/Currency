package com.japan.paypay.dagger

import com.japan.paypay.model.repo.CurrencyRepo
import dagger.Binds
import dagger.Module
import paypay.fakes.FakeCurrencyRepo
import javax.inject.Singleton

@Module
abstract class TestRepoModule {

    @Singleton
    @Binds
    abstract fun bindWeatherRepo(repoImpl: FakeCurrencyRepo): CurrencyRepo

}