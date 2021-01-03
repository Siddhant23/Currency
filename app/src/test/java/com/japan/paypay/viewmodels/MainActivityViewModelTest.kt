package com.japan.paypay.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.japan.paypay.MainCoroutineRule
import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.repo.CurrencyRepo
import com.japan.paypay.utils.Status
import com.japan.paypay.vm.MainActivityViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import paypay.fakes.FakeCurrencyRepo
import paypay.fakes.cachedCurrencies
import paypay.fakes.cachedExchangeRates
import paypay.fakes.remoteExchangeRates
import java.util.concurrent.TimeUnit

class MainActivityViewModelTest {
    // set the instant executor rule for live data
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var repo: CurrencyRepo

    @Before
    fun setUp() {
        repo = FakeCurrencyRepo(cachedCurrencies, cachedExchangeRates, remoteExchangeRates)

    }

    /**
     * load data from repo correctly & initialize
     * */
    @Test
    fun init_dataInitialize() {
        //GIVEN: viewModel is constructed with repo
        val viewModel = MainActivityViewModel(repo)
        //THEN: verify data is loaded
        assertThat(viewModel.currencies.value!!.data).isEqualTo(cachedCurrencies)
        assertThat(viewModel.currencies.value!!.status).isEqualTo(Status.SUCCESS)
        assertThat(
            viewModel.currencies.value!!.message == null ||
                    viewModel.currencies.value!!.message!!.isEmpty()
        ).isEqualTo(true)
        assertThat(viewModel.exchangeRates.value?.data).isEqualTo(cachedExchangeRates)
        assertThat(viewModel.exchangeRates.value?.status).isEqualTo(Status.SUCCESS)
        assertThat(
            viewModel.exchangeRates.value?.message == null ||
                    viewModel.exchangeRates.value?.message!!.isEmpty()
        ).isEqualTo(true)
    }

    /**
     * verify loading status
     * */
    @ExperimentalCoroutinesApi
    @Test
    fun init_verifyLoading() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // GIVEN:
        val viewModel = MainActivityViewModel(repo)

