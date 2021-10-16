package ru.alexandershirokikh.asyncnavigation.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.alexandershirokikh.asyncnavigation.R
import ru.alexandershirokikh.asyncnavigation.databinding.FragmentTestBinding
import ru.alexandershirokikh.asyncnavigation.ui.base.BaseFragment

class TestFragment : BaseFragment(R.layout.fragment_test) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentTestBinding.inflate(inflater, container, false)

        binding.btnSendResult.setOnClickListener {
            val text = binding.etText.text.toString()

            setAsyncResult(text)
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    companion object {
        const val FRAGMENT_RESULT_KEY = "TestFragment.RESULT"
        const val RESULT_TEXT = "text"
    }
}
