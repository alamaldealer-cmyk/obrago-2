package com.obrago.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String,
    val conversionFactor: Double = 1.0 // relative to PKR base or USD base
)

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ENGLISH("en", "English", "🇺🇸"),
    URDU("ur", "Urdu (اردو)", "🇵🇰"),
    SPANISH("es", "Spanish (Español)", "🇪🇸"),
    ARABIC("ar", "Arabic (العربية)", "🇸🇦")
}

object AppSettingsManager {
    private const val PREFS_NAME = "obrago_app_settings"
    private const val KEY_DARK_MODE = "key_dark_mode"
    private const val KEY_LANGUAGE = "key_language"
    private const val KEY_CURRENCY_CODE = "key_currency_code"
    private const val KEY_CURRENCY_MANUALLY_SET = "key_currency_manually_set"
    private const val KEY_PUSH_NOTIFS = "key_push_notifs"
    private const val KEY_SMS_ALERTS = "key_sms_alerts"

    val AVAILABLE_CURRENCIES = listOf(
        CurrencyOption("PKR", "PKR ", "Pakistani Rupee (PKR)", 1.0),
        CurrencyOption("USD", "$", "US Dollar (USD)", 0.0036),
        CurrencyOption("EUR", "€", "Euro (EUR)", 0.0033),
        CurrencyOption("GBP", "£", "British Pound (GBP)", 0.0028),
        CurrencyOption("AED", "AED ", "UAE Dirham (AED)", 0.0132),
        CurrencyOption("SAR", "SAR ", "Saudi Riyal (SAR)", 0.0135)
    )

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentCurrency = MutableStateFlow(AVAILABLE_CURRENCIES[0])
    val currentCurrency: StateFlow<CurrencyOption> = _currentCurrency.asStateFlow()

    private val _pushNotificationsEnabled = MutableStateFlow(true)
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()

    private val _smsAlertsEnabled = MutableStateFlow(true)
    val smsAlertsEnabled: StateFlow<Boolean> = _smsAlertsEnabled.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp

        // 1. Dark Mode
        val savedDark = sp.getBoolean(KEY_DARK_MODE, false)
        _isDarkMode.value = savedDark

        // 2. Language
        val savedLangCode = sp.getString(KEY_LANGUAGE, null)
        if (savedLangCode != null) {
            _currentLanguage.value = AppLanguage.values().firstOrNull { it.code == savedLangCode } ?: AppLanguage.ENGLISH
        } else {
            // Auto detect system language
            val systemLang = Locale.getDefault().language
            _currentLanguage.value = when (systemLang) {
                "ur" -> AppLanguage.URDU
                "es" -> AppLanguage.SPANISH
                "ar" -> AppLanguage.ARABIC
                else -> AppLanguage.ENGLISH
            }
        }

        // 3. Currency (Location Auto Detect)
        val savedCurrCode = sp.getString(KEY_CURRENCY_CODE, null)
        if (savedCurrCode != null) {
            _currentCurrency.value = AVAILABLE_CURRENCIES.firstOrNull { it.code == savedCurrCode } ?: AVAILABLE_CURRENCIES[0]
        } else {
            // Auto detect location/country
            val country = Locale.getDefault().country.uppercase()
            _currentCurrency.value = when (country) {
                "US" -> AVAILABLE_CURRENCIES.first { it.code == "USD" }
                "GB" -> AVAILABLE_CURRENCIES.first { it.code == "GBP" }
                "AE" -> AVAILABLE_CURRENCIES.first { it.code == "AED" }
                "SA" -> AVAILABLE_CURRENCIES.first { it.code == "SAR" }
                "DE", "FR", "ES", "IT", "NL" -> AVAILABLE_CURRENCIES.first { it.code == "EUR" }
                else -> AVAILABLE_CURRENCIES.first { it.code == "PKR" }
            }
        }

