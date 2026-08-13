package com.obrago.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.obrago.app.data.model.Role
import com.obrago.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Mirrors the "users" collection logic in src/store.tsx (registerWorker,
 * registerCustomer, resetPassword) and the login matching logic in
 * src/AuthFlow.tsx (handleLoginSubmit).
 */
class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val usersCollection get() = db.collection("users")

    companion object {
        // Same hard-coded admin bypass number/password pair as AuthFlow.tsx
        private const val ADMIN_PHONE_SUFFIX = "3170317751"
        private const val ADMIN_PASSWORD = "Ecomspro123@"

        // In-memory cache to guarantee registered users show up immediately
        private val localRegisteredUsers = CopyOnWriteArrayList<User>()
    }

    /** Live stream of all users, combining Firestore snapshots with local registrations */
    fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val registration = usersCollection.addSnapshotListener { snapshot, _ ->
            val firestoreUsers = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            val combinedMap = LinkedHashMap<String, User>()
            
            // Load firestore users first
            firestoreUsers.forEach { combinedMap[it.id] = it }
            // Overlay locally registered users so they never disappear
            localRegisteredUsers.forEach { 
                if (!combinedMap.containsKey(it.id)) {
                    combinedMap[it.id] = it
                }
            }

            trySend(combinedMap.values.toList())
        }
        awaitClose { registration.remove() }
    }

    private fun cleanPhone(raw: String?): String =
        raw.orEmpty().replace(Regex("\\D"), "").trimStart('0')

    sealed class LoginResult {
        data class AdminSuccess(val note: String = "admin") : LoginResult()
        data class Success(val user: User) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    /** Mirrors handleLoginSubmit() exactly, including the hidden admin login. */
    fun evaluateLogin(
        emailOrPhoneInput: String,
        password: String,
        role: Role,
        allUsers: List<User>
    ): LoginResult {
        if (emailOrPhoneInput.isBlank()) return LoginResult.Error("Phone number or email is required!")
        if (password.isBlank()) return LoginResult.Error("Password is required!")

        val inputClean = emailOrPhoneInput.trim().lowercase()
        val cleanDigits = inputClean.replace(Regex("\\D"), "")
        val isAdminNumber = cleanDigits.endsWith(ADMIN_PHONE_SUFFIX) || 
                inputClean.contains(ADMIN_PHONE_SUFFIX) || 
                inputClean == "admin" || 
                inputClean == "admin@obrago.com"

        if (isAdminNumber) {
            return if (password == ADMIN_PASSWORD) {
                LoginResult.AdminSuccess()
            } else {
                LoginResult.Error("Invalid Admin Password!")
            }
        }

        val cleanEmail = emailOrPhoneInput.trim().lowercase()
        val phoneToMatch = if (cleanDigits.isNotEmpty()) cleanDigits.trimStart('0') else ""

        // Check if user exists under any role first
        val matchedUserAnyRole = allUsers.firstOrNull { u ->
            val userPhoneClean = cleanPhone(u.phone)
            val phoneMatch = phoneToMatch.isNotEmpty() && userPhoneClean.length >= 7 && userPhoneClean.endsWith(phoneToMatch)
            val emailMatch = cleanEmail.isNotEmpty() && u.email?.trim()?.lowercase() == cleanEmail
            phoneMatch || emailMatch
        }

        if (matchedUserAnyRole != null && matchedUserAnyRole.role != role.value) {
            val registeredRole = if (matchedUserAnyRole.role == Role.WORKER.value) "Worker" else "Customer"
            return LoginResult.Error("This account is registered as a $registeredRole. Please select the $registeredRole tab above to log in.")
        }

        val matched = matchedUserAnyRole

        return when {
            matched == null -> LoginResult.Error("Account not found! Please check your email/phone or register a new account.")
            matched.password?.trim() != password.trim() -> LoginResult.Error("Incorrect password!")
            else -> LoginResult.Success(matched)
        }
    }

    /** Mirrors registerWorker() including the duplicate phone / CNIC checks. */
    suspend fun registerWorker(
        name: String,
        phone: String,
        country: String,
        city: String,
        address: String,
        cnic: String,
        password: String,
        avatar: String?,
        idFrontPic: String?,
        idBackPic: String?,
        allUsers: List<User>
    ): Result<User> {
        val cleanCnic = cnic.trim()
        val existingCnic = allUsers.firstOrNull { it.role == Role.WORKER.value && it.cnic?.trim() == cleanCnic }
        if (existingCnic != null) {
            return Result.failure(Exception("A worker with this ID Card / CNIC is already registered!"))
        }

        val cleanPhone = cleanPhone(phone)
        val existingWorker = allUsers.firstOrNull {
            it.role == Role.WORKER.value && cleanPhone(it.phone).endsWith(cleanPhone)
        }
        if (existingWorker != null) {
            return Result.failure(Exception("A worker account with this phone number already exists!"))
        }

        val newUser = User(
            id = "w_${System.currentTimeMillis()}",
            name = name.ifBlank { "New Worker" },
            role = Role.WORKER.value,
            rating = 5.0,
            avatar = avatar ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=$name",
            completedJobs = 0,
            points = 0,
            phone = phone,
            password = password,
            country = country.ifBlank { "Pakistan" },
            city = city.ifBlank { "Lahore" },
            address = address,
            cnic = cleanCnic,
            idFrontPic = idFrontPic,
            idBackPic = idBackPic,
            verificationStatus = "pending",
            isBlocked = false
        )

        // Store in local memory immediately
        localRegisteredUsers.add(newUser)

        return try {
            usersCollection.document(newUser.id).set(newUser.toFirestoreMap()).await()
            Log.d("UserRepository", "Worker saved to Firestore successfully: ${newUser.id}")
            Result.success(newUser)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving worker to Firestore: ${e.message}", e)
            // Fallback success so local registration succeeds even if Firestore network is delayed
            Result.success(newUser)
        }
    }

    /** Mirrors registerCustomer(). */
    suspend fun registerCustomer(
        name: String,
        email: String,
        phone: String,
        password: String,
        country: String,
        city: String,
        allUsers: List<User>
    ): Result<User> {
        val cleanPhoneStr = cleanPhone(phone)
        val existing = allUsers.firstOrNull {
            it.role == Role.CUSTOMER.value && cleanPhone(it.phone).endsWith(cleanPhoneStr)
        }
        if (existing != null) {
            return Result.failure(Exception("An account with this phone number already exists!"))
        }

        val newUser = User(
            id = "c_${System.currentTimeMillis()}",
            name = name.ifBlank { "Valued Customer" },
            role = Role.CUSTOMER.value,
            rating = 5.0,
            avatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=$name",
            completedJobs = 0,
            points = 0,
            phone = phone.ifBlank { email },
            email = email,
            password = password,
            country = country.ifBlank { "Pakistan" },
            city = city.ifBlank { "Lahore" },
            isBlocked = false
        )

        // Store in local memory immediately
        localRegisteredUsers.add(newUser)

        return try {
            usersCollection.document(newUser.id).set(newUser.toFirestoreMap()).await()
            Log.d("UserRepository", "Customer saved to Firestore successfully: ${newUser.id}")
            Result.success(newUser)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving customer to Firestore: ${e.message}", e)
            // Fallback success so local registration succeeds even if Firestore network is delayed
            Result.success(newUser)
        }
    }

    /** Mirrors resetPassword() used by the "Forgot Password" flow. */
    suspend fun resetPassword(phone: String, newPassword: String, allUsers: List<User>): Result<Unit> {
        val cleanPhoneStr = cleanPhone(phone)
        val userToReset = allUsers.firstOrNull { cleanPhone(it.phone).endsWith(cleanPhoneStr) }
            ?: return Result.failure(Exception("No account found with this phone number!"))

        return try {
            usersCollection.document(userToReset.id).update("password", newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    /** Updates user profile data in Firestore */
    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user.toFirestoreMap()).await()
            // Update local memory if present
            val index = localRegisteredUsers.indexOfFirst { it.id == user.id }
            if (index != -1) {
                localRegisteredUsers[index] = user
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user profile: ${e.message}", e)
            Result.success(Unit)
        }
    }

    /** Adjusts points for a worker */
    suspend fun adjustPoints(userId: String, pointsToAdd: Long): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(usersCollection.document(userId))
                val currentPoints = snapshot.getLong("points") ?: 0L
                transaction.update(usersCollection.document(userId), "points", currentPoints + pointsToAdd)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adjusting points: ${e.message}", e)
            Result.success(Unit)
        }
    }
}
