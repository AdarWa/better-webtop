package net.adarw.bettersmartschool.api

import kotlinx.serialization.Serializable

@Serializable
data class ShotefScheduleRequest(
    val institutionCode: Int,
    val selectedValue: String,
    val typeView: Int
)