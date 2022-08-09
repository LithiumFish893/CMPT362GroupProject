package com.example.restaurant_review.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.restaurant_review.Model.Inspection
import com.example.restaurant_review.Model.InspectionManager
import com.example.restaurant_review.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity that shows health check information of a restaurant.
 */
class InspectionDetailActivity: AppCompatActivity(){
    var mInspection: Inspection? = null
    var ID: String? = null
    var INDEX = 0

    fun makeLaunchIntent(context: Context?, ID: String?, index: Int): Intent {
        val intent = Intent(context, InspectionDetailActivity::class.java)
        intent.putExtra(java.lang.String.valueOf(R.string.intent_extra_id), ID)
        intent.putExtra(java.lang.String.valueOf(R.string.intent_extra_index), index)
        return intent
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspection_detail)
        val intent = intent
        ID = intent.getStringExtra(java.lang.String.valueOf(R.string.intent_extra_id))
        INDEX = intent.getIntExtra(java.lang.String.valueOf(R.string.intent_extra_index), -1)

        // setup UI
        setupUI()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupUI() {
        // setup toolbar
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.toolbar_inspection_detail)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(getSupportActionBar()).setDisplayHomeAsUpEnabled(true)

        // setup UI Views
        val manger: InspectionManager? = InspectionManager.instance
        val inspectionList: ArrayList<Inspection>? = ID?.let { manger?.getInspections(it) }
        mInspection = inspectionList?.get(INDEX)

        // Get views
        val date: TextView = findViewById<TextView>(R.id.inspection_date)
        val type: TextView = findViewById<TextView>(R.id.inspection_type)
        val critical: TextView = findViewById<TextView>(R.id.inspection_critical)
        val non_critical: TextView = findViewById<TextView>(R.id.inspection_non_critical)
        val hazard: TextView = findViewById<TextView>(R.id.inspection_hazard)
        val hazardIcon: ImageView = findViewById<ImageView>(R.id.inspection_hazard_icon)
        val df = SimpleDateFormat("MMM dd, yyyy")
        val simpleDate = df.format(mInspection?.simpleDate!!)

        // Set values
        date.text = simpleDate
        val typeIcon: ImageView = findViewById<ImageView>(R.id.inspection_type_icon)
        when (mInspection?.hazard) {
            "Low" -> {
                hazardIcon.setImageResource(R.drawable.ic_hazard_low)
                hazard.text = getString(R.string.hazardLevel, mInspection!!.hazard)
                hazard.setTextColor(getColor(R.color.colorLowHazard))
            }
            "Moderate" -> {
                hazardIcon.setImageResource(R.drawable.ic_hazard_moderate)
                hazard.text = getString(R.string.hazardLevel, mInspection!!.hazard)
                hazard.setTextColor(getColor(R.color.colorModerateHazard))
            }
            "High" -> {
                hazardIcon.setImageResource(R.drawable.ic_hazard_high)
                hazard.text = getString(R.string.hazardLevel, mInspection!!.hazard)
                hazard.setTextColor(getColor(R.color.colorHighHazard))
            }
            else -> hazardIcon.setImageResource(R.drawable.ic_hazard_unknown)
        }
    }


}