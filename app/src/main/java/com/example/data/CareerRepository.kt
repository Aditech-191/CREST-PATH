package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CareerRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUid: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(uid == expectedUid) { "Signed-in account changed; refusing cross-user Firestore operation." }
        return uid
    }

    fun observeStudentProfile(userId: String): Flow<StudentProfileDoc?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        StudentProfileDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                logIfStillSignedIn(error, OperationType.GET, path, userId)
                throw error
            }
    }

    fun observeApplications(userId: String): Flow<List<ApplicationTrackerDoc>> {
        val path = "users/$userId/applications"
        return db.collection("users").document(userId)
            .collection("applications")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ApplicationTrackerDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                logIfStillSignedIn(error, OperationType.LIST, path, userId)
                throw error
            }
    }

    fun observeCustomOpportunities(userId: String): Flow<List<CustomOpportunityDoc>> {
        val path = "users/$userId/custom_opportunities"
        return db.collection("users").document(userId)
            .collection("custom_opportunities")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CustomOpportunityDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                logIfStillSignedIn(error, OperationType.LIST, path, userId)
                throw error
            }
    }

    suspend fun ensureProfileInitialized(
        userId: String,
        defaultName: String
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val safeName = defaultName.trim().ifBlank { "Ugandan Scholar" }.take(100)
                val payload = mapOf(
                    "userId" to uid,
                    "fullName" to safeName,
                    "university" to "Makerere University",
                    "program" to "BSc Computer Science",
                    "yearOfStudy" to 3,
                    "gpa" to "4.20",
                    "skills" to listOf("Kotlin", "Python", "SQL", "Git", "Data Analysis"),
                    "interests" to listOf("Software Engineering", "Scholarships", "FinTech"),
                    "activeDemoProfileId" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateStudentProfile(
        userId: String,
        fullName: String,
        university: String,
        program: String,
        yearOfStudy: Int,
        gpa: String,
        skills: List<String>,
        interests: List<String>
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val safeName = fullName.trim().ifBlank { "Ugandan Scholar" }.take(100)
            val safeUni = university.trim().ifBlank { "Makerere University" }.take(120)
            val safeProg = program.trim().ifBlank { "Undergraduate Studies" }.take(120)
            val safeYear = yearOfStudy.coerceIn(1, 7)
            val safeGpa = gpa.trim().take(20)
            val safeSkills = skills.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(25)
            val safeInterests = interests.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(15)

            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updateMap = mutableMapOf<String, Any>(
                    "fullName" to safeName,
                    "university" to safeUni,
                    "program" to safeProg,
                    "yearOfStudy" to safeYear,
                    "gpa" to safeGpa,
                    "skills" to safeSkills,
                    "interests" to safeInterests,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateMap).await()
            } else {
                val createMap = mapOf(
                    "userId" to uid,
                    "fullName" to safeName,
                    "university" to safeUni,
                    "program" to safeProg,
                    "yearOfStudy" to safeYear,
                    "gpa" to safeGpa,
                    "skills" to safeSkills,
                    "interests" to safeInterests,
                    "activeDemoProfileId" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createMap).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun setActiveDemoProfile(
        userId: String,
        demoProfileId: String
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val allowed = setOf("", "demo_amina", "demo_daniel", "demo_grace")
            val safeDemoId = if (demoProfileId in allowed) demoProfileId else ""
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "activeDemoProfileId" to safeDemoId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "fullName" to (auth.currentUser?.displayName?.take(100) ?: "Ugandan Scholar"),
                        "university" to "Makerere University",
                        "program" to "BSc Computer Science",
                        "yearOfStudy" to 3,
                        "gpa" to "4.20",
                        "skills" to listOf("Kotlin", "Python", "SQL", "Git"),
                        "interests" to listOf("Software Engineering", "Scholarships"),
                        "activeDemoProfileId" to safeDemoId,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleSaved(
        userId: String,
        opportunityId: String,
        existingTracker: ApplicationTrackerDoc?,
        newSaved: Boolean
    ): Result<Unit> {
        val safeOppId = sanitizeDocId(opportunityId)
        val docPath = "users/$userId/applications/$safeOppId"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid)
                .collection("applications").document(safeOppId)

            val currentStatus = ApplicationStatus.fromRaw(existingTracker?.status)
            val nextStatus = when {
                newSaved && currentStatus == ApplicationStatus.NONE -> ApplicationStatus.SAVED
                !newSaved && currentStatus == ApplicationStatus.SAVED -> ApplicationStatus.NONE
                else -> currentStatus
            }

            if (existingTracker == null) {
                val payload = mapOf(
                    "userId" to uid,
                    "opportunityId" to safeOppId,
                    "saved" to newSaved,
                    "status" to nextStatus.name,
                    "notes" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "saved" to newSaved,
                        "status" to nextStatus.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateApplicationTracking(
        userId: String,
        opportunityId: String,
        existingTracker: ApplicationTrackerDoc?,
        saved: Boolean,
        status: ApplicationStatus,
        notes: String
    ): Result<Unit> {
        val safeOppId = sanitizeDocId(opportunityId)
        val docPath = "users/$userId/applications/$safeOppId"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid)
                .collection("applications").document(safeOppId)

            val safeNotes = notes.trim().take(1000)
            val effectiveSaved = saved || status != ApplicationStatus.NONE
            val appliedIso = when {
                status == ApplicationStatus.APPLIED ||
                    status == ApplicationStatus.INTERVIEW ||
                    status == ApplicationStatus.ACCEPTED ->
                    existingTracker?.appliedAtIso?.takeIf { it.isNotBlank() } ?: MatchEngine.todayIso()
                else -> existingTracker?.appliedAtIso ?: ""
            }.take(40)

            if (existingTracker == null) {
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "opportunityId" to safeOppId,
                    "saved" to effectiveSaved,
                    "status" to status.name,
                    "notes" to safeNotes,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (appliedIso.isNotBlank()) {
                    payload["appliedAtIso"] = appliedIso
                }
                docRef.set(payload).await()
            } else {
                val updateMap = mutableMapOf<String, Any>(
                    "saved" to effectiveSaved,
                    "status" to status.name,
                    "notes" to safeNotes,
                    "appliedAtIso" to appliedIso,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateMap).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun saveCustomOpportunity(
        userId: String,
        draft: ParsedOpportunityDraft
    ): Result<String> {
        var docPath = "users/$userId/custom_opportunities"
        return try {
            val uid = requireUserId(userId)
            val safeTitle = draft.title.trim().ifBlank { "Imported Opportunity" }.take(160)
            val safeOrg = draft.organization.trim().ifBlank { "Unspecified Organization" }.take(120)
            val docRef = db.collection("users").document(uid)
                .collection("custom_opportunities").document()
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "title" to safeTitle,
                "organization" to safeOrg,
                "category" to draft.category.name,
                "location" to draft.location.trim().take(120),
                "deadlineText" to draft.deadlineText.trim().take(80),
                "deadlineIso" to draft.deadlineIso.trim().take(40),
                "stipendOrValue" to draft.stipendOrValue.trim().take(120),
                "eligibility" to draft.eligibility.trim().take(600),
                "requirements" to draft.requirements.map { it.trim() }.filter { it.isNotEmpty() }.take(20),
                "requiredSkills" to draft.requiredSkills.map { it.trim() }.filter { it.isNotEmpty() }.take(20),
                "officialUrl" to draft.officialUrl.trim().take(300),
                "summary" to draft.summary.trim().take(1000),
                "verificationWarnings" to draft.verificationWarnings.map { it.trim() }.filter { it.isNotEmpty() }.take(10),
                "verified" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCustomOpportunity(
        userId: String,
        opportunityId: String
    ): Result<Unit> {
        val safeOppId = sanitizeDocId(opportunityId)
        val docPath = "users/$userId/custom_opportunities/$safeOppId"
        return try {
            val uid = requireUserId(userId)
            db.collection("users").document(uid)
                .collection("custom_opportunities").document(safeOppId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private fun logIfStillSignedIn(
        error: Throwable,
        operationType: OperationType,
        path: String,
        uid: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
    }

    private fun sanitizeDocId(rawId: String): String {
        val cleaned = rawId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128)
        require(cleaned.isNotBlank()) { "Document ID cannot be blank" }
        return cleaned
    }
}
