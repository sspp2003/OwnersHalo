package com.example.mymess

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
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
    private var dbRef=FirebaseDatabase.getInstance().getReference()
    private lateinit var mAdapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.menuBar.setOnClickListener {view->
            val popmenu=PopupMenu(this@MainActivity,view)
            popmenu.inflate(R.menu.menu_item)

            popmenu.setOnMenuItemClickListener {menuItem->
                when(menuItem.itemId){
                    R.id.set_amount->{
                        showDialogBox1()
                        true
                    }

                    R.id.check_balance->{
                        startActivity(Intent(this@MainActivity,BalanceActivity::class.java))
                        true
                    }

                    R.id.upi->{
                        showDialogBox2()
                        true
                    }

                    else->{
                        false
                    }
                }
            }

            popmenu.show()
        }

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
                val mAdapter = StudentAdapter(stulist,false)
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

    private fun showDialogBox2() {
        val dialog= Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.upi_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel_btn=dialog.findViewById<Button>(R.id.btn_cancel)
        val confirm_btn=dialog.findViewById<Button>(R.id.btn_confirm)
        val name=dialog.findViewById<EditText>(R.id.et_name)
        val id=dialog.findViewById<EditText>(R.id.et_upiId)

        cancel_btn.setOnClickListener {
            dialog.dismiss()
        }

        confirm_btn.setOnClickListener {
            if(name.text.isEmpty()){
                Toast.makeText(this,"Please Enter Name",Toast.LENGTH_SHORT).show()
            }

            if (id.text.isEmpty()){
                Toast.makeText(this,"Please Enter UPI Id",Toast.LENGTH_SHORT).show()
            }

            val nameInput=name.editableText.toString()
            val idInput=id.editableText.toString()
            dbRef.child("UPI").child("name").setValue(nameInput)
            dbRef.child("UPI").child("UPI_id").setValue(idInput)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDialogBox1() {
        val dialog= Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.setamount_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel_btn=dialog.findViewById<Button>(R.id.btn_cancel)
        val confirm_btn=dialog.findViewById<Button>(R.id.btn_confirm)
        val amount=dialog.findViewById<EditText>(R.id.et_amount)

        cancel_btn.setOnClickListener {
            dialog.dismiss()
        }

        confirm_btn.setOnClickListener {
            if(amount.text.isEmpty()){
                Toast.makeText(this,"Please Enter Amount",Toast.LENGTH_SHORT).show()
            }
            else{
                val amountinput=amount.editableText.toString()
                dbRef.child("Rate").setValue(amountinput)
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}