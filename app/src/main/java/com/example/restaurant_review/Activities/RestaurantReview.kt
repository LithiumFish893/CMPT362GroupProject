package com.example.restaurant_review.Activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.restaurant_review.Data.Review
import com.example.restaurant_review.Fragments.HomeFragment
import com.example.restaurant_review.Model.Restaurant
import com.example.restaurant_review.Model.RestaurantManager
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.ReviewHistoryAdapter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt

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
    private var totalRating = 0.0
    private var countRating = 0
    private lateinit var averageRating: RatingBar

    private lateinit var mPrefs: SharedPreferences


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_review)


        ID = intent.getStringExtra(java.lang.String.valueOf(R.string.intent_extra_id))
        position = intent.getIntExtra("position", -1)

        println("debug: position $position, ID $ID")

        val restaurantName = findViewById<TextView>(R.id.restaurant_title)
        val image = findViewById<ImageView>(R.id.restaurant_image)
        val mRestaurant: Restaurant? = RestaurantManager.instance?.getRestaurant(
            ID
        )
        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        val distance = FloatArray(3)
        val distanceText = findViewById<TextView>(R.id.distance)

        if (mRestaurant != null) {
            restaurantName.text = mRestaurant.name
            locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener {
                    Location.distanceBetween(it.latitude,it.longitude,
                        mRestaurant.latitude, mRestaurant.longitude, distance)
                    distanceText.text = ((distance[0]/10).roundToInt()/100.0).toString() + "KM"
                }
            Glide.with(this).load(mRestaurant.imgUrl).placeholder(R.drawable.ic_restaurant).into(image)
        } else {
            println("debug: mRest is null")
        }
        val address = findViewById<TextView>(R.id.restaurant_review_address)
        val writeReview = findViewById<View>(R.id.write_review)
        averageRating = findViewById(R.id.restaurant_rating)
        address.text = getString(R.string.restaurant_address, mRestaurant?.address, mRestaurant?.city)

        

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
                totalRating = 0.0
                countRating = 0
                list.clear()
                var restaurantID = ""
                for (data in snapshot.children) {
                    val review:HashMap<String,Any> = data.value as HashMap<String, Any>
                    println("Debug: RestaurantID: ${review["Restaurant"]}, id: $ID")
                    if (review["Restaurant"]==ID) {
                        println("Debug: review" + review)
                        val newReview = Review()
                        newReview.id = review["ID"].toString()
                        newReview.title = review["Title"].toString()
                        val rate = review["Rating"]
                        if (rate != null){
                            newReview.rating = review["Rating"].toString().toFloat()
                            countRating ++
                            totalRating += rate.toString().toDouble()
                        }
                        newReview.comment = review["Comment"].toString()
                        val author = review["Author"].toString()
                        newReview.author = author
                        newReview.restaurantTrackingNumber = review["Restaurant"].toString()
                        restaurantID = newReview.restaurantTrackingNumber
                        list.add(newReview)
                    }
                }
                if (countRating!=0){
                    averageRating.rating = (totalRating/countRating).toFloat()
                }
                else
                    averageRating.rating = 0.0F
                database.reference.child("restaurants").child(restaurantID).child("avgRating").setValue(averageRating.rating)
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
                reviewRef.child(key).child("ID").setValue(key)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_restaurant_detail, menu)
        // set favourites icon
        mPrefs = getSharedPreferences(RestaurantDetailActivity.PREFS_NAME, MODE_PRIVATE)
        val faveRestaurants: String? = mPrefs.getString("fave_restaurants", "")
        if (faveRestaurants != null) {
            if (faveRestaurants.contains(ID!!)) {
                menu.getItem(0).setIcon(R.drawable.ic_menu_mark_favorite)
            } else {
                menu.getItem(0).setIcon(R.drawable.ic_menu_unmark_favorite)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            android.R.id.home-> {
                finish()
                true
            }
            R.id.favourite -> {
                if (item.icon.constantState == getDrawable(R.drawable.ic_menu_unmark_favorite)!!.constantState) {
                    addFave(RestaurantDetailActivity.ID)
                    item.setIcon(R.drawable.ic_menu_mark_favorite)
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
                    setViewListIcon(R.drawable.ic_menu_mark_favorite)
                } else {
                    removeFave(RestaurantDetailActivity.ID)
                    item.setIcon(R.drawable.ic_menu_unmark_favorite)
                    Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    setViewListIcon(R.drawable.ic_menu_unmark_favorite)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setViewListIcon(res: Int) {
        val pos = intent.getIntExtra("position", -1)
        if (pos != -1) {
            val view = HomeFragment.restaurantListView?.let { getViewByPosition(pos, it) }
            val icon = view?.findViewById<ImageView>(R.id.list_restaurant_fave)
            icon?.setImageResource(res)
        }
    }

    fun getViewByPosition(pos: Int, listView: ListView): View {
        val firstListItemPosition = listView.firstVisiblePosition
        +listView.childCount - 1
        return if (pos < firstListItemPosition || pos > firstListItemPosition) {
            listView.adapter.getView(pos, null, listView)
        } else {
            val childIndex = pos - firstListItemPosition
            listView.getChildAt(childIndex)
        }
    }

    private fun removeFave(id: String?) {
        var faveRestaurants: String ?= mPrefs.getString("fave_restaurants", "")
        // Log.d("TAG","before remove: " + faveRestaurants);
        if (faveRestaurants != null) {
            if (faveRestaurants.contains("$id,")) {
                faveRestaurants = faveRestaurants.replace("$id,", "")
            } else if (faveRestaurants.contains(",$id")) {
                faveRestaurants = faveRestaurants.replace(",$id", "")
            } else if (faveRestaurants.contains(id!!)) {
                faveRestaurants = faveRestaurants.replace(id, "")
            }
        }
        mPrefs.edit().putString("fave_restaurants", faveRestaurants).apply()
        //Log.d("TAG","after remove: " + faveRestaurants);
    }

    private fun addFave(id: String?) {
        val faveRestaurants: String ?= mPrefs.getString("fave_restaurants", "")
        if (faveRestaurants != null) {
            if (faveRestaurants.length == 0) {
                mPrefs.edit().putString("fave_restaurants", id).apply()
            } else {
                mPrefs.edit().putString("fave_restaurants", "$faveRestaurants,$id").apply()
            }
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
