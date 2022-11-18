package com.example.recipeapplication.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Recipe(
    var id:String ?= "",
    var recipeName: String ?= "",
    var recipeType: String ?= "",
    var recipeImg: String ?= "",
    var recipeDesc: String ?= "",
    var recipeIngredients: String ?= "",
    var recipeSteps: String ?= ""
) : Parcelable
