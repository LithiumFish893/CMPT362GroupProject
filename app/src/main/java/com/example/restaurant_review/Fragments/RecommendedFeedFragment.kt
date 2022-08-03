package com.example.restaurant_review.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RecommendedFeedFragment : Fragment() {
    private lateinit var postList: ArrayList<SocialMediaPostModel>
    private lateinit var adapter: SocialMediaPostAdapter
    private lateinit var progressBar: LinearLayout

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        postList = arrayListOf()
        val pView = inflater.inflate(R.layout.sm_fragment_recommended_feed, container)
        val recyclerView : RecyclerView = pView.findViewById(R.id.testRV)
        adapter = SocialMediaPostAdapter(postList)
        progressBar = pView.findViewById(R.id.sm_recommended_progress_bar)
        //val database = SocialMediaPostDatabase.getInstance(requireContext())
        //val repository = SocialMediaPostRepository(database)
        //val factory = SocialMediaPostViewModelFactory(repository)
        //val viewModel = ViewModelProvider(this, factory).get(SocialMediaPostViewModel::class.java)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        val start = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == SocialMediaPostActivity.RESULT_CODE && it.data != null){
                val intent = it.data!!
                val post = Util.bundleToPost(intent.extras!!)
                //viewModel.insert(post)
            }
        }
        val button : Button = pView.findViewById(R.id.button)
        button.setOnClickListener {
            val intent = Intent(requireContext(), SocialMediaPostActivity::class.java)
            start.launch(intent)
        }
        val button2 : Button = pView.findViewById(R.id.button_delete)
        button2.setOnClickListener {
            //viewModel.deleteAllEntries()
            //viewModel.deleteAllComments()
        }
        return pView
    }

    override fun onResume() {
        super.onResume()
        println("resumed")
        updateFirebaseDb()
    }

    // get all social media posts from the database
    fun updateFirebaseDb () {
        val fireDatabase = Firebase.database
        val socialMediaPosts = fireDatabase.reference.child("socialMediaPost")
        socialMediaPosts.get().addOnSuccessListener { dataSnapshot ->
            postList = arrayListOf()
            dataSnapshot.children.forEach { users ->
                users.children.forEach { userId ->
                    userId.child("posts").children.forEach { postId ->
                        val postIdVal = postId.key!!.toInt()
                        val post = postId.value as HashMap<String, Object>
                        val list = arrayListOf<String>()
                        // transfer imglist to the list
                        if (postId.child("imgList").value != null) {
                            (postId.child("imgList").value as List<String>).forEach{ e ->
                                list.add(e)
                            }
                        }
                        val postUpdate: SocialMediaPostModel = SocialMediaPostModel(
                            id = postIdVal,
                            userId = userId.key!! as String,
                            timeStamp = (post["timeStamp"] as Long as Number).toLong(),
                            locationLat = (post["locationLat"] as Number).toDouble(),
                            locationLong = (post["locationLong"] as Number).toDouble(),
                            likeCount = (post["likeCount"] as Number).toInt(),
                            title = post["title"] as String,
                            textContent = post["content"] as String,
                            imgList = list
                        )
                        postList.add(postUpdate)
                    }
                }
            }
            progressBar.visibility = View.GONE
            adapter.updateList(postList)
        }

    }
}