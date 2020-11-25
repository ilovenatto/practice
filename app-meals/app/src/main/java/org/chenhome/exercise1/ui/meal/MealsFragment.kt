package org.chenhome.exercise1.ui.meal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import org.chenhome.exercise1.databinding.CardMealBinding
import org.chenhome.exercise1.databinding.MealsFragmentBinding
import org.chenhome.exercise1.model.MealStub
import timber.log.Timber

@AndroidEntryPoint
class MealsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val mealsVM : MealsVM by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        mealsVM.load()
        val binding = MealsFragmentBinding.inflate(inflater, container, false)
        binding.vm = mealsVM

        // bind meals list
        with (binding.meals) {
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                // listener for when item selected
                val stubListener = StubListener { mealId ->
                    Timber.d("Going to meal $mealId")
                    findNavController()
                        .navigate(MealsFragmentDirections.actionMealsFragmentToMealFragment(mealId))
                }

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): RecyclerView.ViewHolder {
                    Timber.d("Creating $viewType")
                    return MealStubVH(CardMealBinding.inflate(inflater, parent, false))
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    (holder as MealStubVH).bind(mealsVM.meals?.value?.get(position),stubListener)
                }

                override fun getItemCount(): Int =
                    mealsVM.meals?.value?.size ?: 0
            }
        }

        // spinner
        binding.spinner.adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.spinner.onItemSelectedListener = this

        // update meals list on network load
        with (mealsVM) {
            meals?.observe(viewLifecycleOwner, {
                Timber.d("MealStubs received ${it.size}")
                binding.meals.adapter?.notifyDataSetChanged()
            })
            categories?.observe(viewLifecycleOwner, {
                it?.let {
                    (binding.spinner.adapter as ArrayAdapter<String>).addAll(it.map { category->category.name })
                }
            })

            // update on spinner change
            current.observe(viewLifecycleOwner, {target->
                val pos = (binding.spinner.adapter as ArrayAdapter<String>).getPosition(target)
                Timber.d("Got pos $pos")
                binding.spinner.setSelection(pos)
            })
        }

        // complete setup
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    inner class MealStubVH(val binding: CardMealBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stub: MealStub?, stubListener: StubListener) {
            Timber.d("Binding from stub $stub")
            binding.mealStub = stub
            binding.listener = stubListener

            Glide.with(requireContext())
                .load(stub?.thumbUri)
                .placeholder(android.R.drawable.gallery_thumb)
                .into(binding.thumb)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val category = parent?.adapter?.getItem(position) as String
        Timber.d("Seleted $position and cat $category")
        mealsVM.current.value = category
        mealsVM.load()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }


}

class StubListener(val clickListener: (mealId:String) -> Unit) {
    fun onClick(stub: MealStub) = clickListener(stub.id)
}