package com.example.recipeapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.recipeapplication.databinding.ActivityRecipeDetailBinding
import com.example.recipeapplication.model.Recipe
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

private const val REQUEST_CODE = 1234

class RecipeDetailActivity : AppCompatActivity() {

    companion object{
        fun start(context: Context, recipe: Recipe): Intent{
            val intent = Intent(context, RecipeDetailActivity::class.java).apply{
                putExtra("recipe_key", recipe)
            }
            return (intent)
        }
    }

    private lateinit var dataBinding: ActivityRecipeDetailBinding
    private var database: FirebaseDatabase?= null
    private lateinit var storage: FirebaseStorage
    private var recipe: Recipe ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val intent: Intent = intent
        recipe = intent.getParcelableExtra<Recipe>("recipe_key")
        dataBinding.rTitle.text = recipe?.recipeName
        dataBinding.rType.text = getString(R.string.r_type) + recipe?.recipeType
        Glide.with(this).load(recipe?.recipeImg).into(dataBinding.rImage)
        dataBinding.rIngredient.text = recipe?.recipeIngredients
        dataBinding.rSteps.text = recipe?.recipeSteps
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.edit ->{
                startActivityForResult(AddRecipeActivity.start(this, recipe!!), REQUEST_CODE)
                return true
            }
            R.id.delete ->{
                AlertDialog.Builder(this)
                    .setTitle(R.string.delete)
                    .setIcon(R.drawable.ic_warning)
                    .setMessage(R.string.delete_message)
                    .setPositiveButton(R.string.yes){
                            dialog, _->
                        deleteData(recipe?.id)
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.no){
                            dialog, _->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                return true
            }else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteImage() {
        storage = FirebaseStorage.getInstance()

        storage.getReferenceFromUrl(recipe?.recipeImg!!)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "onSuccess: deleted file");
            }
            .addOnFailureListener{
                Log.d(TAG, "onFailure: did not delete file");
            }
    }

    private fun deleteData(id: String?) {
        database = FirebaseDatabase.getInstance()

        database!!.reference.child("recipeInfo")
            .child(id!!)
            .removeValue()
            .addOnSuccessListener {
                deleteImage()
                finish()
                Toast.makeText(this, R.string.success_deleted, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this, R.string.fail_deleted, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val recipeData = data?.getParcelableExtra<Recipe>("recipeModel")

            dataBinding.rTitle.text = recipeData?.recipeName
            dataBinding.rType.text = "Recipe Type: " + recipeData?.recipeType
            Glide.with(this).load(recipeData?.recipeImg).into(dataBinding.rImage)
            dataBinding.rIngredient.text = recipeData?.recipeIngredients
            dataBinding.rSteps.text = recipeData?.recipeSteps
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}