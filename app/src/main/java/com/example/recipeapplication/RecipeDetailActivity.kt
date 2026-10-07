package com.example.recipeapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.recipeapplication.data.AppDatabase
import com.example.recipeapplication.data.ImageStorage
import com.example.recipeapplication.databinding.ActivityRecipeDetailBinding
import com.example.recipeapplication.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context, recipe: Recipe): Intent {
            val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                putExtra("recipe_key", recipe)
            }
            return (intent)
        }
    }

    private lateinit var dataBinding: ActivityRecipeDetailBinding
    private var recipe: Recipe? = null

    // Replaces startActivityForResult + onActivityResult
    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val updated = result.data?.getParcelableExtra<Recipe>("recipeModel")
            if (updated != null) {
                recipe = updated
                showRecipe(updated)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val intent: Intent = intent
        recipe = intent.getParcelableExtra("recipe_key")
        recipe?.let {
            showRecipe(it)
        }
    }

    private fun showRecipe(r: Recipe) {
        dataBinding.rTitle.text = r.recipeName
        dataBinding.rType.text = getString(R.string.r_type, r.recipeType)
        Glide.with(this).load(r.recipeImg).into(dataBinding.rImage)
        dataBinding.rIngredient.text = r.recipeIngredients
        dataBinding.rSteps.text = r.recipeSteps
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit -> {
                recipe?.let {
                    editLauncher.launch(AddRecipeActivity.start(this, it))
                }
                true
            }

            R.id.delete -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.delete)
                    .setIcon(R.drawable.ic_warning)
                    .setMessage(R.string.delete_message)
                    .setPositiveButton(R.string.yes) { dialog, _ ->
                        deleteRecipe()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.no) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteRecipe() {
        val r = recipe ?: return
        lifecycleScope.launch {
            AppDatabase.getInstance(applicationContext).recipeDao().delete(r)
            withContext(Dispatchers.IO) { ImageStorage.delete(r.recipeImg) }
            Toast.makeText(this@RecipeDetailActivity, R.string.success_deleted, Toast.LENGTH_SHORT)
                .show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}