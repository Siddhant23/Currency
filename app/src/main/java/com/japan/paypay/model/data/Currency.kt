package com.japan.paypay.model.data

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "currencies",
    indices = [Index(value = ["code", "name"], unique = true)]
)
data class Currency(
    @NonNull
    var code: String,
    @NonNull
    var name: String,
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
)