package com.neuronova.mimomento.ui.prayers

import com.neuronova.mimomento.data.TestContentFixture
import com.neuronova.mimomento.data.local.MiMomentoContentSource
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrayersViewModelTest {

    private lateinit var repository: MiMomentoContentRepository
    private lateinit var viewModel: PrayersViewModel

    @Before
    fun setUp() {
        repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        viewModel = PrayersViewModel(
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )
    }

    @Test
    fun loadsAllPrayerContentAndCatalogMappings() {
        val state = viewModel.uiState.value

        assertEquals(8, state.spiritualMoments.size)
        assertEquals(4, state.prayerRoutes.size)
        assertEquals(9, state.prayerGuides.size)
        assertEquals(5, state.specialContexts.size)
        assertEquals(12, state.categoryNamesById.size)
        assertEquals(12, state.situationLabelsById.size)
    }

    @Test
    fun selectSection_updatesSelectedSection() {
        assertEquals(PrayerSection.MOMENTS, viewModel.uiState.value.selectedSection)

        viewModel.selectSection(PrayerSection.ROUTES)
        assertEquals(PrayerSection.ROUTES, viewModel.uiState.value.selectedSection)

        viewModel.selectSection(PrayerSection.GUIDES)
        assertEquals(PrayerSection.GUIDES, viewModel.uiState.value.selectedSection)

        viewModel.selectSection(PrayerSection.MOMENTS)
        assertEquals(PrayerSection.MOMENTS, viewModel.uiState.value.selectedSection)
    }

    @Test
    fun getGuideDetailUiState_existingGuide_resolvesCorrectly() {
        val detailState = viewModel.getGuideDetailUiState("GO-01")

        assertFalse(detailState.isNotFound)
        assertNotNull(detailState.guide)
        assertEquals("GO-01", detailState.guide?.id)
        assertEquals("Disposición", detailState.guide?.name)
        assertEquals("Comenzar un momento de oración con apertura y sencillez.", detailState.guide?.purpose)
        assertTrue(detailState.categoryNames.isNotEmpty())
        assertTrue(detailState.situationLabels.isNotEmpty())
    }

    @Test
    fun getGuideDetailUiState_nonExistentGuide_returnsNotFoundSafely() {
        val detailState = viewModel.getGuideDetailUiState("GO-INVALID-9999")

        assertTrue(detailState.isNotFound)
        assertNull(detailState.guide)
        assertTrue(detailState.categoryNames.isEmpty())
        assertTrue(detailState.situationLabels.isEmpty())
    }

    @Test
    fun getRouteSessionUiState_ro01_stepByStepExecution() {
        // Step 1 of 3: GO-01
        val step1 = viewModel.getRouteSessionUiState("RO-01", stepIndex = 0)
        assertFalse(step1.isNotFound)
        assertNotNull(step1.route)
        assertEquals("RO-01", step1.route?.id)
        assertEquals(3, step1.totalSteps)
        assertEquals(3, step1.resolvedSteps.size)
        assertEquals(0, step1.currentStepIndex)
        assertEquals("GO-01", step1.currentGuide?.id)
        assertEquals("Disposición", step1.currentGuide?.name)
        assertFalse("Step 1 must not have previous step", step1.hasPrevious)
        assertTrue("Step 1 must have next step", step1.hasNext)
        assertFalse("Step 1 is not completed", step1.isCompleted)

        // Step 2 of 3: GO-02
        val step2 = viewModel.getRouteSessionUiState("RO-01", stepIndex = 1)
        assertFalse(step2.isNotFound)
        assertEquals(3, step2.totalSteps)
        assertEquals(1, step2.currentStepIndex)
        assertEquals("GO-02", step2.currentGuide?.id)
        assertEquals("Calma y preocupación", step2.currentGuide?.name)
        assertTrue("Step 2 must have previous step", step2.hasPrevious)
        assertTrue("Step 2 must have next step", step2.hasNext)
        assertFalse("Step 2 is not completed", step2.isCompleted)

        // Step 3 of 3: GO-03
        val step3 = viewModel.getRouteSessionUiState("RO-01", stepIndex = 2)
        assertFalse(step3.isNotFound)
        assertEquals(3, step3.totalSteps)
        assertEquals(2, step3.currentStepIndex)
        assertEquals("GO-03", step3.currentGuide?.id)
        assertEquals("Cierre abierto", step3.currentGuide?.name)
        assertTrue("Step 3 must have previous step", step3.hasPrevious)
        assertFalse("Step 3 (last step) must not have next step", step3.hasNext)
        assertFalse("Step 3 is not completed", step3.isCompleted)

        // Completed State
        val completed = viewModel.getRouteSessionUiState("RO-01", stepIndex = 3, isCompleted = true)
        assertFalse(completed.isNotFound)
        assertEquals(3, completed.totalSteps)
        assertTrue("Session must be completed", completed.isCompleted)
        assertNull(completed.currentGuide)
        assertFalse(completed.hasPrevious)
        assertFalse(completed.hasNext)
    }

    @Test
    fun getRouteSessionUiState_nonExistentRoute_returnsNotFoundSafely() {
        val state = viewModel.getRouteSessionUiState("RO-INVALID-9999", stepIndex = 0)

        assertTrue(state.isNotFound)
        assertNull(state.route)
        assertEquals(0, state.totalSteps)
        assertTrue(state.resolvedSteps.isEmpty())
        assertNull(state.currentGuide)
        assertFalse(state.hasPrevious)
        assertFalse(state.hasNext)
        assertFalse(state.isCompleted)
    }

    @Test
    fun getRouteSessionUiState_negativeStepIndex_clampsSafelyToFirstStep() {
        val state = viewModel.getRouteSessionUiState("RO-01", stepIndex = -5)

        assertFalse(state.isNotFound)
        assertEquals(0, state.currentStepIndex)
        assertEquals("GO-01", state.currentGuide?.id)
        assertFalse(state.hasPrevious)
        assertTrue(state.hasNext)
    }

    @Test
    fun getRouteSessionUiState_outOfBoundsStepIndex_clampsSafelyToLastStep() {
        val state = viewModel.getRouteSessionUiState("RO-01", stepIndex = 99)

        assertFalse(state.isNotFound)
        assertEquals(2, state.currentStepIndex)
        assertEquals("GO-03", state.currentGuide?.id)
        assertTrue(state.hasPrevious)
        assertFalse(state.hasNext)
    }

    @Test
    fun getRouteDetailUiState_existingRoute_resolvesStepsAndCategoriesInOrder() {
        val routeState = viewModel.getRouteDetailUiState("RO-01")

        assertFalse(routeState.isNotFound)
        assertNotNull(routeState.route)
        assertEquals("RO-01", routeState.route?.id)
        assertEquals("Oración breve para comenzar", routeState.route?.name)
        assertEquals(3, routeState.steps.size)
        assertEquals(1, routeState.steps[0].stepIndex)
        assertEquals("GO-01", routeState.steps[0].guide.id)
        assertEquals(2, routeState.steps[1].stepIndex)
        assertEquals("GO-02", routeState.steps[1].guide.id)
        assertEquals(3, routeState.steps[2].stepIndex)
        assertEquals("GO-03", routeState.steps[2].guide.id)
        assertTrue(routeState.categoryNames.isNotEmpty())
        assertTrue(routeState.situationLabels.isNotEmpty())
    }

    @Test
    fun getRouteDetailUiState_nonExistentRoute_returnsNotFoundSafely() {
        val routeState = viewModel.getRouteDetailUiState("RO-INVALID-9999")

        assertTrue(routeState.isNotFound)
        assertNull(routeState.route)
        assertTrue(routeState.steps.isEmpty())
        assertTrue(routeState.categoryNames.isEmpty())
        assertTrue(routeState.situationLabels.isEmpty())
    }

    @Test
    fun getSpiritualMomentDetailUiState_existingMoment_resolvesCorrectly_andHasNoDerivedRoutesOrGuides() {
        val momentState = viewModel.getSpiritualMomentDetailUiState("ME-01")

        assertFalse(momentState.isNotFound)
        assertNotNull(momentState.moment)
        assertEquals("ME-01", momentState.moment?.id)
        assertEquals("Al despertar", momentState.moment?.label)
        assertEquals("Mañana", momentState.moment?.timeOfDay)
        assertTrue(momentState.categoryNames.isNotEmpty())
        assertTrue(momentState.situationLabels.isNotEmpty())
        assertTrue("Must not derive related routes without explicit JSON relationship", momentState.relatedRoutes.isEmpty())
        assertTrue("Must not derive related guides without explicit JSON relationship", momentState.relatedGuides.isEmpty())
    }

    @Test
    fun getSpiritualMomentDetailUiState_nonExistentMoment_returnsNotFoundSafely() {
        val momentState = viewModel.getSpiritualMomentDetailUiState("ME-INVALID-9999")

        assertTrue(momentState.isNotFound)
        assertNull(momentState.moment)
        assertTrue(momentState.categoryNames.isEmpty())
        assertTrue(momentState.situationLabels.isEmpty())
    }

    @Test
    fun allPrayerRoutes_guideReferencesAreFullyResolvable() {
        val routes = repository.getPrayerRoutes()
        val guidesById = repository.getPrayerGuides().associateBy { it.id }

        routes.forEach { route ->
            assertTrue("Route ${route.id} has no guideIds", route.guideIds.isNotEmpty())
            route.guideIds.forEach { guideId ->
                assertTrue("Guide $guideId in route ${route.id} does not exist", guidesById.containsKey(guideId))
            }
        }
    }

    @Test
    fun prayerExperience_preservesDevotionalsAndCategoriesIntegrity() {
        assertEquals(360, repository.getAllDevotionals().size)
        assertEquals(12, repository.getCategories().size)
        assertEquals(12, repository.getSituations().size)
        assertEquals(48, repository.getSubthemes().size)
        assertEquals(212, repository.getTags().size)
    }

    @Test
    fun prayerRoutes_storesPersonalContentIsFalse() {
        val routes = repository.getPrayerRoutes()
        routes.forEach { route ->
            assertFalse("Route ${route.id} must not store personal content", route.storesPersonalContent)
        }
    }

    @Test
    fun spiritualMomentDetail_ME01_resolvesSuggestedDevotionalDEV0002() {
        val momentState = viewModel.getSpiritualMomentDetailUiState("ME-01")

        assertFalse(momentState.isNotFound)
        assertNotNull(momentState.suggestedDevotional)
        assertEquals("DEV-0002", momentState.suggestedDevotional?.id)
        assertEquals("Cuando una decisión pesa", momentState.suggestedDevotional?.title)
        assertEquals("Sí", momentState.suggestedDevotional?.autoRecommendation)
        assertEquals("Estándar", momentState.suggestedDevotional?.sensitivity)
        assertTrue(momentState.suggestedDevotionalCategoryName.isNotBlank())
    }

    @Test
    fun prayerGuideDetail_GO01_resolvesSuggestedDevotionalDEV0004() {
        val guideState = viewModel.getGuideDetailUiState("GO-01")

        assertFalse(guideState.isNotFound)
        assertNotNull(guideState.suggestedDevotional)
        assertEquals("DEV-0004", guideState.suggestedDevotional?.id)
        assertEquals("Cuando no veo el camino", guideState.suggestedDevotional?.title)
        assertEquals("Sí", guideState.suggestedDevotional?.autoRecommendation)
        assertEquals("Estándar", guideState.suggestedDevotional?.sensitivity)
        assertTrue(guideState.suggestedDevotionalCategoryName.isNotBlank())
    }

    @Test
    fun prayerRouteSession_inProgressSteps_haveNoSuggestedDevotional() {
        val step1 = viewModel.getRouteSessionUiState("RO-01", stepIndex = 0)
        assertNull(step1.suggestedDevotional)
        assertTrue(step1.suggestedDevotionalCategoryName.isEmpty())

        val step2 = viewModel.getRouteSessionUiState("RO-01", stepIndex = 1)
        assertNull(step2.suggestedDevotional)
        assertTrue(step2.suggestedDevotionalCategoryName.isEmpty())

        val step3 = viewModel.getRouteSessionUiState("RO-01", stepIndex = 2)
        assertNull(step3.suggestedDevotional)
        assertTrue(step3.suggestedDevotionalCategoryName.isEmpty())
    }

    @Test
    fun prayerRouteSession_completedStep_resolvesSuggestedDevotionalDEV0004() {
        val completed = viewModel.getRouteSessionUiState("RO-01", stepIndex = 3, isCompleted = true)

        assertTrue(completed.isCompleted)
        assertNotNull(completed.suggestedDevotional)
        assertEquals("DEV-0004", completed.suggestedDevotional?.id)
        assertEquals("Cuando no veo el camino", completed.suggestedDevotional?.title)
        assertEquals("Sí", completed.suggestedDevotional?.autoRecommendation)
        assertEquals("Estándar", completed.suggestedDevotional?.sensitivity)
        assertTrue(completed.suggestedDevotionalCategoryName.isNotBlank())
    }

    @Test
    fun all8SpiritualMoments_suggestedDevotionalsAreEligible() {
        val moments = repository.getSpiritualMoments()
        assertEquals(8, moments.size)

        moments.forEach { moment ->
            val state = viewModel.getSpiritualMomentDetailUiState(moment.id)
            assertFalse(state.isNotFound)
            state.suggestedDevotional?.let { dev ->
                assertEquals("Sí", dev.autoRecommendation)
                assertEquals("Estándar", dev.sensitivity)
            }
        }
    }

    @Test
    fun all9PrayerGuides_suggestedDevotionalsAreEligible() {
        val guides = repository.getPrayerGuides()
        assertEquals(9, guides.size)

        guides.forEach { guide ->
            val state = viewModel.getGuideDetailUiState(guide.id)
            assertFalse(state.isNotFound)
            state.suggestedDevotional?.let { dev ->
                assertEquals("Sí", dev.autoRecommendation)
                assertEquals("Estándar", dev.sensitivity)
            }
        }
    }

    @Test
    fun all4PrayerRoutes_suggestedDevotionalsOnCompletionAreEligible() {
        val routes = repository.getPrayerRoutes()
        assertEquals(4, routes.size)

        routes.forEach { route ->
            val state = viewModel.getRouteSessionUiState(route.id, stepIndex = 0, isCompleted = true)
            assertFalse(state.isNotFound)
            state.suggestedDevotional?.let { dev ->
                assertEquals("Sí", dev.autoRecommendation)
                assertEquals("Estándar", dev.sensitivity)
            }
        }
    }
}
