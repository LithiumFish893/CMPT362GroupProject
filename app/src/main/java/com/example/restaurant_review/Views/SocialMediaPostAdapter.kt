package com.example.restaurant_review.Views

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.local_database.*
import com.example.restaurant_review.socialmediapost.FullPostActivity

class SocialMediaPostAdapter : PagingDataAdapter<SocialMediaPostModel, RecyclerView.ViewHolder>(
    COMPARATOR
) {

    companion object {
        private val MAX_CONTENT_LENGTH = 100
        private val COMPARATOR = object: DiffUtil.ItemCallback<SocialMediaPostModel>(){
            override fun areItemsTheSame(
                oldItem: SocialMediaPostModel,
                newItem: SocialMediaPostModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SocialMediaPostModel,
                newItem: SocialMediaPostModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    fun interface OnItemClickListener {
        fun onClick(position: Int)
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    @OptIn(ExperimentalPagingApi::class)
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
            if (len > 0){
                val img = Util.filePathToBitmap(context, post.imgList[0])
                thumbnail.setImageBitmap(img)
            }
            thumbnail.setOnClickListener {
                val intent = Intent((context as AppCompatActivity), FullPostActivity::class.java)
                println(post)
                intent.putExtras(Util.postToBundle(post))
                context.startActivity(intent)
            }
            photo.setImageDrawable(Util.getProfilePhotoFromUserId(post.userId, context))
            user.text = Util.getNameFromUserId(post.userId)

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
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder.getInstance(viewGroup)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { (viewHolder as PostViewHolder).bind(it) }
    }
}