package org.chenhome.favmeals.ui.fav

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.chenhome.favmeals.db.Meal
import org.chenhome.favmeals.db.MealDb

class FavMealsVM @ViewModelInject constructor(@ApplicationContext val context: Context) : ViewModel() {
    val favMeals : LiveData<List<Meal>>
        get() =
            MealDb.getInstance(context).mealDao.getFavMeals()

}