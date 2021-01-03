package com.japan.paypay.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.japan.paypay.model.data.Currency

@Dao
interface CurrenciesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(currency: Currency)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(currencies: List<Currency>)

    @Query("SELECT * FROM currencies")
    fun load(): List<Currency>?

    @Query("DELETE FROM currencies")
    fun clear(): Int
}