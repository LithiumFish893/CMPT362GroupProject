package com.example.restaurant_review.Views

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.restaurant_review.Model.Inspection
import com.example.restaurant_review.Model.MyApplication
import com.example.restaurant_review.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * InspectionListAdapter Class Implementation
 *
 * To populate the ListView as we need.
 */
class InspectionListAdapter(
    context: Context?,
    listViewResId: Int,
    inspectionsList: ArrayList<Inspection>
) : ArrayAdapter<Inspection?>(context!!, listViewResId, inspectionsList as List<Inspection?>) {
    private val mInspectionsList: ArrayList<Inspection>
    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val viewInspection: View =
            view ?: LayoutInflater.from(context).inflate(R.layout.list_item_inspection, null)

        // Get each view from the Inspection ListView
        val mInspection: Inspection = mInspectionsList[position]
        val inspectionHazardIcon =
            viewInspection.findViewById<ImageView>(R.id.list_inspection_hazard_icon)
        val inspectionDate: TextView =
            viewInspection.findViewById<TextView>(R.id.list_inspection_date)
        val inspectionHazard: TextView =
            viewInspection.findViewById<TextView>(R.id.list_inspection_hazard)
        when (mInspection.hazard) {
            "Low" -> {
                inspectionHazardIcon.setImageResource(R.drawable.ic_hazard_low)
                inspectionHazard.text = mInspection.hazard.toString() + " " + MyApplication.context
                        ?.getString(R.string.inspection_hazard_list)

                inspectionHazard.setTextColor(getContext().getColor(R.color.colorLowHazard))
            }
            "Moderate" -> {
                inspectionHazardIcon.setImageResource(R.drawable.ic_hazard_moderate)
                inspectionHazard.text = mInspection.hazard + " " + MyApplication.context
                        ?.getString(R.string.inspection_hazard_list)
                inspectionHazard.setTextColor(context.getColor(R.color.colorModerateHazard))
            }
            "High" -> {
                inspectionHazardIcon.setImageResource(R.drawable.ic_hazard_high)
                inspectionHazard.text = mInspection.hazard + " " + MyApplication.context
                        ?.getString(R.string.inspection_hazard_list)
                inspectionHazard.setTextColor(context.getColor(R.color.colorHighHazard))
            }
            else -> {
                println("other ${mInspection.hazard}")
                inspectionHazardIcon.setImageResource(R.drawable.ic_hazard_unknown)
            }
        }

        // setup date format
        var calendar = Calendar.getInstance()
        val now = calendar.time
        calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val oneMonth = calendar.time
        calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        val oneYear = calendar.time
        if (mInspection.simpleDate?.before(oneYear) == true) {
            val df = SimpleDateFormat("MMM yyyy")
            val simpleDate = df.format(mInspection.simpleDate)
            inspectionDate.text = simpleDate
        } else if (mInspection.simpleDate?.before(oneMonth) == true) {
            val df = SimpleDateFormat("MMM dd")
            val simpleDate = df.format(mInspection.simpleDate)
            inspectionDate.text = simpleDate
        } else {
            // within one month
            val days: Long =
                (now.time - mInspection.simpleDate?.time!!) / (24 * 60 * 60 * 1000)
            inspectionDate.text = days.toString() + " " + MyApplication.context?.getString(R.string.days_ago)
        }
        return viewInspection
    }

    // Constructor
    init {
        mInspectionsList = inspectionsList
    }
}