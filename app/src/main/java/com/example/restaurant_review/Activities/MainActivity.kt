package com.example.restaurant_review.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.util.Util
import com.example.restaurant_review.Model.HealthInspectionHtmlScraper
import com.example.restaurant_review.Nav.KeepStateNavigator
import com.example.restaurant_review.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var mFragmentManager: FragmentManager? = null
    private var mAppBarConfiguration: AppBarConfiguration? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        // setup toolbar and navigation
        setupUI()
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

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        val navController = findNavController(R.id.nav_host_fragment)
        return (navController.navigateUp( mAppBarConfiguration!!))
    }
}