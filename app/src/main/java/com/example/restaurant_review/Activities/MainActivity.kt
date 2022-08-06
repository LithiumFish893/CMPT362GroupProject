package com.example.restaurant_review.Activities

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.util.Util
import com.example.restaurant_review.Fragments.DownloadFragment
import com.example.restaurant_review.Fragments.MapsFragment
import com.example.restaurant_review.Model.DataRequest
import com.example.restaurant_review.Model.ReadCSV
import com.example.restaurant_review.Nav.KeepStateNavigator
import com.example.restaurant_review.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private var mFragmentManager: FragmentManager? = null
    private var mAppBarConfiguration: AppBarConfiguration? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var greeting: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        // setup toolbar and navigation
        val t0 = Calendar.getInstance().timeInMillis
        setupUI()

        val t1 = Calendar.getInstance().timeInMillis
        // per-load the get request
        DataRequest.instance
        val t2 = Calendar.getInstance().timeInMillis
        // load local database - takes 3000 ms!!!
        CoroutineScope(Dispatchers.IO).launch{ ReadCSV().LoadLocalData() }
        val t3 = Calendar.getInstance().timeInMillis
        println("Setup UI took ${t1-t0} ms, Data request took ${t2-t1} ms, readcsv took ${t3-t2} ms")
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

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser == null){
            println("Debug: Current user == null")
            startActivity(Intent(this, LoginActivity::class.java))
        }else{
            println("Debug: Current user exist")
            val username = auth.currentUser?.email.toString()
        }
    }

    fun isReadyToUpdate(): Boolean {
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
        //Setup Navigation Bar
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        mAppBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_maps, R.id.nav_home, R.id.nav_social_media, R.id.nav_vr_tour, R.id.nav_about, R.id.profile
            ), drawer
        )
        val navController = findNavController( R.id.nav_host_fragment)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val navigator =
            KeepStateNavigator(this, navHostFragment!!.childFragmentManager, R.id.nav_host_fragment)
        navController.navigatorProvider.addNavigator(navigator)
        navController.setGraph(R.navigation.navigation)
        setupActionBarWithNavController( navController, mAppBarConfiguration!!)
        navigationView.setupWithNavController(navController)
        mFragmentManager = supportFragmentManager

        val search = findViewById<FloatingSearchView>(R.id.floating_search_bar)
        val hostFragmentFrameLayout : FrameLayout = findViewById(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener {
            _, dest, _ ->
                // add more views here to make the search bar gone
                if (dest.id == R.id.nav_social_media || dest.id == R.id.profile || dest.id == R.id.nav_vr_tour){
                    search.visibility = View.GONE
                    // hacky way to remove the search bar
                    val params = hostFragmentFrameLayout.layoutParams as FrameLayout.LayoutParams
                    params.topMargin = 0
                    hostFragmentFrameLayout.layoutParams = params
                }
                else{
                    search.visibility = View.VISIBLE
                    val params = hostFragmentFrameLayout.layoutParams as FrameLayout.LayoutParams
                    params.topMargin = Util.dpToPx(70)
                    hostFragmentFrameLayout.layoutParams = params
                }
        }


        val menu = navigationView.menu
        val logoutButton = menu.findItem(R.id.log_out)
        val profileButton = menu.findItem(R.id.profile)

        logoutButton.setOnMenuItemClickListener(){
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            true
        }
        profileButton.setOnMenuItemClickListener(){
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            true
        }
        // Setup Fragment Manager

    }

    fun askUserUpdateNow() {
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
        super.onSupportNavigateUp()
        val navController = findNavController(R.id.nav_host_fragment)
        return (navController.navigateUp( mAppBarConfiguration!!))
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_main_activity, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Toast.makeText(this, "Main activity", Toast.LENGTH_SHORT).show()
//        if (item.itemId == R.id.menu_check_update) {
//            if (isReadyToUpdate()) {
//                askUserUpdateNow()
//                return true
//            }
//        }
//        return false
//    }
}