package com.example.fuzzysearchapp

import android.content.res.AssetManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.debatty.java.stringsimilarity.Levenshtein
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class SpellCheckerViewModel : ViewModel() {
    private val _isDatasetLoaded = MutableLiveData<Boolean>()
    private val _suggestions = MutableLiveData<List<Suggestion>>()
    val suggestions: LiveData<List<Suggestion>> get() = _suggestions
    private lateinit var dataset: List<String>
    private var searchMode: SearchMode = SearchMode.LEVENSHTEIN
    private val levenshtein = Levenshtein()

    fun setSearchMode(mode: SearchMode) {
        searchMode = mode
    }

    fun loadDictionary(assets: AssetManager) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = assets.open("filtered_wordlist.txt")
                val reader = BufferedReader(InputStreamReader(inputStream))
                dataset = reader.readLines()
                reader.close()

                _isDatasetLoaded.postValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _isDatasetLoaded.postValue(false)
            }
        }
    }

    fun search(query: String) {
        if (!::dataset.isInitialized) return

        viewModelScope.launch(Dispatchers.IO) {
            val suggestions = when (searchMode) {
                SearchMode.LEVENSHTEIN -> {
                    println("Using Levenshtein mode")
                    levenshteinSearch(query).sortedBy { it.distance }
                }
                SearchMode.FUZZY -> {
                    println("Using FuzzyWuzzy mode")
                    fuzzySearch(query).sortedByDescending { it.distance }
                }
            }.take(5)

            println("Suggestions: $suggestions")

            _suggestions.postValue(suggestions)
        }
    }

    private fun levenshteinSearch(query: String): List<Suggestion> {
        return dataset.map { term ->
            val distance = levenshtein(query, term)
            Suggestion(term, distance.toDouble())
        }
    }

    private fun fuzzySearch(query: String): List<Suggestion> {
        return dataset.map { term ->
            val distance = levenshtein.distance(query, term)
            val maxLen = maxOf(query.length, term.length)
            val similarity = if (maxLen == 0) 100.0 else (1 - (distance / maxLen)) * 100
            Suggestion(term, similarity)
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        val lenA = a.length
        val lenB = b.length
        val matrix = Array(lenA + 1) { IntArray(lenB + 1) }

        for (i in 0..lenA) matrix[i][0] = i
        for (j in 0..lenB) matrix[0][j] = j

        for (i in 1..lenA) {
            for (j in 1..lenB) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                matrix[i][j] = minOf(matrix[i - 1][j] + 1, matrix[i][j - 1] + 1, matrix[i - 1][j - 1] + cost)
            }
        }

        return matrix[lenA][lenB]
    }
}

enum class SearchMode {
    LEVENSHTEIN,
    FUZZY
}
