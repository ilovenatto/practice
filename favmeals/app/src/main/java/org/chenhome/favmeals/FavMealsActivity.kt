package org.chenhome.favmeals

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import org.chenhome.favmeals.ui.fav.FavMealsFragDirections
import org.chenhome.favmeals.ui.search.SearchMealFragDirections
import timber.log.Timber

@AndroidEntryPoint
class FavMealsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        with(findViewById<BottomNavigationView>(R.id.bottom_navigation)) {
            // Setup onclicks
            setOnNavigationItemSelectedListener { menuitem ->
                Timber.d("$menuitem")
                val navCtrl = findNavController(R.id.navHostFragment)

                when (menuitem.itemId) {
                    R.id.dest_fav -> {
                        if (navCtrl.currentDestination?.id != R.id.favMealsFrag) {
                            navCtrl.navigate(SearchMealFragDirections.actionSearchMealFragToFavMealsFrag())
                            true
                        } else {
                            false
                        }
                    }
                    R.id.dest_search -> {
                        if (navCtrl.currentDestination?.id != R.id.searchMealFrag) {
                            navCtrl.navigate(FavMealsFragDirections.actionFavMealsFragToSearchMealFrag())
                            true
                        } else {
                            false
                        }
                    }

                    else -> false
                }
            }

            // set default tab to be search
            selectedItemId =R.id.dest_search
        }

    }
}