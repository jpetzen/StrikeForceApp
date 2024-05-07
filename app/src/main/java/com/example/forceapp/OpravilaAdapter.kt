package com.example.forceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class OpravilaAdapter(private val opravilaList: List<Opravila>) : RecyclerView.Adapter<OpravilaAdapter.OpravilaViewHolder>() {

    class OpravilaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userTextView: TextView = itemView.findViewById(R.id.userTextView)
        val opraviloTextView: TextView = itemView.findViewById(R.id.opraviloTextView)
        val doneTextView: TextView = itemView.findViewById(R.id.doneTextView)
        val datumTextView: TextView = itemView.findViewById(R.id.datumTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpravilaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_layout_cleaning, parent, false)
        return OpravilaViewHolder(view)
    }

    override fun onBindViewHolder(holder: OpravilaViewHolder, position: Int) {
        val opravila = opravilaList[position]
        holder.userTextView.text = opravila.user_username
        holder.opraviloTextView.text = opravila.opravilo
        holder.doneTextView.text = opravila.done
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        try {
            val date = originalFormat.parse(opravila.datum)
            val formattedDate = dateFormat.format(date)
            holder.datumTextView.text = formattedDate
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle parsing or formatting exceptions
            holder.datumTextView.text = "Invalid Date"
        }
    }

    override fun getItemCount() = opravilaList.size
}