package com.example.restaurant_review.Util

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.fragment.app.DialogFragment
import com.example.restaurant_review.Activities.SocialMediaPostActivity
import com.example.restaurant_review.R
import java.io.File
import java.io.FileOutputStream

class TourNodeDialog(var listener: OnDialogSetListener?):  DialogFragment(), DialogInterface.OnClickListener {
    companion object {
        const val DIALOG_KEY = "key"
        const val TITLE_KEY = "title key"
        const val ARRAY_KEY = "array key"
    }
    private val TEMP_FILE_NAME = "temp_file_name.jpg"
    private lateinit var editText: EditText
    private lateinit var title: String
    private lateinit var button: Button
    private lateinit var textView: TextView
    private val GET_GALLERY_CODE = 102
    private lateinit var file: File
    private lateinit var uri: Uri
    private lateinit var res: ActivityResultLauncher<Intent>

    interface OnDialogSetListener {
        fun onDialogSet(text: String, imageUri: Uri)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        dialog = createDialog("Create a room")
        res = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                println("image selected2")
                // get the gallery image
                println("image selected")
                val targetUri: Uri? = it.data!!.data
                uri = targetUri!!
                println(uri)
                textView.text = uri.toString()
            }
        }
        return dialog
    }

    private fun createDialog(title: String): Dialog {
        /**
         * A camera dialog that can ONLY be used for SocialMediaPostActivity!
         */
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_create_tour_dialog, null)
        editText = view.findViewById(R.id.vr_tour_edit_title)
        button = view.findViewById(R.id.vr_tour_select_image)
        textView = view.findViewById(R.id.dialog_image_selected)
        button.setOnClickListener { openGallery() }
        textView.text = "No image selected"
        builder.setView(view)
        builder.setTitle(title)
        builder.setPositiveButton("ok", this)
        builder.setNegativeButton("cancel", this)
        return builder.create()
    }


    override fun onClick(dialog: DialogInterface?, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE){
            listener?.onDialogSet(editText.text.toString(), uri)
        }
    }

    fun openGallery() {
        // https://stackoverflow.com/questions/11144783/how-to-access-an-image-from-the-phones-photo-gallery
        file = File(requireActivity().getExternalFilesDir(null), TEMP_FILE_NAME)
        var uri = FileProvider.getUriForFile(requireActivity(), "com.example.restaurant_review", file)
        val intent = Intent(Intent.ACTION_PICK, uri)
        //ActivityCompat.startActivityForResult(requireActivity(), intent, GET_GALLERY_CODE, null)
        res.launch(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("image selected2, $requestCode, $resultCode")
        // get the gallery image
        if (requestCode == GET_GALLERY_CODE && resultCode == AppCompatActivity.RESULT_OK)
        {
            println("image selected")
            val targetUri: Uri? = data?.data
            uri = targetUri!!
            textView.text = uri.toFile().name
        }
    }

}