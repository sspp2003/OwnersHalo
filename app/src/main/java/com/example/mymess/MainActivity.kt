package com.example.mymess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymess.Adapters.StudentAdapter
import com.example.mymess.Models.StudentItemModel
import com.example.mymess.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var stulist= mutableListOf<StudentItemModel>()
    private var auth=FirebaseAuth.getInstance()
    private var databaseReference=FirebaseDatabase.getInstance().reference.child("users")
    private lateinit var mAdapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAdapter= StudentAdapter(stulist)
        val recyclerView=binding.studentRecyclerView
        recyclerView.adapter=mAdapter
        recyclerView.layoutManager=LinearLayoutManager(this)

        val currentUser=auth.currentUser

            databaseReference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    stulist.clear()
                    for(postsnapshot in snapshot.children){
                        val userdata=postsnapshot.getValue(StudentItemModel::class.java)

                        if(userdata!=null) {
                            val username = userdata.name
                            val userimage=userdata.profileImage

                            val stuitem=StudentItemModel(
                                username,
                                "",
                                userimage
                            )
                            stulist.add(stuitem)
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}