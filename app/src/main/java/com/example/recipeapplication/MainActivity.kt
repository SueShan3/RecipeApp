package com.example.recipeapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.recipeapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var dataBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.recipe_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            dataBinding.recipeTypeSpinner.adapter = adapter
            dataBinding.recipeTypeSpinner.onItemSelectedListener = this
        }

        dataBinding.addingBtn.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
        var recipeType = parent?.getItemAtPosition(pos).toString()

        dataBinding.search.setOnClickListener{
            when(pos){
                0->{
                    //Nothing to do
                }
                else->{
                    val intent = Intent(this, RecipeListActivity::class.java)
                    intent.putExtra("rType", recipeType)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Toast.makeText(this, R.string.nothing_select, Toast.LENGTH_SHORT).show()
    }
}