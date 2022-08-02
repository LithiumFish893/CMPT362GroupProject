package com.example.restaurant_review.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.restaurant_review.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity:AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailTV: EditText
    private lateinit var passwordTV: EditText
    private lateinit var confirmPasswordTV: EditText
    private lateinit var registerButton: Button
    private lateinit var signInButton: TextView
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstance: Bundle?){
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_register)

        emailTV = findViewById(R.id.reg_email_input)
        passwordTV = findViewById(R.id.reg_password_input)
        confirmPasswordTV = findViewById(R.id.confirm_reg_password_input)
        registerButton = findViewById(R.id.register_button)
        signInButton = findViewById(R.id.have_Account)

        auth =  FirebaseAuth.getInstance()
        database = Firebase.database

        registerButton.setOnClickListener(){
            createUser()
        }
        signInButton.setOnClickListener(){
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    private fun createUser(){
        var email = emailTV.text.toString()
        var password = passwordTV.text.toString()
        var confirmPassword = confirmPasswordTV.text.toString()

        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(email.isEmpty()){
            emailTV.error = "Email cannot be empty!"
            emailTV.requestFocus()
            imm.hideSoftInputFromWindow(emailTV.windowToken, 0)
        }else if(password.isEmpty()){
            passwordTV.error = "Password cannot be empty!"
            passwordTV.requestFocus()
            imm.hideSoftInputFromWindow(passwordTV.windowToken, 0)

        }else if(confirmPassword.isEmpty()){
            confirmPasswordTV.error = "Password cannot be empty!"
            confirmPasswordTV.requestFocus()
            imm.hideSoftInputFromWindow(confirmPasswordTV.windowToken, 0)

        }else if(confirmPassword!= password){
            confirmPasswordTV.error = "Confirm password is different!"
            confirmPasswordTV.requestFocus()
            imm.hideSoftInputFromWindow(confirmPasswordTV.windowToken, 0)
        }else{
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
                if(it.isSuccessful){
                    println("debug: User created: ${it.result.user.toString()}")
                    database.reference.child("user").child(it.result.user?.uid!!).child("username").setValue(
                        it.result.user!!.email)
                    Toast.makeText(this,"Successes!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                }else{
                    Toast.makeText(this,"Error!" + it.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}