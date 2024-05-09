package com.example.mymess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymess.Adapters.TodayStatusAdapter
import com.example.mymess.Models.AttendanceItemModel
import com.example.mymess.Models.StudentItemModel
import com.example.mymess.databinding.ActivityTodayAbsentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodayAbsentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodayAbsentBinding
    private lateinit var auth: FirebaseAuth
    private var attlist = mutableListOf<StudentItemModel>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAdapter: TodayStatusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayAbsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swipeRefreshLayout.setOnRefreshListener {
            recreate()
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = simpleDateFormat.format(calendar.time)

        val attRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance")

        mAdapter = TodayStatusAdapter(attlist)
        val recyclerView = binding.todayAbsentRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter

        attRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                attlist.clear()
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key
                        val attendanceData = userSnapshot.getValue(AttendanceItemModel::class.java)

                        if (userId != null && attendanceData != null) {
                            val presentDates = attendanceData.presentDates

                            if (presentDates == null || !presentDates.contains(currentDate)) {
                                fetchUserDetails(userId)
                            }
                        }
                    }
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation
            }
        })
    }

    private fun fetchUserDetails(userId: String) {
        val userRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("users").child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userDetails = snapshot.getValue(StudentItemModel::class.java)
                    if (userDetails != null) {
                        attlist.add(userDetails)
                        mAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle cancellation
            }
        })
    }
}
