package com.example.mymess

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mymess.MainActivity
import com.example.mymess.Models.UserData
import com.example.mymess.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val PICK_IMAGE_REQEST=1
    private var imageUri: Uri?=null

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()

        val action=intent.getStringExtra("action")

        if(action=="login"){
            binding.loginEmail.visibility= View.VISIBLE
            binding.loginPassword.visibility= View.VISIBLE
            binding.tvNewhere.visibility= View.INVISIBLE
            binding.buttonRegister.visibility= View.INVISIBLE
            binding.registerEmail.visibility= View.GONE
            binding.registerName.visibility= View.GONE
            binding.registerPassword.visibility= View.GONE
            binding.cardView.visibility= View.GONE

            //Handling login

            binding.buttonLogin.setOnClickListener {
                val email=binding.loginEmail.text.toString()
                val password=binding.loginPassword.text.toString()

                if(email.isBlank() || password.isBlank()){
                    Toast.makeText(this,"Please fill all the Credentials", Toast.LENGTH_SHORT).show()
                }
                else{
                    auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this){
                        if(it.isSuccessful){
                            Toast.makeText(this,"Logged In😁", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        else{
                            Toast.makeText(this,"Not proper credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }

        else if(action=="register"){
            binding.buttonLogin.isEnabled=false
            binding.buttonLogin.alpha=0.5f

            binding.buttonRegister.setOnClickListener {
                val email=binding.registerEmail.text.toString()
                val password=binding.registerPassword.text.toString()
                val name=binding.registerName.text.toString()

                if(email.isBlank() || password.isBlank() || name.isBlank()){
                    Toast.makeText(this,"Please fill all the Credentials", Toast.LENGTH_SHORT).show()
                }
                else{
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                        if(it.isSuccessful){
                            //auth.signOut()
                            val user=auth.currentUser
                            //Adding the user Database
                            user?.let {
                                addUserData(name,email,auth.currentUser!!.uid)
                                Toast.makeText(this,"Logged In",Toast.LENGTH_SHORT).show()
                                val intent=Intent(this,MainActivity::class.java)
                                intent.putExtra("messname",name)
                                intent.putExtra("messid",auth.currentUser!!.uid)
                                startActivity(intent)
                                finish()
                            }
                        }
                        else{
                            Toast.makeText(this,"Error Connecting User",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun addUserData(name: String, email: String, uid: String) {
        database= FirebaseDatabase.getInstance().getReference()
        database.child("MessOwners").child(uid).child("MessName").setValue(name)
    }
}