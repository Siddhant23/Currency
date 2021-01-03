package com.japan.paypay.repos

import com.google.common.truth.Truth.assertThat
import com.japan.paypay.model.data.CurrenciesResp
import com.japan.paypay.model.data.CurrencyExchangeRate
import com.japan.paypay.model.repo.CurrencyRepoImpl
import com.japan.paypay.service.APIs
import com.japan.paypay.utils.CurrencyException
import com.japan.paypay.utils.convertCurrencyMapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.OkHttpClient
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import paypay.fakes.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.util.concurrent.CountDownLatch

class CurrencyRepoImplTest {
    private lateinit var currenciesDao: FakeCurrenciesDao
    private lateinit var exchangeRateDao: FakeExchangeRateDao
    private lateinit var webService: FakeAPIs

    // Class under test
    private lateinit var currencyRepository: CurrencyRepoImpl

    @Before
    fun setUp() {
        currenciesDao = FakeCurrenciesDao(cachedCurrencies)
        exchangeRateDao = FakeExchangeRateDao(cachedExchangeRates)
        webService = FakeAPIs(remoteCurrencyResp, remoteExchangeRates, behaviourDelegate())
        currencyRepository = CurrencyRepoImpl(webService, currenciesDao, exchangeRateDao)
        currencyRepository.dispatcher = Dispatchers.Unconfined
    }

    /**
     * test whether the data is cached when u load remote
     * */
    @ExperimentalCoroutinesApi
    @Test
    fun loadCurrencies_withNoCachedData_remoteDataIsCached() = runBlockingTest {
        //GIVEN: empty cached data
        val currenciesDao = FakeCurrenciesDao(arrayListOf()) //empty cache dao
        val currencyRepository = CurrencyRepoImpl(webService, currenciesDao, exchangeRateDao)
        currencyRepository.dispatcher = Dispatchers.Unconfined //very important
        //make sure no cache data in teh repo
        assertThat(currencyRepository.loadCachedCurrencies()).isEmpty()
        //WHEN:
        currencyRepository.loadRemoteCurrencies()
        //THEN:
        val cachedData = currencyRepository.loadCachedCurrencies()
        MatcherAssert.assertThat(cachedData != null, CoreMatchers.`is`(true))
        assertThat(cachedData).isNotEmpty()
        assertThat(cachedData!!.size).isEqualTo(remoteCurrencyResp.currencies.size)
        val firstCurrency = cachedData[0]
        assertThat(firstCurrency.name).isEqualTo(remoteCurrencyResp.currencies[firstCurrency.code])
    }

    /**
     * test whether the data is cached when u load remote
     * */
    @ExperimentalCoroutinesApi
    @Test
    fun loadExchangeRates_withNoCachedData_remoteDataIsCached() = runBlockingTest {
        //GIVEN: empty cached data
        val exchangeRateDao = FakeExchangeRateDao(null) //empty cache dao
        val currencyRepository = CurrencyRepoImpl(webService, currenciesDao, exchangeRateDao)
        currencyRepository.dispatcher = Dispatchers.Unconfined //very important
        //make sure no cache data in the repo
        assertThat(currencyRepository.loadCachedExchangeRates()).isNull()
        //WHEN:
        currencyRepository.loadRemoteExchangeRates {
            runBlockingTest {
                //THEN:
                val cachedData = currencyRepository.loadCachedExchangeRates()
                MatcherAssert.assertThat(cachedData != null, CoreMatchers.`is`(true))
                MatcherAssert.assertThat(
                    cachedData!!.quotes,
                    CoreMatchers.`is`(remoteExchangeRates.quotes)
                )
                MatcherAssert.assertThat(
                    cachedData.source,
                    CoreMatchers.`is`(remoteExchangeRates.source)
                )
                MatcherAssert.assertThat(
                    cachedData.timestamp,
                    CoreMatchers.`is`(remoteExchangeRates.timestamp)
                )
                MatcherAssert.assertThat(
                    cachedData.quotes,
                    CoreMatchers.`is`(remoteExchangeRates.quotes)
                )
                MatcherAssert.assertThat(
                    cachedData.quotes["USD"], CoreMatchers.`is`(
                        remoteExchangeRates.quotes["USD"]
                    )
                )
                MatcherAssert.assertThat(
                    cachedData.quotes["GBP"], CoreMatchers.`is`(
                        remoteExchangeRates.quotes["GBP"]
                    )
                )
            }
        }

    }

