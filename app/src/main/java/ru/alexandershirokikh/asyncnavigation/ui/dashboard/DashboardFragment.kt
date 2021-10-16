package ru.alexandershirokikh.asyncnavigation.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.alexandershirokikh.asyncnavigation.R
import ru.alexandershirokikh.asyncnavigation.databinding.FragmentDashboardBinding
import ru.alexandershirokikh.asyncnavigation.ui.base.BaseFragment
import ru.alexandershirokikh.asyncnavigation.ui.test.TestFragment

class DashboardFragment : BaseFragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })

        binding.btnStartTestFragment.setOnClickListener {
            lifecycleScope.launch {
                val result = startFragmentAsync<String>(
                    TestFragment()
                ) { fragment ->
                    replace(R.id.nav_host_fragment_activity_main, fragment).addToBackStack(null)
                }

                dashboardViewModel.setText(result)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
