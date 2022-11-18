package com.example.recipeapplication.viewmodel

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipeapplication.databinding.RecipeItemListBinding
import com.example.recipeapplication.model.Recipe


class RecipeListAdapter (var context: Context, var recipeList:ArrayList<Recipe>, private val listener: OnItemClickListener)
    : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(val binding: RecipeItemListBinding) :
        RecyclerView.ViewHolder(binding.root)

    // An interface that specifies listener’s behaviour.
    // OnItemClickListener used for going to a particular activity when a particular row is touched.
    interface OnItemClickListener{
        fun onItemClick(recipe: Recipe)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        // Create a new view, which defines the UI of the list item
        val listItemBinding = RecipeItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(listItemBinding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        with(holder as RecipeViewHolder){
            val newList = recipeList[position]

            Glide.with(context).load(newList.recipeImg).centerCrop().into(binding.itemImage)
            binding.itemTitle.text = newList.recipeName
            binding.itemDetail.text = newList.recipeDesc

            binding.root.setOnClickListener {
                listener.onItemClick(newList)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return recipeList.size
    }
}