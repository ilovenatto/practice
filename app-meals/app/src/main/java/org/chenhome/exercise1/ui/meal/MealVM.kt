package org.chenhome.exercise1.ui.meal

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.chenhome.exercise1.model.Meal
import org.chenhome.exercise1.model.MealService
import org.chenhome.exercise1.model.Meals

class MealVM @ViewModelInject constructor() : ViewModel() {
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelScope.coroutineContext)

    private var _meal = MutableLiveData<Meals>()
    val meal: LiveData<Meal?>
        get() = Transformations.map(_meal) {
            it?.meals?.let { meals ->
                meals.fold(meals[0], { _, next ->
                    next
                })
            }
        }


    fun load(mealId: String) {
        ioScope.launch {
            _meal.value = MealService.mealApi.getMeal(mealId)

        }
    }
}