package com.example.restaurant_review.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.restaurant_review.Model.ImagesViewModel
import com.example.restaurant_review.Model.MyApplication
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.CameraDialog
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.Views.HorizontalImageAdapter
import com.example.restaurant_review.local_database.SocialMediaPostModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.util.*
import org.json.JSONObject


class SocialMediaPostActivity : AppCompatActivity() {
    private var locationName: String = ""
    private var isLookingForLocation: Boolean = false
    private val MIN_TEXTCONTENT_LENGTH: Int = 5
    private var SHARED_PREF_FILE_NAME: String = "shared_pref"
    private lateinit var uri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private var FILE_NAME: String = "img.jpg"
    private var TEMP_FILE_NAME: String = "img_temp.jpg"
    private lateinit var viewModel: ImagesViewModel
    private lateinit var currImg : String
    private val GET_GALLERY_CODE = 102
    private val MAX_IMAGE_COUNT = 3
    private lateinit var file: File

    private var savedLatitude: Double = 0.0
    private var savedLongitude: Double = 0.0

    private lateinit var adapter: HorizontalImageAdapter
    private lateinit var imgView: RecyclerView
    private lateinit var titleView: EditText
    private lateinit var noneLocationTv: TextView
    private lateinit var noneTv: TextView
    private lateinit var textContentView: EditText
    private lateinit var locationConstraintLayout: ConstraintLayout
    private lateinit var locationTextView: TextView
    private lateinit var findLocationButton: Button
    private lateinit var postButton: Button
    private lateinit var cancelButton: Button

    private lateinit var auth: FirebaseAuth

    companion object {
        const val ID_KEY = "id key"
        const val LAT_KEY = "lat key"
        const val LONG_KEY = "long key"
        const val LOCATION_NAME_KEY = "location name key"
        const val TITLE_KEY = "title key"
        const val USERID_KEY = "userid key"
        const val TEXT_CONTENT_KEY = "text content key"
        const val TIMESTAMP_KEY = "timestamp key"
        const val IMAGE_URIS_KEY = "image uris keys"
        const val RESULT_CODE = 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sm_activity_social_media_post)
        title = "New Post"
        // Get all the views
        imgView = findViewById(R.id.profile_picture)
        //titleView = findViewById(R.id.smp_title)
        noneLocationTv = findViewById(R.id.none_location_tv)
        noneTv = findViewById(R.id.none_images_tv)
        textContentView = findViewById(R.id.smp_textContent)
        locationConstraintLayout = findViewById(R.id.sm_post_location_constraint_layout)
        locationTextView = findViewById(R.id.sm_post_location_text_view)
        findLocationButton = findViewById(R.id.sm_post_find_location_button)
        postButton = findViewById(R.id.smp_save_button)
        cancelButton = findViewById(R.id.smp_cancel_button)
        postButton.setOnClickListener { onSaveClicked() }
        cancelButton.setOnClickListener { onCancelClicked() }
        findLocationButton.setOnClickListener { findApproximateLocation() }

        auth = FirebaseAuth.getInstance()

