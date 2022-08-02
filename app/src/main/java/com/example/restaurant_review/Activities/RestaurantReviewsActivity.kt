package com.example.restaurant_review.Activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.Activities.LoginActivity
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

class RestaurantReviewsActivity: AppCompatActivity(), DialogInterface.OnClickListener {

    private lateinit var writeReview: Button
    private lateinit var reviewTitle: EditText
    private lateinit var reviewRating: RatingBar
    private lateinit var reviewComment: EditText
    private lateinit var writeReviewDialog: Dialog
    private lateinit var database: FirebaseDatabase
    private lateinit var loggedUser: FirebaseUser
    private lateinit var auth: FirebaseAuth

    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: ReviewHistoryAdapter
    private lateinit var list: ArrayList<Review>
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restarurant_reviews)
        writeReview = findViewById(R.id.write_reviews)
        val bundle = intent.extras
        id = bundle?.getString("ID").toString()
        recyclerView = findViewById(R.id.review_history)
        database = Firebase.database
        recyclerView.layoutManager = LinearLayoutManager(this)
        auth = Firebase.auth

        list = ArrayList()
        myAdapter = ReviewHistoryAdapter(this,list)
        recyclerView.adapter = myAdapter

        writeReview.setOnClickListener() {
            val builder = AlertDialog.Builder(this)
            var view = layoutInflater.inflate(R.layout.dialog_write_review, null)
            builder.setView(view)
            builder.setTitle("Distance")
            builder.setPositiveButton("ok", this)
            builder.setNegativeButton("cancel", this)
            reviewTitle = view.findViewById<EditText>(R.id.write_review_title)
            reviewRating = view.findViewById<RatingBar>(R.id.write_review_rating)
            reviewRating.numStars = 5
            reviewComment = view.findViewById<EditText>(R.id.write_review_comment)
            writeReviewDialog = builder.create()
            writeReviewDialog.show()
        }

        database.reference.child("reviews").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                list.clear()
                for (data in snapshot.children) {
                    val review:HashMap<String,Any> = data.value as HashMap<String, Any>
                    println("Debug: RestaurantID: ${review["Restaurant"]}, id: $id")
                    if (review["Restaurant"]==id) {
                        println("Debug: review" + review)
                        val newReview = Review()
                        newReview.title = review["Title"].toString()
                        val rate = review["Rating"]
                        if (rate != null){
                            newReview.rating = review["Rating"].toString().toFloat()
                        }
                        newReview.comment = review["Comment"].toString()
                        val author = review["Author"].toString()
                        newReview.author = author
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

    override fun onClick(dialog: DialogInterface?, which: Int) {
        if (dialog == writeReviewDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                val historyRef = database.reference.child("user").child(loggedUser.uid).child("history")
                val key = historyRef.push().key
                historyRef.child(key!!).setValue(key)
                val reviewRef = database.reference.child("reviews")
                reviewRef.child(key).child("Restaurant").setValue(id)
                reviewRef.child(key).child("Author").setValue(loggedUser.uid)
                reviewRef.child(key).child("Title").setValue(reviewTitle.text.toString())
                reviewRef.child(key).child("Rating").setValue(reviewRating.rating)
                reviewRef.child(key).child("Comment").setValue(reviewComment.text.toString())
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                println("debug: negative pressed")
            }
        }
    }



    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            loggedUser = currentUser
        }
    }
}