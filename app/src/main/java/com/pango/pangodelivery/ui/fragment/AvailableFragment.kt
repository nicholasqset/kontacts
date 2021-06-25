package com.pango.pangodelivery.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.pango.pangodelivery.databinding.FragmentAvailableBinding

class AvailableFragment : Fragment() {
    private var _binding: FragmentAvailableBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var uid: String? = null
    private var myLat: Double? = null
    private var myLng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAvailableBinding.inflate(inflater, container, false)
        val v = binding.root
        uid = requireArguments().getString("uid")
        myLat = requireArguments().getDouble("myLat", 0.0)
        myLng = requireArguments().getDouble("myLng",0.0)
        binding.viewpager.offscreenPageLimit = 1
        binding.viewpager.adapter = MyAdapter(childFragmentManager, lifecycle)
        TabLayoutMediator(
            binding.tabs,
            binding.viewpager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                when (position) {
                    0 -> tab.text = "Unassigned"
                    1 -> tab.text = "Ongoing Delivery"


                }
            }).attach()




        return v
    }
    private inner class MyAdapter(fm: FragmentManager?, lifecycle: Lifecycle) : FragmentStateAdapter(fm!!, lifecycle) {
        private val intItems = 2
        override fun createFragment(position: Int): Fragment {

            var fragment: Fragment? = null
            when (position) {
                0 -> fragment = OrdersFragment(uid!!,myLat,myLng)
                1 -> fragment = OnDeliveryFragment(uid!!,myLat,myLng)

            }
            return fragment!!
        }
        override fun getItemCount(): Int {
            return intItems
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {

    }
}