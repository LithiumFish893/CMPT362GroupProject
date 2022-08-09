package com.example.restaurant_review.Activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import com.example.restaurant_review.Model.RestaurantTour
import com.example.restaurant_review.Model.TourNode
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.TourNodeDialog
import com.example.restaurant_review.Util.Util
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.*

/**
 * Activity that lets the user create a VR tour.
 */
class CreateVRTourActivity: AppCompatActivity() {
    private lateinit var buttonList: ArrayList<ImageButton>
    private lateinit var rootAddButton: ImageButton
    private lateinit var root: ConstraintLayout
    private lateinit var infoButton: ImageButton
    private lateinit var briefDesc: TextView
    private lateinit var previewButton: Button
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button
    private var tour: RestaurantTour? = null
    private val grid: Array<Array<TourNode?>> = Array(MAX_GRID_SIZE) {Array(MAX_GRID_SIZE) {null} }
    private lateinit var imgNameToUris: HashMap<String, Uri>

    companion object {
        const val TOUR_KEY = "tour key"
        const val PREVIEW_KEY = "preview key"
        const val NAME_KEY = "name key"
        const val MAX_GRID_SIZE = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_vr_tour)
        supportActionBar?.title = "Create VR Tour"
        buttonList = arrayListOf()
        rootAddButton = findViewById(R.id.root_add_button)
        root = findViewById(R.id.root_create_layout)
        infoButton = findViewById(R.id.vr_info_button)
        briefDesc = findViewById(R.id.vr_brief_desc)
        previewButton = findViewById(R.id.vr_preview)
        submitButton = findViewById(R.id.vr_submit)
        cancelButton = findViewById(R.id.vr_cancel)

        imgNameToUris = HashMap()

