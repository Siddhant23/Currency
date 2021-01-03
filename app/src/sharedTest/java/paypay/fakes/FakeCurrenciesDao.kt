package paypay.fakes

import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.local.CurrenciesDao

class FakeCurrenciesDao(
    private val list: ArrayList<Currency>
) : CurrenciesDao {

    override fun save(currency: Currency) {
        list.add(currency)
    }

    override fun save(currencies: List<Currency>) {
        list.addAll(currencies)
    }

    override fun load(): List<Currency>? = list

    override fun clear(): Int {
        list.clear()
        return list.size
    }
}