package com.example.mymess.Models

data class BalanceItemModel(
    val balanceid: String? ="",
    val startDate:String?= "",
    val endDate:String?= "",
    val balanceamount:String?="",
    val presentDates : MutableList<String>?=null,
    val absentDates : MutableList<String>?=null
)
