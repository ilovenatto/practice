package org.chenhome.favmeals.service

import android.net.Uri
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.chenhome.favmeals.db.Meal
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

// Singleton
object MealService {
    // convert String to URI
    private val moshi = Moshi.Builder()
        //.add(UriConverter())
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.themealdb.com/api/json/v1/1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api : MealApi by lazy {
        retrofit.create()
    }
}

private class UriConverter {
    @FromJson
    fun fromJson(uriStr: String): Uri? {
        return Uri.parse(uriStr)
    }

    @ToJson
    fun toJson(uri: Uri): String {
        return uri.toString()
    }
}

data class Meals (
    val meals: List<Meal>
)

interface MealApi {
    @GET("lookup.php")
    suspend fun getMeal(@Query("i") idMeal:String) : Meals

    @GET("search.php")
    suspend fun search(@Query("s") term:String) : Meals
}