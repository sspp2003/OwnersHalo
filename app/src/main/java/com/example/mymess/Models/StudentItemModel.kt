package com.example.mymess.Models

data class StudentItemModel(
    val userid: String="",
    val name: String="",
    val email: String="",
    val profileImage: String=""
){
    constructor():this("","","")
}
