package com.example.restaurant_review.Activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.restaurant_review.Activities.CreateVRTourActivity.Companion.NAME_KEY
import com.example.restaurant_review.Activities.CreateVRTourActivity.Companion.PREVIEW_KEY
import com.example.restaurant_review.Activities.CreateVRTourActivity.Companion.TOUR_KEY
import com.example.restaurant_review.Model.RestaurantTour
import com.example.restaurant_review.Model.TourNode
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.Util
import com.google.firebase.storage.FirebaseStorage
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener
import com.google.vr.sdk.widgets.pano.VrPanoramaView
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Activity that holds the display for a VR Tour.
 * Used and modified from Google's VR SDK.
 */
class VRViewActivity : AppCompatActivity() {
    /** Actual panorama widget.  */
    private var panoWidgetView: VrPanoramaView? = null

    private lateinit var restaurantNameView: TextView
    private lateinit var place_description: TextView
    private lateinit var exitPreviewButton: Button
    private lateinit var upArrow: ImageButton
    private lateinit var leftArrow: ImageButton
    private lateinit var rightArrow: ImageButton
    private lateinit var downArrow: ImageButton
    private lateinit var moveTextView: TextView
    private lateinit var tour: RestaurantTour

    private var preview = false
    /**
     * Arbitrary variable to track load status. In this example, this variable should only be accessed
     * on the UI thread. In a real app, this variable would be code that performs some UI actions when
     * the panorama is fully loaded.
     */
    var loadImageSuccessful = false

    /** Tracks the file to be loaded across the lifetime of this app.  */
    private var fileUri: Uri? = null

    /** Configuration information for the panorama.  */
    private val panoOptions = VrPanoramaView.Options()
    private var backgroundImageLoaderTask: ImageLoaderTask? = null

    /**
     * Called when the app is launched via the app icon or an intent using the adb command above. This
     * initializes the app and loads the image to render.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vrview)
        val tour1 = intent.getParcelableExtra<RestaurantTour>(TOUR_KEY)
        var restaurantName = "Restaurant"
        tour = if (intent != null && tour1 != null){
            intent.getParcelableExtra(TOUR_KEY)!!
        } else {
            RestaurantTour(TourNode("a","a"),0)//createTour()
        }
        if (intent != null){
            preview = intent.getBooleanExtra(PREVIEW_KEY, false)
            restaurantName = intent.getStringExtra(NAME_KEY) ?: "Restaurant"
        }

        // Make the source link clickable.
        panoWidgetView = findViewById<View>(R.id.pano_view) as VrPanoramaView
        panoWidgetView!!.setEventListener(ActivityEventListener())

        restaurantNameView = findViewById(R.id.vr_restaurant_name)
        exitPreviewButton = findViewById(R.id.exit_preview_button)
        place_description = findViewById(R.id.place_description)
        upArrow = findViewById(R.id.arrow_up)
        leftArrow = findViewById(R.id.arrow_left)
        rightArrow = findViewById(R.id.arrow_right)
        downArrow = findViewById(R.id.arrow_down)
        moveTextView = findViewById(R.id.move_info)

        if (preview) {
            restaurantNameView.text = "Preview"
            exitPreviewButton.visibility = View.VISIBLE
            exitPreviewButton.setOnClickListener { finish() }
        }
        else restaurantNameView.text = "VR Tour by $restaurantName"

        updateArrowStates(tour)

        upArrow.setOnClickListener {
            tour.goTop()
            updateArrowStates(tour)
            moveTextView.visibility = View.GONE
        }
        leftArrow.setOnClickListener {
            tour.goLeft()
            updateArrowStates(tour)
            moveTextView.visibility = View.GONE
        }
        rightArrow.setOnClickListener {
            tour.goRight()
            updateArrowStates(tour)
            moveTextView.visibility = View.GONE
        }
        downArrow.setOnClickListener {
            tour.goBottom()
            updateArrowStates(tour)
            moveTextView.visibility = View.GONE
        }
    }

    fun updateArrowStates (tour: RestaurantTour){
        upArrow.visibility = if (tour.hasTop()) View.VISIBLE else View.GONE
        leftArrow.visibility = if (tour.hasLeft()) View.VISIBLE else View.GONE
        rightArrow.visibility = if (tour.hasRight()) View.VISIBLE else View.GONE
        downArrow.visibility = if (tour.hasBottom()) View.VISIBLE else View.GONE
        place_description.text = tour.currNode!!.name
        handleNewImage(tour.currNode!!.image!!)
    }

    /**
     * Load custom images based on the Intent or load the default image. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    private fun handleNewImage(imageName: String) {
        val uri = Util.filePathToUri(this, imageName)
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        if (!File(uri.path!!).exists()){
            val fileName = Util.filePathToName(imageName)
            // store the image in local storage for easy retrieval
            storageRef.child(fileName).getFile(uri).addOnCompleteListener {
                // try accessing local storage again
                try {
                    fileUri = uri
                    panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO

                    // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
                    // take 100s of milliseconds.
                    backgroundImageLoaderTask?.cancel(true)
                    backgroundImageLoaderTask = ImageLoaderTask()
                    backgroundImageLoaderTask!!.execute(Pair.create(fileUri, panoOptions))
                }
                catch (e: FileNotFoundException) {
                }
            }
        }
        else {
            fileUri = uri
            panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO

            // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
            // take 100s of milliseconds.
            backgroundImageLoaderTask?.cancel(true)
            backgroundImageLoaderTask = ImageLoaderTask()
            backgroundImageLoaderTask!!.execute(Pair.create(fileUri, panoOptions))
        }
    }

    override fun onPause() {
        panoWidgetView!!.pauseRendering()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        panoWidgetView!!.resumeRendering()
    }

    override fun onDestroy() {
        // Destroy the widget and free memory.
        panoWidgetView!!.shutdown()

        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        backgroundImageLoaderTask?.cancel(true)
        super.onDestroy()
    }

    /**
     * Helper class to manage threading.
     */
    internal inner class ImageLoaderTask :
        AsyncTask<Pair<Uri, VrPanoramaView.Options?>, Void?, Boolean>() {
        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        override fun doInBackground(vararg fileInformation: Pair<Uri, VrPanoramaView.Options?>): Boolean {
            var panoOptions: VrPanoramaView.Options? =
                null // It's safe to use null VrPanoramaView.Options.
            var bmp: Bitmap
            try {
                bmp = Util.getBitmap(this@VRViewActivity, fileInformation[0].first!!)
                panoOptions = fileInformation[0].second
                bmp = Bitmap.createScaledBitmap(bmp, 4096, 2048, false)
                panoWidgetView!!.loadImageFromBitmap(bmp, panoOptions)
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "Could not load file: $e"
                )
                return false
            }
            return true
        }
    }

    /**
     * Listen to the important events from widget.
     */
    private inner class ActivityEventListener : VrPanoramaEventListener() {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        override fun onLoadSuccess() {
            loadImageSuccessful = true
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        override fun onLoadError(errorMessage: String) {
            loadImageSuccessful = false
            Toast.makeText(
                this@VRViewActivity,
                "Error loading pano: $errorMessage",
                Toast.LENGTH_LONG
            )
                .show()
            Log.e(
                TAG,
                "Error loading pano: $errorMessage"
            )
        }
    }

    companion object {
        private val TAG = VRViewActivity::class.java.simpleName
    }
}