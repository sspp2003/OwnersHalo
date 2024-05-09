package com.example.mymess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mymess.Adapters.StudentAdapter
import com.example.mymess.Models.StudentItemModel
import com.example.mymess.databinding.ActivityBalanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BalanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBalanceBinding
    private lateinit var recyclerView:RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBalanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()

        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // Call a method to refresh data
            recreate()
        }

        recyclerView = binding.balanceRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchDataFromDatabase()

    }

    private fun fetchDataFromDatabase(){
        val balanceRef = FirebaseDatabase.getInstance().reference.child("MessOwners").child(auth.currentUser!!.uid).child("balance")
        balanceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val stulist = mutableListOf<StudentItemModel>()
                var userCount: Long = 0 // Counter to track number of ValueEventListener callbacks

                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key

                    // Fetch user details (name and image URL) based on the user ID
                    val userRef = FirebaseDatabase.getInstance().reference.child("MessOwners").child(auth.currentUser!!.uid).child("users").child(userId!!)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            userCount++
                            if (userSnapshot.exists()) {
                                val userid = userSnapshot.child("userid").getValue(String::class.java)
                                val username = userSnapshot.child("name").getValue(String::class.java)
                                val userImage = userSnapshot.child("profileImage").getValue(String::class.java)

                                val stuitem = username?.let {
                                    if (userImage != null && userid != null) {
                                        StudentItemModel(userid, it, "", userImage)
                                    } else {
                                        null // Return null if either userImage or userid is null
                                    }
                                }

                                stuitem?.let {
                                    stulist.add(it) // Add to the list only if stuitem is not null
                                }

                                // If all callbacks have completed, update the adapter with the data
                                if (userCount == dataSnapshot.childrenCount) {
                                    val mAdapter = StudentAdapter(stulist, true)
                                    recyclerView.adapter = mAdapter
                                    mAdapter.update(stulist) // Call update() function here

                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            userCount++ // Increment counter even if onCancelled occurs
                            // Handle onCancelled event
                        }
                    })
                }
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }
}