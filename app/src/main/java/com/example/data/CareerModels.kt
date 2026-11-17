package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class OpportunityCategory(val label: String) {
    INTERNSHIP("Internship"),
    SCHOLARSHIP("Scholarship"),
    FELLOWSHIP("Fellowship");

    companion object {
        fun fromRaw(raw: String?): OpportunityCategory =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: INTERNSHIP
    }
}

enum class ApplicationStatus(val label: String) {
    NONE("Not Started"),
    SAVED("Saved"),
    IN_PROGRESS("Preparing"),
    APPLIED("Applied"),
    INTERVIEW("Interview"),
    ACCEPTED("Accepted");

    companion object {
        fun fromRaw(raw: String?): ApplicationStatus =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: NONE
    }
}

enum class OpportunitySort(val label: String) {
    BEST_MATCH("Best Match"),
    DEADLINE_SOON("Earliest Deadline"),
    LOCATION_AZ("Region / City")
}

enum class UgandaRegion(
    val label: String,
    val shortLabel: String,
    val cities: List<String>,
    val sortOrder: Int
) {
    ALL("All Uganda", "All Uganda", emptyList(), 0),
    CENTRAL("Central Uganda", "Central", listOf("Kampala", "Entebbe", "Wakiso", "Mukono"), 1),
    WESTERN("Western Uganda", "Western", listOf("Mbarara", "Kabale", "Fort Portal"), 2),
    EASTERN("Eastern Uganda", "Eastern", listOf("Jinja", "Mbale", "Busitema", "Tororo"), 3),
    NORTHERN("Northern Uganda", "Northern", listOf("Gulu", "Lira", "Arua"), 4),
    NATIONWIDE("Nationwide / Multi-Region", "Nationwide", emptyList(), 5)
}

object UgandaLocationMatcher {

    val popularCities: List<String> = listOf(
        "Kampala",
        "Entebbe",
        "Mbarara",
        "Jinja",
        "Mbale",
        "Gulu"
    )

    private val kampalaAreaKeywords = listOf(
        "kampala", "kyebando", "mulago", "nakawa", "makerere",
        "bugolobi", "kololo", "ntinda", "kawempe", "makindye",
        "rubaga", "crested towers", "mapeera", "kyambogo", "mubs"
    )

    private val cityAliases: Map<String, List<String>> = mapOf(
        "Kampala" to kampalaAreaKeywords,
        "Entebbe" to listOf("entebbe", "uvri"),
        "Wakiso" to listOf("wakiso", "entebbe", "nansana", "kira", "gayaza"),
        "Mukono" to listOf("mukono", "ucu"),
        "Mbarara" to listOf("mbarara", "must"),
        "Kabale" to listOf("kabale", "kigezi"),
        "Fort Portal" to listOf("fort portal", "kabarole", "tooro"),
        "Jinja" to listOf("jinja", "nalubaale", "bujagali", "busoga"),
        "Mbale" to listOf("mbale", "elgon", "sironko"),
        "Busitema" to listOf("busitema", "tororo", "busia"),
        "Tororo" to listOf("tororo", "busitema"),
        "Gulu" to listOf("gulu", "acholi"),
        "Lira" to listOf("lira", "lango", "ngetta"),
        "Arua" to listOf("arua", "west nile")
    )

    private val nationwideKeywords = listOf(
        "nationwide", "regional offices", "partner universities",
        "partner campuses", "east africa", "all regions", "remote"
    )

    fun regionForCity(city: String): UgandaRegion? =
        UgandaRegion.entries.firstOrNull { region ->
            region.cities.any { it.equals(city, ignoreCase = true) }
        }

    fun detectRegions(locationText: String): Set<UgandaRegion> {
        val lower = locationText.lowercase()
        val detected = mutableSetOf<UgandaRegion>()

        if (lower.contains("central") ||
            UgandaRegion.CENTRAL.cities.any { matchesCity(locationText, it) }
        ) {
            detected.add(UgandaRegion.CENTRAL)
        }
        if (lower.contains("western") ||
            UgandaRegion.WESTERN.cities.any { matchesCity(locationText, it) }
        ) {
            detected.add(UgandaRegion.WESTERN)
        }
        if (lower.contains("eastern") ||
            UgandaRegion.EASTERN.cities.any { matchesCity(locationText, it) }
        ) {
            detected.add(UgandaRegion.EASTERN)
        }
        if (lower.contains("northern") ||
            UgandaRegion.NORTHERN.cities.any { matchesCity(locationText, it) }
        ) {
            detected.add(UgandaRegion.NORTHERN)
        }
        if (nationwideKeywords.any { lower.contains(it) } || detected.size >= 2) {
            detected.add(UgandaRegion.NATIONWIDE)
        }

        return detected
    }

    fun matchesCity(locationText: String, city: String): Boolean {
        val lower = locationText.lowercase()
        val aliases = cityAliases[city] ?: listOf(city.lowercase())
        return aliases.any { lower.contains(it) }
    }

