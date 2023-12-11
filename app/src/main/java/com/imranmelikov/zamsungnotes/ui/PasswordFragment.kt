package com.imranmelikov.zamsungnotes.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import com.imranmelikov.zamsungnotes.PasswordActivity
import com.imranmelikov.zamsungnotes.R
import com.imranmelikov.zamsungnotes.databinding.FragmentPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordFragment : Fragment() {
    private lateinit var binding: FragmentPasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPasswordBinding.inflate(inflater, container, false)
        val sharedPreferences = requireActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        binding.passwordButton.setOnClickListener {
            val intent= Intent(requireActivity(), PasswordActivity::class.java)
            intent.putExtra("passwordReset","reset")
            startActivity(intent)
        }
        val password=sharedPreferences.getString("password","")
        val editText = binding.passwordEdittext
        editText.requestFocus()
        editText.postDelayed({
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 250)
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val searchText = editText.text.toString().trim()
                if (searchText.isNotEmpty()) {
                    if (searchText.length >= 4) {
                        if (searchText==password){
                            editText.clearFocus()
                            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(editText.windowToken, 0)
                            findNavController().popBackStack()
                        }else{
                            binding.passwordText.text = "Incorrect password entered."
                            editText.text.clear()
                        }
                    }
                }
                true
            } else {
                false
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Before text changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // On text changed
            }

            override fun afterTextChanged(s: Editable?) {
                // After text changed
                val searchText = s.toString()
                if (searchText.isNotEmpty()) {
                    if (searchText.length >= 4) {
                        binding.passwordText.text = "Enter your password."
                    } else {
                        binding.passwordText.text = "Enter a password with at least 4 characters in it."
                    }
                }
            }
        })


        return binding.root
    }
}