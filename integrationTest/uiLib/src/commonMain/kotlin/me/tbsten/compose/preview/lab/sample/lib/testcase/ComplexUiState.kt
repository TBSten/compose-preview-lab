package me.tbsten.compose.preview.lab.sample.lib.testcase

import me.tbsten.compose.preview.lab.generatecombinedfield.GenerateCombinedField

/**
 * Test case 3: Complex combination with multiple nested levels and various types
 *
 * This tests:
 * - Multiple levels of nesting
 * - Various primitive types (String, Int, Boolean, Float)
 * - Multiple nested data classes in a single parent
 */

@GenerateCombinedField
data class UserInfo(
    val name: String,
    val age: Int,
    val isVerified: Boolean,
) {
    companion object
}

fun UserInfo.Companion.fake() = UserInfo(
    name = "John Doe",
    age = 30,
    isVerified = true,
)

@GenerateCombinedField
data class AppSettings(
    val isDarkMode: Boolean,
    val fontSize: Int,
    val volume: Float,
) {
    companion object
}

fun AppSettings.Companion.fake() = AppSettings(
    isDarkMode = false,
    fontSize = 14,
    volume = 0.5f,
)

@GenerateCombinedField
data class ComplexUiState(
    val userInfo: UserInfo,
    val settings: AppSettings,
    val isLoading: Boolean,
    val errorMessage: String,
) {
    companion object
}

fun ComplexUiState.Companion.fake() = ComplexUiState(
    userInfo = UserInfo.fake(),
    settings = AppSettings.fake(),
    isLoading = false,
    errorMessage = "",
)

/**
 * Expected generated code:
 *
 * fun UserInfo.Companion.field(label: String, initialValue: UserInfo) = CombinedField3(
 *     label = label,
 *     field1 = StringField("name", initialValue = initialValue.name),
 *     field2 = IntField("age", initialValue = initialValue.age),
 *     field3 = BooleanField("isVerified", initialValue = initialValue.isVerified),
 *     combine = { name, age, isVerified -> UserInfo(name = name, age = age, isVerified = isVerified) },
 *     split = { splitedOf(it.name, it.age, it.isVerified) },
 * )
 *
 * fun AppSettings.Companion.field(label: String, initialValue: AppSettings) = CombinedField3(
 *     label = label,
 *     field1 = BooleanField("isDarkMode", initialValue = initialValue.isDarkMode),
 *     field2 = IntField("fontSize", initialValue = initialValue.fontSize),
 *     field3 = FloatField("volume", initialValue = initialValue.volume),
 *     combine = { isDarkMode, fontSize, volume -> AppSettings(isDarkMode = isDarkMode, fontSize = fontSize, volume = volume) },
 *     split = { splitedOf(it.isDarkMode, it.fontSize, it.volume) },
 * )
 *
 * fun ComplexUiState.Companion.field(label: String, initialValue: ComplexUiState) = CombinedField4(
 *     label = label,
 *     field1 = UserInfo.field(label = "userInfo", initialValue = initialValue.userInfo),
 *     field2 = AppSettings.field(label = "settings", initialValue = initialValue.settings),
 *     field3 = BooleanField("isLoading", initialValue = initialValue.isLoading),
 *     field4 = StringField("errorMessage", initialValue = initialValue.errorMessage),
 *     combine = { userInfo, settings, isLoading, errorMessage ->
 *         ComplexUiState(
 *             userInfo = userInfo,
 *             settings = settings,
 *             isLoading = isLoading,
 *             errorMessage = errorMessage
 *         )
 *     },
 *     split = { splitedOf(it.userInfo, it.settings, it.isLoading, it.errorMessage) },
 * )
 */
