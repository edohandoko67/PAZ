package com.programmerakhirzaman.paz.model

import java.time.format.DateTimeFormatter

data class modalLocation(
    var user_id: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createTime: String
)