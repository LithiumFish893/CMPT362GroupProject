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
        val review = reviewList[position]
        holder.titleView.text = review.title
        holder.commentView.text = review.comment
        holder.ratingView.rating = review.rating
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val titleView: TextView = itemView.findViewById(R.id.review_card_title)
        val commentView: TextView = itemView.findViewById(R.id.review_card_comment)
        val ratingView: RatingBar = itemView.findViewById(R.id.review_card_rating)
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