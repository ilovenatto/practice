package org.chenhome.exercise1.ui.meal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import org.chenhome.exercise1.R
import org.chenhome.exercise1.databinding.MealFragBinding
import org.chenhome.exercise1.databinding.MealsFragmentBinding
import timber.log.Timber

@AndroidEntryPoint
class MealFragment : Fragment() {

    private val mealVM : MealVM by viewModels()
    private val args : MealFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get binding
        val binding = MealFragBinding.inflate(inflater, container,  false)
        binding.vm = mealVM



        mealVM.meal.observe(viewLifecycleOwner, { meal->
            // bind image
            Glide.with(requireContext())
                .load(meal?.thumbUri)
                .placeholder(android.R.drawable.gallery_thumb)
                .into(binding.thumb)

        })
        // load model async. this will update liveData
        mealVM.load(args.mealId)

        // return view
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }
}