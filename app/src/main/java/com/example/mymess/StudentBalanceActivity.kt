package com.example.mymess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymess.Adapters.BalanceAdapter
import com.example.mymess.Models.BalanceItemModel
import com.example.mymess.databinding.ActivityStudentBalanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StudentBalanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentBalanceBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth:FirebaseAuth
    private lateinit var mAdapter: BalanceAdapter
    private var ballist= mutableListOf<BalanceItemModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityStudentBalanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference=FirebaseDatabase.getInstance().getReference()
        auth=FirebaseAuth.getInstance()
        val userid=intent.getStringExtra("userid")

        mAdapter= BalanceAdapter(ballist)
        val recyclerView=binding.stubalanceRecyclerview
        recyclerView.layoutManager=LinearLayoutManager(this)
        recyclerView.adapter=mAdapter

        if (userid!=null){
            val balReference=databaseReference.child("balance").child(userid)

            balReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    ballist.clear()
                    for (balsnapshot in snapshot.children){
                        val balItem=balsnapshot.getValue(BalanceItemModel::class.java)

                        if(balItem!=null){
                            val startDate=balItem.startDate
                            val endDate=balItem.endDate
                            val balance=balItem.balanceamount

                            val balitem=BalanceItemModel(
                                "",
                                startDate,
                                endDate,
                                balance
                            )

                            ballist.add(balitem)
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