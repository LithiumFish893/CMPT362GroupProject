package com.example.restaurant_review.Fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.restaurant_review.Activities.SocialMediaPostActivity
import com.example.restaurant_review.R
import com.example.restaurant_review.Util.Util
import com.example.restaurant_review.Views.SocialMediaPostAdapter
import com.example.restaurant_review.local_database.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Exception


class RecommendedFeedFragment : Fragment() {
    
    private lateinit var postList: ArrayList<SocialMediaPostModel>
    private lateinit var adapter: SocialMediaPostAdapter
    private lateinit var progressBar: LinearLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        postList = arrayListOf()
        listenToFirebase()
        val pView = inflater.inflate(R.layout.sm_fragment_recommended_feed, container)
        recyclerView = pView.findViewById(R.id.testRV)
        adapter = SocialMediaPostAdapter(postList)
        progressBar = pView.findViewById(R.id.sm_recommended_progress_bar)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        return pView
    }

    override fun onResume() {
        super.onResume()
        println("resumed")
        //updateFirebaseDb()
    }

    fun listenToFirebase () {
        val fireDatabase = Firebase.database
        val socialMediaPosts = fireDatabase.reference.child("socialMediaPost")
        socialMediaPosts.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    updateFirebaseDb()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            }
        )
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
                        try {
                            val postUpdate: SocialMediaPostModel = SocialMediaPostModel(
                                id = postIdVal,
                                userId = userId.key!!,
                                timeStamp = (post["timeStamp"] as Long as Number).toLong(),
                                locationLat = (post["locationLat"] as Number).toDouble(),
                                locationLong = (post["locationLong"] as Number).toDouble(),
                                locationName = if (post.containsKey("locationName")) post["locationName"] as String else "",
                                likeCount = (post["likeCount"] as Number).toInt(),
                                title = post["title"] as String,
                                textContent = post["content"] as String,
                                imgList = list
                            )
                            postList.add(postUpdate)
                        }
                        catch (e: Exception){}
                    }
                }
            }
            progressBar.visibility = View.GONE
            postList.sortBy {
                compare(it)
            }
            adapter.updateList(postList)
            recyclerView.adapter = adapter
        }
    }

    open fun compare(a: SocialMediaPostModel) : Double{
        // sort by nothing by default
        return 1.0
    }

    fun queryPostsFromLocalDb () {
        postList = arrayListOf()

    }


}