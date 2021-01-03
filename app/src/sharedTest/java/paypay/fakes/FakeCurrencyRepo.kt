package paypay.fakes

import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyExchangeRate
import com.japan.paypay.model.repo.CurrencyRepo
import com.japan.paypay.utils.CurrencyException
import javax.inject.Inject

class FakeCurrencyRepo(
    var currencies: List<Currency>?,
    var cachedExchangeRates: CurrencyExchangeRate?,
    var remoteExchangeRates: CurrencyExchangeRate?,
    var refreshCallback: ((CurrencyExchangeRate?) -> Unit)? = null,
    var shouldThrowError: Boolean = false
) : CurrencyRepo {

    @Inject
    constructor() : this(null, null, null)

    override suspend fun loadCurrencies(forceRemote: Boolean): List<Currency> {
        if (shouldThrowError)
            throw CurrencyException(ERROR_MSG)
        return currencies ?: listOf()
    }

    override fun loadExchangeRates(
        callback: ((CurrencyExchangeRate?) -> Unit)?,
        forceRemote: Boolean
    ) {
        if (shouldThrowError)
            throw CurrencyException(ERROR_MSG)
        var rates = cachedExchangeRates ?: remoteExchangeRates
        if (forceRemote) {
            rates = remoteExchangeRates
            refreshCallback?.invoke(remoteExchangeRates)
        }
        callback?.invoke(rates)
    }

    override fun registerExchangeRatesCallback(callback: (CurrencyExchangeRate?) -> Unit) {
        refreshCallback = callback
    }

    companion object {
        const val ERROR_MSG = "sample repo error"
    }
}