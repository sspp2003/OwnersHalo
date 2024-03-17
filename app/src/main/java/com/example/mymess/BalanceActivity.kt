package com.example.mymess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymess.Adapters.StudentAdapter
import com.example.mymess.Models.StudentItemModel
import com.example.mymess.databinding.ActivityBalanceBinding
import com.google.firebase.database.*

class BalanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBalanceBinding
    private lateinit var mAdapter: StudentAdapter
    private val stulist = mutableListOf<StudentItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBalanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.balanceRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)

        mAdapter = StudentAdapter(stulist, true)
        recyclerView.adapter = mAdapter

        val balanceRef = FirebaseDatabase.getInstance().reference.child("balance")
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")

        // Listen for changes in the balance node
        balanceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                stulist.clear() // Clear the list before adding updated data

                // Iterate through each user in the balance node
                dataSnapshot.children.forEach { userSnapshot ->
                    val userId = userSnapshot.key

                    // Fetch user details from the users node using the userId
                    userId?.let { fetchUserDetails(it, usersRef) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }

    private fun fetchUserDetails(userId: String, usersRef: DatabaseReference) {
        // Fetch user details (name and image URL) based on the user ID
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
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

                    // Notify the adapter of changes after fetching user details
                    mAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }
}
