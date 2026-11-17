package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object MatchEngine {

    fun evaluateMatch(
        opportunity: Opportunity,
        profile: ActiveStudentProfile
    ): MatchResult {
        val studentSkillsLower = profile.skills.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        val reqSkills = opportunity.requiredSkills.map { it.trim() }.filter { it.isNotEmpty() }

        val matched = mutableListOf<String>()
        val missing = mutableListOf<String>()

        for (req in reqSkills) {
            val reqLower = req.lowercase()
            val isMatch = studentSkillsLower.any { studentSkill ->
                studentSkill == reqLower ||
                    studentSkill.contains(reqLower) ||
                    reqLower.contains(studentSkill)
            }
            if (isMatch) matched.add(req) else missing.add(req)
        }

        val skillRatio = if (reqSkills.isEmpty()) 0.55 else matched.size.toDouble() / reqSkills.size.toDouble()

        val programAndInterests = (listOf(profile.program) + profile.interests)
            .joinToString(" ")
            .lowercase()

        val fieldMatched = opportunity.preferredFields.any { field ->
            val words = field.lowercase().split(" ").filter { it.length >= 3 }
            words.any { word -> programAndInterests.contains(word) }
        } || opportunity.eligibility.lowercase().let { elig ->
            profile.program.lowercase().split(" ").filter { it.length >= 4 }.any { elig.contains(it) }
        }

        val yearBonus = if (profile.yearOfStudy in 2..4) 10 else 5
        val fieldBonus = if (fieldMatched) 20 else 4
        val skillScore = (skillRatio * 70.0).toInt()

        val total = (skillScore + fieldBonus + yearBonus).coerceIn(15, 99)

        val label = when {
            total >= 80 -> "Strong Fit"
            total >= 60 -> "Good Match"
            total >= 42 -> "Partial Fit"
            else -> "Stretch Goal"
        }

        val skillAlignmentPercent = if (reqSkills.isEmpty()) {
            total
        } else {
            ((matched.size.toDouble() / reqSkills.size.toDouble()) * 100.0)
                .toInt()
                .coerceIn(0, 100)
        }

        return MatchResult(
            scorePercent = total,
            matchedSkills = matched,
            missingSkills = missing,
            fieldMatched = fieldMatched,
            summaryLabel = label,
            skillAlignmentPercent = skillAlignmentPercent
        )
    }

    fun daysUntilDeadline(deadlineIso: String): Long? {
        if (deadlineIso.isBlank()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
            }
            val targetDate = sdf.parse(deadlineIso.trim()) ?: return null
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val targetCal = Calendar.getInstance().apply {
                time = targetDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diffMs = targetCal.timeInMillis - todayCal.timeInMillis
            TimeUnit.MILLISECONDS.toDays(diffMs)
        } catch (_: Exception) {
            null
        }
    }

    fun todayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }
}
