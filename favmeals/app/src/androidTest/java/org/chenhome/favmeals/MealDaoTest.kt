package org.chenhome.favmeals

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import org.chenhome.favmeals.db.Meal
import org.chenhome.favmeals.db.MealDao
import org.chenhome.favmeals.db.MealDb
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class MealDaoTest  {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var appContext : Context
    lateinit var db : MealDb

    @Before
    fun setup() {
        // setup in-memory database
        Timber.plant(Timber.DebugTree())
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // In memory fixture
        db = Room.inMemoryDatabaseBuilder(
            appContext, MealDb::class.java).build()
    }

    @Test
    fun insert() {
        val meal = Meal("123","chki","Meat","i love things","thumb","youtube")
        runBlocking {
            val id = db.mealDao.insert(meal)
            assertTrue(id>0)

            val inserted = db.mealDao.getMeal("123").blockingObserve()
            Timber.d("Got meal $inserted")
            assertNotNull(inserted)
            assertEquals(meal, inserted)

            val meal2 = meal.copy(idMeal="3")
            db.mealDao.insert(meal2)

            val meals = db.mealDao.getFavMeals().blockingObserve()
            assertEquals(2, meals?.size)
            assertEquals(meal, meals?.get(0))
            assertEquals(meal2, meals?.get(1))
        }
    }


    private fun <T> LiveData<T>.blockingObserve(): T? {
        var value: T? = null
        val latch = CountDownLatch(1)

        val observer = Observer<T> { t ->
            value = t
            latch.countDown()
        }

        observeForever(observer)

        latch.await(2, TimeUnit.SECONDS)
        return value
    }

}