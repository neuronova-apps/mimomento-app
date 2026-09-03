package com.neuronova.mimomento.ui.prayers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuronova.mimomento.data.model.Devotional
import com.neuronova.mimomento.data.model.PrayerGuide
import com.neuronova.mimomento.data.model.PrayerRoute
import com.neuronova.mimomento.data.model.SpecialContext
import com.neuronova.mimomento.data.model.SpiritualMoment
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PrayerSection {
    MOMENTS,
    ROUTES,
    GUIDES,
}

data class ResolvedRouteStep(
    val stepIndex: Int,
    val guide: PrayerGuide,
)

data class PrayerGuideDetailUiState(
    val guide: PrayerGuide? = null,
    val categoryNames: List<String> = emptyList(),
    val situationLabels: List<String> = emptyList(),
    val suggestedDevotional: Devotional? = null,
    val suggestedDevotionalCategoryName: String = "",
    val isNotFound: Boolean = false,
)

data class PrayerRouteDetailUiState(
    val route: PrayerRoute? = null,
    val steps: List<ResolvedRouteStep> = emptyList(),
    val categoryNames: List<String> = emptyList(),
    val situationLabels: List<String> = emptyList(),
    val isNotFound: Boolean = false,
)

data class PrayerRouteSessionUiState(
    val route: PrayerRoute? = null,
    val resolvedSteps: List<PrayerGuide> = emptyList(),
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val currentGuide: PrayerGuide? = null,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val isCompleted: Boolean = false,
    val suggestedDevotional: Devotional? = null,
    val suggestedDevotionalCategoryName: String = "",
    val isNotFound: Boolean = false,
)

data class SpiritualMomentDetailUiState(
    val moment: SpiritualMoment? = null,
    val categoryNames: List<String> = emptyList(),
    val situationLabels: List<String> = emptyList(),
    val suggestedDevotional: Devotional? = null,
    val suggestedDevotionalCategoryName: String = "",
    val relatedRoutes: List<PrayerRoute> = emptyList(),
    val relatedGuides: List<PrayerGuide> = emptyList(),
    val isNotFound: Boolean = false,
)

data class PrayersUiState(
    val selectedSection: PrayerSection = PrayerSection.MOMENTS,
    val spiritualMoments: List<SpiritualMoment> = emptyList(),
    val prayerRoutes: List<PrayerRoute> = emptyList(),
    val prayerGuides: List<PrayerGuide> = emptyList(),
    val specialContexts: List<SpecialContext> = emptyList(),
    val categoryNamesById: Map<String, String> = emptyMap(),
    val situationLabelsById: Map<String, String> = emptyMap(),
)

