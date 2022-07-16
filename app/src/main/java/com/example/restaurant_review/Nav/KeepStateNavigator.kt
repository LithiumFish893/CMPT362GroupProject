package com.example.restaurant_review.Nav

import android.content.Context
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavOptions
import androidx.navigation.NavDestination
import java.lang.StringBuilder
import java.util.ArrayDeque

/**
 * @description
 * @author: Created jiangjiwei in 2019-07-23 15:05
 * @link: https://github.com/CherryLover/BlogTest/tree/closeBefore
 *
 * Imported by Pin Wen
 * Description: To override the nav for switching drawer without reload the fragments.
 * (To avoid the google map reload every time when user back to the map view)
 */
@Navigator.Name("keep_state_fragment")
class KeepStateNavigator(
    private val context: Context,
    private val manager: FragmentManager,
    private val containerId: Int
) : FragmentNavigator(
    context, manager, containerId
) {
    private val mBackStack = ArrayDeque<String>()
    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ): NavDestination? {
        val tag = destination.id.toString()
        val transaction = manager.beginTransaction()
        var initialNavigate = false
        val currentFragment = manager.primaryNavigationFragment
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        } /* else {
            initialNavigate = true;
        }*/
        var fragment = manager.findFragmentByTag(tag)
        if (fragment == null) {
            val className = destination.className
            fragment = manager.fragmentFactory.instantiate(context.classLoader, className)
            transaction.add(containerId, fragment, tag)
            initialNavigate = true
            mBackStack.add(tag)
        } else {
//            transaction.attach(fragment);
            transaction.show(fragment)
        }
        transaction.setPrimaryNavigationFragment(fragment)
        transaction.setReorderingAllowed(true)
        transaction.commitNow()
        return if (initialNavigate) destination else null
    }

    override fun popBackStack(): Boolean {
        if (mBackStack.isEmpty()) {
            return false
        }
        //        if (manager.getBackStackEntryCount() > 0) {
//            manager.popBackStack(
//                    generateBackStackName(mBackStack.size(), mBackStack.peekLast()),
//                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        } // else, we're on the first Fragment, so there's nothing to pop from FragmentManager
        val removeTag = mBackStack.removeLast()
        return doNavigate(removeTag)
    }

    fun closeMiddle(destId: Int): Boolean {
        val removeTag = destId.toString()
        val sb = StringBuilder("All stack is : [ ")
        for (s in mBackStack) {
            sb.append(s).append(" ")
        }
        sb.append("]").append(". Waiting for close is ").append(removeTag)
        Log.i(TAG, sb.toString())
        val remove = mBackStack.remove(removeTag)
        return if (remove) {
            doNavigate(removeTag)
        } else {
            false
        }
    }

    private fun doNavigate(removeTag: String): Boolean {
        val transaction = manager.beginTransaction()
        val removeFrag = manager.findFragmentByTag(removeTag)
        if (removeFrag != null) {
            transaction.remove(removeFrag)
        } else {
            return false
        }
        val showTag = mBackStack.last
        val showFrag = manager.findFragmentByTag(showTag)
        if (showFrag != null) {
            transaction.show(showFrag)
            transaction.setPrimaryNavigationFragment(showFrag)
            transaction.setReorderingAllowed(true)
            val stateSaved = manager.isStateSaved
            Log.d(TAG, "popBackStack: state saved$stateSaved")
            if (stateSaved) {
                transaction.commitNowAllowingStateLoss()
            } else {
                transaction.commitNow()
            }
        } else {
            return false
        }
        return true
    }

    companion object {
        private const val TAG = "KeepStateNavigator"
    }
}