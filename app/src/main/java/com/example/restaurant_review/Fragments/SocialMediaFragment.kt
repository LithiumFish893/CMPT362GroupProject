package com.example.restaurant_review.Fragments

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
import com.example.restaurant_review.Activities.SocialMediaPostActivity
import com.example.restaurant_review.local_database.*
import com.example.restaurant_review.Fragments.NearbyFeedFragment
import com.example.restaurant_review.Fragments.RecommendedFeedFragment
import com.example.restaurant_review.Fragments.SubscribedFeedFragment
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.Views.UiFragmentStateAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SocialMediaFragment : Fragment() {
    private lateinit var nearbyFeedFragment: NearbyFeedFragment
    private lateinit var recommendedFeedFragment: RecommendedFeedFragment
    private lateinit var subscribedFeedFragment: SubscribedFeedFragment
    private lateinit var fragmentList: ArrayList<Fragment>
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var uiFragmentStateAdapter: FragmentStateAdapter
    private lateinit var tabLayoutMediator: TabLayoutMediator
    private lateinit var tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy
    private var TAB_TEXT = arrayListOf<String>("Subscribed", "Recommended", "Nearby")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val pView = inflater.inflate(R.layout.sm_fragment_social_media, container, false)
        // Check permissions to take photo
        Log.e("tag", "social media : is created now")
        Util.checkPermissions(requireActivity())

        // Initialize the fragments and add them to the list
        nearbyFeedFragment = NearbyFeedFragment()
        recommendedFeedFragment = RecommendedFeedFragment()
        subscribedFeedFragment = SubscribedFeedFragment()
        fragmentList = ArrayList()
        fragmentList.add(subscribedFeedFragment)
        fragmentList.add(recommendedFeedFragment)
        fragmentList.add(nearbyFeedFragment)

        // Add the fragment info to the state adapter
        // Then add state adapter to view pager so it can show the fragments
        tabLayout = pView.findViewById(R.id.mainTabLayout)
        viewPager = pView.findViewById(R.id.mainViewPager)
        // fix random crashes https://stackoverflow.com/questions/44571895/staggeredgridlayoutmanager-calculatecachedstart-indexoutofboundsexception
        viewPager.offscreenPageLimit = 10
        uiFragmentStateAdapter = UiFragmentStateAdapter(requireActivity(), fragmentList)
        viewPager.adapter = uiFragmentStateAdapter

        // Display the tabs
        tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy {
                tab: TabLayout.Tab, position: Int ->
            tab.text = TAB_TEXT[position]
        }
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager, tabConfigurationStrategy)
        tabLayoutMediator.attach()

        val database = SocialMediaPostDatabase.getInstance(requireContext())
        val repository = SocialMediaPostRepository(database)
        val factory = SocialMediaPostViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory).get(SocialMediaPostViewModel::class.java)
        val start = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == SocialMediaPostActivity.RESULT_CODE && it.data != null){
                val intent = it.data!!
                val post = Util.bundleToPost(intent.extras!!)
                viewModel.insert(post)
            }
        }
        val button : FloatingActionButton = pView.findViewById(R.id.button)
        button.setOnClickListener {
            val intent = Intent(requireContext(), SocialMediaPostActivity::class.java)
            start.launch(intent)
            requireActivity().overridePendingTransition(R.anim.slide_from_bottom_right, androidx.appcompat.R.anim.abc_fade_out)
        }
        return pView
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }
}
