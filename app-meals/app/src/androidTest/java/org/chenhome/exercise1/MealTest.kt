package org.chenhome.exercise1

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.chenhome.exercise1.model.MealService
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class MealTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        Timber.plant(Timber.DebugTree())
        Timber.d("foobar")
    }

    @Test
    fun test() {
        Timber.d("running")
        runBlocking {

            val meals =MealService.mealApi.getMeals("Seafood")
            Timber.d("meals $meals")

            val cats = MealService.mealApi.getCategories()
            Timber.d("cats $cats")
        }
    }
}