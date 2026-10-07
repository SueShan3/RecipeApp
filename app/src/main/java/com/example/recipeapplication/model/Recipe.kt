package com.example.recipeapplication.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var recipeName: String,
    var recipeType: String,
    var recipeImg: String,
    var recipeDesc: String,
    var recipeIngredients: String,
    var recipeSteps: String,
) : Parcelable
