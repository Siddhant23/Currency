package com.japan.paypay.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.japan.paypay.App
import com.japan.paypay.model.repo.CurrencyRepo
import javax.inject.Inject

class ExchangeRatesWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    @Inject
    lateinit var repo: CurrencyRepo

    override fun doWork(): Result {
        val daggerAppComponent = (applicationContext as App).daggerComponent()
        daggerAppComponent.inject(this)
        if (!::repo.isInitialized) return Result.retry()
        return try {
            repo.loadExchangeRates(null, true)
            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }
}
