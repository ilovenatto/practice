package org.chenhome.favmeals.ui.fav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import org.chenhome.favmeals.databinding.CardFavmealBinding
import org.chenhome.favmeals.databinding.CardMealBinding
import org.chenhome.favmeals.databinding.FavMealsFragBinding
import org.chenhome.favmeals.db.Meal
import org.chenhome.favmeals.db.MealDb
import timber.log.Timber

@AndroidEntryPoint
class FavMealsFrag : Fragment() {
    private val vm : FavMealsVM by viewModels()
    private val listener = MealListener()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FavMealsFragBinding.inflate(inflater, container, false)
        binding.vm = vm

        // setup list
        with(binding.favs) {
            adapter = FavMealsAdapter(MealDb.getInstance(requireContext()).mealDao.getFavMeals())
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    inner class FavMealsAdapter (meals: LiveData<List<Meal>>): RecyclerView.Adapter<MealVH>() {
        private var favMeals : MutableList<Meal> = mutableListOf()

        init {
            meals.observe(viewLifecycleOwner){
                it?.let{
                    favMeals = it.toMutableList()
                    notifyDataSetChanged()
                }

            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealVH =
            MealVH(CardFavmealBinding.inflate(LayoutInflater.from(requireContext()), parent, false))

        override fun onBindViewHolder(holder: MealVH, position: Int) {
            favMeals[position].let { holder.bind(it) }
            holder.binding.listener = listener
        }

        override fun getItemCount(): Int =
            favMeals.size

    }

    inner class MealVH(val binding: CardFavmealBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(meal: Meal){
            binding.meal = meal
            Glide.with(requireContext())
                .load(meal.strMealThumb)
                .into(binding.thumb)
        }
    }

    inner class MealListener {
        fun onClick(meal: Meal){
            Timber.d("got click $meal")
            findNavController().navigate(FavMealsFragDirections.actionFavMealsFragToViewMealFrag(meal.idMeal, false))
        }
    }


}