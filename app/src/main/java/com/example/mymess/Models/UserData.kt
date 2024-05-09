package com.example.mymess.Models

data class UserData(
    val userid: String="",
    val Messname: String="",
    val email: String="",
){
    constructor():this("","","")
}
