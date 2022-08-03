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

class CreateVRTourActivity: AppCompatActivity() {
    private lateinit var buttonList: ArrayList<ImageButton>
    private lateinit var rootAddButton: ImageButton
    private lateinit var root: ConstraintLayout
    private lateinit var infoButton: ImageButton
    private lateinit var briefDesc: TextView
    private lateinit var previewButton: Button
    private var tour: RestaurantTour? = null
    private val grid: Array<Array<TourNode?>> = Array(MAX_GRID_SIZE) {Array(MAX_GRID_SIZE) {null} }

    companion object {
        const val TOUR_KEY = "tour key"
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

        infoButton.setOnClickListener {
            val v = briefDesc.visibility
            if (v == View.GONE) briefDesc.visibility = View.VISIBLE
            else briefDesc.visibility = View.GONE
        }
        previewButton.setOnClickListener {
            if (tour != null) {
                val intent = Intent(this, VRViewActivity::class.java)
                intent.putExtra(TOUR_KEY, tour)
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "There is nothing to preview!", Toast.LENGTH_SHORT).show()
            }
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
                    val node = TourNode(text, imageUri)
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