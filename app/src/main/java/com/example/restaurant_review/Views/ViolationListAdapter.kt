package com.example.restaurant_review.Views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.restaurant_review.Model.Violation
import com.example.restaurant_review.R
import java.util.ArrayList

/**
 * ViolationListAdapter Class Implementation
 *
 * To populate the ListView as we need.
 */
class ViolationListAdapter(context: Context?, violations: ArrayList<Violation>?) :
    ArrayAdapter<Violation?>(context!!, R.layout.list_item_violation,
        violations!! as List<Violation?>
    ) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val viewViolation: View =
            view ?: LayoutInflater.from(context).inflate(R.layout.list_item_violation, null)

        // Get the views from the inspection item.
        val violationIcon = viewViolation.findViewById<ImageView>(R.id.list_violation_icon)
        val violationDetail: TextView =
            viewViolation.findViewById<TextView>(R.id.list_violation_detail)
        val violationType: TextView = viewViolation.findViewById<TextView>(R.id.list_violation_type)
        val item: Violation? = getItem(position)

        // setup values for each view

        // setup the violation icon
        if (item?.violationDetail?.lowercase()?.contains("food") == true) {
            violationIcon.setImageResource(R.drawable.ic_violation_food)
        } else if (item?.violationDetail?.lowercase()?.contains("pest") == true) {
            violationIcon.setImageResource(R.drawable.ic_violation_pest)
        } else if (item?.violationDetail?.lowercase()?.contains("equipment") == true) {
            violationIcon.setImageResource(R.drawable.ic_violation_equipment)
        } else if (item?.violationDetail?.lowercase()?.contains("employee") == true) {
            violationIcon.setImageResource(R.drawable.ic_violation_employee)
        } else {
            violationIcon.setImageResource(R.drawable.ic_hazard_unknown)
        }

        // setup the text color
        if (item?.violationCritical?.lowercase()?.contains("not critical") == true) {
            violationType.setTextColor(context.getColor(R.color.colorModerateHazard))
        } else {
            violationType.setTextColor(context.getColor(R.color.colorHighHazard))
        }
        violationType.text = context.getString(
                R.string.violationType,
                item?.violationCode,
                item?.violationCritical
            )
        violationDetail.text = item?.violationDetail
        return viewViolation
    }
}