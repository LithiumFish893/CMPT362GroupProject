package com.example.restaurant_review.Views

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.local_database.*
import com.example.restaurant_review.Activities.FullPostActivity
import com.example.restaurant_review.Util.Util.getUsernameFromUserId
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.FileNotFoundException

class SocialMediaPostAdapter(private var postList: MutableList<SocialMediaPostModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val MAX_CONTENT_LENGTH = 100

    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class PostViewHolder(view: View, val context: Context) : RecyclerView.ViewHolder(view) {
        companion object {
            // https://medium.com/swlh/paging3-recyclerview-pagination-made-easy-333c7dfa8797
            fun getInstance (parent: ViewGroup): PostViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.sm_view_social_media_post, parent, false)
                return PostViewHolder(view, parent.context)
            }
        }

        private var content : TextView = view.findViewById(R.id.smp_textContent)
        private var thumbnail : ImageView = view.findViewById(R.id.smp_thumbnail)
        private var thumbsUpButton : ImageButton = view.findViewById(R.id.thumbsUpButton)
        private var likeCount : TextView = view.findViewById(R.id.likeCount)
        private var photo : ImageView = view.findViewById(R.id.smp_user_photo)
        private var user : TextView = view.findViewById(R.id.smp_user)

        fun bind (post: SocialMediaPostModel){
            val len = post.imgList.size
            content.text = post.textContent.slice(
                IntRange(0, Math.min(post.textContent.length-1, MAX_CONTENT_LENGTH -3)))
            if (post.textContent.length > MAX_CONTENT_LENGTH -3) {
                content.text =  content.text.toString() + "..."
            }
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            if (len > 0){
                val path = post.imgList[0]
                // first see if image is available locally
                try {
                    val img = Util.filePathToBitmap(context, path)
                    thumbnail.setImageBitmap(img)
                }
                // if it's not then try the cloud
                catch (e: FileNotFoundException){
                    val fileName = Util.filePathToName(path)
                    // store the image in local storage for easy retrieval
                    storageRef.child(fileName).getFile(Util.filePathToUri(context, path)).addOnCompleteListener {
                        // try accessing local storage again
                        try {
                            val img = Util.filePathToBitmap(context, path)
                            thumbnail.setImageBitmap(img)
                        }
                        catch (e: FileNotFoundException) {
                        }
                    }
                }
            }
            thumbnail.setOnClickListener {
                val intent = Intent((context as AppCompatActivity), FullPostActivity::class.java)

                intent.putExtras(Util.postToBundle(post))
                context.startActivity(intent)
            }
            photo.setImageDrawable(Util.getProfilePhotoFromUserId(post.userId, context))

            val fireDatabase = Firebase.database
            fireDatabase.reference.child("user")
                .child(post.userId).child("username").get().addOnCompleteListener() {
                    if (it.isSuccessful) {
                        user.text = it.result.value.toString()
                    } else {
                        user.text = "Unknown User"
                    }
                }

            val orange = AppCompatResources.getDrawable(context, R.drawable.thumbs_up_orange)
            val greyUp = AppCompatResources.getDrawable(context, R.drawable.thumbs_up_gray)
            val database = SocialMediaPostDatabase.getInstance(context)
            val repository = SocialMediaPostRepository(database)
            val factory = SocialMediaPostViewModelFactory(repository)
            val viewModel = ViewModelProvider(context as AppCompatActivity, factory).get(SocialMediaPostViewModel::class.java)

            thumbsUpButton.setImageDrawable(greyUp)
            likeCount.text = "0"
            viewModel.allLikedPostsLiveData.observe(context) {
                if (it.contains(post.id)){
                    thumbsUpButton.setImageDrawable(orange)
                }
            }
            thumbsUpButton.setOnClickListener {
                if (thumbsUpButton.drawable == greyUp){
                    viewModel.insertLikedPost(LikedPostsModel(Util.getUserId(), post.id, LikedPostsModel.LIKE))
                    thumbsUpButton.setImageDrawable(orange)
                }
                // un like the image
                else {
                    viewModel.deleteLikedPost(Util.getUserId(), post.id)
                    thumbsUpButton.setImageDrawable(greyUp)
                }
            }
            viewModel.getLikeCountLiveData(post.id).observe(context){
                likeCount.text = it?.toString() ?: "0"
            }
        }
    }

    fun updateList(newList: List<SocialMediaPostModel>) {
        this.postList = newList as MutableList<SocialMediaPostModel>
        notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder.getInstance(viewGroup)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PostViewHolder).bind(postList[position])
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}