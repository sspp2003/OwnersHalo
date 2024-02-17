package com.example.mymess.Models

data class AttendanceItemModel(
    val presentDates : MutableList<String>?=null,
    val absentDates : MutableList<String>?=null,
    val presentCount: Int=0,
    val absentCount: Int=0,
    val startDate:String = "",
    val endDate:String = ""
    )
