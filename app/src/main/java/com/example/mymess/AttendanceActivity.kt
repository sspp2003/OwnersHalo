package com.example.mymess

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mymess.databinding.ActivityAttendanceBinding
import java.util.Calendar
import java.util.Locale

class AttendanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceBinding
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userid = intent.getStringExtra("userid")

        val calendar = Calendar.getInstance()

        selectedDate = String.format(
            Locale.getDefault(), "%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(
                Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))

        binding.buttonMarkPresent.setOnClickListener {
            Toast.makeText(this,"attendance marked present on date $selectedDate",Toast.LENGTH_SHORT).show()
        }

        binding.buttonMarkAbsent.setOnClickListener {
            Toast.makeText(this,"attendance marked absent on date $selectedDate",Toast.LENGTH_SHORT).show()
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            Log.d("AttendanceActivity", "Selected Date: $selectedDate")
        }
    }
    private fun getSelectedDateFromCalendar(): String {
        return selectedDate
    }
}