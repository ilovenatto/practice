package org.chenhome.favmeals.ui.search

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.chenhome.favmeals.db.Meal
import org.chenhome.favmeals.service.MealService
import timber.log.Timber

class SearchMealsVM @ViewModelInject constructor() : ViewModel() {
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelScope.coroutineContext)
    var search = MutableLiveData<String>("")

    private var _meals = MutableLiveData<List<Meal>>()
    val meals : LiveData<List<Meal>>
        get() = _meals

    fun load() {
        ioScope.launch {

            val meals = MealService.api.search(search.value ?: "")
            Timber.d("Loading ${meals.meals}")
            _meals.postValue(meals.meals)
        }
    }
}