        // THEN:
        assertThat(viewModel.currencies.value?.status).isEqualTo(Status.LOADING)

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden
        assertThat(viewModel.currencies.value?.status).isEqualTo(Status.SUCCESS)
    }

    /**
     * verify refreshed data reflected on viewModel
     * */
    @Test
    suspend fun verifyDataRefresh_onRepoRefreshed() {
        //GIVEN: repo with stale exchange rates data
        val repo = FakeCurrencyRepo(cachedCurrencies, cachedExchangeRates, null)
        val viewModel = MainActivityViewModel(repo)
        //confirm
        assertThat(viewModel.exchangeRates.value?.data).isEqualTo(cachedExchangeRates)
        //WHEN: repo is refreshed
        repo.remoteExchangeRates = remoteExchangeRates
        repo.loadExchangeRates(null, true)
        //THEN: verify viewModel data refreshed
        assertThat(viewModel.exchangeRates.value?.data).isEqualTo(remoteExchangeRates)
    }

    /**
     * repo data is empty or null - verify error handling
     * */
    @Test
    fun verify_error_status_when_repo_has_no_data() {
        //GIVEN: repo with no data
        val repo = FakeCurrencyRepo(null, null, null)
        val viewModel = MainActivityViewModel(repo)
        //confirm
        assertThat(viewModel.currencies.value?.data).isEmpty()
        //THEN: verify error status
        assertThat(viewModel.currencies.value?.status).isEqualTo(Status.ERROR)
        assertThat(viewModel.currencies.value?.message).isEqualTo("Server returned empty data")
    }

    /**
     * error occurs while repo fetch data - verify error handling
     */
    @Test
    fun verify_error_is_handled() {
        //GIVEN: faulty repo
        val repo = FakeCurrencyRepo(null, null, null, shouldThrowError = true)
        val viewModel = MainActivityViewModel(repo)
        //THEN: verify viewModel handled CurrencyException
        assertThat(viewModel.currencies.value?.status).isEqualTo(Status.ERROR)
        assertThat(viewModel.currencies.value?.message).isEqualTo(FakeCurrencyRepo.ERROR_MSG)
        assertThat(viewModel.currencies.value?.data).isEmpty()
        assertThat(viewModel.exchangeRates.value?.data).isNull()
    }

    /**
     * when viewModel already has valid data, should not swap for null or empty new data from repo
     */
    @Test
    suspend fun continueWithPreviousData_onNullOrEmptyNewData() {
        //GIVEN: view model with data
        val repo = FakeCurrencyRepo(cachedCurrencies, cachedExchangeRates, remoteExchangeRates)
        val viewModel = MainActivityViewModel(repo)
        //confirm exchange rates isn't null
        assertThat(viewModel.exchangeRates.value?.data).isNotNull()
        //WHEN: repo is refreshed with null data
        repo.remoteExchangeRates = null
        repo.loadExchangeRates(null, true)
        //THEN: verify viewModel is with previous data
        assertThat(viewModel.exchangeRates.value?.data).isNotNull()
    }

    @Test
    fun init_verify_calculator_is_initialised() {
        //GIVEN: view model with data
        val viewModel = MainActivityViewModel(repo)
        //THEN: calculator is initialised
        assertThat(viewModel.exchangeRateCalculator).isNotNull()
        assertThat(viewModel.exchangeRateCalculator?.currencies)
            .isEqualTo(viewModel.currencies.value?.data)
        assertThat(viewModel.exchangeRateCalculator?.exchangeRates)
            .isEqualTo(viewModel.exchangeRates.value?.data)
    }

    /**
     * verify calculator cannot initialise due to null exchangeRates or currencies
     */
    @Test
    fun init_onNullExchangeRatesOrCurrencies_verifyCalculatorIsNull() {
        //GIVEN viewModel with no-data repo
        var repo = FakeCurrencyRepo(null, null, null)
        var viewModel = MainActivityViewModel(repo)
        //THEN: verify calculator cannot initialise
        assertThat(viewModel.exchangeRateCalculator).isNull()
        //**************NEW TEST*****************
        //GIVEN viewModel with no currencies
        repo = FakeCurrencyRepo(null, cachedExchangeRates, remoteExchangeRates)
        viewModel = MainActivityViewModel(repo)
        //THEN: verify calculator cannot initialise
        assertThat(viewModel.exchangeRateCalculator).isNull()
        //**************NEW TEST*****************
        //GIVEN viewModel with no exchange rates at all
        repo = FakeCurrencyRepo(cachedCurrencies, null, null)
        viewModel = MainActivityViewModel(repo)
        //THEN: verify calculator cannot initialise
        assertThat(viewModel.exchangeRateCalculator).isNull()
    }

    /**
     * verify rates data is loaded on calculate
     */
    @Test
    fun calculate_verifyRatesIsCalculated() {
        //GIVEN: view model with data
        val viewModel = MainActivityViewModel(repo)
        //CONFIRM: empty or null exchange data
        assertThat(viewModel.resource.value?.data).isNull()
        //WHEN: find rates for 10 dollars
        viewModel.calculate(10.0, cachedCurrencies[0])
        //not reliable but helpful, sleep for 5 seconds to ensure the async method calculate(), invokes callback
        TimeUnit.SECONDS.sleep(5)
        //THEN: data has value
        assertThat(viewModel.resource.value?.data).isNotNull()
        assertThat(viewModel.resource.value?.data).isNotEmpty()
    }

    /**
     * verify calculate error with null calculator
     */
    @Test
    fun calculate_withNullCalculator_dataStatusIsError() {
        //GIVEN viewModel with no-data repo
        val repo = FakeCurrencyRepo(null, null, null)
        val viewModel = MainActivityViewModel(repo)
        //CONFIRM: calculator is null
        assertThat(viewModel.exchangeRateCalculator).isNull()
        //WHEN: find rates for 10 dollars
        viewModel.calculate(10.0, cachedCurrencies[0])
        //THEN: data has value
        assertThat(viewModel.resource.value?.status).isEqualTo(Status.ERROR)
        assertThat(viewModel.resource.value?.message).isEqualTo("unable to calculate exchange rates")
        assertThat(viewModel.resource.value?.data).isNull()
    }

    /**
     * verify calculate does not retain previous rates data on new data
     * NOTE: test will take long because it makes use of TimeUnit.SECONDS.sleep several times
     *       for the async calculate method
     */
    @Test
    fun calculate_withData_notRetain() {
        //GIVEN: view model with data
        val repo = FakeCurrencyRepo(cachedCurrencies, cachedExchangeRates, remoteExchangeRates)
        val viewModel = MainActivityViewModel(repo)
        //WHEN: calculate is called
        viewModel.calculate(10.0, cachedCurrencies[0])
        TimeUnit.SECONDS.sleep(5) //not reliable but helpful
        //THEN:assert data is not null & retrieve value
        assertThat(viewModel.resource.value?.data).isNotNull()
        val prev = viewModel.resource.value?.data!!

        //********NEW TEST**************//
        //WHEN: calculate is called again with different data
        viewModel.calculate(2.0, cachedCurrencies[1])
        TimeUnit.SECONDS.sleep(5)//not reliable
        //THEN: assert data is not equal to previous
        assertThat(viewModel.resource.value?.data).isNotEqualTo(prev)

        //********NEW TEST**************//
        //WHEN: calculate currency that doesn't exist
        viewModel.calculate(2.0, Currency("XXX", "No currency"))
        TimeUnit.SECONDS.sleep(5)//not reliable
        //THEN: assert data is null or empty i.e. didn't retain previous
        assertThat(
            viewModel.resource.value?.data == null ||
                    viewModel.resource.value?.data!!.isEmpty()
        ).isTrue()
    }
}
