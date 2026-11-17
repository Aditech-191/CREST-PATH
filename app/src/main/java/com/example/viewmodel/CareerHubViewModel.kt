package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActiveStudentProfile
import com.example.data.AdvisorMessage
import com.example.data.ApplicationStatus
import com.example.data.ApplicationTrackerDoc
import com.example.data.CareerAiRepository
import com.example.data.CareerRepository
import com.example.data.CustomOpportunityDoc
import com.example.data.MatchEngine
import com.example.data.OpportunityCardItem
import com.example.data.OpportunityCategory
import com.example.data.OpportunitySort
import com.example.data.ParseUiState
import com.example.data.ParsedOpportunityDraft
import com.example.data.StudentProfileDoc
import com.example.data.UgandaLocationMatcher
import com.example.data.UgandaRegion
import com.example.data.UiState
import com.example.data.VerifiedCatalog
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val VM_TAG = "CareerHubVM"
private const val STOP_TIMEOUT_MS = 5000L

enum class TrackerFilter(val label: String) {
    ALL("All"),
    SAVED("Saved"),
    APPLIED("Applied"),
    IMPORTED("Imported")
}

data class DashboardFilterState(
    val searchQuery: String = "",
    val selectedCategory: OpportunityCategory? = null,
    val selectedRegion: UgandaRegion = UgandaRegion.ALL,
    val selectedCity: String? = null,
    val trackerFilter: TrackerFilter = TrackerFilter.ALL,
    val sort: OpportunitySort = OpportunitySort.BEST_MATCH
) {
    val hasActiveLocationFilter: Boolean
        get() = selectedRegion != UgandaRegion.ALL || !selectedCity.isNullOrBlank()

    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
            selectedCategory != null ||
            hasActiveLocationFilter ||
            trackerFilter != TrackerFilter.ALL
}

data class HubUiData(
    val personalProfile: StudentProfileDoc?,
    val activeProfile: ActiveStudentProfile,
    val allItems: List<OpportunityCardItem>,
    val filteredItems: List<OpportunityCardItem>,
    val regionCounts: Map<UgandaRegion, Int>,
    val cityCounts: Map<String, Int>,
    val urgentCount: Int,
    val savedCount: Int,
    val appliedCount: Int,
    val strongMatchCount: Int,
    val filter: DashboardFilterState
)

data class AdvisorUiState(
    val messages: List<AdvisorMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val errorMessage: String? = null
)

