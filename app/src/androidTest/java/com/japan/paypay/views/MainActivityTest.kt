package com.japan.paypay.views

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.japan.paypay.R
import com.japan.paypay.model.data.Currency
import com.japan.paypay.utils.EspressoIdlingResource
import com.japan.paypay.utils.ExchangeRateCalculator
import com.japan.paypay.utils.Resource
import com.japan.paypay.view.MainActivity
import com.japan.paypay.vm.MainActivityViewModel
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import paypay.fakes.cachedCurrencies
import paypay.fakes.cachedExchangeRates
import paypay.fakes.currencyAmountList

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var viewModel: MainActivityViewModel

    @get:Rule
    val activityRule =
        object : ActivityTestRule<MainActivity>(MainActivity::class.java, true, false) {
            override fun afterActivityLaunched() {
                super.afterActivityLaunched()
                viewModel = activity.viewModel()
            }
        }

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        //ActivityRule Setup & Launch
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        activityRule.launchActivity(intent)
    }

    @After
    fun cleanUp() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    /**
     * test activity with data
     * */
    @Test
    fun havingData_listDisplayed() {
        //confirm initial state
        onView(withId(R.id.tv_feedback)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
        //GIVEN:
        viewModel.resource.postValue(Resource.success(currencyAmountList))
        //THEN
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_feedback)).check(matches(not(isDisplayed())))
    }

    /**
     *  test activity's state without data
     * */
    @Test
    fun onEmptyData_displayFeedback() {
        //confirm initial state
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tv_feedback)).check(matches(isDisplayed()))
        //GIVEN: viewModel retrieves empty data
        viewModel.resource.postValue(Resource.success(listOf()))
        //THEN:
//        onView(withText("Empty Data")).check(matches(isDisplayed()))
        onView(withId(R.id.tv_feedback)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_feedback)).check(matches(withText("Empty Data")))
    }

    /**
     * test activity when compute exchange rate
     * */
    @Test
    fun compute_displayData() {
        //GIVEN: viewmodel has data
        viewModel.currencies.postValue(Resource.success(cachedCurrencies))
        viewModel.exchangeRateCalculator =
            ExchangeRateCalculator(cachedExchangeRates, cachedCurrencies)
        //CONFIRM STATE :
        onView(withId(R.id.recyclerView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.tv_feedback)).check(matches(isDisplayed()))
        // WHEN: user enter amount, select currency and submit
        onView(withId(R.id.edtxt_amt)).perform(replaceText(10.0.toString()))
        onView(withId(R.id.spn_currencies)).perform(click())
        onData(
            allOf(
                `is`(instanceOf(Currency::class.java)),
                `is`(cachedCurrencies[1])
            )
        ).perform(click())
        onView(withId(R.id.btn_submit)).perform(click())
        // THEN:
        onView(withId(R.id.tv_feedback)).check(matches(not(isDisplayed())))
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }

    /**
     *  test activity when compute returns error
     */
    @Test
    fun compute_hasError_displayErrorFeedback() {
        //GIVEN: viewmodel has null data already
        // WHEN: user enter amount, but can't select currency then clicks submit
        onView(withId(R.id.edtxt_amt)).perform(replaceText(10.toString()))
        onView(withId(R.id.btn_submit)).perform(click())
        // THEN: display toast
        onView(withText(any(String::class.java)))
            .inRoot(withDecorView(not(`is`(activityRule.activity.window.decorView))))
            .check(matches(isDisplayed()))
    }

    /**
     *  test controls are disabled on compute
     */
    @Test
    fun progress_controlsDisabled() {
        //GIVEN: viewmodel data value is in loading state
        viewModel.resource.postValue(Resource.loading(null))
        // THEN: controls are disabled
        onView(withId(R.id.edtxt_amt)).check(matches(not(isEnabled())))
        onView(withId(R.id.spn_currencies)).check(matches(not(isEnabled())))
        onView(withId(R.id.btn_submit)).check(matches(not(isEnabled())))
        // AFTER: viewmodel data value is in success state
        // THEN: controls are enabled
        viewModel.resource.postValue(Resource.success(currencyAmountList))
        onView(withId(R.id.edtxt_amt)).check(matches(isEnabled()))
        onView(withId(R.id.spn_currencies)).check(matches(isEnabled()))
        onView(withId(R.id.btn_submit)).check(matches(isEnabled()))
    }
}
