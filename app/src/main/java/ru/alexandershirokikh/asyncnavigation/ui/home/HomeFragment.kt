package ru.alexandershirokikh.asyncnavigation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.alexandershirokikh.asyncnavigation.R
import ru.alexandershirokikh.asyncnavigation.databinding.FragmentHomeBinding
import ru.alexandershirokikh.asyncnavigation.ui.base.BaseFragment
import ru.alexandershirokikh.asyncnavigation.ui.test.TestFragment

class HomeFragment : BaseFragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome

        homeViewModel.text.observe(
            viewLifecycleOwner,
            {
                textView.text = it
            }
        )

        binding.btnStartTestFragment.setOnClickListener {
            lifecycleScope.launch {
                val result = startFragmentAsync<String>(
                    TestFragment()
                ) { fragment ->
                    replace(R.id.nav_host_fragment_activity_main, fragment).addToBackStack(null)
                }

                homeViewModel.setText(result)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
