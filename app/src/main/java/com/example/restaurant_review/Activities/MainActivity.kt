package com.example.restaurant_review.Activities

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.restaurant_review.Fragments.DownloadFragment
import com.example.restaurant_review.Model.DataRequest
import com.example.restaurant_review.Model.ReadCVS
import com.example.restaurant_review.Nav.KeepStateNavigator
import com.example.restaurant_review.R
import com.google.android.material.navigation.NavigationView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mFragmentManager: FragmentManager? = null
    private var mAppBarConfiguration: AppBarConfiguration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setup toolbar and navigation
        setupUI()

        // per-load the get request
        DataRequest.instance

        // load local database
        ReadCVS.LoadLocalData()

        // set a timer for check update task.
        val timer = Timer()
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    object : Thread() {
                        override fun run() {
                            runOnUiThread {
                                // check is update available
                                if (isReadyToUpdate()) {
                                    // ask user to update now or late
                                    askUserUpdateNow()
                                }
                            }
                        }
                    }.start()
                }
            },
            5000
        ) // delay 5s run check update task after app launching to wait the GET request finish
    }

    private fun isReadyToUpdate(): Boolean {
        // Check the value of SharedPreferences (is first time running)
        val mPrefs = this.getSharedPreferences("mPrefs", MODE_PRIVATE)
        val restaurantsLastModified = mPrefs.getString("RestaurantsLastModified", null)
        val inspectionsLastModified = mPrefs.getString("InspectionsLastModified", null)

        // check the network state
        if (!isNetworkAvailable()) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
            return false
        }

        // check the connection with server
        // Log.d("TAG","isRestaurantsConnected:" + Boolean.toString(DataRequest.getInstance().isRestaurantsConnected()));
        // Log.d("TAG","isInspectionsConnected:" + Boolean.toString(DataRequest.getInstance().isInspectionsConnected()));
        if (!DataRequest.instance?.isRestaurantsConnected!! || !DataRequest.instance
                ?.isInspectionsConnected!!
        ) {
            Toast.makeText(
                applicationContext,
                R.string.cannot_connect_with_server,
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        // Not First Running, check is more than 20 hours sincere last update
        if (restaurantsLastModified != null && inspectionsLastModified != null) {
            //  If it more than 20 hours sincere last update
            try {
                if (isMoreThan20hours(restaurantsLastModified) or
                    isMoreThan20hours(inspectionsLastModified)) {
                    // Check is new database available
                    if (restaurantsLastModified == DataRequest.instance!!
                            .restaurantsLastModified && inspectionsLastModified == DataRequest.instance!!
                            .inspectionsLastModified
                    ) {
                        Toast.makeText(
                            applicationContext,
                            R.string.lastest_database,
                            Toast.LENGTH_SHORT
                        ).show()
                        return false
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        R.string.lastest_database,
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return true
    }

    private fun startDownload() {
        // Loading the download fragments
        val dialog = DownloadFragment()
        mFragmentManager?.let { dialog.show(it, "DownloadManager") }
    }


    private fun setupUI() {
        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        //TODO: set up navigation drawer
        // Setup Navigation Bar
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        var builder =  AppBarConfiguration.Builder()
        mAppBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_maps, R.id.nav_home
            ), drawer
        )
        //Builder(R.id.nav_maps, R.id.nav_home, R.id.nav_about).setOpenableLayout(drawer).build()
        val navController = findNavController(this, R.id.nav_host_fragment)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val navigator =
            KeepStateNavigator(this, navHostFragment!!.childFragmentManager, R.id.nav_host_fragment)
        navController.navigatorProvider.addNavigator(navigator)
        navController.setGraph(R.navigation.navigation)
        setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        setupWithNavController(navigationView, navController)

        // Setup Fragment Manager
        mFragmentManager = supportFragmentManager
    }

    private fun askUserUpdateNow() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.new_database_is_available)
        builder.setMessage(R.string.update_now)
        builder.setCancelable(true)
        builder.setNegativeButton(
            R.string.not_now
        ) { dialog, id -> dialog.cancel() }
        builder.setPositiveButton(
            R.string.OK
        ) { dialog, id -> // start Downloading.
            dialog.cancel()
            startDownload()
        }
        val askUserUpdate = builder.create()
        askUserUpdate.show()
    }

    private fun isNetworkAvailable(): Boolean {
        val manager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        return if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                true
            } else capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else false
    }

    @Throws(ParseException::class)
    private fun isMoreThan20hours(lastUpdateDate: String): Boolean {
        var localTime = lastUpdateDate
        localTime = localTime.replace("T", " ")
        val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        // convert string to Date
        val local = df.parse(localTime)
        // get the current time
        val currentTime = Date()
        // compare the time
        val diff = currentTime.time - local.time
        val hours = diff / (1000 * 60 * 60)
        // Log.d("TAG", "It has been " + hours + " hours since last update.");
        return hours > 20
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment)
        return (navigateUp(navController, mAppBarConfiguration!!)
                || super.onSupportNavigateUp())
    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main_activity, menu)
//        return super.onCreateOptionsMenu(menu!!)
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_check_update) {
            if (isReadyToUpdate()) {
                askUserUpdateNow()
                return true
            }
        }
        return false
    }
}
