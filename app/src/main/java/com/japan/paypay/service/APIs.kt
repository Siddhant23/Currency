package com.japan.paypay.service

import com.japan.paypay.model.data.CurrenciesResp
import com.japan.paypay.model.data.CurrencyExchangeRate
import com.japan.paypay.utils.CURRENCIES_PATH
import com.japan.paypay.utils.EXCHANGE_RATE_PATH
import retrofit2.Call
import retrofit2.http.GET

interface APIs {
    @GET(EXCHANGE_RATE_PATH)
    fun getExchangeRates(): Call<CurrencyExchangeRate>

    @GET(CURRENCIES_PATH)
    fun getCurrencies(): Call<CurrenciesResp>
}