class PrayersViewModel(
    private val repository: MiMomentoContentRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayersUiState())
    val uiState: StateFlow<PrayersUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch(dispatcher) {
            val moments = repository.getSpiritualMoments()
            val routes = repository.getPrayerRoutes()
            val guides = repository.getPrayerGuides()
            val contexts = repository.getSpecialContexts()
            val categories = repository.getCategories().associate { it.id to it.name }
            val situations = repository.getSituations().associate { it.id to it.label }

            _uiState.value = _uiState.value.copy(
                spiritualMoments = moments,
                prayerRoutes = routes,
                prayerGuides = guides,
                specialContexts = contexts,
                categoryNamesById = categories,
                situationLabelsById = situations,
            )
        }
    }

    fun selectSection(section: PrayerSection) {
        _uiState.value = _uiState.value.copy(selectedSection = section)
    }

    fun getGuideDetailUiState(guideId: String): PrayerGuideDetailUiState {
        val guide = repository.getPrayerGuideById(guideId)
            ?: return PrayerGuideDetailUiState(isNotFound = true)

        val categoryMap = getCategoryMap()
        val situationMap = getSituationMap()

        val categoryNames = guide.categoryIds.map { categoryMap[it] ?: it }
        val situationLabels = guide.situationIds.map { situationMap[it] ?: it }

        val suggestedDevotional = repository.getSuggestedDevotional(
            situationIds = guide.situationIds,
            categoryIds = guide.categoryIds,
        )
        val suggestedCategoryName = suggestedDevotional?.let {
            categoryMap[it.categoryId] ?: it.categoryId
        }.orEmpty()

        return PrayerGuideDetailUiState(
            guide = guide,
            categoryNames = categoryNames,
            situationLabels = situationLabels,
            suggestedDevotional = suggestedDevotional,
            suggestedDevotionalCategoryName = suggestedCategoryName,
            isNotFound = false,
        )
    }

    fun getRouteDetailUiState(routeId: String): PrayerRouteDetailUiState {
        val route = repository.getPrayerRouteById(routeId)
            ?: return PrayerRouteDetailUiState(isNotFound = true)

        val categoryMap = getCategoryMap()
        val situationMap = getSituationMap()
        val allGuidesById = repository.getPrayerGuides().associateBy { it.id }

        val steps = route.guideIds.mapIndexedNotNull { index, id ->
            allGuidesById[id]?.let { guide ->
                ResolvedRouteStep(stepIndex = index + 1, guide = guide)
            }
        }

        val categoryNames = route.categoryIds.map { categoryMap[it] ?: it }
        val situationLabels = route.situationIds.map { situationMap[it] ?: it }

        return PrayerRouteDetailUiState(
            route = route,
            steps = steps,
            categoryNames = categoryNames,
            situationLabels = situationLabels,
            isNotFound = false,
        )
    }

    fun getRouteSessionUiState(
        routeId: String,
        stepIndex: Int = 0,
        isCompleted: Boolean = false,
    ): PrayerRouteSessionUiState {
        val route = repository.getPrayerRouteById(routeId)
            ?: return PrayerRouteSessionUiState(isNotFound = true)

        val allGuidesById = repository.getPrayerGuides().associateBy { it.id }
        val resolvedSteps = route.guideIds.mapNotNull { allGuidesById[it] }
        val totalSteps = resolvedSteps.size

        if (totalSteps == 0) {
            return PrayerRouteSessionUiState(
                route = route,
                resolvedSteps = emptyList(),
                currentStepIndex = 0,
                totalSteps = 0,
                currentGuide = null,
                hasPrevious = false,
                hasNext = false,
                isCompleted = isCompleted,
                suggestedDevotional = null,
                suggestedDevotionalCategoryName = "",
                isNotFound = false,
            )
        }

        if (isCompleted) {
            val categoryMap = getCategoryMap()
            val suggestedDevotional = repository.getSuggestedDevotional(
                situationIds = route.situationIds,
                categoryIds = route.categoryIds,
            )
            val suggestedCategoryName = suggestedDevotional?.let {
                categoryMap[it.categoryId] ?: it.categoryId
            }.orEmpty()

            return PrayerRouteSessionUiState(
                route = route,
                resolvedSteps = resolvedSteps,
                currentStepIndex = totalSteps,
                totalSteps = totalSteps,
                currentGuide = null,
                hasPrevious = false,
                hasNext = false,
                isCompleted = true,
                suggestedDevotional = suggestedDevotional,
                suggestedDevotionalCategoryName = suggestedCategoryName,
                isNotFound = false,
            )
        }

        val clampedIndex = stepIndex.coerceIn(0, totalSteps - 1)
        val guide = resolvedSteps[clampedIndex]
        val hasPrevious = clampedIndex > 0
        val hasNext = clampedIndex < totalSteps - 1

        return PrayerRouteSessionUiState(
            route = route,
            resolvedSteps = resolvedSteps,
            currentStepIndex = clampedIndex,
            totalSteps = totalSteps,
            currentGuide = guide,
            hasPrevious = hasPrevious,
            hasNext = hasNext,
            isCompleted = false,
            suggestedDevotional = null,
            suggestedDevotionalCategoryName = "",
            isNotFound = false,
        )
    }

    fun getSpiritualMomentDetailUiState(momentId: String): SpiritualMomentDetailUiState {
        val moment = repository.getSpiritualMomentById(momentId)
            ?: return SpiritualMomentDetailUiState(isNotFound = true)

        val categoryMap = getCategoryMap()
        val situationMap = getSituationMap()

        val categoryNames = moment.categoryIds.map { categoryMap[it] ?: it }
        val situationLabels = moment.situationIds.map { situationMap[it] ?: it }

        val suggestedDevotional = repository.getSuggestedDevotional(
            situationIds = moment.situationIds,
            categoryIds = moment.categoryIds,
        )
        val suggestedCategoryName = suggestedDevotional?.let {
            categoryMap[it.categoryId] ?: it.categoryId
        }.orEmpty()

        return SpiritualMomentDetailUiState(
            moment = moment,
            categoryNames = categoryNames,
            situationLabels = situationLabels,
            suggestedDevotional = suggestedDevotional,
            suggestedDevotionalCategoryName = suggestedCategoryName,
            relatedRoutes = emptyList(),
            relatedGuides = emptyList(),
            isNotFound = false,
        )
    }

    private fun getCategoryMap(): Map<String, String> {
        val stateMap = _uiState.value.categoryNamesById
        return if (stateMap.isNotEmpty()) stateMap else repository.getCategories().associate { it.id to it.name }
    }

    private fun getSituationMap(): Map<String, String> {
        val stateMap = _uiState.value.situationLabelsById
        return if (stateMap.isNotEmpty()) stateMap else repository.getSituations().associate { it.id to it.label }
    }

    companion object {
        fun provideFactory(
            repository: MiMomentoContentRepository,
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PrayersViewModel::class.java)) {
                    return PrayersViewModel(repository, dispatcher) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
