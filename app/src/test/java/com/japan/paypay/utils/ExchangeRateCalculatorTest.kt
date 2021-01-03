package com.japan.paypay.utils


import com.google.common.truth.Truth.assertThat
import com.japan.paypay.MainCoroutineRule
import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyAmount
import com.japan.paypay.model.data.CurrencyExchangeRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import paypay.fakes.cachedCurrencies

class ExchangeRateCalculatorTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule =
        MainCoroutineRule()
    private lateinit var ratesUSD: CurrencyExchangeRate
    private lateinit var ratesJPY: CurrencyExchangeRate
    private lateinit var expected_10_GBP: List<CurrencyAmount>
    private lateinit var expected_10_JPY: List<CurrencyAmount>
    private val amt = 10.0
    private val pounds = cachedCurrencies[1]
    private val yen = cachedCurrencies[3]

    @ExperimentalCoroutinesApi
    @Test
    fun calculate_generatesValidExchangeRates() = runBlockingTest {
        //GIVEN: calculator with exchange rates in japanese yen
        val calculator = ExchangeRateCalculator(ratesJPY, cachedCurrencies, Dispatchers.Unconfined)
        //WHEN: calculate for £10
        calculator.calculate(amt, pounds) {
            //before testing, round up floating point numbers to one decimal place to avoid precision errors
            it.forEach { amt -> amt.amount = amt.amount.round(1) }
            expected_10_GBP.forEach { amt -> amt.amount = amt.amount.round(1) }
            //THEN:
            assertThat(it).containsAnyIn(expected_10_GBP)
            assertThat(it).containsAnyOf(expected_10_GBP[0], expected_10_GBP[2], expected_10_GBP[3])
        }
        //WHEN: calculate for ¥10
        calculator.calculate(amt, yen) {
            it.forEach { amt -> amt.amount = amt.amount.round(1) }
            expected_10_JPY.forEach { amt -> amt.amount = amt.amount.round(1) }
            //THEN:
            assertThat(it).containsAnyIn(expected_10_JPY)
            assertThat(it).containsAnyOf(expected_10_JPY[0], expected_10_JPY[1], expected_10_JPY[2])
        }
        //WHEN: calculate for ¥0
        calculator.calculate(0.0, yen) {
            it.forEach { amt ->
                //THEN:
                assertThat(amt.amount).isEqualTo(0.0)
            }
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun calculate_withInvalidCurrency_returnsEmptyList() = runBlockingTest {
        //GIVEN: calculator with exchange rates in USD
        val calculator = ExchangeRateCalculator(ratesUSD, cachedCurrencies, Dispatchers.Unconfined)
        //WHEN: calculate with invalid or new currency not present in the repo
        calculator.calculate(amt, Currency("XXX", "Invalid Currency")) {
            //THEN: list returned is empty
            assertThat(it).isEmpty()
        }
    }

    @Before
    fun setUp() {
        ratesUSD = CurrencyExchangeRate(
            1,
            "111111111",
            "USD",
            mapOf(
                Pair("USDUSD", 1.0),
                Pair("USDGBP", 0.82),
                Pair("USDEUR", 0.91),
                Pair("USDJPY", 107.77)
            )
        )
        ratesJPY = CurrencyExchangeRate(
            2,
            "111111111",
            "JPY",
            mapOf(
                Pair("JPYUSD", 0.0093),
                Pair("JPYGBP", 0.0076),
                Pair("JPYEUR", 0.0084),
                Pair("JPYJPY", 1.0)
            )
        )
        expected_10_GBP = listOf(
            CurrencyAmount(12.1951219512195, cachedCurrencies[0]),//USD
            CurrencyAmount(amt, cachedCurrencies[1]),//GBP
            CurrencyAmount(11.09756097560976, cachedCurrencies[2]),//euro
            CurrencyAmount(1314.2682926, cachedCurrencies[3])//yen
        )
        expected_10_JPY = listOf(
            CurrencyAmount(0.093, cachedCurrencies[0]),//USD
            CurrencyAmount(0.076, cachedCurrencies[1]),//GBP
            CurrencyAmount(0.084, cachedCurrencies[2]),//euro
            CurrencyAmount(amt, cachedCurrencies[3])//yen
        )
    }
}