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
import com.bumptech.glide.Glide
import com.example.recipeapplication.databinding.ActivityAddRecipeBinding
import com.example.recipeapplication.model.Recipe
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

private const val REQUEST_CODE = 45

class AddRecipeActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    companion object{
        fun start(context: Context, recipe: Recipe): Intent{
            val intent = Intent(context, AddRecipeActivity::class.java).apply{
                putExtra("pre_data", recipe)
            }
            return (intent)
        }
    }

    private lateinit var dataBinding: ActivityAddRecipeBinding
    private var rType = ""
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImg : Uri ?= null
    private var recipeData: Recipe ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_recipe)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val intent: Intent = intent
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

        if (recipeData != null){
            dataBinding.recipeNameEt.setText(recipeData?.recipeName)
            dataBinding.recipeDescEt.setText(recipeData?.recipeDesc)
            val spinnerPosition: Int = adapter.getPosition(recipeData?.recipeType)
            dataBinding.recipeType.setSelection(spinnerPosition)
            Glide.with(this).load(recipeData?.recipeImg).into(dataBinding.recipeImg)
            dataBinding.recipeIngredientsEt.setText(recipeData?.recipeIngredients)
            dataBinding.recipeStepsEt.setText(recipeData?.recipeSteps)

            dataBinding.btnAdd.text = getString(R.string.update)
        }

        dataBinding.selectImg.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }

        dataBinding.cancelImage.setOnClickListener {
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
        if(dataBinding.recipeNameEt.text?.isNotEmpty()!! && dataBinding.recipeDescEt.text?.isNotEmpty()!! && selectedImg != null &&
            dataBinding.recipeIngredientsEt.text?.isNotEmpty()!! && dataBinding.recipeStepsEt.text?.isNotEmpty()!! &&
            rType != getString(R.string.select) ){
            uploadImage()
        } else if (recipeData != null && selectedImg == null){
            updateData(recipeData?.recipeImg.toString())
        }else{
            Toast.makeText(this, R.string.validation, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateData(imgUrl: String) {
        val data = mapOf<String, String>(
            "recipeName" to dataBinding.recipeNameEt.text.toString(),
            "recipeDesc" to dataBinding.recipeDescEt.text.toString(),
            "recipeImg" to imgUrl,
            "recipeType" to rType,
            "recipeIngredients" to dataBinding.recipeIngredientsEt.text.toString(),
            "recipeSteps" to dataBinding.recipeStepsEt.text.toString()
        )

        val recipeData = Recipe(recipeData?.id!!, dataBinding.recipeNameEt.text.toString(), rType, imgUrl,
            dataBinding.recipeDescEt.text.toString(), dataBinding.recipeIngredientsEt.text.toString(), dataBinding.recipeStepsEt.text.toString())

        database.reference.child("recipeInfo")
            .child(recipeData?.id!!)
            .updateChildren(data)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.success_updated, Toast.LENGTH_SHORT).show()
                // Pass the value using Intent
                val intent = Intent()
                intent.putExtra("recipeModel", recipeData)
                // Send data to RecipeDetailActivity
                setResult(RESULT_OK, intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.fail_updated, Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage() {
        val reference = storage.reference.child("Recipes").child(Date().time.toString())
        reference.putFile(selectedImg!!).addOnCompleteListener{
            if(it.isSuccessful){
                reference.downloadUrl.addOnSuccessListener { task->
                    if(recipeData != null){
                        updateData(task.toString())
                    }else{
                        uploadInfo(task.toString())
                    }
                }
            }
        }
    }

    private fun uploadInfo(imgUrl:String) {
        val id: String? = database.reference.push().key

        val recipe = Recipe(id, dataBinding.recipeNameEt.text.toString(), rType, imgUrl,
            dataBinding.recipeDescEt.text.toString(), dataBinding.recipeIngredientsEt.text.toString(), dataBinding.recipeStepsEt.text.toString())

        database.reference.child("recipeInfo")
            .child(id!!)
            .setValue(recipe)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.success_added, Toast.LENGTH_SHORT).show()
                dataBinding.progressBar.visibility = View.GONE
                finish()
            }
            .addOnFailureListener{
                Toast.makeText(this, R.string.fail_added, Toast.LENGTH_SHORT).show()
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
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE){
            if(data != null){
                if(data.data != null){
                    selectedImg = data.data!!
                    dataBinding.recipeImg.setImageURI(selectedImg)
                    dataBinding.cancelImage.visibility = View.VISIBLE
                }
            }
        }
    }
}