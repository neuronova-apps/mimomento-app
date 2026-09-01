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

    @Test
    fun getDevotionalDetailUiState_forFirstDevotional_hasNoPreviousAndHasNextDev0002() {
        val detailState = viewModel.getDevotionalDetailUiState("DEV-0001")
        assertNotNull(detailState.devotional)
        assertEquals("DEV-0001", detailState.devotional?.id)
        assertEquals("Paz", detailState.categoryName)
        assertNull(detailState.previousDevotionalId)
        assertEquals("DEV-0002", detailState.nextDevotionalId)
        assertEquals(false, detailState.isNotFound)
    }

    @Test
    fun getDevotionalDetailUiState_forLastDevotional_hasPreviousDev0359AndHasNoNext() {
        val detailState = viewModel.getDevotionalDetailUiState("DEV-0360")
        assertNotNull(detailState.devotional)
        assertEquals("DEV-0360", detailState.devotional?.id)
        assertEquals("DEV-0359", detailState.previousDevotionalId)
        assertNull(detailState.nextDevotionalId)
        assertEquals(false, detailState.isNotFound)
    }

    @Test
    fun getDevotionalDetailUiState_forIntermediateDevotional_resolvesCorrectStateAndNavigation() {
        val detailState = viewModel.getDevotionalDetailUiState("DEV-0042")
        assertNotNull(detailState.devotional)
        assertEquals("DEV-0042", detailState.devotional?.id)
        assertEquals("DEV-0041", detailState.previousDevotionalId)
        assertEquals("DEV-0043", detailState.nextDevotionalId)
        assertEquals(false, detailState.isNotFound)
        assertTrue(detailState.categoryName.isNotBlank())
    }

    @Test
    fun getDevotionalDetailUiState_forNonExistentId_returnsNotFoundStateSafely() {
        val detailState = viewModel.getDevotionalDetailUiState("DEV-INVALID-NON-EXISTENT")
        assertNull(detailState.devotional)
        assertNull(detailState.previousDevotionalId)
        assertNull(detailState.nextDevotionalId)
        assertEquals(true, detailState.isNotFound)
    }

    @Test
    fun navigationHelpers_returnExpectedIds() {
        // DEV-0001
        assertNull(viewModel.getPreviousDevotionalId("DEV-0001"))
        assertEquals("DEV-0002", viewModel.getNextDevotionalId("DEV-0001"))

        // DEV-0360
        assertEquals("DEV-0359", viewModel.getPreviousDevotionalId("DEV-0360"))
        assertNull(viewModel.getNextDevotionalId("DEV-0360"))

        // Intermediate DEV-0042
        assertEquals("DEV-0041", viewModel.getPreviousDevotionalId("DEV-0042"))
        assertEquals("DEV-0043", viewModel.getNextDevotionalId("DEV-0042"))

        // Non-existent ID
        assertNull(viewModel.getPreviousDevotionalId("NON-EXISTENT"))
        assertNull(viewModel.getNextDevotionalId("NON-EXISTENT"))
    }
}
