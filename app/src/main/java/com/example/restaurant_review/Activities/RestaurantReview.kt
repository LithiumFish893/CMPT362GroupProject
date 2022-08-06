package com.example.restaurant_review.Activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.restaurant_review.Data.Review
import com.example.restaurant_review.Model.Restaurant
import com.example.restaurant_review.Model.RestaurantManager
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

class RestaurantReview : AppCompatActivity(), DialogInterface.OnClickListener {
    private var position = 0;
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
    //private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_review)

        ID = intent.getStringExtra(java.lang.String.valueOf(R.string.intent_extra_id))
        position = intent.getIntExtra("position", -1)

        println("debug: position $position, ID $ID")

        val restaurantName = findViewById<TextView>(R.id.restaurant_title)
        val imageSlider = findViewById<ImageSlider>(R.id.restaurant_image)
        val imageList = ArrayList<SlideModel>()
        val mRestaurant: Restaurant? = RestaurantManager.instance?.getRestaurant(
            ID
        )
        if (mRestaurant != null) {
            restaurantName.text = mRestaurant.name
        } else{
            println("debug: mRest is null")
        }
        val address = findViewById<TextView>(R.id.restaurant_review_address)
        val writeReview = findViewById<View>(R.id.write_review)
        address.text = getString(R.string.restaurant_address, mRestaurant?.address, mRestaurant?.city)


        imageList.add(SlideModel("https://media-cdn.tripadvisor.com/media/photo-s/0e/f0/e6/28/breathtaking-views-of.jpg"))
        imageList.add(SlideModel("https://www.tourismnorthbay.com/wp-content/uploads/2020/06/Dairy-Queen-DQ-North-Bay-Blizzards.jpg"))

        imageSlider.setImageList(imageList, ScaleTypes.FIT)

        val healthButton = findViewById<TextView>(R.id.health_level)
        healthButton.setOnClickListener(){
            val intent: Intent? =
                RestaurantDetailActivity.makeLaunchIntent(applicationContext,
                    ID, position)
            startActivity(intent)
        }

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
            builder.setTitle("Review")
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
                    println("Debug: RestaurantID: ${review["Restaurant"]}, id: $ID")
                    if (review["Restaurant"]==ID) {
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
                reviewRef.child(key).child("Restaurant").setValue(ID)
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

    companion object {
        var ID: String? = null
        fun makeLaunchIntent(context: Context?, ID: String?, position: Int): Intent {
            val intent = Intent(context, RestaurantReview::class.java)
            intent.putExtra(java.lang.String.valueOf(R.string.intent_extra_id), ID)
            intent.putExtra("position", position)
            return intent
        }
    }
}
