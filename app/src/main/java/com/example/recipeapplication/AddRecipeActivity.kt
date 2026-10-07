package com.example.recipeapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.recipeapplication.data.AppDatabase
import com.example.recipeapplication.data.ImageStorage
import com.example.recipeapplication.databinding.ActivityAddRecipeBinding
import com.example.recipeapplication.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val REQUEST_CODE = 45

class AddRecipeActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    companion object {
        fun start(context: Context, recipe: Recipe): Intent {
            val intent = Intent(context, AddRecipeActivity::class.java).apply {
                putExtra("pre_data", recipe)
            }
            return (intent)
        }
    }

    private lateinit var dataBinding: ActivityAddRecipeBinding
    private var rType = ""
    private var selectedImg: Uri? = null
    private var recipeData: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_recipe)

        recipeData = intent.getParcelableExtra<Recipe>("pre_data")

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.recipe_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            dataBinding.recipeType.adapter = adapter
            dataBinding.recipeType.onItemSelectedListener = this
        }

        if (recipeData != null) {
            dataBinding.recipeNameEt.setText(recipeData?.recipeName)
            dataBinding.recipeDescEt.setText(recipeData?.recipeDesc)
            val spinnerPosition: Int = adapter.getPosition(recipeData?.recipeType)
            dataBinding.recipeType.setSelection(spinnerPosition)
            Glide.with(this).load(recipeData?.recipeImg).into(dataBinding.recipeImg)
            dataBinding.recipeIngredientsEt.setText(recipeData?.recipeIngredients)
            dataBinding.recipeStepsEt.setText(recipeData?.recipeSteps)

            dataBinding.btnAdd.text = getString(R.string.update)
        }

        dataBinding.selectImg.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }

        dataBinding.cancelImage.setOnClickListener {
            selectedImg = null
            dataBinding.recipeImg.setImageURI(null)
            dataBinding.cancelImage.visibility = View.GONE
        }

        dataBinding.btnAdd.setOnClickListener {
            dataBinding.progressBar.visibility = View.VISIBLE
            checkValue()
        }

        dataBinding.btnCancel.setOnClickListener {
            onBackPressed()
        }
    }

    private fun checkValue() {
        val name = dataBinding.recipeNameEt.text.toString()
        val desc = dataBinding.recipeDescEt.text.toString()
        val ingredients = dataBinding.recipeIngredientsEt.text.toString()
        val steps = dataBinding.recipeStepsEt.text.toString()
        val hasImage = selectedImg != null || recipeData != null

        if (name.isEmpty() || desc.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || rType == getString(
                R.string.select
            ) || !hasImage
        ) {
            Toast.makeText(this, R.string.validation, Toast.LENGTH_SHORT).show()
            dataBinding.progressBar.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            val imgPath = selectedImg?.let { uri ->
                withContext(Dispatchers.IO) {
                    recipeData?.let {
                        ImageStorage.delete(it.recipeImg)
                    }
                    ImageStorage.copy(this@AddRecipeActivity, uri)
                }
            } ?: recipeData!!.recipeImg

            val recipe = Recipe(
                id = recipeData?.id ?: 0,
                recipeName = name,
                recipeType = rType,
                recipeImg = imgPath,
                recipeDesc = desc,
                recipeIngredients = ingredients,
                recipeSteps = steps
            )

            val dao = AppDatabase.getInstance(applicationContext).recipeDao()

            if (recipeData != null) {
                dao.update(recipe)
                setResult(RESULT_OK, Intent().putExtra("recipeModel", recipe))
                Toast.makeText(this@AddRecipeActivity, R.string.success_updated, Toast.LENGTH_SHORT)
                    .show()
            } else {
                dao.insert(recipe)
                Toast.makeText(this@AddRecipeActivity, R.string.success_added, Toast.LENGTH_SHORT)
                    .show()
            }
            dataBinding.progressBar.visibility = View.GONE
            finish()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
        rType = parent?.getItemAtPosition(pos).toString()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Toast.makeText(this, R.string.nothing_select, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            data?.data?.let { uri ->
                selectedImg = uri
                dataBinding.recipeImg.setImageURI(uri)
                dataBinding.cancelImage.visibility = View.VISIBLE
            }
        }
    }
}