package com.example.restaurant_review.Views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.Model.RestaurantTour
import com.example.restaurant_review.R


class TourAdapter(private var tourList: MutableList<Pair<String, RestaurantTour>>,
    private val listener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun interface OnItemClickListener {
        fun onClick(position: Int)
    }

    class ViewHolder(view: View, private val context: Context) : RecyclerView.ViewHolder(view) {
        companion object {
            fun getInstance (parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.vr_tour_view, parent, false)
                return ViewHolder(view, parent.context)
            }
        }

        private var textView: TextView = view.findViewById(R.id.tv_info)
        private var button: Button = view.findViewById(R.id.vr_view_button)

        fun bind (restaurantTour: RestaurantTour, listener: OnItemClickListener, name: String){
            textView.text = "Submitted by $name"
            button.setOnClickListener {
                listener.onClick(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.getInstance(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(tourList[position].second, listener, tourList[position].first)
    }

    override fun getItemCount(): Int {
        return tourList.size
    }

    fun updateList(newList: List<Pair<String, RestaurantTour>>) {
        this.tourList = newList as MutableList<Pair<String, RestaurantTour>>
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        tourList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun get(position: Int): Pair<String, RestaurantTour>{
        return tourList[position]
    }

}