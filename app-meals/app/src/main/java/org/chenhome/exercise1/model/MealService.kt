package org.chenhome.exercise1.model

import android.net.Uri
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


// singleton
object MealService {
    private val moshi = Moshi.Builder()
        .add(UriConverter())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.themealdb.com/api/json/v1/1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val mealApi : MealApi by lazy {
        retrofit.create(MealApi::class.java)
    }
}

interface MealApi {
    @GET("filter.php")
    suspend fun getMeals(@Query("c") category:String) : MealStubs

    @GET("list.php?c=list")
    suspend fun getCategories() : Categories

    @GET("lookup.php")
    suspend fun getMeal(@Query("i") mealId:String) : Meals
}


private class UriConverter {
    @FromJson fun fromJson(uriStr:String) : Uri? {
        return Uri.parse(uriStr)
    }
    @ToJson fun toJson(uri:Uri) : String {
        return uri.toString()
    }
}