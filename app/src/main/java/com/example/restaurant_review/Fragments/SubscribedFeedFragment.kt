package com.example.restaurant_review.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.SocialMediaPostAdapter
import com.example.restaurant_review.local_database.SocialMediaPostModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Fragment that shows all posts in the user's Subscribed feed for
 * social media.
 */
class SubscribedFeedFragment : Fragment() {

    private lateinit var subscribedList: ArrayList<String>
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
        subscribedList = arrayListOf()
        val pView = inflater.inflate(R.layout.sm_fragment_recommended_feed, container)
        recyclerView = pView.findViewById(R.id.testRV)
        adapter = SocialMediaPostAdapter(postList)
        progressBar = pView.findViewById(R.id.sm_recommended_progress_bar)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        listenToFirebase()

        val firebaseAuth = Firebase.auth
        val firebaseDatabase = Firebase.database
        val currentUser = firebaseAuth.currentUser!!.uid
        val subscriptionRef = firebaseDatabase.reference.child("subscriptions").child(currentUser)
        subscriptionRef.get().addOnSuccessListener {
            updateSubscriptions(it)
        }
        subscriptionRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateSubscriptions(snapshot)
                updateFirebaseDb()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        return pView
    }

    fun updateSubscriptions (snapshot: DataSnapshot){
        subscribedList = arrayListOf()
        snapshot.children.forEach {
            subscribedList.add(it.key!!)
        }
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
            postList = postList.filter { subscribedList.contains(it.userId) } as ArrayList<SocialMediaPostModel>
            adapter.updateList(postList)
            recyclerView.adapter = adapter
        }
    }

}