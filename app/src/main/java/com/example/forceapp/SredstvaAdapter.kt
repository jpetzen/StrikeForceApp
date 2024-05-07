package com.example.forceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class SredstvaAdapter(private val sredstvaList: List<Sredstva>) : RecyclerView.Adapter<SredstvaAdapter.SredstvaViewHolder>() {

    class SredstvaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userTextView: TextView = itemView.findViewById(R.id.user_text_view)
        val cleaningProductTextView: TextView = itemView.findViewById(R.id.cleaning_product_text_view)
        val numberTextView: TextView = itemView.findViewById(R.id.number_text_view)
        val costTextView: TextView = itemView.findViewById(R.id.cost_text_view)
        val dateTextView: TextView = itemView.findViewById(R.id.date_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SredstvaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_layout, parent, false)
        return SredstvaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SredstvaViewHolder, position: Int) {
        val sredstva = sredstvaList[position]
        holder.userTextView.text = sredstva.user_username
        holder.cleaningProductTextView.text = sredstva.cistilo
        holder.numberTextView.text = sredstva.stevilo.toString()
        holder.costTextView.text = sredstva.denar.toString()
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        try {
            val date = originalFormat.parse(sredstva.date)
            val formattedDate = dateFormat.format(date)
            holder.dateTextView.text = formattedDate
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle parsing or formatting exceptions
            holder.dateTextView.text = "Invalid Date"
        }
    }

    override fun getItemCount() = sredstvaList.size
}