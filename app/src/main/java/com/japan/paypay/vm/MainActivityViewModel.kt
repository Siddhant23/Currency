package com.japan.paypay.vm

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyAmount
import com.japan.paypay.model.data.CurrencyExchangeRate
import com.japan.paypay.model.repo.CurrencyRepo
import com.japan.paypay.utils.ExchangeRateCalculator
import com.japan.paypay.utils.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

open class MainActivityViewModel
@Inject constructor(
    private val repo: CurrencyRepo
) : ViewModel() {

    open val currencies: MutableLiveData<Resource<List<Currency>>> = MutableLiveData()

    open val exchangeRates: MutableLiveData<Resource<CurrencyExchangeRate?>> = MutableLiveData()

    open val resource: MutableLiveData<Resource<List<CurrencyAmount>?>> = MutableLiveData()

    var exchangeRateCalculator: ExchangeRateCalculator? = null
        @VisibleForTesting set

    // used exclusively for the calculator, MutableLiveData.postValue() can delay
    private lateinit var calcCurrencies: List<Currency>

    init {
        init()
    }

    private fun init() {
        loadCurrencies {
            initExchangeRates()
        }
    }

    private fun loadCurrencies(initExchangeRate: () -> Unit) {
        //load the currencies first
        currencies.postValue(Resource.loading(currencies.value?.data))
        viewModelScope.launch {
            val values = repo.loadCurrencies()
            if (values.isEmpty())
                currencies.postValue(
                    Resource.error(
                        currencies.value?.data ?: listOf(),
                        "Server returned empty data"
                    )
                )
            else {
                currencies.postValue(Resource.success(values))
                calcCurrencies = values
                initExchangeRate.invoke()
            }
        }
    }

    private fun initExchangeRates() {
        exchangeRates.postValue(Resource.loading(exchangeRates.value?.data))
        viewModelScope.launch {
            val callback: (CurrencyExchangeRate?) -> Unit = {
                if (it == null)
                    exchangeRates.postValue(
                        Resource.error(
                            exchangeRates.value?.data,
                            "Server returned empty data"
                        )
                    )
                else {
                    exchangeRates.postValue(Resource.success(it))
                    initCalculator(it)
                }
            }
            repo.loadExchangeRates(callback)
            repo.registerExchangeRatesCallback(callback)
        }
    }

    private fun initCalculator(exchangeRates: CurrencyExchangeRate) {
        if (exchangeRateCalculator == null)
            exchangeRateCalculator = ExchangeRateCalculator(exchangeRates, calcCurrencies)
        else {
            exchangeRateCalculator?.exchangeRates = exchangeRates
            exchangeRateCalculator?.currencies = calcCurrencies
        }
    }

    fun calculate(amount: Double, currency: Currency) {
        if (exchangeRateCalculator == null) {
            resource.postValue(Resource.error(null, "unable to calculate exchange rates"))
            return
        }
        resource.postValue(Resource.loading(null))
        viewModelScope.launch {
            exchangeRateCalculator?.calculate(amount, currency) {
                resource.postValue(Resource.success(it))
            }
        }
    }
}