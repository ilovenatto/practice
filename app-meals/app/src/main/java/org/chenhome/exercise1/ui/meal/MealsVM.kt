package org.chenhome.exercise1.ui.meal

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.chenhome.exercise1.model.*
import timber.log.Timber

class MealsVM @ViewModelInject constructor() : ViewModel() {

    // IO scope
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelScope.coroutineContext)

    private var _meals = MutableLiveData<MealStubs>()
    val meals : LiveData<List<MealStub>>? = Transformations.map(_meals) {
        it?.let {
            it.meals
        }
    }

    private var _categories = MutableLiveData<Categories>()
    val categories = Transformations.map(_categories) {
        it?.let {
            it.categories
        }
    }

    var current = MutableLiveData<String>("Seafood")


    fun load() {
        // run async
        ioScope.launch {
            with(MealService.mealApi) {
                val cats = getCategories()
                Timber.d("Getting categories $cats")
                _categories.value = cats

                    val meals = getMeals(current.value!!)
                Timber.d("Got meals $meals")
                _meals.value = meals
            }


        }

    }
}