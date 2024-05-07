package com.example.forceapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import java.util.*
import com.example.forceapp.databinding.FragmentSredstvaAddBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


class SredstvaFragmentAdd : Fragment() {
    private var _binding: FragmentSredstvaAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSredstvaAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val apiService = ApiService(requireContext())

        // Fetch data and populate spinner
        lifecycleScope.launch(Dispatchers.IO) {
            val cistilaList = apiService.fetchCistila()
            withContext(Dispatchers.Main) {
                this@SredstvaFragmentAdd.populateSpinner(cistilaList)
            }
        }

        val editTextDate = binding.idEdtDate
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        editTextDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    editTextDate.setText(selectedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        binding.btnSubmit.setOnClickListener {
            val cistila = binding.spinnerProducts.selectedItem.toString()
            val steviloString = binding.editNumberOfProducts.text.toString()
            val denarString = binding.editCost.text.toString()
            val selectedDate = binding.idEdtDate.text.toString()
            // Check if all fields are filled
            if (cistila.isEmpty() || steviloString.isEmpty() || denarString.isEmpty() || selectedDate.isEmpty()) {
                // If any field is empty, display a Toast message and return
                Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val originalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val targetFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.getDefault())
            originalFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = originalFormat.parse(selectedDate)
            val formattedDate = targetFormat.format(date)

            val denar = denarString.toDouble()

            // Check if 'stevilo' is an integer
            val stevilo = steviloString.toIntOrNull()
            if (stevilo == null) {
                // If 'stevilo' is not an integer, display a Toast message and return
                Toast.makeText(requireContext(), "Number of products needs to be an integer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            apiService.submitData(requireContext(), cistila, stevilo, denar, formattedDate)
        }

        binding.btnShow.setOnClickListener {
            replaceFragment(SredstvaFragmentShow())
        }
    }

    private fun populateSpinner(cistilaList: List<String>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cistilaList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerProducts.adapter = adapter
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

