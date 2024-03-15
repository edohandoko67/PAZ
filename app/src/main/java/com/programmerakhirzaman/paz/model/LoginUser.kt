package com.programmerakhirzaman.paz.model

data class LoginUser(
    val user: String,
    val email: String = "",
    val password: String = "",
    val level: String = "user",
    val status: Int = 0
)
