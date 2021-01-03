package com.japan.paypay.persistence

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.local.AppDatabase
import com.japan.paypay.model.local.CurrenciesDao
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class CurrenciesDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: CurrenciesDao
    private lateinit var testList: ArrayList<Currency>

    @Before
    fun setUp() {
        //database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        //dao
        dao = database.currenciesDao()
        //test object
        testList = arrayListOf(
            Currency("USD", "United States Dollar"),
            Currency("GBP", "British Pound Sterling"),
            Currency("EUR", "Euro"),
            Currency("JPY", "Japanese Yen"),
            Currency("NGN", "Nigerian Naira")
        )
    }

    @After
    fun cleanUp() = database.close()

    @Test
    fun data_isSaved() {
        // GIVEN - live exchange rates is cached
        dao.save(testList)
        // WHEN - load cached data
        val currencies = dao.load()
        // THEN - contains the expected values
        assertThat(currencies != null, `is`(true))
        assertThat(currencies!!.size, `is`(testList.size))
        assertThat(currencies[0].code, `is`(testList[0].code))
        assertThat(currencies[currencies.size - 1].name, `is`(testList[testList.size - 1].name))
    }

    @Test
    fun clearDB() {
        // GIVEN - live exchange rates is cached
        dao.save(testList)
        // WHEN - clear cached data
        val count = dao.clear()
        // THEN - no data in DB
        val currencies = dao.load()
        assertThat(count, `is`(testList.size))
        assertThat(currencies, `is`(emptyList()))
    }
}