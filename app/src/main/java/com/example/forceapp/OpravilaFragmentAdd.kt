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
import com.example.forceapp.databinding.FragmentOpravilaAddBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class OpravilaFragmentAdd : Fragment() {
    private var _binding: FragmentOpravilaAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOpravilaAddBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val apiService = ApiService(requireContext())

        // Fetch data and populate spinner
        lifecycleScope.launch(Dispatchers.IO) {
            val evidencaList = apiService.fetchEvidenca()
            withContext(Dispatchers.Main) {
                this@OpravilaFragmentAdd.populateSpinner(evidencaList)
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
            val opravilo = binding.spinnerTasks.selectedItem.toString()
            val done = binding.checkboxDone.isChecked
            val selectedDate = binding.idEdtDate.text.toString()
            // Check if all fields are filled
            if (opravilo.isEmpty() || selectedDate.isEmpty()) {
                // If any field is empty, display a Toast message and return
                Toast.makeText(requireContext(), "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val originalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val targetFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00", Locale.getDefault())
            originalFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = originalFormat.parse(selectedDate)
            val formattedDate = targetFormat.format(date)

            apiService.submitData(requireContext(), opravilo, done, formattedDate)
        }
        binding.btnChoresShow.setOnClickListener {
            replaceFragment(OpravilaFragmentShow())
        }
    }

    private fun populateSpinner(evidencaList: List<String>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            evidencaList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTasks.adapter = adapter
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