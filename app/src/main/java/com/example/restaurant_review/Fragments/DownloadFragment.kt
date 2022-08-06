package com.example.restaurant_review.Fragments

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.app.ActivityCompat
import com.example.restaurant_review.Model.DataRequest
import com.example.restaurant_review.Model.MyApplication
import com.example.restaurant_review.Model.ReadCSV
import com.example.restaurant_review.R
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * DownloadFragment Class Implementation
 *
 * To load the download fragment and execute the download task.
 */
class DownloadFragment : AppCompatDialogFragment() {
    private lateinit var mPrefs: SharedPreferences
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // initialize SharedPreferences, to avoid the null by call getActivity().
        val PREFS_NAME = "mPrefs"
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private var rootView: View? = null
    private lateinit var tv: TextView
    private lateinit var progress: ProgressBar
    private val button: Button? = null
    var contentLen // size of the download file
            = 0
    var filePath: String? = null
    private lateinit var fileName: String
    var d1: AsyncTask<*, *, *>? = null
    var d2: AsyncTask<*, *, *>? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create the view to show
        rootView = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_download, null)
        // Create a button Listener
        val listener: DialogInterface.OnClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    d1?.cancel(true)
                    d2?.cancel(true)
                    // clear the SP
                    val editor: SharedPreferences.Editor ?= mPrefs.edit()
                    editor?.putString("RestaurantsFilePath", "")
                    editor?.putString("InspectionsFilePath", "")
                    editor?.putString("RestaurantsLastModified", "")
                    editor?.putString("InspectionsLastModified", "")
                    editor?.apply()
                }
            }
        tv = rootView!!.findViewById<View>(R.id.loading_tv) as TextView
        progress = rootView!!.findViewById(R.id.download_progress_bar) as ProgressBar

        // start the download task
        if (DataRequest.instance?.isRestaurantsConnected == true) {
            d1 = Download().execute(DataRequest.instance!!.restaurantsUrl)
        }
        if (DataRequest.instance?.isInspectionsConnected == true) {
            d2 = Download().execute(DataRequest.instance!!.inspectionsUrl)
        }

        // Build the dialog
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.dialog_title)
            .setView(rootView)
            .setNegativeButton(android.R.string.cancel, listener)
            .create()
    }

    internal inner class Download : AsyncTask<String?, Int?, String?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            // init updating progress
            progress.progress = 0
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                val url = URL(params[0])
                // Request connection
                val connection = url.openConnection() as HttpURLConnection
                // Request the size of file
                contentLen = connection.contentLength
                // Setup progress bar base on file size
//                publishProgress(PROGRESS_MAX, contentLen)
//                progress.max = contentLen
                // Generating the file path and file name
                filePath = activity!!.getExternalFilesDir("csv")!!.absolutePath
                fileName = url.file
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1)
                fileName = System.currentTimeMillis().toString() + "_" + fileName
                val bis = BufferedInputStream(connection.inputStream)
                val bos = BufferedOutputStream(
                    FileOutputStream(
                        File(filePath + File.separator + fileName)
                    )
                )
                var len = -1
                val bytes = ByteArray(1024)
//                progress.visibility = View.VISIBLE
                progress.isIndeterminate = true
                while (bis.read(bytes).also { len = it } != -1) {
                    bos.write(bytes, 0, len)
                    bos.flush()
                    // real time downloading progress
//                    publishProgress(UPDATE, len)
                }
                progress.isIndeterminate = false
                bos.close()
                bis.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return getString(R.string.update_completed)
        }

//        override fun onProgressUpdate(vararg values: Int?) {
//            super.onProgressUpdate(*values)
//            when (values[0]) {
//                PROGRESS_MAX -> values[1]?.let { progress.setMax(it) }
//                UPDATE -> {
//                    values[1]?.let { progress.incrementProgressBy(it) }
//                    // get the download progress and update the TextView
//                    val i = progress.getProgress() .div(contentLen)
//                    tv.text = getString(R.string.update_progress, i)
//                }
//            }
//        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progress.visibility = View.GONE
            tv.text = result
            // When download completed, update SP
            // check if download successfully
            val editor: SharedPreferences.Editor = mPrefs.edit()
            if (fileName.lowercase(Locale.getDefault()).contains("restaurants")) {
                editor.putString("RestaurantsFilePath", filePath + File.separator + fileName)
                editor.putString(
                    "RestaurantsLastModified",
                    DataRequest.instance?.restaurantsLastModified
                )
                editor.apply()
            } else {
                editor.putString("InspectionsFilePath", filePath + File.separator + fileName)
                editor.putString(
                    "InspectionsLastModified",
                    DataRequest.instance?.inspectionsLastModified
                )
                editor.apply()

                // two download task finished:
                // 1. now load new data to ListView
                ReadCSV().LoadLocalData()
                // 2. Refresh Map Activity
                val permissions = arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // update markers on maps
                    MapsFragment.updateMarkersOnMaps()
                } else {
                    Toast.makeText(
                        MyApplication.context,
                        getString(R.string.download_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // pop-up the faves updates dialog
                val faveRestaurants: String ?= mPrefs.getString("fave_restaurants", "")
                if (faveRestaurants != null) {
                    if (faveRestaurants.isNotEmpty()) {
                        // load faves update dialog
                        showFavesUpdateDialog()
                    }
                }
                // dialog finish
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        // delay 2 s to close the dialog
                        dismiss()
                    }
                }, 2000)
            }
        }


    }

    private fun showFavesUpdateDialog() {
        val mFragmentManager = requireActivity().supportFragmentManager
        val dialog = FavesUpdateFragment()
        dialog.show(mFragmentManager, "FavesUpdateList")
    }

    companion object {
        // publishProgress to update the progress Bar
        private const val PROGRESS_MAX = 0X1
        private const val UPDATE = 0X2
    }
}