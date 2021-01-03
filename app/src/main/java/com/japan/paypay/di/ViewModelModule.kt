package com.japan.paypay.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.japan.paypay.vm.MainActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelFactory.ViewModelKey(MainActivityViewModel::class)
    internal abstract fun MainActivityViewModel(viewModel: MainActivityViewModel): ViewModel
}