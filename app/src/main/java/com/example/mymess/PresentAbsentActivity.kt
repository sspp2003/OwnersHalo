package com.example.mymess

import PresentAbsentAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymess.Models.AttendanceItemModel
import com.example.mymess.databinding.ActivityPresentAbsentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PresentAbsentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPresentAbsentBinding
    private lateinit var auth: FirebaseAuth
    private var attlist= mutableListOf<AttendanceItemModel>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var mAdapter: PresentAbsentAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPresentAbsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()
        databaseReference= FirebaseDatabase.getInstance().getReference()

        val userid=intent.getStringExtra("userid")
        val action=intent.getStringExtra("action")

        if(userid!=null){
            if (action == "present") {
                mAdapter = PresentAbsentAdapter(attlist, true)
            } else {
                binding.presentabsentTv.text="ABSENT DATES"
                mAdapter = PresentAbsentAdapter(attlist, false)
            }

            val recyclerview = binding.attDatesRecyclerview
            recyclerview.layoutManager = LinearLayoutManager(this)
            recyclerview.adapter = mAdapter

            val attRef = databaseReference.child("attendance").child(userid)

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

                }

            })
        }
    }
}