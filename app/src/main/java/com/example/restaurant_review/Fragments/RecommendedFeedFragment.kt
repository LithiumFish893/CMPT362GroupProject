package com.example.restaurant_review.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.restaurant_review.Activities.SocialMediaPostActivity
import com.example.restaurant_review.local_database.*
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.Views.SocialMediaPostAdapter
import com.example.restaurant_review.local_database.SocialMediaPostDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class RecommendedFeedFragment : Fragment() {
    @OptIn(ExperimentalPagingApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        val pView = inflater.inflate(R.layout.sm_fragment_recommended_feed, container)
        val recyclerView : RecyclerView = pView.findViewById(R.id.testRV)
        val adapter = SocialMediaPostAdapter()
        val database = SocialMediaPostDatabase.getInstance(requireContext())
        val repository = SocialMediaPostRepository(database)
        val factory = SocialMediaPostViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory).get(SocialMediaPostViewModel::class.java)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        lifecycleScope.launch {
            viewModel.getAllPosts().distinctUntilChanged().collectLatest {
                adapter.submitData(it)
            }
        }
        val start = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == SocialMediaPostActivity.RESULT_CODE && it.data != null){
                val intent = it.data!!
                val post = Util.bundleToPost(intent.extras!!)
                viewModel.insert(post)
            }
        }
        val button : Button = pView.findViewById(R.id.button)
        button.setOnClickListener {
            val intent = Intent(requireContext(), SocialMediaPostActivity::class.java)
            start.launch(intent)
        }
        val button2 : Button = pView.findViewById(R.id.button_delete)
        button2.setOnClickListener {
            viewModel.deleteAllEntries()
            viewModel.deleteAllComments()
        }
        return pView
    }
}