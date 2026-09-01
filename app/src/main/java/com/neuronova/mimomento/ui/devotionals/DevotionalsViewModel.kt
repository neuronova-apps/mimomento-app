package com.neuronova.mimomento.ui.devotionals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuronova.mimomento.data.model.Category
import com.neuronova.mimomento.data.model.Devotional
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryFilterItem(
    val id: String?,
    val name: String? = null,
    val count: Int,
)

data class DevotionalDetailUiState(
    val devotional: Devotional? = null,
    val categoryName: String = "",
    val previousDevotionalId: String? = null,
    val nextDevotionalId: String? = null,
    val isNotFound: Boolean = false,
)

data class DevotionalsUiState(
    val allDevotionals: List<Devotional> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val filteredDevotionals: List<Devotional> = emptyList(),
    val categoryNamesById: Map<String, String> = emptyMap(),
    val filterItems: List<CategoryFilterItem> = emptyList(),
    val totalCount: Int = 0,
)

class DevotionalsViewModel(
    private val repository: MiMomentoContentRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevotionalsUiState())
    val uiState: StateFlow<DevotionalsUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch(dispatcher) {
            val devotionals = repository.getAllDevotionals()
            val categories = repository.getCategories()
            val categoryMap = categories.associate { it.id to it.name }

            val countsByCategory = devotionals.groupingBy { it.categoryId }.eachCount()

            val filterItems = buildList {
                add(CategoryFilterItem(id = null, name = null, count = devotionals.size))
                categories.forEach { category ->
                    add(
                        CategoryFilterItem(
                            id = category.id,
                            name = category.name,
                            count = countsByCategory[category.id] ?: 0,
                        ),
                    )
                }
            }

            val currentSelected = _uiState.value.selectedCategoryId
            val filtered = if (currentSelected == null) {
                devotionals
            } else {
                devotionals.filter { it.categoryId == currentSelected }
            }

            _uiState.value = DevotionalsUiState(
                allDevotionals = devotionals,
                categories = categories,
                selectedCategoryId = currentSelected,
                filteredDevotionals = filtered,
                categoryNamesById = categoryMap,
                filterItems = filterItems,
                totalCount = devotionals.size,
            )
        }
    }

    fun selectCategory(categoryId: String?) {
        val current = _uiState.value
        val filtered = if (categoryId == null) {
            current.allDevotionals
        } else {
            current.allDevotionals.filter { it.categoryId == categoryId }
        }
        _uiState.value = current.copy(
            selectedCategoryId = categoryId,
            filteredDevotionals = filtered,
        )
    }

    fun getDevotionalDetailUiState(id: String): DevotionalDetailUiState {
        val devotional = repository.getDevotionalById(id)
            ?: return DevotionalDetailUiState(isNotFound = true)

        val previousId = repository.getPreviousDevotionalId(id)
        val nextId = repository.getNextDevotionalId(id)
        val categoryName = getCategoryName(devotional.categoryId)

        return DevotionalDetailUiState(
            devotional = devotional,
            categoryName = categoryName,
            previousDevotionalId = previousId,
            nextDevotionalId = nextId,
            isNotFound = false,
        )
    }

    fun getPreviousDevotionalId(id: String): String? = repository.getPreviousDevotionalId(id)

    fun getNextDevotionalId(id: String): String? = repository.getNextDevotionalId(id)

    fun getDevotionalById(id: String): Devotional? {
        return repository.getDevotionalById(id)
    }

    fun getCategoryName(categoryId: String): String {
        return _uiState.value.categoryNamesById[categoryId]
            ?: repository.getCategories().firstOrNull { it.id == categoryId }?.name
            ?: categoryId
    }

    companion object {
        fun provideFactory(
            repository: MiMomentoContentRepository,
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DevotionalsViewModel::class.java)) {
                    return DevotionalsViewModel(repository, dispatcher) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
