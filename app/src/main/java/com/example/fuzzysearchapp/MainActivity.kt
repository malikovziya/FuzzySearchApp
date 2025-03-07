package com.example.fuzzysearchapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var clearButton: ExtendedFloatingActionButton
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var modeSpinner: Spinner
    private val suggestionsAdapter = SuggestionsAdapter()

    private val viewModel: SpellCheckerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchBar = findViewById(R.id.searchBar)
        clearButton = findViewById(R.id.button)
        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView)
        modeSpinner = findViewById(R.id.modeSpinner)

        suggestionsRecyclerView.layoutManager = LinearLayoutManager(this)
        suggestionsRecyclerView.adapter = suggestionsAdapter

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.search_modes,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modeSpinner.adapter = adapter

        modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMode = when (position) {
                    0 -> SearchMode.LEVENSHTEIN
                    1 -> SearchMode.FUZZY
                    else -> SearchMode.LEVENSHTEIN
                }
                viewModel.setSearchMode(selectedMode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        viewModel.loadDictionary(assets)

        viewModel.suggestions.observe(this) { suggestions ->
            println("Updating UI with suggestions: $suggestions")
            suggestionsAdapter.updateSuggestions(suggestions)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length >= 2) {
                    viewModel.search(query)
                } else {
                    suggestionsAdapter.updateSuggestions(emptyList())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        clearButton.setOnClickListener {
            searchBar.text.clear()
            suggestionsAdapter.updateSuggestions(emptyList())
        }
    }
}