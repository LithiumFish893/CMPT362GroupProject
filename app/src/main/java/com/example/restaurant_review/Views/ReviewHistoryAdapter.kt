package com.example.restaurant_review.Views

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.Data.Review
import com.example.restaurant_review.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class ReviewHistoryAdapter(private val context: Context, private var reviewList: List<Review>):
    RecyclerView.Adapter<ReviewHistoryAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_review, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val auth = Firebase.auth
        val database = Firebase.database
        val review = reviewList[position]
        holder.titleView.text = review.title
        holder.commentView.text = review.comment
        holder.ratingView.rating = review.rating
        holder.deleteText.visibility = View.INVISIBLE
        if (review.author == auth.currentUser!!.uid){
            holder.deleteText.visibility = View.VISIBLE
            holder.deleteText.setOnClickListener(){
                database.reference.child("reviews").child(review.id).removeValue()
                database.reference.child("user").child(auth.currentUser!!.uid).child("history")
                    .child(review.id).removeValue()
            }
        }
        database.reference.child("user")
            .child(review.author).child("username").get().addOnCompleteListener() {
                if (it.isSuccessful) {
                    holder.author.text = "By: " + it.result.value.toString()
                }else {
                }
            }
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val deleteText: TextView = itemView.findViewById(R.id.delete_review)
        val titleView: TextView = itemView.findViewById(R.id.review_card_title)
        val commentView: TextView = itemView.findViewById(R.id.review_card_comment)
        val ratingView: RatingBar = itemView.findViewById(R.id.review_card_rating)
        val author: TextView = itemView.findViewById(R.id.review_card_author)
    }

    /*
    override fun getCount(): Int {
        return reviewList.size
    }

    override fun getItem(position: Int): Any {
        return reviewList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context,R.layout.cardview_review,null)
        val titleView = view.findViewById<EditText>(R.id.review_card_title)
        val commentView = view.findViewById<EditText>(R.id.review_card_comment)
        titleView.text = f
        return view
    }
     */

}