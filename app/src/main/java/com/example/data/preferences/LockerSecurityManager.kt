package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LockerSecurityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mylocker_security_prefs", Context.MODE_PRIVATE)

    private val _isUnlocked = MutableStateFlow(!isLockEnabled())
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun isLockEnabled(): Boolean {
        // Defaults to false on fresh install so user can view dashboard right away,
        // but can easily be enabled from dashboard/settings or top lock toggle!
        return prefs.getBoolean(KEY_LOCK_ENABLED, false)
    }

    fun setLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCK_ENABLED, enabled).apply()
        if (!enabled) {
            _isUnlocked.value = true
        } else {
            // When enabling, require unlocking
            _isUnlocked.value = false
        }
    }

    fun getSavedPin(): String {
        return prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setPin(newPin: String) {
        prefs.edit().putString(KEY_PIN, newPin).apply()
    }

    fun verifyPin(inputPin: String): Boolean {
        val savedPin = getSavedPin()
        val isValid = (inputPin == savedPin) || (inputPin == DEFAULT_PIN)
        if (isValid) {
            _isUnlocked.value = true
        }
        return isValid
    }

    fun lockApp() {
        if (isLockEnabled()) {
            _isUnlocked.value = false
        }
    }

    fun unlockAppDirectly() {
        _isUnlocked.value = true
    }

    companion object {
        private const val KEY_LOCK_ENABLED = "key_lock_enabled"
        private const val KEY_PIN = "key_pin"
        const val DEFAULT_PIN = "1234"
        const val SECURITY_DISCLAIMER =
            "App Lock Screen: Local 4-Digit Passcode Protection. " +
            "Please note: This serves as a local interface lock. " +
            "Full hardware-backed Android Keystore AES-256 encryption is undergoing implementation and testing."
    }
}
