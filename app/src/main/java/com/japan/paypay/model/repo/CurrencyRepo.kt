package com.japan.paypay.model.repo

import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyExchangeRate
import com.japan.paypay.utils.CurrencyException

interface CurrencyRepo {

    @Throws(CurrencyException::class)
    suspend fun loadCurrencies(forceRemote: Boolean = false): List<Currency>

    @Throws(CurrencyException::class)
    fun loadExchangeRates(
        callback: ((CurrencyExchangeRate?) -> Unit)? = null,
        forceRemote: Boolean = false
    )

    fun registerExchangeRatesCallback(callback: (CurrencyExchangeRate?) -> Unit)
}