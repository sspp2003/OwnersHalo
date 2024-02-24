package com.example.mymess

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mymess.Models.AttendanceItemModel
import com.example.mymess.databinding.ActivityAttendanceBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceBinding
    private var selectedDate: String = ""
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference=FirebaseDatabase.getInstance().getReference()

        //Selected User
        val userid = intent.getStringExtra("userid")

        //updating the present and update count
        handlepresentabsentcount(userid)

        //updating starting and ending date
        handlestartendDate(userid)

        //Handling The Calender
        val calendar = Calendar.getInstance()

        selectedDate = String.format(
            Locale.getDefault(), "%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(
                Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))

        binding.buttonMarkPresent.setOnClickListener {
            HandlePresent(selectedDate,userid)
            handlepresentabsentcount(userid)
        }

        binding.buttonMarkAbsent.setOnClickListener {
            HandleAbsent(selectedDate,userid)
            handlepresentabsentcount(userid)
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            Log.d("AttendanceActivity", "Selected Date: $selectedDate")
        }
        binding.editStart.setOnClickListener(){
            showDatePickerDialog(userid)
//            editstartDate(userid)
            handlestartendDate(userid)
        }

        binding.editEnd.setOnClickListener(){
            showDatePickerDialogForEndDate(userid)
//            editendDate(userid)
            handlestartendDate(userid)
        }

    }

    private fun showDatePickerDialog(userid:String?) {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Update the TextView with the selected date
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)

                binding.messStartDate.text = formattedDate

                // Call editstartDate after the date is selected
                editstartDate(userid)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun showDatePickerDialogForEndDate(userid: String?){
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Update the TextView with the selected date
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)

                binding.messEndDate.text = formattedDate

                editendDate(userid)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()

    }
    private fun editendDate(userid: String?){
        if(userid!=null){
            val attendRef = databaseReference.child("attendance").child(userid)

            attendRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                        if (attendanceData!=null){
                            val newEndDate = binding.messEndDate.text.toString()
//                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                            val updateAttendance = attendanceData.copy(
                                endDate = newEndDate
                            )
                            attendRef.setValue(updateAttendance)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    private fun editstartDate(userid: String?) {
        if (userid != null) {
            val attendRef = databaseReference.child("attendance").child(userid)

            attendRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                        if (attendanceData != null) {
                            val newStartDate = binding.messStartDate.text.toString()
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                            try {
                                val startDate = dateFormat.parse(newStartDate)
                                val calendar = Calendar.getInstance()
                                calendar.time = startDate
                                calendar.add(Calendar.DAY_OF_MONTH, 30)
                                val endDate = calendar.time
                                val formattedEndDate = dateFormat.format(endDate)

                                val updateAttendance = attendanceData.copy(
                                    startDate = newStartDate,
                                    endDate = formattedEndDate
                                )

                                attendRef.setValue(updateAttendance)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle cancellation
                }
            })
        }
    }

    private fun handlestartendDate(userid: String?) {
        if (userid != null) {
            val attendRef = databaseReference.child("attendance").child(userid)

            attendRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                        if (attendanceData != null) {
                            binding.messStartDate.text = attendanceData.startDate
                            binding.messEndDate.text = attendanceData.endDate
                        }
                    } else {
                        binding.messStartDate.text=""
                        binding.messEndDate.text=""
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun handlepresentabsentcount(userid: String?) {
        if(userid!=null){
            val attendRef=databaseReference.child("attendance").child(userid)

            attendRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                        if(attendanceData!=null){
                            binding.presentCount.text=attendanceData.presentCount.toString()
                            binding.absentCount.text=attendanceData.absentCount.toString()
                        }
                    }
                    else{
                        binding.presentCount.text="0"
                        binding.absentCount.text="0"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun HandleAbsent(selectedDate: String, userid: String?) {
        if(userid!=null){
            val attendRef=databaseReference.child("attendance").child(userid)

            databaseReference.child("attendance").child(userid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)

                            if (attendanceData != null) {
                                val presentDates = attendanceData.presentDates ?: mutableListOf()
                                val absentDates=attendanceData.absentDates?: mutableListOf()

                                if (absentDates.contains(selectedDate)) {
                                    // Date already marked present
                                    Toast.makeText(this@AttendanceActivity, "$selectedDate already marked absent", Toast.LENGTH_SHORT).show()
                                }

                                else if(presentDates.contains(selectedDate)){
                                    //date already marked present
                                    presentDates.remove(selectedDate)
                                    absentDates.add(selectedDate)
                                    val updatedAttendanceData = attendanceData.copy(
                                        absentDates = absentDates,
                                        absentCount = absentDates.size,
                                        presentDates = presentDates,
                                        presentCount = presentDates.size
                                    )
                                    attendRef.setValue(updatedAttendanceData)
                                    Toast.makeText(this@AttendanceActivity,"attendance marked absent on date $selectedDate"
                                        ,Toast.LENGTH_SHORT).show()
                                }

                                else {
                                    // Date not marked absent, update the attendance data
                                    absentDates.add(selectedDate)
                                    val updatedAttendanceData = attendanceData.copy(
                                        absentDates = absentDates,
                                        absentCount = absentDates.size
                                    )
                                    attendRef.setValue(updatedAttendanceData)
                                    Toast.makeText(this@AttendanceActivity,"attendance marked absent on date $selectedDate"
                                        ,Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        else{
                            val absentDates = mutableListOf(selectedDate)
                            val currentdate= SimpleDateFormat("yyy-MM-dd").format(Date())
                            val currentDate = Calendar.getInstance()
                            currentDate.add(Calendar.DAY_OF_MONTH, 30)
                            val futureDate = currentDate.time
                            val formattedFutureDate = SimpleDateFormat("yyyy-MM-dd").format(futureDate)

                            val attItem=AttendanceItemModel(
                                absentDates=absentDates,
                                absentCount = absentDates.size,
                                startDate = currentdate,
                                endDate = formattedFutureDate
                            )
                            databaseReference.child("attendance").child(userid).setValue(attItem)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }

    private fun HandlePresent(selectedDate: String, userid: String?) {
        if(userid!=null){
            val attendRef=databaseReference.child("attendance").child(userid)

            databaseReference.child("attendance").child(userid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                   override fun onDataChange(snapshot: DataSnapshot) {
                       if(snapshot.exists()){
                           val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                           if (attendanceData != null) {
                               val presentDates = attendanceData.presentDates ?: mutableListOf()
                               val absentDates=attendanceData.absentDates?: mutableListOf()

                               if (presentDates.contains(selectedDate)) {
                                   // Date already marked present
                                   Toast.makeText(this@AttendanceActivity, "$selectedDate already marked present"
                                       , Toast.LENGTH_SHORT).show()
                               }

                               else if(absentDates.contains(selectedDate)){
                                   //date already marked absent
                                   absentDates.remove(selectedDate)
                                   presentDates.add(selectedDate)
                                   val updatedAttendanceData = attendanceData.copy(
                                       absentDates = absentDates,
                                       absentCount = absentDates.size,
                                       presentDates = presentDates,
                                       presentCount = presentDates.size
                                   )
                                   attendRef.setValue(updatedAttendanceData)
                                   Toast.makeText(this@AttendanceActivity,"attendance marked present on date $selectedDate"
                                       ,Toast.LENGTH_SHORT).show()
                               }

                               else {
                                   // Date not marked present, update the attendance data
                                   presentDates.add(selectedDate)
                                   val updatedAttendanceData = attendanceData.copy(
                                       presentDates = presentDates,
                                       presentCount = presentDates.size
                                   )
                                   attendRef.setValue(updatedAttendanceData)
                                   Toast.makeText(this@AttendanceActivity,"attendance marked present on date $selectedDate"
                                       ,Toast.LENGTH_SHORT).show()
                               }
                           }
                       }

                       else{
                           val presentDates = mutableListOf(selectedDate)
                           val currentdate= SimpleDateFormat("yyy-MM-dd").format(Date())
                           val currentDate = Calendar.getInstance()
                           currentDate.add(Calendar.DAY_OF_MONTH, 30)
                           val futureDate = currentDate.time
                           val formattedFutureDate = SimpleDateFormat("yyyy-MM-dd").format(futureDate)

                           val attItem=AttendanceItemModel(
                               presentDates = presentDates,
                               absentDates = mutableListOf(),
                               presentCount = presentDates.size,
                               absentCount = 0,
                               startDate = currentdate,
                               endDate = formattedFutureDate
                           )
                           databaseReference.child("attendance").child(userid).setValue(attItem)
                       }
                   }

                   override fun onCancelled(error: DatabaseError) {
                       TODO("Not yet implemented")
                   }

               })
        }
    }

    private fun getSelectedDateFromCalendar(): String {
        return selectedDate
    }
}