        infoButton.setOnClickListener {
            val v = briefDesc.visibility
            if (v == View.GONE) briefDesc.visibility = View.VISIBLE
            else briefDesc.visibility = View.GONE
        }
        previewButton.setOnClickListener {
            if (tour != null) {
                val intent = Intent(this, VRViewActivity::class.java)
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                val sz = imgNameToUris.size
                val progressBar = ProgressBar(this)
                val textView = TextView(this)
                progressBar.id = View.generateViewId()
                textView.id = View.generateViewId()
                root.addView(progressBar)
                root.addView(textView)
                textView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    endToEnd = root.id
                    topToBottom = previewButton.id
                }
                progressBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    endToStart = textView.id
                    topToBottom = previewButton.id
                    width = com.arlib.floatingsearchview.util.Util.dpToPx(24)
                    height = com.arlib.floatingsearchview.util.Util.dpToPx(24)
                }
                progressBar.isIndeterminate = true
                textView.text = "Loading the preview..."
                imgNameToUris.onEachIndexed{ i, pair ->
                    val name = pair.component1()
                    val uri = pair.component2()
                    val imgRef = storageRef.child(name)
                    imgRef.putFile(uri).addOnCompleteListener(object : OnCompleteListener<UploadTask.TaskSnapshot>{
                        override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                            if (i == sz-1){
                                root.removeView(progressBar)
                                root.removeView(textView)
                                intent.putExtra(TOUR_KEY, tour)
                                intent.putExtra(PREVIEW_KEY, true)
                                startActivity(intent)
                            }
                        }

                    })
                }
                imgNameToUris = hashMapOf()
            }
            else {
                Toast.makeText(this, "There is nothing to preview!", Toast.LENGTH_SHORT).show()
            }
        }
        submitButton.setOnClickListener {
            if (tour != null){
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                // save the images
                for (pair in imgNameToUris){
                    val name = pair.component1()
                    val uri = pair.component2()
                    val imgRef = storageRef.child(name)
                    imgRef.putFile(uri)
                }
                val firebaseAuth = Firebase.auth
                val currentUser = firebaseAuth.currentUser!!.uid
                val database = Firebase.database.reference
                val tourRef = database.child("tours")

                val a = tour!!.getArrayList()
                tourRef.child(currentUser).setValue(a)
                finish()
            }
            else {
                Toast.makeText(this, "There is nothing to submit!", Toast.LENGTH_SHORT).show()
            }
        }
        cancelButton.setOnClickListener {
            finish()
        }
        briefDesc.text = """
            Click on the center + icon to start
            This will be the entrance to your tour
            Add a title to the place (eg. "Entrance")
            And add a panoramic image of the location
        """.trimIndent()
        rootAddButton.setOnClickListener {
            create_node(it as ImageButton, MAX_GRID_SIZE/2, MAX_GRID_SIZE/2)
        }
    }

    private fun create_node(srcButton: ImageButton, row: Int, col: Int) {
        if (row >= MAX_GRID_SIZE-1 || row <= 0 || col >= MAX_GRID_SIZE-1 || col <= 0) return
        val dialog = TourNodeDialog(
            object : TourNodeDialog.OnDialogSetListener {
                override fun onDialogSet(text: String, imageUri: Uri) {
                    val imgName = Calendar.getInstance().timeInMillis.toString()
                    imgNameToUris[imgName] = imageUri
                    val node = TourNode(text, imgName)
                    val leftCol = col-1
                    val rightCol = col+1
                    val topRow = row-1
                    val bottomRow = row+1
                    grid[row][col] = node
                    if (tour == null){
                        tour = RestaurantTour(node, MAX_GRID_SIZE)
                    }
                    // add nodes to tour
                    else {
                        val top = tour!!.tourGrid[topRow][col]
                        val bottom = tour!!.tourGrid[bottomRow][col]
                        val left = tour!!.tourGrid[row][leftCol]
                        val right = tour!!.tourGrid[row][rightCol]
                        if (top?.image != null){
                            tour!!.addBottom(top, node)
                        }
                        if (bottom?.image != null){
                            tour!!.addTop(bottom, node)
                        }
                        if (left?.image != null){
                            tour!!.addRight(left, node)
                        }
                        if (right?.image != null){
                            tour!!.addLeft(right, node)
                        }
                    }
                    srcButton.setImageBitmap(Util.getBitmap(this@CreateVRTourActivity, imageUri))
                    srcButton.setOnClickListener { }
                    val tv = TextView(this@CreateVRTourActivity)
                    tv.id = View.generateViewId()
                    root.addView(tv)
                    tv.text = text
                    tv.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        bottomToTop = srcButton.id
                        startToStart = srcButton.id
                        endToEnd = srcButton.id
                        height = WRAP_CONTENT
                        width = WRAP_CONTENT
                    }
                    if (grid[row][leftCol] == null){
                        val ibLeft = createAndAddImageButton(row, leftCol)
                        ibLeft.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            endToStart = srcButton.id
                            topToTop = srcButton.id
                            bottomToBottom = srcButton.id
                        }
                    }
                    if (grid[row][rightCol] == null){
                        val ibRight = createAndAddImageButton(row, rightCol)
                        ibRight.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            startToEnd = srcButton.id
                            topToTop = srcButton.id
                            bottomToBottom = srcButton.id
                        }
                    }
                    if (grid[topRow][col] == null){
                        val ibTop = createAndAddImageButton(topRow, col)
                        ibTop.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            startToStart = srcButton.id
                            endToEnd = srcButton.id
                            bottomToTop = tv.id
                        }
                    }
                    if (grid[bottomRow][col] == null){
                        val ibBottom = createAndAddImageButton(bottomRow, col)
                        ibBottom.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            startToStart = srcButton.id
                            endToEnd = srcButton.id
                            topToBottom = srcButton.id
                        }
                    }
                }
            }
        )
        dialog.show(supportFragmentManager, "tag")
    }

    private fun createAndAddImageButton(row: Int, col: Int): ImageButton{
        val ib = ImageButton(this@CreateVRTourActivity)
        grid[row][col] = TourNode("", null) // empty image means no tour node, but there is a plus button
        ib.id = View.generateViewId()
        root.addView(ib)
        with(TypedValue()) {
            theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
            ib.setBackgroundResource(resourceId)
        }
        ib.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.add_box, null))
        ib.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val dp64 = com.arlib.floatingsearchview.util.Util.dpToPx(64)
            width = dp64
            height = dp64
            setMargins(com.arlib.floatingsearchview.util.Util.dpToPx(5))
        }
        ib.setOnClickListener { create_node(it as ImageButton, row, col) }
        return ib
    }
}