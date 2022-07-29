package com.example.restaurant_review.socialmediapost

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.R
import com.example.restaurant_review.local_database.*
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.Views.CommentAdapter
import com.example.restaurant_review.Views.HorizontalImageAdapter
import com.example.restaurant_review.local_database.*
import java.util.*
import kotlin.collections.ArrayList


class FullPostActivity : AppCompatActivity() {
    private lateinit var titleView : TextView
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

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sm_activity_post_full)
        val bundle = intent.extras!!
        val post = Util.bundleToPost(bundle)
        titleView = findViewById(R.id.smp_title)
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

        titleView.text = "Location: "//post.title.ifEmpty { "<No Title>" }
        timeView.text = "Posted on " + Util.toDateString(post.timeStamp) + ", by " + Util.getNameFromUserId(
            post.userId
        )
        contentView.text = post.textContent
        bitmaps = arrayListOf<Bitmap>()
        for (path: String in post.imgList) {
            bitmaps.add(Util.filePathToBitmap(this, path))
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
                userId = Util.getUserId(),
                timeStamp = Calendar.getInstance().timeInMillis,
                textContent = commentEditText.text.toString()
            )
            viewModel.insertComment(comment)
            commentEditText.text.clear()
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

    private fun getCurrentItem(): Int {
        val res = (galleryView.layoutManager as LinearLayoutManager)
            .findFirstCompletelyVisibleItemPosition()
        if (res <= 0) {
            return res //previousGoodValue
        }
        else {
            //previousGoodValue = res
            return res
        }
    }
}