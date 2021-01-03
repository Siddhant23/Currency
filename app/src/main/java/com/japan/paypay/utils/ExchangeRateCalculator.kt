package com.japan.paypay.utils

import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyAmount
import com.japan.paypay.model.data.CurrencyExchangeRate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExchangeRateCalculator(
    var exchangeRates: CurrencyExchangeRate,
    var currencies: List<Currency>,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun calculate(
        amt: Double,
        currency: Currency,
        callback: (List<CurrencyAmount>) -> Unit
    ) =
        withContext(dispatchers) {
            //convert amount to source currency
            val sourceAmt = convertToSourceAmt(amt, currency)
            val list = arrayListOf<CurrencyAmount>()
            for (entry: Map.Entry<String, Double> in exchangeRates.quotes) {
                val key = entry.key.substring(3)
                val entryCurrency = findCurrency(key) ?: continue
                list.add(CurrencyAmount(sourceAmt * entry.value, entryCurrency))
            }
            callback.invoke(list)
        }

    private fun convertToSourceAmt(amt: Double, currency: Currency): Double {
        val key = "${exchangeRates.source}${currency.code}"//concatenate source currency to key
        // 1 source currency = this currency's rate
        val rate = exchangeRates.quotes[key]
            ?: kotlin.run { throw CurrencyException("currency does not exist") }
        // x source currency = ?
        return amt / rate
    }

    private fun findCurrency(code: String): Currency? =
        currencies.find { it.code == code }
}