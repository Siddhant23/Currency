package paypay.fakes

import com.japan.paypay.model.data.CurrenciesResp
import com.japan.paypay.model.data.CurrencyExchangeRate
import com.japan.paypay.service.APIs
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls

class FakeAPIs(
    var currencies: CurrenciesResp?,
    var exchangeRates: CurrencyExchangeRate?,
    private val delegate: BehaviorDelegate<APIs>,
    var shouldFail: Boolean = false
) : APIs {

    override fun getExchangeRates(): Call<CurrencyExchangeRate> {
        return if (shouldFail)
            Calls.failure(Exception(SERVER_ERROR_MSG))
        else
            delegate.returningResponse(exchangeRates).getExchangeRates()
    }

    override fun getCurrencies(): Call<CurrenciesResp> {
        return if (shouldFail)
            Calls.failure(Exception(SERVER_ERROR_MSG))
        else
            delegate.returningResponse(currencies).getCurrencies()
    }
}