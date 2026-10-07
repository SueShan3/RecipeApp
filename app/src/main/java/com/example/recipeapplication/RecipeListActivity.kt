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

class RecipeListActivity : AppCompatActivity(), RecipeListAdapter.OnItemClickListener {

    private lateinit var dataBinding: ActivityRecipeListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_list)

        dataBinding.progressBar.visibility = View.VISIBLE

        // showing the back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                    dataBinding.emptyText.visibility =
                        if (list.isEmpty()) View.VISIBLE else View.GONE
                    dataBinding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onItemClick(recipe: Recipe) {
        startActivity(RecipeDetailActivity.start(this, recipe))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}