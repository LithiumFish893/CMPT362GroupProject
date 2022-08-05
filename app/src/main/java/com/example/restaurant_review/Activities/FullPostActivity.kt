package com.example.restaurant_review.Activities

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.Views.CommentAdapter
import com.example.restaurant_review.Views.HorizontalImageAdapter
import com.example.restaurant_review.local_database.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.FileNotFoundException
import java.util.*


class FullPostActivity : AppCompatActivity() {
    private var subscribed: Boolean = false
    private lateinit var locationImageView: ImageView
    private lateinit var locationTextView : TextView
    private lateinit var subscriptionButton: Button
    private lateinit var timeView : TextView
    private lateinit var contentView : TextView
    private lateinit var imagesView : ImageView //RecyclerView
    private lateinit var galleryView : RecyclerView
    private lateinit var fadeBackground : View
    private lateinit var imagesCountView : TextView
    private lateinit var closeButton: ImageButton
    private lateinit var bitmaps : ArrayList<Bitmap>
    private lateinit var replyTextView : TextView
    private lateinit var commentEditText: EditText
    private lateinit var commentPostButton: Button
    private lateinit var array : ArrayList<CommentModel>
    private lateinit var comments: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sm_activity_post_full)
        val bundle = intent.extras!!
        val post = Util.bundleToPost(bundle)
        locationImageView = findViewById(R.id.smp_location_image)
        locationTextView = findViewById(R.id.smp_title)
        subscriptionButton = findViewById(R.id.smp_subscribe_button)
        timeView = findViewById(R.id.smp_time)
        contentView = findViewById(R.id.smp_textContent)
        imagesView = findViewById(R.id.smp_imgs)
        galleryView = findViewById(R.id.gallery_images)
        imagesCountView = findViewById(R.id.imagesCount)
        fadeBackground = findViewById(R.id.fadeBackground)
        closeButton = findViewById(R.id.button_delete)
        replyTextView = findViewById(R.id.replyTextView)
        commentEditText = findViewById(R.id.commentEditText)
        commentPostButton = findViewById(R.id.postCommentButton)
        comments = findViewById(R.id.comment_list)

        val firebaseAuth = Firebase.auth
        val firebaseDatabase = Firebase.database
        val currentUser = firebaseAuth.currentUser!!.uid
        val userRef = firebaseDatabase.reference.child("user").child(currentUser).child("username")
        // get all the subscriptions from the currently logged in user
        val subscriptionRef = firebaseDatabase.reference.child("subscriptions").child(currentUser)

        if (post.locationName.isEmpty()){
            locationImageView.visibility = GONE
            locationTextView.visibility = GONE
        }
        locationTextView.text = post.locationName

        userRef.get().addOnCompleteListener() {
                if (it.isSuccessful) {
                    timeView.text = "Posted on " + Util.toDateString(post.timeStamp) + ", by " + it.result.value.toString()
                    println("Debug: Success comment username, ${it.result.value.toString()}")
                } else {
                    println("Debug: Failed comment username")
                }
            }

        subscriptionRef.get().addOnSuccessListener {
            updateSubscriptions(it, post)
        }

        if (post.userId != currentUser) {
            subscriptionRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    updateSubscriptions(snapshot, post)
                }

                override fun onCancelled(error: DatabaseError) { }

            })

            // subscribe/unsubscribe to post author
            subscriptionButton.setOnClickListener {
                if (!subscribed) {
                    subscriptionRef.child(post.userId).setValue(Calendar.getInstance().timeInMillis)
                    subscribed = true
                } else {
                    subscriptionRef.child(post.userId).removeValue()
                    subscribed = false
                }
            }

        }
        else {
            subscriptionButton.visibility = GONE
        }
        contentView.text = post.textContent
        bitmaps = arrayListOf<Bitmap>()
        for (path: String in post.imgList) {
            // TODO: Change when we get cloud storage
            try {
                bitmaps.add(Util.filePathToBitmap(this, path))
            }
            catch (e: FileNotFoundException){}
        }
        if (bitmaps.isNotEmpty()) imagesView.setImageBitmap(bitmaps[0])
        imagesView.setOnClickListener {
            viewImageGallery(0)
        }
        val adapter = HorizontalImageAdapter(bitmaps, 0){}
        closeButton.setOnClickListener {
            unFadeBackground()
        }

        closeButton.visibility = GONE
        imagesCountView.visibility = GONE
        galleryView.visibility = GONE
        galleryView.adapter = adapter
        galleryView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        array = arrayListOf()
        val commentAdapter = CommentAdapter(array)
        comments.adapter = commentAdapter
        comments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val database = SocialMediaPostDatabase.getInstance(this)
        val repository = SocialMediaPostRepository(database)
        val factory = SocialMediaPostViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory).get(SocialMediaPostViewModel::class.java)


        viewModel.getAllCommentsWithId(post.id).observe(this) {
            commentAdapter.updateList(it)
        }
        commentPostButton.setOnClickListener {
            if (commentEditText.text.toString().isEmpty()){
                Toast.makeText(this, "Comment must not be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val comment = CommentModel(
                parentPostId = post.id,
                parentPostUserId = post.userId,
                userId = currentUser,
                timeStamp = Calendar.getInstance().timeInMillis,
                textContent = commentEditText.text.toString()
            )
            viewModel.insertComment(comment)
            commentEditText.text.clear()
        }
    }

    fun updateSubscriptions(snapshot: DataSnapshot, post: SocialMediaPostModel){
        snapshot.children.forEach {
            println("listening in FullPostActivity")
            // see if current user subscribed to whoever made the post
            println("${it.key}, ${post.userId}")
            if (it.key as String == post.userId) {
                // make button green
                subscribed = true
                subscriptionButton.backgroundTintList = (
                        resources.getColorStateList(
                            R.color.bootstrap_green,
                            null
                        )
                        )
                subscriptionButton.text = "Subscribed"
                subscriptionButton.setTextColor(resources.getColor(R.color.white, null))
                return@forEach
            } else {
                subscribed = false
                subscriptionButton.backgroundTintList = (
                        resources.getColorStateList(
                            R.color.yellow_500,
                            null
                        )
                        )
                subscriptionButton.text = "Subscribe"
                subscriptionButton.setTextColor(resources.getColor(R.color.black, null))
            }
        }
    }

    fun viewImageGallery(position: Int){
        fadeBackground.visibility = VISIBLE
        fadeBackground.animate().alpha(0.5f).duration = 160 // The higher the alpha value the more it will be grayed out
        closeButton.visibility = VISIBLE
        galleryView.visibility = VISIBLE
        imagesCountView.visibility = VISIBLE
        galleryView.scrollToPosition(position)
        imagesCountView.text = "Images (${bitmaps.size})"
    }

    fun unFadeBackground(){
        fadeBackground.animate().alpha(0.0f)
        closeButton.visibility = GONE
        galleryView.visibility = GONE
        imagesCountView.visibility = GONE
    }

}