        // Define the file & uri to store the image in
        file = File(getExternalFilesDir(null), TEMP_FILE_NAME)
        uri = FileProvider.getUriForFile(this, "com.example.restaurant_review", file)
        // Use view models to dynamically update the profile photo
        viewModel = ViewModelProvider(this).get(ImagesViewModel::class.java)
        viewModel.imgs.value = arrayListOf()
        viewModel.imgUris.value = arrayListOf()
        viewModel.imgs.observe(this) {
            if (it.isNotEmpty()) noneTv.visibility = View.GONE
            else noneTv.visibility = View.VISIBLE
            adapter.updateList(it)
        }
        // Get the camera
        cameraResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK && file.exists()) {
                val bitmap = Util.getBitmap(this, uri)
                // save the image to the gallery
                // credit to
                // https://developer.android.com/training/camera/photobasics
                // https://stackoverflow.com/questions/8560501/android-save-image-into-gallery
                val tempFile = createImg()
                currImg = tempFile.absolutePath
                MediaStore.Images.Media.insertImage(contentResolver, bitmap, currImg, "")
                saveImg(bitmap)
            }
        }
        adapter = HorizontalImageAdapter(arrayListOf()){
            adapter.removeItem(it)
        }
        imgView.adapter = adapter
        imgView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val takenPicture = File(getExternalFilesDir(null), FILE_NAME)
        val takenUri = FileProvider.getUriForFile(this, "com.example.restaurant_review", takenPicture)
        // update the image view if it's already there (used when saved)
        if (takenPicture.exists()) {
            val bitmap = Util.getBitmap(this, takenUri)
            //imgView.setImageBitmap(bitmap)
        }

    }

    private fun findApproximateLocation() {
        /**
         * Gets the user's location and finds an approximate one close to the restaurant
         */
        if (isLookingForLocation) return
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            makeText(this, "Please enable location tracking first!", LENGTH_SHORT).show()
        }
        else {
            isLookingForLocation = true
            noneLocationTv.visibility = View.GONE
            val progressBar = ProgressBar(this)
            val textView = TextView(this)
            progressBar.isIndeterminate = false
            progressBar.id = View.generateViewId()
            textView.id = View.generateViewId()
            locationConstraintLayout.addView(progressBar)
            locationConstraintLayout.addView(textView)
            progressBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToStart = locationTextView.id
                topToBottom = locationTextView.id
                width = com.arlib.floatingsearchview.util.Util.dpToPx(24)
                height = com.arlib.floatingsearchview.util.Util.dpToPx(24)
            }
            progressBar.updatePadding(0,0,com.arlib.floatingsearchview.util.Util.dpToPx(10),0,)
            textView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                startToEnd = progressBar.id
                topToBottom = locationTextView.id
            }
            textView.setTypeface(textView.typeface, Typeface.ITALIC)
            textView.text = "Finding your location..."

            val locationClient = LocationServices.getFusedLocationProviderClient(this)
            locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener {
                    val queue = Volley.newRequestQueue(MyApplication.context)
                    textView.text = "Finding a close restaurant..."
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val url = "https://api.yelp.com/v3/businesses/search?term=res&latitude=$latitude&longitude=$longitude&radius=1000&limit=50"
                    val restaurantsRequest: StringRequest = object : StringRequest(
                        Method.GET, url,
                        Response.Listener { response ->
                            if (response != null) {
                                val responseJSON = JSONObject(response)
                                val businessArray = responseJSON.getJSONArray("businesses")
                                if (businessArray.length() == 0){
                                    progressBar.visibility = View.GONE
                                    textView.text = "Could not find nearby restaurant."
                                    return@Listener
                                }
                                var minDist = 100000f
                                var minIndex = 0
                                var minLat = 0.0
                                var minLong = 0.0
                                for (i in 0 until businessArray.length()){
                                    val coords = businessArray.getJSONObject(i).getJSONObject("coordinates")
                                    val lat = coords.getDouble("latitude")
                                    val long = coords.getDouble("longitude")
                                    val results = floatArrayOf(0f,0f,0f)
                                    Location.distanceBetween(latitude, longitude, lat, long, results)
                                    var dist = results[0]  // in meters
                                    // todo: check
                                    dist = businessArray.getJSONObject(i).getDouble("distance").toFloat()
                                    if (dist < minDist){
                                        minDist = dist
                                        minIndex = i
                                        minLat = lat
                                        minLong = long
                                    }
                                }
                                savedLatitude = minLat
                                savedLongitude = minLong
                                val minDistBusiness = businessArray.getJSONObject(minIndex)
                                var name = minDistBusiness.getString("name")
                                locationName = name
                                var displayName = "Using "
                                progressBar.visibility = View.GONE
                                displayName += name
                                textView.text = displayName
                            }
                        },
                        Response.ErrorListener { }) {
                        //This is for Headers If You Needed
                        override fun getHeaders(): Map<String, String> {
                            val params: MutableMap<String, String> = HashMap()
                            params["Content-Type"] = "application/json; charset=UTF-8"
                            params["Authorization"] = "Bearer 5DID7gCf8IVCfpHugs-cdm6My39YfL3nvQHIu9XMgKphdbVsXRaM4SNZ740uPQ_dwRUcgh3KHqi7P-Bla1OiK2FuuhKpSaewxI0t_KuPmHzhHPWselIlD2co62zrYnYx"
                            return params
                        }
                    }
                    queue.add(restaurantsRequest)
                }
        }
    }

    fun onChangeClicked(view: View) {
        /**
         * This functions triggers when the "Add"
         * button is clicked
         */
        if (viewModel.imgs.value!!.size >= MAX_IMAGE_COUNT){
            makeText(this, "Max images allowed is $MAX_IMAGE_COUNT", LENGTH_SHORT).show()
            return
        }
        val dialog = CameraDialog()
        val bundle = Bundle()
        bundle.putInt(CameraDialog.DIALOG_KEY, CameraDialog.CAMERA)
        bundle.putString(CameraDialog.TITLE_KEY, "Pick Profile Picture")
        bundle.putStringArrayList(
            CameraDialog.ARRAY_KEY,
            arrayListOf("Open Camera", "Select From Gallery "))

        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "tag")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        /**
         * Define how to save information when the device is rotated.
         */
        super.onSaveInstanceState(outState)
        val sharedPreferences = getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.commit()
    }

    fun onSaveClicked() {
        // Using shared preferences to store data
        val sharedPreferences = getSharedPreferences(SHARED_PREF_FILE_NAME, MODE_PRIVATE)
        val title = ""//titleView.text.toString()
        val textContent = textContentView.text.toString()
        val timeStamp = Calendar.getInstance().timeInMillis

        // don't let user save if textcontent is too short
        if (textContent.trim().length < MIN_TEXTCONTENT_LENGTH){
            makeText(this, "Post content must have at least ${MIN_TEXTCONTENT_LENGTH} characters!",
                LENGTH_SHORT).show()
            return
        }

        // Save a copy of the most recent image we took
        // Do this by renaming the temp file to the image file
        val savedPicture = File(getExternalFilesDir(null), FILE_NAME)
        val tempPicture = File(getExternalFilesDir(null), TEMP_FILE_NAME)
        tempPicture.renameTo(savedPicture)

        // Store the user info inside the shared preferences
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.putString(TITLE_KEY, title)
        editor.putString(TEXT_CONTENT_KEY, textContent)
        editor.commit()

        // put all the info into bundle
        val intent = Intent()
        println("using $locationName")
        val post = SocialMediaPostModel(
            title = title, userId = auth.currentUser!!.uid, locationLat = savedLatitude, locationLong = savedLongitude,
            locationName = locationName, textContent = textContent, timeStamp = timeStamp, imgList = viewModel.imgUris.value!!)
        intent.putExtras(Util.postToBundle(post))
        setResult(RESULT_CODE, intent)
        finish()
    }

    fun onCancelClicked() {
        finish()
    }

    fun launchCamera(){
        // Launch the camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        cameraResult.launch(intent)
    }

    fun openGallery() {
        // https://stackoverflow.com/questions/11144783/how-to-access-an-image-from-the-phones-photo-gallery
        val intent = Intent(Intent.ACTION_PICK, uri)
        ActivityCompat.startActivityForResult(this, intent, GET_GALLERY_CODE, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // get the gallery image
        if (requestCode == GET_GALLERY_CODE && resultCode == RESULT_OK)
        {
            val targetUri: Uri? = data?.data
            val bitmap = Util.getBitmap(this, targetUri!!)
            // save the selected image to the (temp) file
            // courtesy of https://stackoverflow.com/questions/649154/save-bitmap-to-location
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

            saveImg(bitmap)
            //imgView.setImageBitmap(bitmap)
        }
    }

    private fun createImg() : File {
        // Create an image file name using the unix timestamp
        val name: String = Date().time.toString()
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tempFile = File.createTempFile(name, ".jpg", storageDir)
        return tempFile
    }

    fun saveImg(bitmap: Bitmap) {
        val file2 = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!, Date().time.toString())
        val fileOutputStream = FileOutputStream(file2)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

        viewModel.imgs.value = viewModel.imgs.value?.plus(bitmap)
        viewModel.imgUris.value = viewModel.imgUris.value?.plus(file2.path)
    }
}