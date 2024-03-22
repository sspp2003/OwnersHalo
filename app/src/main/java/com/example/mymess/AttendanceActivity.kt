package com.example.mymess

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mymess.Models.AttendanceItemModel
import com.example.mymess.Models.BalanceItemModel
import com.example.mymess.databinding.ActivityAttendanceBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

        binding.addTobalanceBtn.setOnClickListener {
            val presentcount=binding.presentCount.text.toString()
            val pc=presentcount.toInt()
            val absentcount=binding.absentCount.text.toString()
            val ac=absentcount.toInt()
            showDialogBalance(binding.messStartDate.text.toString(),binding.messEndDate.text.toString(),pc,userid)
        }

        //Moving to PresentAbsent activity on clicking present and absent button
        binding.linear1.setOnClickListener {
            val intent=Intent(this,PresentAbsentActivity::class.java)
            intent.putExtra("action","present")
            intent.putExtra("userid",userid)
            startActivity(intent)
        }

        binding.linear2.setOnClickListener {
            val intent=Intent(this,PresentAbsentActivity::class.java)
            intent.putExtra("action","absent")
            intent.putExtra("userid",userid)
            startActivity(intent)
        }
    }

    private fun showDialogBalance(startdate: String, enddate: String, pc: Int, userid: String?) {
        val dialog= Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.balance_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val confirm_btn: Button=dialog.findViewById(R.id.btn_addtobalance)
        val cancel: Button=dialog.findViewById(R.id.btn_cancel)
        val messstartdate: TextView=dialog.findViewById(R.id.startDate)
        val messenddate: TextView=dialog.findViewById(R.id.enddate)
        val amount_et: EditText=dialog.findViewById(R.id.balanceamount_et)

        messstartdate.text=startdate
        messenddate.text=enddate

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        confirm_btn.setOnClickListener {
            if (userid != null) {
                val balanceRef = databaseReference.child("balance").child(userid)

                balanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var balanceAlreadyAdded = false
                        var key: String? = null

                        for (snapshot in dataSnapshot.children) {
                            val balanceItem = snapshot.getValue(BalanceItemModel::class.java)

                            if (balanceItem != null && balanceItem.startDate == startdate && balanceItem.endDate == enddate) {
                                balanceAlreadyAdded = true
                                key = snapshot.key
                                break
                            }
                        }

                        if (!balanceAlreadyAdded) {
                            key = balanceRef.push().key
                            if (key != null) {
                                val balRef = balanceRef.child(key)
                                val balanceItem = BalanceItemModel(
                                    key,
                                    startdate,
                                    enddate,
                                    amount_et.text.toString()
                                )

                                balRef.setValue(balanceItem).addOnSuccessListener {
                                    val intent = Intent(this@AttendanceActivity, BalanceActivity::class.java)
                                    startActivity(intent)
                                    dialog.dismiss()
                                }
                            }
                        } else {
                            Toast.makeText(this@AttendanceActivity, "Balance already added", Toast.LENGTH_SHORT).show()
                        }

                        val attendRef = databaseReference.child("attendance").child(userid)

                        attendRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)

                                    if (attendanceData != null) {
                                        balanceRef.child(key ?: "").child("presentDates").setValue(attendanceData.presentDates)
                                        balanceRef.child(key ?: "").child("absentDates").setValue(attendanceData.absentDates)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@AttendanceActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        databaseReference.child("Rate").get().addOnSuccessListener { dataSnapshot ->

            if (dataSnapshot.exists()) {
                val rateDataString = dataSnapshot.value as? String
                val rateDataInt = rateDataString?.toIntOrNull() ?: 0
                val totalamount=pc*rateDataInt
                val totalAmountString = totalamount.toString()

                amount_et.setText(totalAmountString)
            }
            else{
                Toast.makeText(this,"Please Set Rate",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this,"unable to fetch data",Toast.LENGTH_SHORT).show()
        }

        dialog.show()

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