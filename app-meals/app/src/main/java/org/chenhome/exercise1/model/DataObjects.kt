package org.chenhome.exercise1.model

import android.net.Uri
import com.squareup.moshi.Json

data class MealStub (
    @Json(name = "idMeal") val id:String,
    @Json(name = "strMeal") val title:String,
    @Json(name = "strMealThumb") val thumbUri:Uri // how convert string to uri with Moshi TODO
)

data class MealStubs (
    val meals : List<MealStub>
)

data class Meals (
    val meals: List<Meal>
)

data class Categories (
    @Json(name="meals") val categories: List<Category>
)

data class Meal (
    @Json(name = "idMeal") val id:String,
    @Json(name = "strMeal") val title:String,
    @Json(name = "strMealThumb") val thumbUri:Uri,
    @Json(name = "strInstructions") val descrip:String,
    @Json(name = "strYoutube") val youtubeUri:Uri,
    @Json (name = "strCategory") val category:String
)

data class Category (
    @Json(name="strCategory") val name:String
)
