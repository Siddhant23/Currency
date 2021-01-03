package com.japan.paypay.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyExchangeRate

@Database(
    entities = [CurrencyExchangeRate::class, Currency::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(value = [ExchangeRatesMapTypeConverter::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun currenciesDao(): CurrenciesDao

    companion object {
        private const val DB_NAME = "currency_converter_db"
        fun getInstance(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME).build()
    }
}