    /**
     *  test whether cached data is retrieved when there is cached data
     */
    @ExperimentalCoroutinesApi
    @Test
    fun loadCurrencies_withCachedData_retrievesCachedData() = runBlockingTest {
        //WHEN:
        val data = currencyRepository.loadCurrencies()
        //THEN:
        assertThat(data.size).isEqualTo(cachedCurrencies.size)
        assertThat(data[0].id).isEqualTo(cachedCurrencies[0].id)
        assertThat(data[0].code).isEqualTo(cachedCurrencies[0].code)
        assertThat(data[data.size - 1].name).isEqualTo(cachedCurrencies[cachedCurrencies.size - 1].name)
        assertThat(data).isEqualTo(cachedCurrencies)
    }

    /**
     *  test whether cached data is retrieved when there is cached data
     */
    @ExperimentalCoroutinesApi
    @Test
    fun loadExchangeRates_withCachedData_retrievesCachedData() = runBlockingTest {
        //WHEN: repo.loadExchangeRates
        currencyRepository.loadExchangeRates({
            // THEN: verify data from callback
            assertThat(it).isNotNull()
            assertThat(it!!.id).isEqualTo(cachedExchangeRates.id)
            assertThat(it).isEqualTo(cachedExchangeRates)
        })
    }

    /**
     *  test whether remote currencies is retrieved when there is no cached data
     */
    @ExperimentalCoroutinesApi
    @Test
    fun loadCurrencies_withoutCachedData_retrievesRemoteData() = runBlockingTest {
        //GIVEN: Empty cached data
        val emptyCurrencyDao = FakeCurrenciesDao(arrayListOf())
        val currencyRepository = CurrencyRepoImpl(webService, emptyCurrencyDao, exchangeRateDao)
        currencyRepository.dispatcher = Dispatchers.Unconfined//vry important
        // make sure cache data is empty
        assertThat(currencyRepository.loadCachedCurrencies()).isEmpty()
        //WHEN: Load currencies
        val data = currencyRepository.loadCurrencies()
        //THEN: retrieve remote data
        assertThat(data.size).isEqualTo(remoteCurrencyResp.currencies.size)
        val firstElement = data[0]
        val lastElement = data[data.size - 1]
        assertThat(firstElement.name).isEqualTo(remoteCurrencyResp.currencies[firstElement.code])
        assertThat(lastElement.name).isEqualTo(remoteCurrencyResp.currencies[lastElement.code])
    }

    /**
     *  test whether remote exchange rates is retrieved when there is no cached data
     */
    @ExperimentalCoroutinesApi
    @Test
    fun loadExchangeRates_withoutCachedData_retrievesRemoteData() = runBlockingTest {
        //GIVEN: Empty cached data
        val emptyExchangeRatesDao = FakeExchangeRateDao(null)
        val currencyRepository = CurrencyRepoImpl(webService, currenciesDao, emptyExchangeRatesDao)
        currencyRepository.dispatcher = Dispatchers.Unconfined //vry important
        // make sure cache data is empty
        assertThat(currencyRepository.loadCachedExchangeRates()).isNull()
        //WHEN: Load exchange rates
        currencyRepository.loadExchangeRates({
            //THEN: retrieve remote data
            assertThat(it).isNotNull()
            assertThat(it!!.timestamp).isEqualTo(remoteExchangeRates.timestamp)
            assertThat(it.source).isEqualTo(remoteExchangeRates.source)
            assertThat(it.quotes).isEqualTo(remoteExchangeRates.quotes)
        })
    }

