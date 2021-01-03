package com.japan.paypay.utils

import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyAmount
import java.text.DecimalFormat
import kotlin.math.round

private val decimalFormat = DecimalFormat("#,###.##")

fun convertCurrencyMapToList(map: Map<String, String>): ArrayList<Currency> {
    val list = arrayListOf<Currency>()
    for (entry in map)
        list.add(Currency(entry.key, entry.value))
    return list
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun display(currency: CurrencyAmount): String =
    currency.currency.code + "\n" + decimalFormat.format(currency.amount.round(2))