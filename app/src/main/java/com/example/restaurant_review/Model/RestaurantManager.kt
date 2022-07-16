package com.example.restaurant_review.Model

import java.util.ArrayList

/**
 * RestaurantManager Class Implementation
 *
 * Singleton supported
 * To store an ArrayList of restaurant data in class.
 */
class RestaurantManager constructor() {
    val allRestaurants: ArrayList<Restaurant>
    fun addRestaurant(r: Restaurant) {
        allRestaurants.add(r)
    }

    fun getRestaurant(id: String?): Restaurant? {
        for (r: Restaurant in allRestaurants) {
            if (r.id == id) {
                return r
            }
        }
        return null
    }

    fun clear() {
        if (!isEmpty) {
            allRestaurants.clear()
        }
    }

    val isEmpty: Boolean
        get() {
            return allRestaurants.size == 0
        }

    companion object {
        var instance: RestaurantManager? = null
            get() {
                if (field == null) {
                    field = RestaurantManager()
                }
                return field
            }
            private set
    }

    init {
        allRestaurants = ArrayList()
    }
}