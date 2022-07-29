package com.example.restaurant_review.Util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.restaurant_review.Activities.SocialMediaPostActivity
import com.example.restaurant_review.R


class CameraDialog: DialogFragment(), AdapterView.OnItemClickListener {
    companion object {
        const val CAMERA = 1
        const val DIALOG_KEY = "key"
        const val TITLE_KEY = "title key"
        const val ARRAY_KEY = "array key"
    }
    private var dialogType: Int = CAMERA
    private lateinit var title: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        val bundle = arguments
        val type = bundle!!.getInt(DIALOG_KEY)
        title = bundle.getString(TITLE_KEY)!!
        when (type) {
            CAMERA -> dialog = cameraDialog(title)
        }
        dialogType = type
        return dialog
    }

    override fun onPause() {
        // https://stackoverflow.com/questions/13401632/android-app-crashed-on-screen-rotation-with-dialog-open
        // close the dialog window if the screen is rotated
        super.onPause()
        dismiss()
    }

    private fun cameraDialog(title: String): Dialog{
        /**
         * A camera dialog that can ONLY be used for SocialMediaPostActivity!
         */
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.sm_fragment_dialog_text_field, null)
        val bundle = arguments
        // Get the descriptions of each text view
        val textList = bundle!!.getStringArrayList(ARRAY_KEY)!!
        val linearLayout: ListView = view.findViewById(R.id.text_field_start)
        // create a new text view for each description
        val adapter = ArrayAdapter(requireContext(), R.layout.sm_list_view, textList)
        linearLayout.adapter = adapter
        linearLayout.onItemClickListener = this
        builder.setView(view)
        builder.setTitle(title)
        return builder.create()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        this.dismiss()
        if (requireActivity() !is SocialMediaPostActivity) {
            return
        }
        when (position){
            // get the current activity (SocialMediaPostActivity) and use the appropriate methods
            0 -> {
                if (Util.checkCameraPermissions(requireActivity())) {
                    (requireActivity() as SocialMediaPostActivity).launchCamera()
                }
                else {
                    Toast.makeText(requireContext(), "Allow camera permissions first!", Toast.LENGTH_SHORT).show()
                }
            }
            1 -> {
                if (Util.checkReadExternalStoragePermissions(requireActivity())) {
                    (requireActivity() as SocialMediaPostActivity).openGallery()
                }
                else {
                    Toast.makeText(requireContext(), "Allow read gallery permissions first!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}