package org.chenhome.favmeals

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import org.chenhome.favmeals.service.MealService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class MealServiceTest {

    val api = MealService.api

    @Before
    fun before() {
        Timber.plant(Timber.DebugTree())
    }

    @Test
    fun search() {
        runBlocking {
            val res = api.search("chicken")
            assertTrue(res.meals.isNotEmpty())
            val first = res.meals[0]
            Timber.d("Got result $first")
            assertTrue(first.idMeal.isNotBlank())

            val retrieved = api.getMeal(first.idMeal)
            assertTrue(retrieved.meals.size ==1)
            assertEquals(first, retrieved.meals[0])
        }
    }
}