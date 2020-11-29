package org.chenhome.favmeals

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.chenhome.favmeals.databinding.ViewMealFragmentBinding
import org.chenhome.favmeals.db.Meal
import org.chenhome.favmeals.db.MealDb
import org.chenhome.favmeals.service.MealService

class ViewMealFrag : Fragment() {

    private val args : ViewMealFragArgs by navArgs()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = ViewMealFragmentBinding.inflate(inflater, container, false)

        // get meal
        if (!args.isRemote) {
            MealDb.getInstance(requireContext()).mealDao.getMeal(args.mealId)
                .observe(viewLifecycleOwner) {
                    binding.meal = it
                    loadImage(binding, it)
                }
        } else {
            ioScope.launch {
                val meal = MealService.api.getMeal(args.mealId).meals[0]
                loadImage(binding, meal)
                binding.meal = meal
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    private fun loadImage(binding: ViewMealFragmentBinding, meal: Meal) {
        GlobalScope.launch (Dispatchers.Main) {
            Glide.with(requireContext())
                .load(meal.strMealThumb)
                .into(binding.thumb)
        }
    }


}