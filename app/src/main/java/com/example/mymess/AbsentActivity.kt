package com.example.mymess

import AbsentAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymess.Models.AttendanceItemModel
import com.example.mymess.databinding.ActivityAbsentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AbsentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAbsentBinding
    private lateinit var auth: FirebaseAuth
    private var attlist= mutableListOf<AttendanceItemModel>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAdapter: AbsentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()

        val userid = intent.getStringExtra("userid")

        if (userid != null) {
            mAdapter = AbsentAdapter(attlist)

            val recyclerview = binding.attDatesRecyclerview
            recyclerview.layoutManager = LinearLayoutManager(this)
            recyclerview.adapter = mAdapter

            val attRef = databaseReference.child("MessOwners").child(auth.currentUser!!.uid).child("attendance").child(userid)

            attRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    attlist.clear()
                    if (snapshot.exists()) {
                        val attitem = snapshot.getValue(AttendanceItemModel::class.java)

                        if (attitem != null) {
                            val attItem = AttendanceItemModel(
                                attitem.presentDates,
                                attitem.absentDates
                            )

                            attlist.add(attItem)
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })

        }
    }
}
