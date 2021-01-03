package com.japan.paypay.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.japan.paypay.model.data.CurrencyExchangeRate
import com.japan.paypay.model.local.AppDatabase
import com.japan.paypay.model.local.ExchangeRateDao
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ExchangeRateDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ExchangeRateDao
    private lateinit var testObject: CurrencyExchangeRate

    @Before
    fun setUp() {
        //database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        //dao
        dao = database.exchangeRateDao()
        //test object
        testObject = CurrencyExchangeRate(
            1,
            "1590338106",
            "USD",
            mapOf(
                Pair("USD", 1.0),
                Pair("GBP", 0.821959),
                Pair("EUR", 0.917011),
                Pair("JPY", 107.650385),
                Pair("NGN", 390.503727)
            )
        )
    }

    @After
    fun cleanUp() = database.close()

    @Test
    fun data_isSaved() {
        // GIVEN - live exchange rates is cached
        dao.save(testObject)
        // WHEN - load cached data
        val exchangeRate = dao.load()
        // THEN - contains the expected values
        assertThat(exchangeRate != null, `is`(true))
        assertThat(exchangeRate!!.id, `is`(testObject.id))
        assertThat(exchangeRate.source, `is`(testObject.source))
        assertThat(exchangeRate.timestamp, `is`(testObject.timestamp))
        assertThat(exchangeRate.quotes.size, `is`(testObject.quotes.size))
        assertThat(exchangeRate.quotes["USD"], `is`(testObject.quotes["USD"]))
        assertThat(exchangeRate.quotes["GBY"], `is`(testObject.quotes["GBY"]))
    }

    @Test
    fun clearDB() {
        // GIVEN - live exchange rates is cached
        dao.save(testObject)
        // WHEN - clear cached data
        val count = dao.clear()
        // THEN - no data in DB
        val exchangeRate = dao.load()
        assertThat(exchangeRate == null, `is`(true))
        assertThat(count, `is`(1))
    }
}