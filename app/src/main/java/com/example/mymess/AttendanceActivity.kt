package com.example.mymess

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION_CODES.R
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
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().getReference()

        //Selected User
        auth=FirebaseAuth.getInstance()
        val userid = intent.getStringExtra("userid")
        val currentUserid=auth.currentUser!!.uid

        //putting name
        if(userid!=null){
            val nameRef=databaseReference.child("MessOwners").child(currentUserid).child("users").child(userid).child("name")

            nameRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val name=snapshot.getValue(String::class.java)

                        binding.attendName.text=name
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

        //updating the present and update count
        handlepresentabsentcount(userid)

        //updating starting and ending date
        handlestartendDate(userid)

        //Handling The Calender
        //changed format to dd mm yyyy
        val calendar = Calendar.getInstance()

        selectedDate = String.format(
            Locale.getDefault(),
            "%02d %02d %04d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )

        //Handle Deleting of wrongly marked Dates

        binding.deleteDate.setOnClickListener {
            HandleDeleteDate(selectedDate,userid)
            handlepresentabsentcount(userid)
        }

        binding.buttonMarkPresent.setOnClickListener {
            HandlePresent(selectedDate, userid)
            handlepresentabsentcount(userid)
        }

        binding.buttonMarkAbsent.setOnClickListener {
            HandleAbsent(selectedDate, userid)
            handlepresentabsentcount(userid)
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(
                Locale.getDefault(),
                "%02d %02d %04d",
                dayOfMonth,
                month + 1,
                year
            )
            Log.d("AttendanceActivity", "Selected Date: $selectedDate")
        }

        binding.editStart.setOnClickListener() {
            showDatePickerDialog(userid)
//            editstartDate(userid)
            handlestartendDate(userid)
        }

        binding.editEnd.setOnClickListener() {
            showDatePickerDialogForEndDate(userid)
//            editendDate(userid)
            handlestartendDate(userid)
        }

        binding.addTobalanceBtn.setOnClickListener {
            val presentcount = binding.presentCount.text.toString()
            val pc = presentcount.toInt()
            val absentcount = binding.absentCount.text.toString()
            val ac = absentcount.toInt()
            showDialogBalance(
                binding.messStartDate.text.toString(),
                binding.messEndDate.text.toString(),
                pc,
                userid
            )
        }

        //Moving to PresentAbsent activity on clicking present and absent button
        binding.linear1.setOnClickListener {
            val intent = Intent(this, PresentActivity::class.java)
            intent.putExtra("userid", userid)
            startActivity(intent)
        }

        binding.linear2.setOnClickListener {
            val intent = Intent(this, AbsentActivity::class.java)
            intent.putExtra("userid", userid)
            startActivity(intent)
        }
    }

    private fun showDialogBalance(startdate: String, enddate: String, pc: Int, userid: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.balance_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val confirm_btn: Button = dialog.findViewById(R.id.btn_addtobalance)
        val cancel: Button = dialog.findViewById(R.id.btn_cancel)
        val messstartdate: TextView = dialog.findViewById(R.id.startDate)
        val messenddate: TextView = dialog.findViewById(R.id.enddate)
        val amount_et: EditText = dialog.findViewById(R.id.balanceamount_et)

        messstartdate.text = startdate
        messenddate.text = enddate

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        confirm_btn.setOnClickListener {
            if (userid != null) {
                val balanceRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("balance").child(userid)
                val attRef=databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

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
                                    val intent =
                                        Intent(this@AttendanceActivity, BalanceActivity::class.java)
                                    startActivity(intent)
                                    dialog.dismiss()
                                }

                                //Adding present dates and absent dates to balance node

                                val attendRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

                                attendRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val attendanceData =
                                                snapshot.getValue(AttendanceItemModel::class.java)

                                            if (attendanceData != null) {
                                                balanceRef.child(key ?: "").child("presentDates")
                                                    .setValue(attendanceData.presentDates)
                                                balanceRef.child(key ?: "").child("absentDates")
                                                    .setValue(attendanceData.absentDates)
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })

                                attRef.removeValue()
                            }
                        } else {
                            Toast.makeText(
                                this@AttendanceActivity,
                                "Balance already added",
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@AttendanceActivity,
                            "Error fetching data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }

        databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("Rate").get().addOnSuccessListener { dataSnapshot ->

            if (dataSnapshot.exists()) {
                val rateDataString = dataSnapshot.value as? String
                val rateDataInt = rateDataString?.toIntOrNull() ?: 0
                val totalamount = pc * rateDataInt
                val totalAmountString = totalamount.toString()

                amount_et.setText(totalAmountString)
            } else {
                Toast.makeText(this, "Please Set Rate", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "unable to fetch data", Toast.LENGTH_SHORT).show()
        }

        dialog.show()

    }


    private fun showDatePickerDialog(userid: String?) {
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

                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
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

    private fun showDatePickerDialogForEndDate(userid: String?) {
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

                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
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

    private fun editendDate(userid: String?) {
        if (userid != null) {
            val attendRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

            attendRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                        if (attendanceData != null) {
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
            val attendRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

            attendRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                        if (attendanceData != null) {
                            val newStartDate = binding.messStartDate.text.toString()
                            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

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
            val attendRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

            attendRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                        if (attendanceData != null) {
                            binding.messStartDate.text = attendanceData.startDate
                            binding.messEndDate.text = attendanceData.endDate
                        }
                    } else {
                        binding.messStartDate.text = ""
                        binding.messEndDate.text = ""
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun handlepresentabsentcount(userid: String?) {
        if (userid != null) {
            val attendRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

            attendRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                        if (attendanceData != null) {
                            binding.presentCount.text = attendanceData.presentCount.toString()
                            binding.absentCount.text = attendanceData.absentCount.toString()
                        }
                    } else {
                        binding.presentCount.text = "0"
                        binding.absentCount.text = "0"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun HandleAbsent(selectedDate: String, userid: String?) {
        // Format the selected date with "-" separator
        val formattedDate = formatDate(selectedDate)

        if (userid != null) {
            val attendRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

                attendRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)

                            if (attendanceData != null) {
                                val presentDates = attendanceData.presentDates ?: mutableListOf()
                                val absentDates = attendanceData.absentDates ?: mutableListOf()

                                if (absentDates.contains(formattedDate)) {
                                    // Date already marked present
                                    Toast.makeText(
                                        this@AttendanceActivity,
                                        "$formattedDate already marked absent",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (presentDates.contains(formattedDate)) {
                                    //date already marked present
                                    presentDates.remove(formattedDate)
                                    absentDates.add(formattedDate)
                                    val updatedAttendanceData = attendanceData.copy(
                                        absentDates = absentDates,
                                        absentCount = absentDates.size,
                                        presentDates = presentDates,
                                        presentCount = presentDates.size
                                    )
                                    attendRef.setValue(updatedAttendanceData)
                                    Toast.makeText(
                                        this@AttendanceActivity,
                                        "attendance marked absent on date $formattedDate",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Check if the selected date is the day after messEndDate
                                    val endDateString = binding.messEndDate.text.toString()
                                    val endDate = SimpleDateFormat("dd-MM-yyyy").parse(endDateString)

                                    val calendar = Calendar.getInstance()
                                    calendar.time = endDate
                                    calendar.add(Calendar.DAY_OF_MONTH, 1) // Add one day to endDate

                                    val nextDayAfterEnd = calendar.time
                                    val nextDayAfterEndFormatted =
                                        SimpleDateFormat("dd-MM-yyyy").format(nextDayAfterEnd)

                                    val selectedDateFormat = SimpleDateFormat("dd-MM-yyyy").parse(formattedDate)
                                    if (selectedDateFormat.after(endDate) || formattedDate == nextDayAfterEndFormatted) {
                                        // The selected date is after endDate or the next day after endDate
                                        Toast.makeText(
                                            this@AttendanceActivity,
                                            "Please add to balance",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }else {
                                        // Date not marked absent, update the attendance data
                                        absentDates.add(formattedDate)
                                        val updatedAttendanceData = attendanceData.copy(
                                            absentDates = absentDates,
                                            absentCount = absentDates.size
                                        )
                                        attendRef.setValue(updatedAttendanceData)
                                        Toast.makeText(
                                            this@AttendanceActivity,
                                            "attendance marked absent on date $formattedDate",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            val absentDates = mutableListOf(formattedDate)
                            val currentdate = SimpleDateFormat("dd-MM-yyyy").format(Date())
                            val currentDate = Calendar.getInstance()
                            currentDate.add(Calendar.DAY_OF_MONTH, 30)
                            val futureDate = currentDate.time
                            val formattedFutureDate =
                                SimpleDateFormat("dd-MM-yyyy").format(futureDate)

                            val attItem = AttendanceItemModel(
                                absentDates = absentDates,
                                absentCount = absentDates.size,
                                startDate = currentdate,
                                endDate = formattedFutureDate
                            )
                            databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid).setValue(attItem)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle cancellation
                    }

                })
        }
    }

    private fun HandleDeleteDate(selectedDate: String, userid: String?) {
        val formattedDate = formatDate(selectedDate)

        if(userid!=null){
            val attendRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

            attendRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)

                        if(attendanceData!=null){
                            val presentDates = attendanceData.presentDates ?: mutableListOf()
                            val absentDates = attendanceData.absentDates ?: mutableListOf()

                            if (presentDates.contains(formattedDate)){
                                presentDates.remove(formattedDate)
                                val updatedAttendanceData = attendanceData.copy(
                                    presentDates = presentDates,
                                    presentCount = presentDates.size
                                )

                                attendRef.setValue(updatedAttendanceData)
                                Toast.makeText(this@AttendanceActivity,"$formattedDate removed",Toast.LENGTH_SHORT).show()

                            }

                            else if(absentDates.contains(formattedDate)){
                                absentDates.remove(formattedDate)
                                val updatedAttendanceData = attendanceData.copy(
                                    absentDates = absentDates,
                                    absentCount = absentDates.size
                                )

                                attendRef.setValue(updatedAttendanceData)
                                Toast.makeText(this@AttendanceActivity,"$formattedDate removed",Toast.LENGTH_SHORT).show()

                            }

                            else{
                                Toast.makeText(this@AttendanceActivity,"Date Not Marked",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }
    private fun HandlePresent(selectedDate: String, userid: String?) {
        // Format the selected date with "-" separator
        val formattedDate = formatDate(selectedDate)

        if (userid != null) {
            val attendRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

            attendRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val attendanceData = snapshot.getValue(AttendanceItemModel::class.java)
                            if (attendanceData != null) {
                                val presentDates = attendanceData.presentDates ?: mutableListOf()
                                val absentDates = attendanceData.absentDates ?: mutableListOf()

                                if (presentDates.contains(formattedDate)) {
                                    // Date already marked present
                                    Toast.makeText(
                                        this@AttendanceActivity,
                                        "$formattedDate already marked present",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (absentDates.contains(formattedDate)) {
                                    //date already marked absent
                                    absentDates.remove(formattedDate)
                                    presentDates.add(formattedDate)
                                    val updatedAttendanceData = attendanceData.copy(
                                        absentDates = absentDates,
                                        absentCount = absentDates.size,
                                        presentDates = presentDates,
                                        presentCount = presentDates.size
                                    )
                                    attendRef.setValue(updatedAttendanceData)
                                    Toast.makeText(
                                        this@AttendanceActivity,
                                        "attendance marked present on date $formattedDate",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Check if the selected date is the day after messEndDate
                                    val endDateString = binding.messEndDate.text.toString()
                                    val endDate = SimpleDateFormat("dd-MM-yyyy").parse(endDateString)

                                    val calendar = Calendar.getInstance()
                                    calendar.time = endDate
                                    calendar.add(Calendar.DAY_OF_MONTH, 1) // Add one day to endDate

                                    val nextDayAfterEnd = calendar.time
                                    val nextDayAfterEndFormatted =
                                        SimpleDateFormat("dd-MM-yyyy").format(nextDayAfterEnd)

                                    val selectedDateFormat = SimpleDateFormat("dd-MM-yyyy").parse(formattedDate)
                                    if (selectedDateFormat.after(endDate) || formattedDate == nextDayAfterEndFormatted) {
                                        // The selected date is after endDate or the next day after endDate
                                        Toast.makeText(
                                            this@AttendanceActivity,
                                            "Please add to balance",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        // Date not marked present, update the attendance data
                                        presentDates.add(formattedDate)
                                        val updatedAttendanceData = attendanceData.copy(
                                            presentDates = presentDates,
                                            presentCount = presentDates.size
                                        )
                                        attendRef.setValue(updatedAttendanceData)
                                        Toast.makeText(
                                            this@AttendanceActivity,
                                            "attendance marked present on date $formattedDate",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            val presentDates = mutableListOf(formattedDate)
                            val currentdate = SimpleDateFormat("dd-MM-yyyy").format(Date())
                            val currentDate = Calendar.getInstance()
                            currentDate.add(Calendar.DAY_OF_MONTH, 30)
                            val futureDate = currentDate.time
                            val formattedFutureDate =
                                SimpleDateFormat("dd-MM-yyyy").format(futureDate)

                            val attItem = AttendanceItemModel(
                                presentDates = presentDates,
                                absentDates = mutableListOf(),
                                presentCount = presentDates.size,
                                absentCount = 0,
                                startDate = currentdate,
                                endDate = formattedFutureDate
                            )
                            databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid).setValue(attItem)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle cancellation
                    }

                })
        }
    }

    // Function to format the date with "-" separator
    private fun formatDate(dateString: String): String {
        val parts = dateString.split(" ")
        return if (parts.size == 3) {
            "${parts[0]}-${parts[1]}-${parts[2]}"
        } else {
            dateString
        }
    }
}