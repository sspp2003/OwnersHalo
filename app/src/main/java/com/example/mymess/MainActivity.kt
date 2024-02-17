package com.example.mymess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.studentRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = auth.currentUser

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stulist = mutableListOf<StudentItemModel>()
                for (postSnapshot in snapshot.children) {
                    val userData = postSnapshot.getValue(StudentItemModel::class.java)

                    if (userData != null) {
                        val username = userData.name
                        val userImage = userData.profileImage
                        val userid=userData.userid

                        val stuitem = StudentItemModel(
                            userid,
                            username,
                            "",
                            userImage
                        )
                        stulist.add(stuitem)
                    }
                }
                val mAdapter = StudentAdapter(stulist)
                recyclerView.adapter = mAdapter

                binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        mAdapter.filter(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }
}