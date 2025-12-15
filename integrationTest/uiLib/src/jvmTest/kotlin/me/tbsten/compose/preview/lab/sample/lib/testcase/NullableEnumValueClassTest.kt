package me.tbsten.compose.preview.lab.sample.lib.testcase

import me.tbsten.compose.preview.lab.MutablePreviewLabField
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NullableEnumValueClassTest {

    // ========== Enum Tests ==========

    @Test
    fun `field function should be generated for EnumTestState`() {
        val initialValue = EnumTestState.fake()
        val field = EnumTestState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field")
        assertEquals(initialValue, field.value, "Initial value should match")
    }

    @Test
    fun `EnumTestState should preserve enum values`() {
        val state = EnumTestState(
            status = Status.SUCCESS,
            priority = Priority.HIGH,
            count = 42
        )

        val field = EnumTestState.field("test", state)

        assertEquals(Status.SUCCESS, field.value.status)
        assertEquals(Priority.HIGH, field.value.priority)
        assertEquals(42, field.value.count)
    }

    @Test
    fun `EnumTestState field should have correct type`() {
        val initialValue = EnumTestState.fake()
        val field = EnumTestState.field("test", initialValue)

        assert(field is MutablePreviewLabField<EnumTestState>) {
            "Generated field should be a MutablePreviewLabField"
        }
    }

    // ========== Nullable Tests ==========

    @Test
    fun `field function should be generated for NullableTestState`() {
        val initialValue = NullableTestState.fake()
        val field = NullableTestState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field")
        assertEquals(initialValue, field.value)
    }

    @Test
    fun `NullableTestState should handle null values`() {
        val state = NullableTestState(
            nullableString = null,
            nullableInt = null,
            requiredString = "test"
        )

        val field = NullableTestState.field("test", state)

        assertNull(field.value.nullableString)
        assertNull(field.value.nullableInt)
        assertEquals("test", field.value.requiredString)
    }

    @Test
    fun `NullableTestState should handle non-null values`() {
        val state = NullableTestState(
            nullableString = "hello",
            nullableInt = 42,
            requiredString = "required"
        )

        val field = NullableTestState.field("test", state)

        assertEquals("hello", field.value.nullableString)
        assertEquals(42, field.value.nullableInt)
        assertEquals("required", field.value.requiredString)
    }

    // ========== Value Class Tests ==========

    @Test
    fun `field function should be generated for ValueClassTestState`() {
        val initialValue = ValueClassTestState.fake()
        val field = ValueClassTestState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field")
        assertEquals(initialValue, field.value)
    }

    @Test
    fun `ValueClassTestState should preserve value class values`() {
        val state = ValueClassTestState(
            userId = UserId("user789"),
            productId = ProductId(123),
            price = Price(49.99),
            name = "Test Product"
        )

        val field = ValueClassTestState.field("test", state)

        assertEquals(UserId("user789"), field.value.userId)
        assertEquals(ProductId(123), field.value.productId)
        assertEquals(Price(49.99), field.value.price)
        assertEquals("Test Product", field.value.name)
    }

    // ========== Combined Tests: Nullable + Enum ==========

    @Test
    fun `field function should be generated for NullableEnumState`() {
        val initialValue = NullableEnumState.fake()
        val field = NullableEnumState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field")
        assertEquals(initialValue, field.value)
    }

    @Test
    fun `NullableEnumState should handle nullable enum`() {
        val stateWithNull = NullableEnumState(
            status = null,
            priority = Priority.HIGH,
            errorMessage = null
        )

        val field1 = NullableEnumState.field("test1", stateWithNull)
        assertNull(field1.value.status)
        assertEquals(Priority.HIGH, field1.value.priority)

        val stateWithValue = NullableEnumState(
            status = Status.ERROR,
            priority = Priority.URGENT,
            errorMessage = "Something went wrong"
        )

        val field2 = NullableEnumState.field("test2", stateWithValue)
        assertEquals(Status.ERROR, field2.value.status)
        assertEquals("Something went wrong", field2.value.errorMessage)
    }

    // ========== Combined Tests: Nullable + Value Class ==========

    @Test
    fun `field function should be generated for NullableValueClassState`() {
        val initialValue = NullableValueClassState.fake()
        val field = NullableValueClassState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field")
        assertEquals(initialValue, field.value)
    }

    @Test
    fun `NullableValueClassState should handle nullable value classes`() {
        val stateWithNull = NullableValueClassState(
            userId = null,
            email = "test@example.com",
            productId = null
        )

        val field1 = NullableValueClassState.field("test1", stateWithNull)
        assertNull(field1.value.userId)
        assertEquals("test@example.com", field1.value.email)
        assertNull(field1.value.productId)

        val stateWithValues = NullableValueClassState(
            userId = UserId("user999"),
            email = "user@example.com",
            productId = ProductId(777)
        )

        val field2 = NullableValueClassState.field("test2", stateWithValues)
        assertEquals(UserId("user999"), field2.value.userId)
        assertEquals(ProductId(777), field2.value.productId)
    }

    // ========== Complex Combined Test ==========

    @Test
    fun `field function should be generated for ComplexCombinedState`() {
        val initialValue = ComplexCombinedState.fake()
        val field = ComplexCombinedState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field")
        assertEquals(initialValue, field.value)
    }

    @Test
    fun `ComplexCombinedState should handle all new features together`() {
        val state = ComplexCombinedState(
            role = UserRole.ADMIN,
            email = EmailAddress("admin@example.com"),
            age = Age(30),
            status = Status.SUCCESS,
            optionalNote = "This is a note",
            isActive = true
        )

        val field = ComplexCombinedState.field("test", state)

        assertEquals(UserRole.ADMIN, field.value.role)
        assertEquals(EmailAddress("admin@example.com"), field.value.email)
        assertEquals(Age(30), field.value.age)
        assertEquals(Status.SUCCESS, field.value.status)
        assertEquals("This is a note", field.value.optionalNote)
        assertEquals(true, field.value.isActive)
    }

    @Test
    fun `ComplexCombinedState with all nulls`() {
        val state = ComplexCombinedState(
            role = UserRole.GUEST,
            email = null,
            age = null,
            status = Status.IDLE,
            optionalNote = null,
            isActive = false
        )

        val field = ComplexCombinedState.field("test", state)

        assertEquals(UserRole.GUEST, field.value.role)
        assertNull(field.value.email)
        assertNull(field.value.age)
        assertNull(field.value.optionalNote)
    }

    // ========== Nested with New Features ==========

    @Test
    fun `field function should be generated for AddressInfo`() {
        val initialValue = AddressInfo.fake()
        val field = AddressInfo.field("test", initialValue)

        assertNotNull(field, "AddressInfo should have field() generated recursively")
        assertEquals(initialValue, field.value)
    }

    @Test
    fun `field function should be generated for UserProfileWithNewFeatures`() {
        val initialValue = UserProfileWithNewFeatures.fake()
        val field = UserProfileWithNewFeatures.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field")
        assertEquals(initialValue, field.value)
    }

    @Test
    fun `UserProfileWithNewFeatures should preserve all property types`() {
        val address = AddressInfo("456 Oak Ave", "12345", "Canada")
        val state = UserProfileWithNewFeatures(
            userId = UserId("user001"),
            name = "Jane Smith",
            email = EmailAddress("jane@example.com"),
            age = Age(25),
            role = UserRole.ADMIN,
            address = address,
            status = Status.SUCCESS
        )

        val field = UserProfileWithNewFeatures.field("test", state)

        assertEquals(UserId("user001"), field.value.userId)
        assertEquals("Jane Smith", field.value.name)
        assertEquals(EmailAddress("jane@example.com"), field.value.email)
        assertEquals(Age(25), field.value.age)
        assertEquals(UserRole.ADMIN, field.value.role)
        assertEquals("456 Oak Ave", field.value.address.street)
        assertEquals("12345", field.value.address.zipCode)
        assertEquals(Status.SUCCESS, field.value.status)
    }
}
