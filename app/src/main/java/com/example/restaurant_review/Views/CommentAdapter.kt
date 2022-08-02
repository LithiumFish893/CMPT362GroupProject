package com.example.restaurant_review.Views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.local_database.CommentModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class CommentAdapter(private var commentList: MutableList<CommentModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(view: View, private val context: Context) : RecyclerView.ViewHolder(view) {
        companion object {
            fun getInstance (parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.sm_view_comment, parent, false)
                return ViewHolder(view, parent.context)
            }
        }

        private var imageView: ImageView = view.findViewById(R.id.comment_profile_photo)
        private var username: TextView = view.findViewById(R.id.comment_username)
        private var date: TextView = view.findViewById(R.id.comment_date)
        private var textContent: TextView = view.findViewById(R.id.comment_text_content)

        val database = Firebase.database

        fun bind (comment: CommentModel){
            imageView.setImageDrawable(Util.getProfilePhotoFromUserId(comment.userId, context))
            database.reference.child("user")
                .child(comment.userId).child("username").get().addOnCompleteListener() {
                    if (it.isSuccessful) {
                        username.text = it.result.value.toString()
                        println("Debug: Success comment username, ${it.result.value.toString()}")
                    } else {
                        println("Debug: Failed comment username")
                    }
                }
            //TODO needs new logic.
            date.text = Util.toDateString(comment.timeStamp)
            textContent.text = comment.textContent
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.getInstance(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(commentList[position])
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun updateList(newList: List<CommentModel>) {
        println(newList)
        this.commentList = newList as MutableList<CommentModel>
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        commentList.removeAt(position)
        notifyItemRemoved(position)
    }

}