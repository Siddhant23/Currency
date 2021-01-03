package com.japan.paypay.di

import android.content.Context
import com.japan.paypay.BuildConfig
import com.japan.paypay.model.local.AppDatabase
import com.japan.paypay.model.repo.CurrencyRepo
import com.japan.paypay.model.repo.CurrencyRepoImpl
import com.japan.paypay.service.APIs
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
abstract class RepoModule {

    @Module
    companion object {

        @Singleton
        @JvmStatic
        @Provides
        fun provideWebService(): APIs {
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(APIs::class.java)
        }

        @Singleton
        private fun okHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(requestInterceptor())
                .build()
        }

        private fun requestInterceptor(): (chain: Interceptor.Chain) -> Response {
            return { chain ->
                val url = chain
                    .request()
                    .url()
                    .newBuilder()
                    .addQueryParameter("access_key", BuildConfig.API_KEY)
                    .build()
                chain.proceed(
                    chain.request().newBuilder()
                        .url(url)
                        .build()
                )
            }
        }

        @Singleton
        @JvmStatic
        @Provides
        fun provideExchangeRateDao(context: Context) =
            AppDatabase.getInstance(context).exchangeRateDao()

        @Singleton
        @JvmStatic
        @Provides
        fun provideCurrenciesDao(context: Context) =
            AppDatabase.getInstance(context).currenciesDao()
    }

    @Singleton
    @Binds
    abstract fun bindWeatherRepo(repoImpl: CurrencyRepoImpl): CurrencyRepo

}