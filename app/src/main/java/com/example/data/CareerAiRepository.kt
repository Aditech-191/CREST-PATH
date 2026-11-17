package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val AI_TAG = "CareerAiRepository"

class CareerAiRepository {

    private val parserSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Clean structured role or scholarship title"),
            "organization" to Schema.string(description = "Host organization or sponsor name"),
            "category" to Schema.enumeration(
                values = listOf("INTERNSHIP", "SCHOLARSHIP", "FELLOWSHIP"),
                description = "Category of opportunity"
            ),
            "location" to Schema.string(description = "Location or remote/hybrid setup if mentioned, else 'Not specified'"),
            "deadlineText" to Schema.string(description = "Human-readable deadline from text, or 'Not specified' if absent. NEVER invent a date."),
            "deadlineIso" to Schema.string(description = "YYYY-MM-DD date ONLY if exact day, month, and year are stated in text; otherwise empty string ''."),
            "stipendOrValue" to Schema.string(description = "Stipend, allowance, or scholarship coverage if stated, else 'Not specified'"),
            "eligibility" to Schema.string(description = "Concise summary of who can apply based strictly on the post"),
            "requirements" to Schema.array(
                items = Schema.string(),
                description = "List of required documents or application steps explicitly stated"
            ),
            "requiredSkills" to Schema.array(
                items = Schema.string(),
                description = "List of concise technical or soft skill keywords mentioned in the post (max 10)"
            ),
            "officialUrl" to Schema.string(description = "URL ONLY if explicitly present verbatim in the pasted text; otherwise empty string ''."),
            "summary" to Schema.string(description = "2-sentence clean summary of the opportunity without hype"),
            "verificationWarnings" to Schema.array(
                items = Schema.string(),
                description = "Concrete safety or verification warnings (e.g., personal Gmail address used, WhatsApp number only, missing official URL, missing deadline, asks for payment)"
            )
        )
    )

    private val parserModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = parserSchema
            },
            systemInstruction = content {
                text(
                    """
                    You are an accurate opportunity extractor for Ugandan university students.
                    Convert messy WhatsApp forwards, Telegram posts, or LinkedIn announcements into clean structured JSON.
                    STRICT SAFETY RULES:
                    1. NEVER invent URLs, email addresses, deadlines, stipends, or organization names not present in the input text.
                    2. If a deadline or link is missing, set deadlineText to "Not specified", deadlineIso to "", and officialUrl to "".
                    3. Always inspect for student safety risks and add concise warnings to verificationWarnings when appropriate:
                       - Free email domain (gmail.com / yahoo.com) instead of an official organization domain
                       - Phone / WhatsApp number only with no official website
                       - Any mention of application fee or registration fee (flag strongly as potential scam)
                       - Missing deadline or vague eligibility
                    4. Keep text crisp, factual, and tailored for Ugandan university students.
                    """.trimIndent()
                )
            }
        )
    }

    private val advisorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are CrestPath Career Advisor, a warm, honest, grounded career mentor for Ugandan university students.

                    ABSOLUTE GROUNDING RULES (NEVER BREAK):
                    1. You may ONLY recommend, cite, or give details about opportunities present in the VERIFIED_OPPORTUNITIES_CATALOG provided in the prompt.
                    2. NEVER invent, guess, or recall external internships, scholarships, URLs, application portals, emails, stipends, or deadlines outside the provided VERIFIED_OPPORTUNITIES_CATALOG.
                    3. If the student asks about an opportunity NOT in the VERIFIED_OPPORTUNITIES_CATALOG (or asks for a link/detail that isn't listed), clearly state: "I can only advise from CrestPath's verified opportunities, and that listing is not in our verified catalog."
                    4. Always be honest about skill fit: celebrate matched skills AND constructively point out missing skills or eligibility gaps (such as CGPA or academic year requirements) so students invest their application time wisely.
                    5. Whenever you cite a verified opportunity, include its exact title and exact official URL and deadline from the catalog verbatim.
                    6. Keep responses structured, concise, practical, and encouraging for Ugandan university students (Makerere, Kyambogo, MUST, MUBS, UCU, Gulu, etc.).
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun parseForwardText(rawText: String): Result<ParsedOpportunityDraft> {
        return try {
            val trimmed = rawText.trim()
            if (trimmed.length < 20) {
                return Result.failure(
                    IllegalArgumentException("Please paste a fuller WhatsApp forward or LinkedIn post (at least 20 characters).")
                )
            }

            val response = withTimeout(60_000L) {
                parserModel.generateContent(
                    "Extract a structured opportunity card from this pasted message. Today's date is ${MatchEngine.todayIso()}.\n\nMESSAGE:\n$trimmed"
                )
            }

            val jsonString = response.text
                ?: return Result.failure(IllegalStateException("AI parser returned an empty response."))

            val json = JSONObject(jsonString)

            fun readStringList(key: String, max: Int): List<String> {
                val arr = json.optJSONArray(key) ?: return emptyList()
                return (0 until arr.length())
                    .mapNotNull { idx -> arr.optString(idx)?.trim()?.takeIf { it.isNotEmpty() } }
                    .distinct()
                    .take(max)
            }

            val rawUrl = json.optString("officialUrl", "").trim()
            // Double-check grounding: only keep URL if its host/string actually appeared in the pasted input
            val groundedUrl = if (rawUrl.isNotBlank() && trimmed.contains(rawUrl.removePrefix("https://").removePrefix("http://"), ignoreCase = true)) {
                rawUrl
            } else {
                ""
            }

            val warnings = readStringList("verificationWarnings", 10).toMutableList()
            if (groundedUrl.isBlank() && warnings.none { it.contains("link", ignoreCase = true) || it.contains("URL", ignoreCase = true) }) {
                warnings.add("No official application link detected — verify with the organization's official website before sharing documents.")
            }

            val draft = ParsedOpportunityDraft(
                title = json.optString("title", "Untitled Opportunity").trim().take(160),
                organization = json.optString("organization", "Unspecified Organization").trim().take(120),
                category = OpportunityCategory.fromRaw(json.optString("category", "INTERNSHIP")),
                location = json.optString("location", "Not specified").trim().take(120),
                deadlineText = json.optString("deadlineText", "Not specified").trim().take(80),
                deadlineIso = json.optString("deadlineIso", "").trim().take(40),
                stipendOrValue = json.optString("stipendOrValue", "Not specified").trim().take(120),
                eligibility = json.optString("eligibility", "Not specified").trim().take(600),
                requirements = readStringList("requirements", 20),
                requiredSkills = readStringList("requiredSkills", 15),
                officialUrl = groundedUrl.take(300),
                summary = json.optString("summary", "").trim().take(1000),
                verificationWarnings = warnings.distinct().take(10)
            )
            Result.success(draft)
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Opportunity parsing timed out", e)
            Result.failure(Exception("AI parsing timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Failed to parse opportunity forward", e)
            Result.failure(Exception(e.localizedMessage ?: "Failed to parse the pasted text."))
        }
    }

    fun streamVerifiedAdvisorReply(
        userQuestion: String,
        studentProfile: ActiveStudentProfile,
        verifiedItems: List<OpportunityCardItem>,
        recentHistory: List<AdvisorMessage>
    ): Flow<String> = flow {
        val verifiedOnly = verifiedItems.filter { it.opportunity.verified }

        val catalogBlock = buildString {
            appendLine("VERIFIED_OPPORTUNITIES_CATALOG (${verifiedOnly.size} verified listings as of ${MatchEngine.todayIso()}):")
            verifiedOnly.forEachIndexed { idx, item ->
                val opp = item.opportunity
                val daysText = item.daysRemaining?.let { days ->
                    when {
                        days < 0 -> "CLOSED (${-days} days ago)"
                        days == 0L -> "CLOSES TODAY"
                        else -> "$days days left"
                    }
                } ?: "Deadline date check required"

                appendLine(
                    """
                    [${idx + 1}] ID=${opp.id}
                    Title: ${opp.title}
                    Organization: ${opp.organization}
                    Category: ${opp.category.label}
                    Location: ${opp.location}
                    Deadline: ${opp.deadlineText} (ISO: ${opp.deadlineIso}, $daysText)
                    Stipend/Value: ${opp.stipendOrValue}
                    Eligibility: ${opp.eligibility}
                    Required Skills: ${opp.requiredSkills.joinToString(", ")}
                    Requirements: ${opp.requirements.joinToString("; ")}
                    Official URL: ${opp.officialUrl}
                    Verified Source: ${opp.verifiedSource}
                    Student Match Score: ${item.match.scorePercent}% (${item.match.summaryLabel}) | Matched Skills: ${item.match.matchedSkills.joinToString(", ").ifBlank { "None" }} | Missing Skills: ${item.match.missingSkills.joinToString(", ").ifBlank { "None" }}
                    Student Tracking Status: ${item.status.label} (Saved=${item.saved})
                    """.trimIndent()
                )
                appendLine("---")
            }
        }

        val profileBlock = """
            ACTIVE_STUDENT_PROFILE:
            Name: ${studentProfile.fullName}
            University: ${studentProfile.university}
            Program: ${studentProfile.program}
            Year of Study: Year ${studentProfile.yearOfStudy}
            GPA: ${studentProfile.gpa.ifBlank { "Not provided" }}
            Skills: ${studentProfile.skills.joinToString(", ").ifBlank { "None listed" }}
            Interests: ${studentProfile.interests.joinToString(", ").ifBlank { "None listed" }}
        """.trimIndent()

        val historyBlock = recentHistory.takeLast(6).joinToString("\n") { msg ->
            val role = if (msg.fromUser) "Student" else "Advisor"
            "$role: ${msg.text}"
        }

        val fullPrompt = buildString {
            appendLine(profileBlock)
            appendLine()
            appendLine(catalogBlock)
            if (historyBlock.isNotBlank()) {
                appendLine("RECENT_CONVERSATION:")
                appendLine(historyBlock)
                appendLine()
            }
            appendLine("STUDENT_QUESTION:")
            appendLine(userQuestion.trim())
            appendLine()
            appendLine("Respond directly to STUDENT_QUESTION using ONLY facts from VERIFIED_OPPORTUNITIES_CATALOG above. Do not fabricate any links or deadlines.")
        }

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            advisorModel.generateContentStream(fullPrompt).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    accumulated.append(chunkText)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("The career advisor returned an empty response. Please try again.")
        }
    }
}
