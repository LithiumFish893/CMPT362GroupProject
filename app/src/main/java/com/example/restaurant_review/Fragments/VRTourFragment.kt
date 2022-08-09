package com.example.restaurant_review.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurant_review.Activities.CreateVRTourActivity
import com.example.restaurant_review.Activities.CreateVRTourActivity.Companion.NAME_KEY
import com.example.restaurant_review.Activities.VRViewActivity
import com.example.restaurant_review.Model.RestaurantTour
import com.example.restaurant_review.Model.TourNode
import com.example.restaurant_review.R
import com.example.restaurant_review.Views.TourAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Fragment that lets the user choose if they want to view a tour
 * or upload their own tour.
 */
class VRTourFragment : Fragment() {
    private lateinit var viewVrRv: RecyclerView
    private lateinit var startVrButton: Button
    private lateinit var submitVrButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tourArrayList: ArrayList<Pair<String, RestaurantTour>>
    private var currIntent: Intent? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val pView = inflater.inflate(R.layout.fragment_vr_tour, container, false)
        viewVrRv = pView.findViewById(R.id.view_vr_tour_recycler_view)
        startVrButton = pView.findViewById(R.id.view_vr_tour_button)
        submitVrButton = pView.findViewById(R.id.submit_vr_tour_button)
        progressBar = pView.findViewById(R.id.vr_progress_bar)
        tourArrayList = arrayListOf<Pair<String, RestaurantTour>>()
        val database = Firebase.database.reference
        val tourRef = database.child("tours")
        tourRef.addValueEventListener (object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onSnapshotChanged(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        viewVrRv.adapter = TourAdapter(tourArrayList){
            val pair = (viewVrRv.adapter as TourAdapter).get(it)
            val name = pair.first
            val tour = pair.second
            val intent = Intent(requireContext(), VRViewActivity::class.java)
            intent.putExtra(CreateVRTourActivity.TOUR_KEY, tour)
            intent.putExtra(NAME_KEY, name)
            currIntent = intent
            startVrButton.backgroundTintList = resources.getColorStateList(
                R.color.bootstrap_green,
                null
            )
            startVrButton.text = "${name}'s VR Tour"
            startVrButton.setOnClickListener {
                startActivity(currIntent)
            }
        }
        viewVrRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        submitVrButton.setOnClickListener {
            val intent = Intent(requireContext(), CreateVRTourActivity::class.java)
            startActivity(intent)
        }
        return pView
    }

    fun onSnapshotChanged(snapshot: DataSnapshot){
        val database = Firebase.database.reference
        tourArrayList = arrayListOf()
        snapshot.children.forEachIndexed { index, it ->
            val uid = it.key
            val tour = it.value as List<List<String>>
            val sz = tour.size
            val tourGrid = Array<Array<TourNode?>>(sz){Array<TourNode?>(sz){null} }
            for (i in tour.indices){
                for (j in tour[i].indices){
                    var res: TourNode? = null
                    if (tour[i][j] != "null"){
                        val p = tour[i][j].split("~")
                        val p1 = p[0]
                        val p2 = p[1]
                        val name = p1.replace("name=", "")
                        val image = p2.replace("image=", "")
                        res = TourNode(name, image)
                    }
                    tourGrid[i][j] = res
                }
            }
            val userRef = database.child("user").child(uid!!).child("username")

            userRef.get().addOnCompleteListener { getUName ->

                if (getUName.isSuccessful) {
                    tourArrayList.add(Pair(getUName.result.value.toString(), RestaurantTour(tourGrid)))
                } else {

                }
                // hacky way to see if we're at the last item
                if (index == snapshot.children.toList().size-1){
                    progressBar.visibility = View.GONE
                    (viewVrRv.adapter as TourAdapter).updateList(tourArrayList)
                    //viewVrRv.adapter!!.notifyDataSetChanged()
                }
            }
        }
    }
}