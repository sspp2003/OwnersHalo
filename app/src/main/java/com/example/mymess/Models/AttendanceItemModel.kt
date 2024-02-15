package com.example.mymess.Models

data class AttendanceItemModel(
    val present : MutableList<String>?=null,
    val absent : MutableList<String>?=null,
    val startDate:String = "",
    val endDate:String = ""
    )
