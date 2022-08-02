package com.example.restaurant_review.Util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.restaurant_review.Activities.SocialMediaPostActivity
import com.example.restaurant_review.local_database.SocialMediaPostModel
import com.example.restaurant_review.Activities.SocialMediaPostActivity.Companion.ID_KEY
import com.example.restaurant_review.Activities.SocialMediaPostActivity.Companion.LAT_KEY
import com.example.restaurant_review.Activities.SocialMediaPostActivity.Companion.LONG_KEY
import com.example.restaurant_review.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Month
import java.util.*

object Util {
    private const val kmToMiles = 1.609344f

    /**
     * Check permissions in the given Activity
     * @param activity The given Activity
     * @return Boolean - if permissions are granted.
     */
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (!checkCameraPermissions(activity)){
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 0)
        }
        if (!checkReadExternalStoragePermissions(activity)){
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),0)
        }
    }

    fun checkReadExternalStoragePermissions (activity: Activity?) : Boolean{
        val perm : Boolean = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!perm) ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),0)
        return perm
    }

    fun checkCameraPermissions (activity: Activity?) : Boolean{
        val perm: Boolean =  ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED //&&
                //ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        if (!perm) ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 0)
        return perm
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        val c = context.contentResolver.openInputStream(imgUri)
        val bitmap = BitmapFactory.decodeStream(c)
        val matrix = Matrix()
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun toInt(s: String): Int {
        return try {
            s.toInt()
        } catch (e: NumberFormatException){
            0
        }
    }
    fun toFloat(s: String): Float {
        return try {
            s.toFloat()
        } catch (e: NumberFormatException){
            0f
        }
    }
    fun toDouble(s: String): Double {
        return try {
            s.toDouble()
        } catch (e: NumberFormatException){
            0.0
        }
    }

    fun toDateString(t: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = t
        return Month.of(calendar.get(Calendar.MONTH)+1).toString().lowercase(Locale.ROOT).capitalize(Locale.ROOT) + " " +
                calendar.get(Calendar.DAY_OF_MONTH) + ", " +
                calendar.get(Calendar.YEAR) + " · " +
                ((calendar.get(Calendar.HOUR_OF_DAY)-1)%12+1).toString() + ":" +
                "%02d".format(calendar.get(Calendar.MINUTE)) + " " +
                (if (calendar.get(Calendar.HOUR_OF_DAY) > 12) "pm" else "am")
    }

    fun toMiles(km: Double): Double { return km/ kmToMiles }
    fun toKm(miles: Double): Double { return miles* kmToMiles }

    fun bitmapToByteArray (bmp: Bitmap) : ByteArray {
        val res: ByteArray
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        bmp.recycle()
        res = stream.toByteArray()
        stream.close()
        return res
    }

    fun byteArrayToBitmap (byteArray: ByteArray) : Bitmap {
         return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun filePathToBitmap (context: Context, path: String) : Bitmap {
        val f = filePathToName(path)
        return getBitmap(context, Uri.fromFile(File(context.getExternalFilesDir(DIRECTORY_PICTURES), f)))
    }

    fun filePathToName (path: String) : String {
        val l = path.split("/")
        return l[l.lastIndex]
    }

    fun bundleToPost (bundle: Bundle) : SocialMediaPostModel {
        val id = bundle.getInt(ID_KEY)
        val lat = bundle.getDouble(LAT_KEY)
        val long = bundle.getDouble(LONG_KEY)
        val titleString = bundle.getString(SocialMediaPostActivity.TITLE_KEY)
        val userId = bundle.getString(SocialMediaPostActivity.USERID_KEY)!!
        val textContent = bundle.getString(SocialMediaPostActivity.TEXT_CONTENT_KEY)
        val timeStamp = bundle.getLong(SocialMediaPostActivity.TIMESTAMP_KEY, 0L)
        val imageUris = bundle.getStringArrayList(SocialMediaPostActivity.IMAGE_URIS_KEY)
        return SocialMediaPostModel(
            id = id, locationLat = lat, locationLong = long, timeStamp = timeStamp, userId = userId, title = titleString!!,
            textContent = textContent!!, imgList = imageUris!!)
    }

    fun postToBundle (post: SocialMediaPostModel) : Bundle {
        val bundle = Bundle()
        val list = arrayListOf<String>()
        post.imgList.forEach{list.add(it)}  // need to do this because kotlin doesn't like lists
        bundle.putInt(ID_KEY, post.id)
        bundle.putDouble(LAT_KEY, post.locationLat)
        bundle.putDouble(LONG_KEY, post.locationLong)
        bundle.putString(SocialMediaPostActivity.TITLE_KEY, post.title)
        bundle.putString(SocialMediaPostActivity.USERID_KEY, post.userId)
        bundle.putString(SocialMediaPostActivity.TEXT_CONTENT_KEY, post.textContent)
        bundle.putLong(SocialMediaPostActivity.TIMESTAMP_KEY, post.timeStamp)
        bundle.putStringArrayList(SocialMediaPostActivity.IMAGE_URIS_KEY, list)
        return bundle
    }

    fun getNameFromUserId (id: Int) : String{
        return "User$id"
    }

    fun getUsernameFromUserId (id: String) : String{
        val database = Firebase.database
        var userName = "Unknown User"
        database.reference.child("user")
            .child(id).child("username").get().addOnCompleteListener() {
                if (it.isSuccessful) {
                    userName = it.result.value.toString()
                    println("Debug: Success username $userName, ${it.result.value.toString()}")
                } else {
                    println("Debug: Failed username")
                }
            }
        return userName
    }

    fun getUserId () : Int {
        return 1234
    }

    fun userLikedPost (userId: Int, postId: Int) : Boolean {
        TODO("when db is implemented")
    }

    fun selfLikedPost (postId: Int) : Boolean {
        return false
    }

    fun getProfilePhotoFromUserId (id: String, context: Context) : Drawable {
        return AppCompatResources.getDrawable(context, R.drawable.person)!!
    }
}
