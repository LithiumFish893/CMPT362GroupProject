package com.example.restaurant_review.Model

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImagesViewModel : ViewModel(){
    val imgs = MutableLiveData<List<Bitmap>>()
    val imgUris = MutableLiveData<List<String>>()
}
