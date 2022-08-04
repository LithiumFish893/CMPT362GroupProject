package com.example.restaurant_review.Activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.restaurant_review.R

class RestaurantReview : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_review)

        val imageSlider = findViewById<ImageSlider>(R.id.restaurant_image)
        val imageList = ArrayList<SlideModel>()

        //imageList.add(SlideModel("https://media-cdn.tripadvisor.com/media/photo-s/0e/f0/e6/28/breathtaking-views-of.jpg"))
        imageList.add(SlideModel("https://www.tourismnorthbay.com/wp-content/uploads/2020/06/Dairy-Queen-DQ-North-Bay-Blizzards.jpg"))

        imageSlider.setImageList(imageList, ScaleTypes.FIT)

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