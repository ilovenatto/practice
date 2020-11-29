package org.chenhome.favmeals.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "favmeals")
data class Meal (
    @PrimaryKey
    val idMeal:String,
    val strMeal:String, //title
val strCategory : String,
    val strInstructions:String,
    val strMealThumb:String,
    val strYoutube:String
    )


@Dao
interface MealDao {
    @Query("SELECT * FROM favmeals WHERE idMeal=:mealId")
    fun getMeal(mealId:String) : LiveData<Meal>

    @Insert
    suspend fun insert(meal:Meal) : Long // return db id

    @Query("SELECT * FROM favmeals")
    fun getFavMeals() : LiveData<List<Meal>>
}


@Database(entities = [Meal::class], version = 1, exportSchema = false)
abstract class MealDb : RoomDatabase() {
    abstract val mealDao: MealDao

    // The companion object allows clients to access the methods for creating or
    // getting the database without instantiating the class.
    companion object {
        //The INSTANCE variable will keep a reference to the database,
        // once one has been created. This helps you avoid repeatedly
        // opening connections to the database, which is expensive.
        @Volatile
        private var INSTANCE: MealDb? = null

        fun getInstance(context: Context) : MealDb {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext,
                        MealDb::class.java,
                        "sleep_history_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
