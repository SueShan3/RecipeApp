package com.example.recipeapplication

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipeapplication.databinding.ActivityRecipeListBinding
import com.example.recipeapplication.model.Recipe
import com.example.recipeapplication.viewmodel.RecipeListAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val REQUEST_CODE = 2345

class RecipeListActivity : AppCompatActivity(), RecipeListAdapter.OnItemClickListener {

    private lateinit var dataBinding: ActivityRecipeListBinding
    private var database: FirebaseDatabase ?= null
    private lateinit var recipeList: ArrayList<Recipe>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_list)

        dataBinding.progressBar.visibility = View.VISIBLE

        database = FirebaseDatabase.getInstance()

        // showing the back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recipeList = ArrayList()

        val recipeType = intent.getStringExtra("rType")

        database!!.reference.child("recipeInfo")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    recipeList.clear()

                    for (data in snapshot.children) {
                        val recipe = data.getValue(Recipe::class.java)

                        if (recipe?.recipeType == recipeType) {
                            recipeList.add(recipe!!)
                        }
                    }

                    dataBinding.recycler.layoutManager =
                        LinearLayoutManager(this@RecipeListActivity)
                    dataBinding.recycler.adapter = RecipeListAdapter(
                        this@RecipeListActivity,
                        recipeList,
                        this@RecipeListActivity
                    )
                    dataBinding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onItemClick(recipe: Recipe) {
        startActivityForResult(RecipeDetailActivity.start(this, recipe), REQUEST_CODE)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}