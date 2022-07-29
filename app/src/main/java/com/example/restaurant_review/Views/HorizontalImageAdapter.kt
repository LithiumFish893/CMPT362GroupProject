package com.example.restaurant_review.Views

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.R


class HorizontalImageAdapter(private var imageList: MutableList<Bitmap>, private val imageWidth: Int =50,
                             private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun interface OnItemClickListener {
        fun onClick(position: Int)
    }

    class ViewHolder(view: View, private val context: Context, private val width: Int) : RecyclerView.ViewHolder(view) {
        companion object {
            fun getInstance (parent: ViewGroup, width: Int): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.sm_image_view_social_media_post, parent, false)
                return ViewHolder(view, parent.context, width)
            }
        }

        private var imageView: ImageView = view.findViewById(R.id.smp_img_view)

        fun bind (bitmap: Bitmap, listener: OnItemClickListener){
            imageView.setImageBitmap(bitmap)
            // convert to dp
            if (width > 0) imageView.maxWidth =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    width.toFloat(), context.resources.displayMetrics).toInt()
            imageView.adjustViewBounds = true
            imageView.setOnClickListener {
                listener.onClick(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.getInstance(parent, imageWidth)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(imageList[position], listener)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun updateList(newList: List<Bitmap>) {
        this.imageList = newList as MutableList<Bitmap>
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        imageList.removeAt(position)
        notifyItemRemoved(position)
    }

}