package org.chenhome.favmeals.ui.search

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.chenhome.favmeals.databinding.CardMealBinding
import org.chenhome.favmeals.databinding.SearchMealFragBinding
import org.chenhome.favmeals.db.Meal
import org.chenhome.favmeals.db.MealDb
import timber.log.Timber

@AndroidEntryPoint
class SearchMealFrag : Fragment() {

    val vm : SearchMealsVM by viewModels()
    val listener = MealListener()
    val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = SearchMealFragBinding.inflate(inflater,container,false)
        Timber.d("Got here")
        // observe search term
        vm.search.observe(viewLifecycleOwner){
            Timber.d("Got search $it")
            vm.load()
        }


        // setup list
        binding.list.adapter = object : RecyclerView.Adapter<MealVH>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealVH =
                MealVH(CardMealBinding.inflate(inflater,parent,false))

            override fun onBindViewHolder(holder: MealVH, position: Int) {
                vm.meals.value?.get(position)?.let { holder.bind(it) }
            }
            override fun getItemCount(): Int = vm.meals.value?.size ?: 0
        }
        vm.meals.observe(viewLifecycleOwner){
            (binding.list.adapter as RecyclerView.Adapter).notifyDataSetChanged()
        }

        binding.vm = vm
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    private inner class MealVH (val binding: CardMealBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(meal:Meal){
            binding.meal = meal
            binding.listener = listener
            Glide.with(requireContext())
                .load(Uri.parse(meal.strMealThumb))
                .into(binding.thumb)
        }
    }

    inner class MealListener {
        fun onClick(meal: Meal){
            Timber.d("got click $meal")
            findNavController().navigate(SearchMealFragDirections.actionSearchMealFragToViewMealFrag(meal.idMeal, true))
        }
        fun onSave(meal:Meal) {
            ioScope.launch {
                val id = MealDb.getInstance(requireContext())
                    .mealDao.insert(meal)
                Timber.d("Got db id $id")
            }
        }
    }

}