        _pushNotificationsEnabled.value = sp.getBoolean(KEY_PUSH_NOTIFS, true)
        _smsAlertsEnabled.value = sp.getBoolean(KEY_SMS_ALERTS, true)
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs?.edit()?.putBoolean(KEY_DARK_MODE, enabled)?.apply()
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs?.edit()?.putString(KEY_LANGUAGE, language.code)?.apply()
    }

    fun setCurrency(currency: CurrencyOption) {
        _currentCurrency.value = currency
        prefs?.edit()
            ?.putString(KEY_CURRENCY_CODE, currency.code)
            ?.putBoolean(KEY_CURRENCY_MANUALLY_SET, true)
            ?.apply()
    }

    /** Called on startup or when GPS position detects a country code */
    fun updateCurrencyFromLocation(countryCode: String) {
        val sp = prefs ?: return
        val manuallySet = sp.getBoolean(KEY_CURRENCY_MANUALLY_SET, false)
        if (!manuallySet) {
            val detected = when (countryCode.uppercase()) {
                "PK" -> AVAILABLE_CURRENCIES.first { it.code == "PKR" }
                "US" -> AVAILABLE_CURRENCIES.first { it.code == "USD" }
                "GB" -> AVAILABLE_CURRENCIES.first { it.code == "GBP" }
                "AE" -> AVAILABLE_CURRENCIES.first { it.code == "AED" }
                "SA" -> AVAILABLE_CURRENCIES.first { it.code == "SAR" }
                "DE", "FR", "ES", "IT", "NL", "BE", "AT", "PT", "IE", "FI", "GR" -> AVAILABLE_CURRENCIES.first { it.code == "EUR" }
                else -> AVAILABLE_CURRENCIES.first { it.code == "USD" }
            }
            _currentCurrency.value = detected
            sp.edit().putString(KEY_CURRENCY_CODE, detected.code).apply()
        }
    }

    fun setPushNotifications(enabled: Boolean) {
        _pushNotificationsEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_PUSH_NOTIFS, enabled)?.apply()
    }

    fun setSmsAlerts(enabled: Boolean) {
        _smsAlertsEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_SMS_ALERTS, enabled)?.apply()
    }

    /** Helper to format price according to current currency */
    fun formatPrice(amount: Double): String {
        val curr = _currentCurrency.value
        return if (curr.code == "PKR") {
            "${curr.symbol}${amount.toInt()}"
        } else if (curr.code == "USD" || curr.code == "EUR" || curr.code == "GBP") {
            val valStr = if (amount % 1.0 == 0.0) "${amount.toInt()}" else String.format("%.2f", amount)
            "${curr.symbol}$valStr"
        } else {
            "${curr.symbol}${amount.toInt()}"
        }
    }

    /** Simple Dictionary for App Translations */
    fun tr(key: String): String {
        return when (_currentLanguage.value) {
            AppLanguage.URDU -> URDU_MAP[key] ?: ENGLISH_MAP[key] ?: key
            AppLanguage.SPANISH -> SPANISH_MAP[key] ?: ENGLISH_MAP[key] ?: key
            AppLanguage.ARABIC -> ARABIC_MAP[key] ?: ENGLISH_MAP[key] ?: key
            AppLanguage.ENGLISH -> ENGLISH_MAP[key] ?: key
        }
    }

    private val ENGLISH_MAP = mapOf(
        "home" to "Home",
        "bookings" to "Bookings",
        "messages" to "Messages",
        "profile" to "Profile",
        "settings" to "Settings",
        "wallet" to "Wallet",
        "post_job" to "Post a Job",
        "search_services" to "Search services or workers...",
        "logout" to "Log Out",
        "app_language" to "App Language",
        "dark_theme" to "Dark Theme",
        "default_currency" to "Default Currency",
        "save_changes" to "Save Changes",
        "personal_info" to "Personal Information",
        "notifications" to "Notifications",
        "help_support" to "Help & Support"
    )

    private val URDU_MAP = mapOf(
        "home" to "ہوم (Home)",
        "bookings" to "بکنگز (Bookings)",
        "messages" to "پیغامات (Messages)",
        "profile" to "پروفائل (Profile)",
        "settings" to "سیٹنگز (Settings)",
        "wallet" to "والٹ (Wallet)",
        "post_job" to "کام پوسٹ کریں (Post Job)",
        "search_services" to "سروس یا ورکر تلاش کریں...",
        "logout" to "لاگ آؤٹ (Log Out)",
        "app_language" to "ایپ کی زبان",
        "dark_theme" to "ڈارک تھیم (Dark Theme)",
        "default_currency" to "کرنسی (Currency)",
        "save_changes" to "تبدیلیاں محفوظ کریں",
        "personal_info" to "ذاتی معلومات",
        "notifications" to "نوٹیفکیشنز",
        "help_support" to "مدد اور تعاون"
    )

    private val SPANISH_MAP = mapOf(
        "home" to "Inicio",
        "bookings" to "Reservas",
        "messages" to "Mensajes",
        "profile" to "Perfil",
        "settings" to "Ajustes",
        "wallet" to "Billetera",
        "post_job" to "Publicar Trabajo",
        "search_services" to "Buscar servicios o trabajadores...",
        "logout" to "Cerrar Sesión",
        "app_language" to "Idioma de la Aplicación",
        "dark_theme" to "Modo Oscuro",
        "default_currency" to "Moneda Predeterminada",
        "save_changes" to "Guardar Cambios",
        "personal_info" to "Información Personal",
        "notifications" to "Notificaciones",
        "help_support" to "Ayuda y Soporte"
    )

    private val ARABIC_MAP = mapOf(
        "home" to "الرئيسية",
        "bookings" to "الحجوزات",
        "messages" to "الرسائل",
        "profile" to "الملف الشخصي",
        "settings" to "الإعدادات",
        "wallet" to "المحفظة",
        "post_job" to "نشر وظيفة",
        "search_services" to "البحث عن الخدمات...",
        "logout" to "تسجيل الخروج",
        "app_language" to "لغة التطبيق",
        "dark_theme" to "الوضع الداكن",
        "default_currency" to "العملة الافتراضية",
        "save_changes" to "حفظ التغييرات",
        "personal_info" to "المعلومات الشخصية",
        "notifications" to "الإشعارات",
        "help_support" to "المساعدة والدعم"
    )
}
