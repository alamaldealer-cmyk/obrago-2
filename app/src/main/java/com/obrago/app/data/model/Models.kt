package com.obrago.app.data.model

/**
 * Mirrors src/types.ts from the original web app.
 * Kept 1:1 with field names so Firestore documents already written
 * by the web/Capacitor app remain fully compatible.
 */

enum class Role(val value: String) {
    CUSTOMER("customer"),
    WORKER("worker"),
    ADMIN("admin");

    companion object {
        fun fromValue(v: String?): Role = entries.find { it.value == v } ?: CUSTOMER
    }
}

enum class VerificationStatus(val value: String) {
    VERIFIED("verified"),
    PENDING("pending"),
    REJECTED("rejected");

    companion object {
        fun fromValue(v: String?): VerificationStatus? = entries.find { it.value == v }
    }
}

enum class JobStatus(val value: String) {
    SEARCHING("searching"),
    BIDDING("bidding"),
    ACCEPTED("accepted"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    companion object {
        fun fromValue(v: String?): JobStatus = entries.find { it.value == v } ?: SEARCHING
    }
}

data class User(
    val id: String = "",
    val name: String = "",
    val role: String = Role.CUSTOMER.value,
    val rating: Double = 5.0,
    val avatar: String = "",
    val completedJobs: Long = 0,
    val points: Long = 0,
    val penaltyFee: Double? = null,
    val verificationStatus: String? = null,
    val cnic: String? = null,
    val idFrontPic: String? = null,
    val idBackPic: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val password: String? = null,
    val country: String? = null,
    val city: String? = null,
    val address: String? = null,
    val isBlocked: Boolean? = false
) {
    // All properties already have default values above, so Firestore's
    // toObject() can use this primary constructor as a no-arg constructor.

    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "role" to role,
        "rating" to rating,
        "avatar" to avatar,
        "completedJobs" to completedJobs,
        "points" to points,
        "penaltyFee" to penaltyFee,
        "verificationStatus" to verificationStatus,
        "cnic" to cnic,
        "idFrontPic" to idFrontPic,
        "idBackPic" to idBackPic,
        "phone" to phone,
        "email" to email,
        "password" to password,
        "country" to country,
        "city" to city,
        "address" to address,
        "isBlocked" to (isBlocked ?: false)
    ).filterValues { it != null }
}

data class CountryOption(
    val name: String,
    val dialCode: String,
    val flagEmoji: String
)

/** Mirrors the Category interface in src/types.ts */
data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val isLongProject: Boolean = false,
    val duration: String? = null,
    val upfrontFee: Double? = null
)

object AppData {
    val COUNTRIES = listOf(
        CountryOption("Pakistan", "+92", "\uD83C\uDDF5\uD83C\uDDF0"),
        CountryOption("United Arab Emirates", "+971", "\uD83C\uDDE6\uD83C\uDDEA"),
        CountryOption("Saudi Arabia", "+966", "\uD83C\uDDF8\uD83C\uDDE6"),
        CountryOption("India", "+91", "\uD83C\uDDEE\uD83C\uDDF3"),
        CountryOption("United States", "+1", "\uD83C\uDDFA\uD83C\uDDF8"),
        CountryOption("United Kingdom", "+44", "\uD83C\uDDEC\uD83C\uDDE7"),
        CountryOption("Qatar", "+974", "\uD83C\uDDF6\uD83C\uDDE6"),
        CountryOption("Oman", "+968", "\uD83C\uDDF4\uD83C\uDDF2"),
        CountryOption("Kuwait", "+965", "\uD83C\uDDF0\uD83C\uDDFC"),
        CountryOption("Canada", "+1", "\uD83C\uDDE8\uD83C\uDDE6")
    )

    val CITIES_BY_COUNTRY: Map<String, List<String>> = mapOf(
        "Pakistan" to listOf(
            "Lahore", "Karachi", "Islamabad", "Rawalpindi", "Peshawar",
            "Multan", "Faisalabad", "Quetta", "Sialkot", "Gujranwala",
            "Hyderabad", "Sukkur", "Bahawalpur", "Abbottabad", "Mardan",
            "Sahiwal", "Sargodha", "Okara", "Sheikhupura", "Jhelum", "Kasur", "Other"
        ),
        "United Arab Emirates" to listOf("Dubai", "Abu Dhabi", "Sharjah", "Ajman", "Ras Al Khaimah", "Other"),
        "Saudi Arabia" to listOf("Riyadh", "Jeddah", "Mecca", "Medina", "Dammam", "Other"),
        "India" to listOf("Mumbai", "Delhi", "Bangalore", "Hyderabad", "Chennai", "Other"),
        "United States" to listOf("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Other"),
        "United Kingdom" to listOf("London", "Birmingham", "Manchester", "Glasgow", "Other"),
        "Qatar" to listOf("Doha", "Al Wakrah", "Al Rayyan", "Other"),
        "Oman" to listOf("Muscat", "Salalah", "Sohar", "Other"),
        "Kuwait" to listOf("Kuwait City", "Hawalli", "Salmiya", "Other"),
        "Canada" to listOf("Toronto", "Vancouver", "Montreal", "Calgary", "Other")
    )

