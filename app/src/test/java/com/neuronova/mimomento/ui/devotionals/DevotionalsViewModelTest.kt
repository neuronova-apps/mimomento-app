package com.neuronova.mimomento.ui.devotionals

import com.neuronova.mimomento.data.TestContentFixture
import com.neuronova.mimomento.data.local.MiMomentoContentSource
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DevotionalsViewModelTest {

    private lateinit var repository: MiMomentoContentRepository
    private lateinit var viewModel: DevotionalsViewModel

    @Before
    fun setUp() {
        repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        viewModel = DevotionalsViewModel(
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )
    }

    @Test
    fun initialState_containsAll360DevotionalsAnd12Categories() {
        val state = viewModel.uiState.value
        assertEquals(360, state.totalCount)
        assertEquals(360, state.allDevotionals.size)
        assertEquals(360, state.filteredDevotionals.size)
        assertEquals(12, state.categories.size)
        assertEquals(13, state.filterItems.size) // General filter (null) + 12 categories
        assertNull(state.selectedCategoryId)
        val generalFilter = state.filterItems.first()
        assertNull(generalFilter.id)
        assertNull(generalFilter.name)
        assertEquals(360, generalFilter.count)
    }

    @Test
    fun filterAll_returnsAll360Devotionals() {
        // First select a specific category
        viewModel.selectCategory("cat-fe-confianza")
        assertTrue(viewModel.uiState.value.filteredDevotionals.size < 360)

        // Then select general filter (null)
        viewModel.selectCategory(null)
        val state = viewModel.uiState.value
        assertNull(state.selectedCategoryId)
        assertEquals(360, state.filteredDevotionals.size)
    }

    @Test
    fun eachCategoryFilter_filtersExclusivelyItsCategoryId() {
        val categories = repository.getCategories()
        assertEquals(12, categories.size)

        var totalItemsAcrossCategories = 0

        categories.forEach { category ->
            viewModel.selectCategory(category.id)
            val state = viewModel.uiState.value

            assertEquals(category.id, state.selectedCategoryId)
            assertTrue("Category ${category.id} should have devotionals", state.filteredDevotionals.isNotEmpty())

            // Verify all items belong exclusively to this category
            state.filteredDevotionals.forEach { devotional ->
                assertEquals(
                    "Devotional ${devotional.id} should belong to category ${category.id}",
                    category.id,
                    devotional.categoryId,
                )
            }

            totalItemsAcrossCategories += state.filteredDevotionals.size
        }

        // Sum of all categories must match total 360 devotionals
        assertEquals(360, totalItemsAcrossCategories)
    }

    @Test
    fun concreteCategoryTest_cat01FiltersCorrectly() {
        viewModel.selectCategory("CAT-01")
        val state = viewModel.uiState.value
        assertEquals("CAT-01", state.selectedCategoryId)
        assertEquals(32, state.filteredDevotionals.size)
        assertTrue(state.filteredDevotionals.all { it.categoryId == "CAT-01" })
    }

    @Test
    fun getDevotionalById_returnsCorrectContentForValidId() {
        val devotional = viewModel.getDevotionalById("DEV-0001")
        assertNotNull(devotional)
        devotional?.let {
            assertEquals("DEV-0001", it.id)
            assertTrue(it.title.isNotBlank())
            assertTrue(it.categoryId.isNotBlank())
            assertTrue(it.bibleReference.isNotBlank())
            assertTrue(it.centralIdea.isNotBlank())
            assertTrue(it.reflection.isNotBlank())
            assertTrue(it.personalQuestion.isNotBlank())
            assertTrue(it.prayerGuide.isNotBlank())
            assertTrue(it.dailyAction.isNotBlank())
            assertTrue(it.estimatedMinutes > 0)
        }
    }

    @Test
    fun getDevotionalById_returnsNullForNonExistentId() {
        val devotional = viewModel.getDevotionalById("DEV-NON-EXISTENT-9999")
        assertNull(devotional)
    }

    @Test
    fun getCategoryName_resolvesCorrectlyFromCatalog() {
        val categoryName = viewModel.getCategoryName("CAT-01")
        assertEquals("Fe", categoryName)
    }
}