    /**
     * test whether remote data is retrieved when u force remote
     * */
    @ExperimentalCoroutinesApi
    @Test
    fun loadCurrencies_forceRemote_retrievesRemoteData() = runBlockingTest {
        //GIVEN: both cached and remote data are the same
        val currencyMap = mapOf(
            Pair("USD", "United States Dollar"),
            Pair("NGN", "Nigerian Naira"),
            Pair("JPY", "Japanese Yen"),
            Pair("RAO", "Nothing")
        )
        val currentCurrencyResp = CurrenciesResp(
            true,
            "",
            "",
            currencyMap
        )
        val currentCurrencies = convertCurrencyMapToList(currencyMap)
        val currenciesDao =
            FakeCurrenciesDao(ArrayList(currentCurrencies)) //very important you clone currentCurrencies
        val webService = FakeAPIs(currentCurrencyResp, remoteExchangeRates, behaviourDelegate())
        val currencyRepository = CurrencyRepoImpl(webService, currenciesDao, exchangeRateDao)
        currencyRepository.dispatcher = Dispatchers.Unconfined //very important
        //confirm
        assertThat(currencyRepository.loadCachedCurrencies()).isEqualTo(currentCurrencies)
        assertThat(currencyRepository.loadRemoteCurrencies()).isEqualTo(currentCurrencies)
        assertThat(currencyRepository.loadCurrencies()).isEqualTo(currentCurrencies)
        // WHEN: assume remote currencies has changed
        webService.currencies = remoteCurrencyResp
        currencyRepository.setWebService(webService)
        // loadCurrencies with force remote
        val data = currencyRepository.loadCurrencies(true)
        //THEN:
        assertThat(data).isNotEqualTo(currentCurrencies)
        assertThat(data.size).isEqualTo(remoteCurrencyResp.currencies.size)
        val newData = convertCurrencyMapToList(remoteCurrencyResp.currencies)
        assertThat(data[0]).isEqualTo(newData[0])
        assertThat(data[data.size - 1]).isEqualTo(newData[newData.size - 1])
    }

    /**
     * test whether remote data is retrieved when u force remote
     * */
    @ExperimentalCoroutinesApi
    @Test
    fun loadExchangeRates_forceRemote_retrievesRemoteData() = runBlockingTest {
        //GIVEN: both cached and remote data are the same
        val currentExchangeRates = CurrencyExchangeRate(
            1,
            "111111",
            "JPY",
            mapOf(
                Pair("USD", 0.2313),
                Pair("GBP", 0.14567),
                Pair("EUR", 1.917021),
                Pair("JPY", 1.0)
            )
        )
        val dao = FakeExchangeRateDao(currentExchangeRates)
        val webService = FakeAPIs(null, currentExchangeRates, behaviourDelegate())
        val currencyRepository = CurrencyRepoImpl(webService, currenciesDao, dao)
        currencyRepository.dispatcher = Dispatchers.Unconfined //very important
        //confirm
        assertThat(currencyRepository.loadCachedExchangeRates()).isEqualTo(currentExchangeRates)
        currencyRepository.loadRemoteExchangeRates {
            assertThat(it).isEqualTo(currentExchangeRates)
        }
        // WHEN: assume remote currencies has changed
        webService.exchangeRates = remoteExchangeRates
        currencyRepository.setWebService(webService)
        // loadExchangeRates with force remote
        currencyRepository.loadExchangeRates({
            //THEN:
            assertThat(it).isNotEqualTo(currentExchangeRates)
            assertThat(it!!.quotes).isEqualTo(remoteExchangeRates.quotes)
            assertThat(it.source).isEqualTo(remoteExchangeRates.source)
            assertThat(it.timestamp).isEqualTo(remoteExchangeRates.timestamp)
        }, true)
        //assertEquals(true, lock.await(1000, TimeUnit.MILLISECONDS))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testRepoCleanSlate() = runBlockingTest {
        // GIVEN: a clean repo with no data
        val webService = FakeAPIs(null, null, behaviourDelegate())
        val currencyRepository = CurrencyRepoImpl(
            webService,
            FakeCurrenciesDao(arrayListOf()),
            FakeExchangeRateDao(null)
        )
        currencyRepository.dispatcher = Dispatchers.Unconfined
        val lock = CountDownLatch(1) // vry important to avoid this test flaky
        //confirm
        assertThat(currencyRepository.loadCachedCurrencies()).isEmpty()
        assertThat(currencyRepository.loadRemoteCurrencies()).isNull()
        assertThat(currencyRepository.loadCurrencies()).isEmpty()
        assertThat(currencyRepository.loadCachedExchangeRates()).isNull()
        currencyRepository.loadRemoteExchangeRates {
            assertThat(it).isNull()
        }
        //WHEN: server has data & it is fetched
        webService.exchangeRates = remoteExchangeRates
        webService.currencies = remoteCurrencyResp
        currencyRepository.setWebService(webService)
        currencyRepository.loadCurrencies()
        currencyRepository.loadExchangeRates({
            lock.countDown()
        })
        lock.await()
        // THEN
        assertThat(currencyRepository.loadCachedCurrencies()).isNotEmpty()
        assertThat(currencyRepository.loadRemoteCurrencies()).isNotEmpty()
        assertThat(currencyRepository.loadCachedExchangeRates()).isNotNull()
        currencyRepository.loadRemoteExchangeRates {
            assertThat(it).isNotNull()
        }
    }

