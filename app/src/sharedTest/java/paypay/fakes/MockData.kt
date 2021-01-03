package paypay.fakes

import com.japan.paypay.model.data.CurrenciesResp
import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyAmount
import com.japan.paypay.model.data.CurrencyExchangeRate

val cachedCurrencies = arrayListOf(
    Currency("USD", "United States Dollar", 1),
    Currency("GBP", "British Pound Sterling", 2),
    Currency("EUR", "Euro", 3),
    Currency("JPY", "Japanese Yen", 4),
    Currency("NGN", "Nigerian Naira", 5)
)
val cachedExchangeRates = CurrencyExchangeRate(
    1,
    "111111111",
    "USD",
    mapOf(
        Pair("USDUSD", 1.0),
        Pair("USDGBP", 0.534567),
        Pair("USDEUR", 1.917021),
        Pair("USDJPY", 101.650385)
    )
)

val remoteExchangeRates = CurrencyExchangeRate(
    1,
    "22222222",
    "USD",
    mapOf(
        Pair("USDUSD", 1.0),
        Pair("USDGBP", 0.821959),
        Pair("USDEUR", 0.917011),
        Pair("USDJPY", 107.650385),
        Pair("USDNGN", 390.503727)
    )
)

val remoteCurrenciesMap = mapOf(
    Pair("AUD", "Australian Dollar"),
    Pair("GBP", "British Pound Sterling"),
    Pair("EUR", "Euro"),
    Pair("ERR", "Erroneous Currency"),
    Pair("JPY", "Japanese Yen"),
    Pair("NGN", "Nigerian Naira"),
    Pair("USD", "United States Dollar"),
    Pair("ZAR", "South African Rand")
)
val remoteCurrencyResp = CurrenciesResp(
    true,
    "http://example.com/terms",
    "http://example.com/privacy",
    remoteCurrenciesMap
)

const val SERVER_ERROR_MSG = "server error"

val currencyAmountList = listOf(
    CurrencyAmount(21.0, Currency("JPY", "Japanese Yen")),
    CurrencyAmount(0.71, Currency("USD", "United States Dollar")),
    CurrencyAmount(0.36, Currency("GBP", "Pound Sterling"))
)