package com.japan.paypay.model.repo

import androidx.annotation.VisibleForTesting
import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyExchangeRate
import com.japan.paypay.model.local.CurrenciesDao
import com.japan.paypay.model.local.ExchangeRateDao
import com.japan.paypay.service.APIs
import com.japan.paypay.utils.convertCurrencyMapToList
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class CurrencyRepoImpl
@Inject constructor(
    private var APIs: APIs,
    private val currenciesDao: CurrenciesDao,
    private val exchangeRateDao: ExchangeRateDao
) : CurrencyRepo {
    private var refreshCallback: ((CurrencyExchangeRate?) -> Unit)? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var dispatcher: CoroutineDispatcher = Dispatchers.IO

    override suspend fun loadCurrencies(forceRemote: Boolean): List<Currency> =
        withContext(dispatcher) {
            if (forceRemote)
                loadRemoteCurrencies() ?: arrayListOf()
            else {
                val cache = loadCachedCurrencies()
                if (cache.isNullOrEmpty()) {
                    loadRemoteCurrencies() ?: arrayListOf()
                } else
                    cache
            }
        }

    override fun loadExchangeRates(
        callback: ((CurrencyExchangeRate?) -> Unit)?,
        forceRemote: Boolean
    ) {
        GlobalScope.launch(dispatcher) {
            if (forceRemote) {
                loadRemoteExchangeRates(callback)
            } else {
                val data = loadCachedExchangeRates()
                if (data == null)
                    loadRemoteExchangeRates(callback)
                else
                    callback?.invoke(data)
            }
        }
    }

    override fun registerExchangeRatesCallback(callback: (CurrencyExchangeRate?) -> Unit) {
        refreshCallback = callback
    }

    suspend fun loadCachedExchangeRates(): CurrencyExchangeRate? =
        withContext(dispatcher) {
            exchangeRateDao.load()
        }

    suspend fun loadRemoteExchangeRates(callback: ((CurrencyExchangeRate?) -> Unit)?) {
        APIs.getExchangeRates().enqueue(object : Callback<CurrencyExchangeRate?> {
            override fun onFailure(call: Call<CurrencyExchangeRate?>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<CurrencyExchangeRate?>,
                response: Response<CurrencyExchangeRate?>
            ) {
                val body = response.body()
                if (body != null) {
                    GlobalScope.launch { cacheExchangeRates(body) }
                }
                refreshCallback?.invoke(body)
                callback?.invoke(body)
            }
        })
    }

    private suspend fun cacheExchangeRates(data: CurrencyExchangeRate) {
        withContext(dispatcher) {
            exchangeRateDao.clear()
            exchangeRateDao.save(data)
        }
    }

    suspend fun loadCachedCurrencies(): List<Currency>? =
        withContext(dispatcher) {
            currenciesDao.load()
        }

    suspend fun loadRemoteCurrencies(): List<Currency>? =
        withContext(dispatcher) {
            val resp = APIs.getCurrencies().execute().body() ?: return@withContext null
            val list = convertCurrencyMapToList(resp.currencies)
            cacheCurrencies(list)
            return@withContext list
        }

    private suspend fun cacheCurrencies(currencies: List<Currency>) {
        withContext(dispatcher) {
            currenciesDao.clear()
            currenciesDao.save(currencies)
        }
    }

    @VisibleForTesting
    fun setWebService(newAPIs: APIs) {
        APIs = newAPIs
    }
}