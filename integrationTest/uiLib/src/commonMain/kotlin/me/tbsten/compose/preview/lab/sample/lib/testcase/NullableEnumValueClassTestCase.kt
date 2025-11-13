package me.tbsten.compose.preview.lab.sample.lib.testcase

import kotlin.jvm.JvmInline
import me.tbsten.compose.preview.lab.generatecombinedfield.GenerateCombinedField

/**
 * Test cases for Nullable, Enum, and Value class support
 */

// ========== Enum Test Cases ==========

enum class Status {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

@GenerateCombinedField
data class EnumTestState(val status: Status, val priority: Priority, val count: Int,) {
    companion object
}

fun EnumTestState.Companion.fake() = EnumTestState(
    status = Status.IDLE,
    priority = Priority.MEDIUM,
    count = 0,
)

// ========== Nullable Test Cases ==========

@GenerateCombinedField
data class NullableTestState(val nullableString: String?, val nullableInt: Int?, val requiredString: String,) {
    companion object
}

fun NullableTestState.Companion.fake() = NullableTestState(
    nullableString = null,
    nullableInt = null,
    requiredString = "required",
)

// ========== Value Class Test Cases ==========

@JvmInline
value class UserId(val value: String)

@JvmInline
value class ProductId(val value: Int)

@JvmInline
value class Price(val value: Double)

@GenerateCombinedField
data class ValueClassTestState(val userId: UserId, val productId: ProductId, val price: Price, val name: String,) {
    companion object
}

fun ValueClassTestState.Companion.fake() = ValueClassTestState(
    userId = UserId("user123"),
    productId = ProductId(456),
    price = Price(99.99),
    name = "Product Name",
)

// ========== Combined Test: Nullable + Enum ==========

@GenerateCombinedField
data class NullableEnumState(val status: Status?, val priority: Priority, val errorMessage: String?,) {
    companion object
}

fun NullableEnumState.Companion.fake() = NullableEnumState(
    status = null,
    priority = Priority.LOW,
    errorMessage = null,
)

// ========== Combined Test: Nullable + Value Class ==========

@GenerateCombinedField
data class NullableValueClassState(val userId: UserId?, val email: String, val productId: ProductId?,) {
    companion object
}

fun NullableValueClassState.Companion.fake() = NullableValueClassState(
    userId = null,
    email = "test@example.com",
    productId = null,
)

// ========== Complex Combined Test ==========

enum class UserRole {
    GUEST,
    USER,
    ADMIN,
    SUPER_ADMIN
}

@JvmInline
value class EmailAddress(val value: String)

@JvmInline
value class Age(val value: Int)

@GenerateCombinedField
data class ComplexCombinedState(
    val role: UserRole,
    val email: EmailAddress?,
    val age: Age?,
    val status: Status,
    val optionalNote: String?,
    val isActive: Boolean,
) {
    companion object
}

fun ComplexCombinedState.Companion.fake() = ComplexCombinedState(
    role = UserRole.USER,
    email = null,
    age = null,
    status = Status.IDLE,
    optionalNote = null,
    isActive = true,
)

// ========== Nested Data Class with New Features ==========

data class AddressInfo(val street: String, val zipCode: String?, val country: String,) {
    companion object
}

fun AddressInfo.Companion.fake() = AddressInfo(
    street = "123 Main St",
    zipCode = null,
    country = "USA",
)

@GenerateCombinedField
data class UserProfileWithNewFeatures(
    val userId: UserId,
    val name: String,
    val email: EmailAddress?,
    val age: Age?,
    val role: UserRole,
    val address: AddressInfo,
    val status: Status,
) {
    companion object
}

fun UserProfileWithNewFeatures.Companion.fake() = UserProfileWithNewFeatures(
    userId = UserId("user456"),
    name = "John Doe",
    email = null,
    age = null,
    role = UserRole.USER,
    address = AddressInfo.fake(),
    status = Status.IDLE,
)
