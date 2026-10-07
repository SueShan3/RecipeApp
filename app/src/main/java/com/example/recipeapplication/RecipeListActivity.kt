package com.example.recipeapplication

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipeapplication.data.AppDatabase
import com.example.recipeapplication.databinding.ActivityRecipeListBinding
import com.example.recipeapplication.model.Recipe
import com.example.recipeapplication.viewmodel.RecipeListAdapter
import kotlinx.coroutines.launch

private const val REQUEST_CODE = 2345

class RecipeListActivity : AppCompatActivity(), RecipeListAdapter.OnItemClickListener {

    private lateinit var dataBinding: ActivityRecipeListBinding
    private lateinit var recipeList: ArrayList<Recipe>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_list)

        dataBinding.progressBar.visibility = View.VISIBLE

        // showing the back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recipeList = ArrayList()

        val recipeType = intent.getStringExtra("rType") ?: return
        val dao = AppDatabase.getInstance(this).recipeDao()

        dataBinding.recycler.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dao.getByType(recipeType).collect { list ->
                    dataBinding.recycler.adapter =
                        RecipeListAdapter(
                            this@RecipeListActivity,
                            ArrayList(list),
                            this@RecipeListActivity
                        )
                    dataBinding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onItemClick(recipe: Recipe) {
        startActivityForResult(RecipeDetailActivity.start(this, recipe), REQUEST_CODE)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}