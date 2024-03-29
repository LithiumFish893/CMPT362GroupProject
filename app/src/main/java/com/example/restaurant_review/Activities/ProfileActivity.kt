package com.example.restaurant_review.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.Data.Review
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.ReviewHistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Activity that lets the user see profile information
 * (change username, see review history)
 */
class ProfileActivity: AppCompatActivity() {
    private enum class State {
        Change, Save
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var userName: EditText
    private lateinit var commentHistory: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var loggedUser: FirebaseUser
    private lateinit var state: State


    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: ReviewHistoryAdapter
    private lateinit var list: ArrayList<Review>
    private lateinit var changeUsername: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        title = "Profile"
        auth = Firebase.auth
        userName = findViewById(R.id.username)
        userName.inputType = InputType.TYPE_NULL
        changeUsername = findViewById(R.id.usernameChange)
        recyclerView = findViewById(R.id.review_history)
        database = Firebase.database
        recyclerView.layoutManager = LinearLayoutManager(this)

        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        list = ArrayList()
        myAdapter = ReviewHistoryAdapter(this,list)
        recyclerView.adapter = myAdapter

        state = State.Change

        changeUsername.setOnClickListener() {
            if(state == State.Change) {
                userName.inputType = InputType.TYPE_CLASS_TEXT
                userName.requestFocus()
                userName.setSelection(userName.length())
                changeUsername.text = "Save Username"
                imm.showSoftInput(userName, 0)
                state = State.Save
            } else {
                database.reference.child("user").child(loggedUser.uid).child("username")
                    .setValue(userName.text.toString())
                userName.inputType = InputType.TYPE_NULL
                userName.clearFocus()
                changeUsername.text = "Change Username"
                imm.hideSoftInputFromWindow(userName.windowToken, 0)
                state = State.Change
            }
        }
        
        userName.setOnKeyListener(){ view, keycode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keycode == KeyEvent.KEYCODE_ENTER)) {
                database.reference.child("user").child(loggedUser.uid).child("username")
                    .setValue(userName.text.toString())
                userName.inputType = InputType.TYPE_NULL
                userName.clearFocus()
                changeUsername.text = "Change Username"
                imm.hideSoftInputFromWindow(userName.windowToken, 0)
                state = State.Change
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        
        database.reference.child("reviews").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                list.clear()
                for (data in snapshot.children) {
                    val review:HashMap<String,Any> = data.value as HashMap<String, Any>
                    if (review["Author"]==loggedUser.uid) {
                        val newReview = Review()
                        newReview.id = review["ID"].toString()
                        newReview.author = review["Author"].toString()
                        newReview.title = review["Title"].toString()
                        val rate = review["Rating"]
                        if (rate != null){
                            newReview.rating = review["Rating"].toString().toFloat()
                        }
                        newReview.comment = review["Comment"].toString()
                        list.add(newReview)
                    }
                }
                myAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                }
            }
        )


    }

    public override fun onStart(){
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
        }else{
            loggedUser = currentUser
            database.reference.child("user")
                .child(loggedUser.uid).child("username").get().addOnCompleteListener() {
                    if (it.isSuccessful) {
                        userName.setText("${it.result.value}")
                    }else {
                        Toast.makeText(this, "Error!" + it.exception!!.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

}
