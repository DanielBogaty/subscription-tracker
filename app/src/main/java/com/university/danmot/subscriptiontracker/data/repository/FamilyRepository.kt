package com.university.danmot.subscriptiontracker.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.university.danmot.subscriptiontracker.domain.model.BillingCycle
import com.university.danmot.subscriptiontracker.domain.model.Subscription
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import kotlin.random.Random

data class Family(
    val familyId: String = "",
    val name: String = "",
    val ownerId: String = "",
    val members: List<String> = emptyList(),
    val inviteCode: String = ""
)

class FamilyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val familiesCollection = firestore.collection("families")
    private val familySubsCollection = firestore.collection("family_subscriptions")

    // Слушаем текущую семью пользователя в реальном времени
    fun observeUserFamily(userId: String): Flow<Family?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = familiesCollection
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null && doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val members = (doc.get("members") as? List<String>) ?: emptyList()
                    val family = Family(
                        familyId = doc.id,
                        name = doc.getString("name") ?: "Моя семья",
                        ownerId = doc.getString("ownerId") ?: "",
                        members = members,
                        inviteCode = doc.getString("inviteCode") ?: ""
                    )
                    trySend(family)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserFamilyOnce(userId: String): Family? {
        if (userId.isBlank()) return null
        val snapshot = familiesCollection.whereArrayContains("members", userId).get().await()
        val doc = snapshot.documents.firstOrNull() ?: return null
        @Suppress("UNCHECKED_CAST")
        val members = (doc.get("members") as? List<String>) ?: emptyList()
        return Family(
            familyId = doc.id,
            name = doc.getString("name") ?: "Моя семья",
            ownerId = doc.getString("ownerId") ?: "",
            members = members,
            inviteCode = doc.getString("inviteCode") ?: ""
        )
    }

    suspend fun createFamily(name: String, userId: String): Result<Family> {
        return try {
            val inviteCode = Random.nextInt(100000, 999999).toString()
            val docRef = familiesCollection.document()
            val familyData = mapOf(
                "familyId" to docRef.id,
                "name" to name.ifBlank { "Моя семья" },
                "ownerId" to userId,
                "members" to listOf(userId),
                "inviteCode" to inviteCode
            )
            docRef.set(familyData).await()
            Result.success(
                Family(
                    familyId = docRef.id,
                    name = name.ifBlank { "Моя семья" },
                    ownerId = userId,
                    members = listOf(userId),
                    inviteCode = inviteCode
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinFamilyByCode(inviteCode: String, userId: String): Result<Family> {
        return try {
            val snapshot = familiesCollection
                .whereEqualTo("inviteCode", inviteCode.trim())
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull()
                ?: return Result.failure(IllegalArgumentException("Семья с таким кодом не найдена"))

            doc.reference.update("members", FieldValue.arrayUnion(userId)).await()
            @Suppress("UNCHECKED_CAST")
            val members = ((doc.get("members") as? List<String>) ?: emptyList()) + userId
            Result.success(
                Family(
                    familyId = doc.id,
                    name = doc.getString("name") ?: "Моя семья",
                    ownerId = doc.getString("ownerId") ?: "",
                    members = members.distinct(),
                    inviteCode = inviteCode.trim()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveFamily(familyId: String, userId: String): Result<Unit> {
        return try {
            val docRef = familiesCollection.document(familyId)
            docRef.update("members", FieldValue.arrayRemove(userId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Слушаем семейные подписки в реальном времени
    fun observeFamilySubscriptions(familyId: String): Flow<List<Subscription>> = callbackFlow {
        if (familyId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = familySubsCollection
            .whereEqualTo("familyId", familyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToSubscription(doc.id, doc.data)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getFamilySubscriptionsOnce(familyId: String): List<Subscription> {
        if (familyId.isBlank()) return emptyList()
        val snapshot = familySubsCollection.whereEqualTo("familyId", familyId).get().await()
        return snapshot.documents.mapNotNull { doc -> docToSubscription(doc.id, doc.data) }
    }

    suspend fun addFamilySubscription(subscription: Subscription, familyId: String) {
        val generatedId = if (subscription.id == 0L) {
            System.currentTimeMillis()
        } else {
            subscription.id
        }
        val data = mapOf(
            "id" to generatedId,
            "name" to subscription.name,
            "price" to subscription.price,
            "previousPrice" to subscription.previousPrice,
            "currency" to subscription.currency,
            "cycle" to subscription.cycle.name,
            "nextPaymentDate" to subscription.nextPaymentDate.toString(),
            "ownerId" to subscription.ownerId,
            "familyId" to familyId,
            "colorHex" to subscription.colorHex,
            "note" to subscription.note
        )
        familySubsCollection.document(generatedId.toString()).set(data).await()
    }

    suspend fun updateFamilySubscription(subscription: Subscription) {
        val famId = subscription.familyId ?: return
        addFamilySubscription(subscription, famId)
    }

    suspend fun deleteFamilySubscription(subscriptionId: Long) {
        familySubsCollection.document(subscriptionId.toString()).delete().await()
    }

    private fun docToSubscription(docId: String, data: Map<String, Any?>?): Subscription? {
        if (data == null) return null
        return try {
            val id = (data["id"] as? Number)?.toLong() ?: docId.toLongOrNull() ?: 0L
            val name = data["name"] as? String ?: return null
            val price = (data["price"] as? Number)?.toDouble() ?: 0.0
            val previousPrice = (data["previousPrice"] as? Number)?.toDouble()
            val currency = data["currency"] as? String ?: "RUB"
            val cycleStr = data["cycle"] as? String ?: BillingCycle.MONTHLY.name
            val cycle = runCatching { BillingCycle.valueOf(cycleStr) }.getOrDefault(BillingCycle.MONTHLY)
            val dateStr = data["nextPaymentDate"] as? String ?: LocalDate.now().toString()
            val ownerId = data["ownerId"] as? String ?: "me"
            val familyId = data["familyId"] as? String

            Subscription(
                id = id,
                name = name,
                price = price,
                previousPrice = previousPrice,
                currency = currency,
                cycle = cycle,
                nextPaymentDate = LocalDate.parse(dateStr),
                ownerId = ownerId,
                familyId = familyId
            )
        } catch (e: Exception) {
            null
        }
    }
}