    /**
     * test if CurrencyException thrown when remote server has error
     * */
    @ExperimentalCoroutinesApi
    @Test(expected = CurrencyException::class)
    fun loadRemoteCurrencies_remoteError_throwsCurrencyException() = runBlockingTest {
        //GIVEN: server has issues
        webService.shouldFail = true
        currencyRepository.setWebService(webService)
        //WHEN: remote method is called
        currencyRepository.loadRemoteCurrencies()
        //THEN: confirm CurrencyException is thrown by the repo
        fail(SERVER_ERROR_MSG)
    }
    /*@ExperimentalCoroutinesApi
    @Test(expected = CurrencyException::class)
    fun loadRemoteExchangeRates_remoteError_throwsCurrencyException() = runBlockingTest {
        //GIVEN: server has issues
        webService.shouldFail = true
        currencyRepository.setWebService(webService)
        //WHEN: remote method is called
        currencyRepository.loadRemoteExchangeRates {
            // do nothing bcos server will fail any way
        }
        //THEN: confirm CurrencyException is thrown by the repo
        fail(SERVER_ERROR_MSG)
    }*/
    /**
     * test if refresh callback is called when exchange rate is refreshed
     * */
    @Test
    fun registerExchangeRatesCallback_refreshExchangeRates_callBackIsCalled() {
        //GIVEN: currencyRepo registers a refresh callback with an invoke count of zero
        var lock = CountDownLatch(1)
        var invokeCount = 0
        val refreshCallback: (CurrencyExchangeRate?) -> Unit = {
            invokeCount++
            lock.countDown()
            lock =
                CountDownLatch(1) //vry important to reinitialize the lock for further refresh calls
        }
        currencyRepository.registerExchangeRatesCallback(refreshCallback)
        // WHEN: repo is refreshed
        currencyRepository.loadExchangeRates(null, true)
        lock.await()
        // THEN: verify refreshCallback was called
        assertThat(invokeCount).isEqualTo(1)
        // WHEN: repo is refreshed again
        //lock = CountDownLatch(1)
        currencyRepository.loadExchangeRates(null, true)
        lock.await()
        // THEN: verify refreshCallback was called
        assertThat(invokeCount).isEqualTo(2)
    }

    /*
    }
    * Utility method to create mock retrofit delegate
    * */
    private fun behaviourDelegate(): BehaviorDelegate<APIs> {
        val retrofit = Retrofit.Builder().baseUrl("http://example.com")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val mockRetrofit = MockRetrofit.Builder(retrofit)
            .networkBehavior(NetworkBehavior.create())
            .build()
        return mockRetrofit.create(APIs::class.java)
    }
}