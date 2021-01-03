package paypay.fakes

import com.japan.paypay.model.data.CurrencyExchangeRate
import com.japan.paypay.model.local.ExchangeRateDao

class FakeExchangeRateDao(
    private var exchangeRate: CurrencyExchangeRate?
) : ExchangeRateDao {

    override fun save(exchangeRate: CurrencyExchangeRate) {
        this.exchangeRate = exchangeRate
    }

    override fun load(): CurrencyExchangeRate? = exchangeRate

    override fun clear(): Int {
        exchangeRate = null
        return 1
    }
}