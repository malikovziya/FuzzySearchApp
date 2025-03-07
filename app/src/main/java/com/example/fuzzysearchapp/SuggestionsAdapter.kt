package com.example.fuzzysearchapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Suggestion(val word: String, val distance: Double)

class SuggestionsAdapter : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    private var suggestions: List<Suggestion> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.wordTextView.text = suggestion.word
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    fun updateSuggestions(newSuggestions: List<Suggestion>) {
        suggestions = newSuggestions
        notifyDataSetChanged()
    }

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordTextView: TextView = itemView.findViewById(R.id.suggestionWord)
    }
}