package com.japan.paypay.model.data


data class CurrenciesResp(
    val success: Boolean,
    val terms: String,
    val privacy: String,
    val currencies: Map<String, String>
)