    fun matches(
        locationText: String,
        selectedRegion: UgandaRegion,
        selectedCity: String?
    ): Boolean {
        if (!selectedCity.isNullOrBlank()) {
            return matchesCity(locationText, selectedCity)
        }
        if (selectedRegion == UgandaRegion.ALL) return true
        return selectedRegion in detectRegions(locationText)
    }

    fun primaryRegion(locationText: String): UgandaRegion {
        val regions = detectRegions(locationText)
        return when {
            regions.isEmpty() -> UgandaRegion.NATIONWIDE
            regions.size > 1 && UgandaRegion.NATIONWIDE in regions && regions.count { it != UgandaRegion.NATIONWIDE } >= 2 ->
                UgandaRegion.NATIONWIDE
            UgandaRegion.CENTRAL in regions -> UgandaRegion.CENTRAL
            UgandaRegion.WESTERN in regions -> UgandaRegion.WESTERN
            UgandaRegion.EASTERN in regions -> UgandaRegion.EASTERN
            UgandaRegion.NORTHERN in regions -> UgandaRegion.NORTHERN
            else -> UgandaRegion.NATIONWIDE
        }
    }

    fun primaryCityLabel(locationText: String): String {
        val orderedCities = UgandaRegion.entries.flatMap { it.cities }
        return orderedCities.firstOrNull { matchesCity(locationText, it) }
            ?: locationText.trim()
    }
}

data class Opportunity(
    val id: String,
    val title: String,
    val organization: String,
    val category: OpportunityCategory,
    val location: String,
    val deadlineText: String,
    val deadlineIso: String,
    val stipendOrValue: String,
    val eligibility: String,
    val requirements: List<String>,
    val requiredSkills: List<String>,
    val preferredFields: List<String> = emptyList(),
    val officialUrl: String,
    val verifiedSource: String,
    val verified: Boolean,
    val summary: String,
    val verificationWarnings: List<String> = emptyList()
)

@Keep
@IgnoreExtraProperties
data class StudentProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val university: String = "",
    val program: String = "",
    val yearOfStudy: Long = 3L,
    val gpa: String = "",
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val activeDemoProfileId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class ActiveStudentProfile(
    val profileKey: String,
    val fullName: String,
    val university: String,
    val program: String,
    val yearOfStudy: Int,
    val gpa: String,
    val skills: List<String>,
    val interests: List<String>,
    val demoMode: Boolean,
    val demoTagline: String = ""
)

@Keep
@IgnoreExtraProperties
data class ApplicationTrackerDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val opportunityId: String = "",
    val saved: Boolean = false,
    val status: String = ApplicationStatus.NONE.name,
    val notes: String = "",
    val appliedAtIso: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CustomOpportunityDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val organization: String = "",
    val category: String = OpportunityCategory.INTERNSHIP.name,
    val location: String = "",
    val deadlineText: String = "",
    val deadlineIso: String = "",
    val stipendOrValue: String = "",
    val eligibility: String = "",
    val requirements: List<String> = emptyList(),
    val requiredSkills: List<String> = emptyList(),
    val officialUrl: String = "",
    val summary: String = "",
    val verificationWarnings: List<String> = emptyList(),
    val verified: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toOpportunity(): Opportunity = Opportunity(
        id = id,
        title = title,
        organization = organization,
        category = OpportunityCategory.fromRaw(category),
        location = location.ifBlank { "Location not specified" },
        deadlineText = deadlineText.ifBlank { "Deadline not stated" },
        deadlineIso = deadlineIso,
        stipendOrValue = stipendOrValue.ifBlank { "Not specified" },
        eligibility = eligibility.ifBlank { "Check original post for eligibility details." },
        requirements = requirements,
        requiredSkills = requiredSkills,
        preferredFields = emptyList(),
        officialUrl = officialUrl,
        verifiedSource = "Imported Forward (Unverified)",
        verified = false,
        summary = summary,
        verificationWarnings = verificationWarnings
    )
}

data class MatchResult(
    val scorePercent: Int,
    val matchedSkills: List<String>,
    val missingSkills: List<String>,
    val fieldMatched: Boolean,
    val summaryLabel: String,
    val skillAlignmentPercent: Int = scorePercent
)

data class OpportunityCardItem(
    val opportunity: Opportunity,
    val match: MatchResult,
    val saved: Boolean,
    val status: ApplicationStatus,
    val notes: String,
    val appliedAtIso: String,
    val daysRemaining: Long?
)

data class ParsedOpportunityDraft(
    val title: String,
    val organization: String,
    val category: OpportunityCategory,
    val location: String,
    val deadlineText: String,
    val deadlineIso: String,
    val stipendOrValue: String,
    val eligibility: String,
    val requirements: List<String>,
    val requiredSkills: List<String>,
    val officialUrl: String,
    val summary: String,
    val verificationWarnings: List<String>
)

data class AdvisorMessage(
    val id: String,
    val fromUser: Boolean,
    val text: String,
    val citedOpportunityIds: List<String> = emptyList(),
    val streaming: Boolean = false
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface ParseUiState {
    data object Idle : ParseUiState
    data object Parsing : ParseUiState
    data class Ready(val draft: ParsedOpportunityDraft) : ParseUiState
    data class Error(val message: String) : ParseUiState
}