class CareerHubViewModel(
    private val repository: CareerRepository,
    private val aiRepository: CareerAiRepository,
    private val currentUserId: String,
    private val defaultDisplayName: String
) : ViewModel() {

    private val _filterState = MutableStateFlow(DashboardFilterState())
    val filterState: StateFlow<DashboardFilterState> = _filterState.asStateFlow()

    private val _selectedOpportunityId = MutableStateFlow<String?>(null)
    val selectedOpportunityId: StateFlow<String?> = _selectedOpportunityId.asStateFlow()

    private val _parseState = MutableStateFlow<ParseUiState>(ParseUiState.Idle)
    val parseState: StateFlow<ParseUiState> = _parseState.asStateFlow()

    private val _rawForwardInput = MutableStateFlow("")
    val rawForwardInput: StateFlow<String> = _rawForwardInput.asStateFlow()

    private val _advisorState = MutableStateFlow(
        AdvisorUiState(
            messages = listOf(
                AdvisorMessage(
                    id = "welcome_msg",
                    fromUser = false,
                    text = "Hi! I'm your CrestPath Career Advisor. I answer strictly from our verified Ugandan internships and scholarships — I never invent application links or deadlines. Ask me which verified opportunity fits your skills best, what closes soonest, or how to close a skill gap!"
                )
            )
        )
    )
    val advisorState: StateFlow<AdvisorUiState> = _advisorState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _savingAction = MutableStateFlow(false)
    val savingAction: StateFlow<Boolean> = _savingAction.asStateFlow()

    private val profileFlow = repository.observeStudentProfile(currentUserId)
    private val applicationsFlow = repository.observeApplications(currentUserId)
    private val customOppsFlow = repository.observeCustomOpportunities(currentUserId)

    private var latestTrackersMap: Map<String, ApplicationTrackerDoc> = emptyMap()

    val hubState: StateFlow<UiState<HubUiData>> = combine(
        profileFlow,
        applicationsFlow,
        customOppsFlow,
        _filterState
    ) { profileDoc, trackers, customDocs, filter ->
        val trackersByOppId = trackers.associateBy { it.opportunityId.ifBlank { it.id } }
        latestTrackersMap = trackersByOppId

        val activeProfile = resolveActiveProfile(profileDoc)

        val customOpps = customDocs
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
            .map { it.toOpportunity() }

        val combinedOpps = VerifiedCatalog.verifiedOpportunities + customOpps

        val allItems = combinedOpps.map { opp ->
            val tracker = trackersByOppId[opp.id]
            val status = ApplicationStatus.fromRaw(tracker?.status)
            val saved = tracker?.saved == true
            val match = MatchEngine.evaluateMatch(opp, activeProfile)
            val daysLeft = MatchEngine.daysUntilDeadline(opp.deadlineIso)

            OpportunityCardItem(
                opportunity = opp,
                match = match,
                saved = saved,
                status = status,
                notes = tracker?.notes ?: "",
                appliedAtIso = tracker?.appliedAtIso ?: "",
                daysRemaining = daysLeft
            )
        }

        val query = filter.searchQuery.trim().lowercase()
        val baseFiltered = allItems.filter { item ->
            val matchesQuery = query.isEmpty() ||
                item.opportunity.title.lowercase().contains(query) ||
                item.opportunity.organization.lowercase().contains(query) ||
                item.opportunity.location.lowercase().contains(query) ||
                item.opportunity.requiredSkills.any { it.lowercase().contains(query) }

            val matchesCategory = filter.selectedCategory == null ||
                item.opportunity.category == filter.selectedCategory

            val matchesTracker = when (filter.trackerFilter) {
                TrackerFilter.ALL -> true
                TrackerFilter.SAVED -> item.saved || item.status != ApplicationStatus.NONE
                TrackerFilter.APPLIED -> item.status == ApplicationStatus.APPLIED ||
                    item.status == ApplicationStatus.INTERVIEW ||
                    item.status == ApplicationStatus.ACCEPTED
                TrackerFilter.IMPORTED -> !item.opportunity.verified
            }

            matchesQuery && matchesCategory && matchesTracker
        }

        val regionCounts = UgandaRegion.entries.associateWith { region ->
            baseFiltered.count { item ->
                UgandaLocationMatcher.matches(
                    locationText = item.opportunity.location,
                    selectedRegion = region,
                    selectedCity = null
                )
            }
        }

        val candidateCities = (UgandaLocationMatcher.popularCities + UgandaRegion.entries.flatMap { it.cities }).distinct()
        val cityCounts = candidateCities.associateWith { city ->
            baseFiltered.count { item ->
                UgandaLocationMatcher.matchesCity(item.opportunity.location, city)
            }
        }

        val filtered = baseFiltered.filter { item ->
            UgandaLocationMatcher.matches(
                locationText = item.opportunity.location,
                selectedRegion = filter.selectedRegion,
                selectedCity = filter.selectedCity
            )
        }

        val sorted = when (filter.sort) {
            OpportunitySort.BEST_MATCH -> filtered.sortedWith(
                compareByDescending<OpportunityCardItem> { it.match.scorePercent }
                    .thenBy { it.daysRemaining ?: Long.MAX_VALUE }
            )
            OpportunitySort.DEADLINE_SOON -> filtered.sortedWith(
                compareBy<OpportunityCardItem> {
                    val d = it.daysRemaining
                    if (d == null || d < 0) Long.MAX_VALUE else d
                }.thenByDescending { it.match.scorePercent }
            )
            OpportunitySort.LOCATION_AZ -> filtered.sortedWith(
                compareBy<OpportunityCardItem> { UgandaLocationMatcher.primaryRegion(it.opportunity.location).sortOrder }
                    .thenBy { UgandaLocationMatcher.primaryCityLabel(it.opportunity.location).lowercase() }
                    .thenByDescending { it.match.scorePercent }
            )
        }

        val urgentCount = allItems.count { item ->
            val days = item.daysRemaining
            days != null && days in 0..14 && item.status != ApplicationStatus.APPLIED &&
                item.status != ApplicationStatus.INTERVIEW && item.status != ApplicationStatus.ACCEPTED
        }
        val savedCount = allItems.count { it.saved || it.status != ApplicationStatus.NONE }
        val appliedCount = allItems.count {
            it.status == ApplicationStatus.APPLIED ||
                it.status == ApplicationStatus.INTERVIEW ||
                it.status == ApplicationStatus.ACCEPTED
        }
        val strongMatchCount = allItems.count { it.opportunity.verified && it.match.scorePercent >= 75 }

        HubUiData(
            personalProfile = profileDoc,
            activeProfile = activeProfile,
            allItems = allItems,
            filteredItems = sorted,
            regionCounts = regionCounts,
            cityCounts = cityCounts,
            urgentCount = urgentCount,
            savedCount = savedCount,
            appliedCount = appliedCount,
            strongMatchCount = strongMatchCount,
            filter = filter
        )
    }
        .map<HubUiData, UiState<HubUiData>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(VM_TAG, "Error observing career hub streams", rootError)
            emit(UiState.Error(rootError.message ?: "Unable to sync your career workspace."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = UiState.Loading
        )

    init {
        viewModelScope.launch {
            val result = repository.ensureProfileInitialized(
                userId = currentUserId,
                defaultName = defaultDisplayName
            )
            result.onFailure { err ->
                Log.w(VM_TAG, "Initial profile bootstrap warning: ${err.message}")
            }
        }
    }

    private fun resolveActiveProfile(doc: StudentProfileDoc?): ActiveStudentProfile {
        val demoId = doc?.activeDemoProfileId.orEmpty()
        if (demoId.isNotBlank()) {
            val found = VerifiedCatalog.demoProfiles.firstOrNull { it.profileKey == demoId }
            if (found != null) return found
        }
        return ActiveStudentProfile(
            profileKey = "",
            fullName = doc?.fullName?.ifBlank { defaultDisplayName } ?: defaultDisplayName.ifBlank { "Ugandan Scholar" },
            university = doc?.university?.ifBlank { "Makerere University" } ?: "Makerere University",
            program = doc?.program?.ifBlank { "BSc Computer Science" } ?: "BSc Computer Science",
            yearOfStudy = (doc?.yearOfStudy ?: 3L).toInt().coerceIn(1, 7),
            gpa = doc?.gpa ?: "4.20",
            skills = doc?.skills?.ifEmpty { listOf("Kotlin", "Python", "SQL", "Git") }
                ?: listOf("Kotlin", "Python", "SQL", "Git"),
            interests = doc?.interests?.ifEmpty { listOf("Software Engineering", "Scholarships") }
                ?: listOf("Software Engineering", "Scholarships"),
            demoMode = false
        )
    }

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: OpportunityCategory?) {
        _filterState.update {
            it.copy(selectedCategory = if (it.selectedCategory == category) null else category)
        }
    }

    fun selectTrackerFilter(filter: TrackerFilter) {
        _filterState.update { it.copy(trackerFilter = filter) }
    }

    fun selectSort(sort: OpportunitySort) {
        _filterState.update { it.copy(sort = sort) }
    }

    fun selectRegion(region: UgandaRegion) {
        _filterState.update { current ->
            val nextCity = current.selectedCity?.takeIf { city ->
                region == UgandaRegion.ALL || region.cities.any { it.equals(city, ignoreCase = true) }
            }
            current.copy(
                selectedRegion = region,
                selectedCity = nextCity
            )
        }
    }

    fun selectCity(city: String?) {
        _filterState.update { current ->
            val toggledCity = if (city != null && current.selectedCity.equals(city, ignoreCase = true)) {
                null
            } else {
                city
            }
            val alignedRegion = if (toggledCity != null && current.selectedRegion != UgandaRegion.ALL) {
                UgandaLocationMatcher.regionForCity(toggledCity) ?: current.selectedRegion
            } else {
                current.selectedRegion
            }
            current.copy(
                selectedRegion = alignedRegion,
                selectedCity = toggledCity
            )
        }
    }

    fun clearLocationFilter() {
        _filterState.update {
            it.copy(
                selectedRegion = UgandaRegion.ALL,
                selectedCity = null
            )
        }
    }

    fun clearAllFilters() {
        _filterState.update { current ->
            DashboardFilterState(sort = current.sort)
        }
    }

    fun selectOpportunity(opportunityId: String?) {
        _selectedOpportunityId.value = opportunityId
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    fun switchDemoProfile(demoProfileId: String) {
        viewModelScope.launch {
            _savingAction.value = true
            val result = repository.setActiveDemoProfile(currentUserId, demoProfileId)
            _savingAction.value = false
            result.onSuccess {
                val label = VerifiedCatalog.demoProfiles.firstOrNull { it.profileKey == demoProfileId }?.fullName
                    ?: "your personal profile"
                _snackbarMessage.value = "Switched active profile to $label"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to switch profile"
            }
        }
    }

    fun savePersonalProfile(
        fullName: String,
        university: String,
        program: String,
        yearOfStudy: Int,
        gpa: String,
        skills: List<String>,
        interests: List<String>
    ) {
        viewModelScope.launch {
            _savingAction.value = true
            val result = repository.updateStudentProfile(
                userId = currentUserId,
                fullName = fullName,
                university = university,
                program = program,
                yearOfStudy = yearOfStudy,
                gpa = gpa,
                skills = skills,
                interests = interests
            )
            _savingAction.value = false
            result.onSuccess {
                _snackbarMessage.value = "Student profile updated and match scores refreshed."
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Could not save profile."
            }
        }
    }

    fun toggleSaved(item: OpportunityCardItem) {
        viewModelScope.launch {
            val nextSaved = !item.saved
            val result = repository.toggleSaved(
                userId = currentUserId,
                opportunityId = item.opportunity.id,
                existingTracker = latestTrackersMap[item.opportunity.id],
                newSaved = nextSaved
            )
            result.onSuccess {
                _snackbarMessage.value = if (nextSaved) {
                    "Saved ${item.opportunity.title}"
                } else {
                    "Removed bookmark"
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to update bookmark"
            }
        }
    }

    fun updateApplicationStatus(
        item: OpportunityCardItem,
        newStatus: ApplicationStatus,
        notes: String = item.notes
    ) {
        viewModelScope.launch {
            val result = repository.updateApplicationTracking(
                userId = currentUserId,
                opportunityId = item.opportunity.id,
                existingTracker = latestTrackersMap[item.opportunity.id],
                saved = item.saved || newStatus != ApplicationStatus.NONE,
                status = newStatus,
                notes = notes
            )
            result.onSuccess {
                _snackbarMessage.value = "Marked as ${newStatus.label}"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to update application status"
            }
        }
    }

    fun deleteImportedOpportunity(opportunityId: String) {
        viewModelScope.launch {
            val result = repository.deleteCustomOpportunity(currentUserId, opportunityId)
            result.onSuccess {
                if (_selectedOpportunityId.value == opportunityId) {
                    _selectedOpportunityId.value = null
                }
                _snackbarMessage.value = "Deleted imported opportunity"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to delete opportunity"
            }
        }
    }

    // --- AI FORWARD PARSER ---
    fun updateRawForwardInput(text: String) {
        _rawForwardInput.value = text
    }

    fun loadSampleForward(sampleText: String) {
        _rawForwardInput.value = sampleText
        _parseState.value = ParseUiState.Idle
    }

    fun parseCurrentForward() {
        val text = _rawForwardInput.value.trim()
        if (text.length < 20) {
            _parseState.value = ParseUiState.Error(
                "Paste a WhatsApp forward or LinkedIn post (at least 20 characters) to extract details."
            )
            return
        }
        viewModelScope.launch {
            _parseState.value = ParseUiState.Parsing
            val result = aiRepository.parseForwardText(text)
            result.onSuccess { draft ->
                _parseState.value = ParseUiState.Ready(draft)
            }.onFailure { err ->
                _parseState.value = ParseUiState.Error(
                    err.localizedMessage ?: "AI extraction failed. Check your connection and try again."
                )
            }
        }
    }

    fun updateParsedDraft(updated: ParsedOpportunityDraft) {
        _parseState.value = ParseUiState.Ready(updated)
    }

    fun clearParser() {
        _rawForwardInput.value = ""
        _parseState.value = ParseUiState.Idle
    }

    fun saveParsedOpportunity(draft: ParsedOpportunityDraft, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            _savingAction.value = true
            val result = repository.saveCustomOpportunity(currentUserId, draft)
            _savingAction.value = false
            result.onSuccess {
                _parseState.value = ParseUiState.Idle
                _rawForwardInput.value = ""
                _snackbarMessage.value = "Structured card saved to your Imported opportunities!"
                onSaved()
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed to save opportunity card."
            }
        }
    }

    // --- VERIFIED-ONLY AI CAREER ADVISOR ---
    fun askAdvisor(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank() || _advisorState.value.isStreaming) return

        val currentHub = (hubState.value as? UiState.Success)?.data
        if (currentHub == null) {
            _advisorState.update { it.copy(errorMessage = "Please wait for your opportunities to finish loading.") }
            return
        }

        val userMsg = AdvisorMessage(
            id = UUID.randomUUID().toString(),
            fromUser = true,
            text = trimmed
        )
        val assistantMsgId = UUID.randomUUID().toString()
        val placeholderAssistant = AdvisorMessage(
            id = assistantMsgId,
            fromUser = false,
            text = "",
            streaming = true
        )

        val historyBeforeSend = _advisorState.value.messages
        _advisorState.update {
            it.copy(
                messages = it.messages + userMsg + placeholderAssistant,
                isStreaming = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                var finalReply = ""
                aiRepository.streamVerifiedAdvisorReply(
                    userQuestion = trimmed,
                    studentProfile = currentHub.activeProfile,
                    verifiedItems = currentHub.allItems,
                    recentHistory = historyBeforeSend
                ).collect { partialText ->
                    finalReply = partialText
                    val citedIds = extractCitedVerifiedIds(partialText, currentHub.allItems)
                    _advisorState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMsgId) {
                                    msg.copy(
                                        text = partialText,
                                        citedOpportunityIds = citedIds,
                                        streaming = true
                                    )
                                } else {
                                    msg
                                }
                            }
                        )
                    }
                }

                val finalCited = extractCitedVerifiedIds(finalReply, currentHub.allItems)
                _advisorState.update { state ->
                    state.copy(
                        isStreaming = false,
                        messages = state.messages.map { msg ->
                            if (msg.id == assistantMsgId) {
                                msg.copy(
                                    text = finalReply,
                                    citedOpportunityIds = finalCited,
                                    streaming = false
                                )
                            } else {
                                msg
                            }
                        }
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(VM_TAG, "Advisor streaming timed out", e)
                failAdvisorMessage(
                    assistantMsgId,
                    "The advisor timed out after 60 seconds. Please check your network and retry."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(VM_TAG, "Advisor streaming failed", e)
                failAdvisorMessage(
                    assistantMsgId,
                    e.localizedMessage ?: "Advisor could not respond right now. Please try again."
                )
            }
        }
    }

    fun clearAdvisorChat() {
        if (_advisorState.value.isStreaming) return
        _advisorState.value = AdvisorUiState(
            messages = listOf(
                AdvisorMessage(
                    id = "welcome_reset",
                    fromUser = false,
                    text = "Chat cleared. Ask me anything about CrestPath's verified internships and scholarships — I'll evaluate them honestly against your active student profile."
                )
            )
        )
    }

    private fun failAdvisorMessage(assistantMsgId: String, errorText: String) {
        _advisorState.update { state ->
            state.copy(
                isStreaming = false,
                errorMessage = errorText,
                messages = state.messages.filterNot { it.id == assistantMsgId && it.text.isBlank() }
                    .map { msg ->
                        if (msg.id == assistantMsgId) msg.copy(streaming = false) else msg
                    }
            )
        }
    }

    private fun extractCitedVerifiedIds(
        replyText: String,
        items: List<OpportunityCardItem>
    ): List<String> {
        val lower = replyText.lowercase()
        return items
            .filter { it.opportunity.verified }
            .filter { item ->
                val opp = item.opportunity
                lower.contains(opp.title.lowercase()) ||
                    lower.contains(opp.organization.lowercase()) ||
                    (opp.officialUrl.isNotBlank() && lower.contains(opp.officialUrl.lowercase()))
            }
            .map { it.opportunity.id }
            .distinct()
            .take(4)
    }
}