    val PAKISTAN_CITIES = CITIES_BY_COUNTRY["Pakistan"]!!

    val CATEGORIES = listOf(
        Category("electrician", "Electrician", "Zap"),
        Category("plumber", "Plumber", "Droplets"),
        Category("carpenter", "Carpenter", "Hammer"),
        Category("cleaner", "Cleaner", "Sparkles"),
        Category("painter", "Painter", "PaintRoller"),
        Category("mechanic", "Mechanic", "Wrench"),
        Category("ac", "AC Technician", "Fan"),
        Category("mason", "Mason", "BrickWall"),
        Category("house_construction_1m", "House Building (1 Month)", "Building", isLongProject = true, duration = "1 Month", upfrontFee = 500.0),
        Category("commercial_construction_2m", "Commercial Project (2 Months)", "Building2", isLongProject = true, duration = "2 Months", upfrontFee = 1000.0)
    )

    data class HomeCategory(val id: String, val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
}

/** Mirrors src/types.ts Job interface. Field names kept identical for Firestore compatibility. */
data class Job(
    val id: String = "",
    val customerId: String = "",
    val category: String = "",
    val description: String = "",
    val location: String = "",
    val city: String? = null,
    val locationCoords: List<Double>? = null, // [lat, lng]
    val workerLocationCoords: List<Double>? = null,
    val budget: Double = 0.0,
    val status: String = JobStatus.SEARCHING.value,
    val createdAt: Long = 0L,
    val workerId: String? = null,
    val acceptedBidId: String? = null,
    val workerArrived: Boolean? = false,
    val cancelReason: String? = null,
    val isLongProject: Boolean? = false,
    val duration: String? = null,
    val upfrontFee: Double? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "customerId" to customerId,
        "category" to category,
        "description" to description,
        "location" to location,
        "city" to city,
        "locationCoords" to locationCoords,
        "workerLocationCoords" to workerLocationCoords,
        "budget" to budget,
        "status" to status,
        "createdAt" to createdAt,
        "workerId" to workerId,
        "acceptedBidId" to acceptedBidId,
        "workerArrived" to workerArrived,
        "cancelReason" to cancelReason,
        "isLongProject" to isLongProject,
        "duration" to duration,
        "upfrontFee" to upfrontFee
    ).filterValues { it != null }
}

/** Mirrors src/types.ts Bid interface. */
data class Bid(
    val id: String = "",
    val jobId: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val workerRating: Double = 5.0,
    val workerAvatar: String = "",
    val workerJobs: Long = 0,
    val price: Double = 0.0,
    val eta: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
    val counterPrice: Double? = null,
    val counterMessage: String? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "jobId" to jobId,
        "workerId" to workerId,
        "workerName" to workerName,
        "workerRating" to workerRating,
        "workerAvatar" to workerAvatar,
        "workerJobs" to workerJobs,
        "price" to price,
        "eta" to eta,
        "message" to message,
        "createdAt" to createdAt,
        "counterPrice" to counterPrice,
        "counterMessage" to counterMessage
    ).filterValues { it != null }
}

/** Mirrors the ChatMessage shape used in src/CommunicationModals.tsx (chats/{chatId}/messages subcollection). */
data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val senderId: String = "",
    val senderName: String = "",
    val createdAt: Long = 0L
)

/** Lightweight description of who we're chatting/calling with - mirrors CommunicationTarget in store.tsx */
data class CommunicationTarget(
    val name: String,
    val avatar: String,
    val role: String,
    val phone: String? = null,
    val jobId: String? = null,
    val userId: String? = null
)

/** Mirrors AdminSettings in src/types.ts */
data class AdminSettings(
    val commissionRate: Double = 5.0,
    val bankName: String = "",
    val accountTitle: String = "",
    val accountNumber: String = "",
    val easypaisaNumber: String = "",
    val jazzcashNumber: String = "",
    val coinPricePkr: Double = 10.0,
    val minTopupCoins: Long = 20
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "commissionRate" to commissionRate,
        "bankName" to bankName,
        "accountTitle" to accountTitle,
        "accountNumber" to accountNumber,
        "easypaisaNumber" to easypaisaNumber,
        "jazzcashNumber" to jazzcashNumber,
        "coinPricePkr" to coinPricePkr,
        "minTopupCoins" to minTopupCoins
    )
}

/** Mirrors DepositRequest in src/types.ts */
data class DepositRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userRole: String = "customer",
    val amount: Double = 0.0,
    val method: String = "bank", // bank | easypaisa | jazzcash
    val trxId: String = "",
    val status: String = "pending", // pending | approved | rejected
    val createdAt: Long = 0L
)

/** Mirrors SupportTicket for customer/worker support requests and admin live help */
data class SupportMessage(
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false
)

data class SupportTicket(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userRole: String = "customer", // customer | worker
    val userPhone: String = "",
    val subject: String = "",
    val status: String = "open", // open | in_progress | resolved
    val messages: List<SupportMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

