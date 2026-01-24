package me.tbsten.compose.preview.lab

/**
 * The API marked with this annotation is experimental and unstable.
 * Binary compatibility is not guaranteed and may be changed or removed in the future.
 *
 * | User | Status |
 * |---|---|
 * | Users of this library | You can use this experimental API with caution. |
 * | Intermediate users who want to customize parts of this library | You can use this experimental API with caution. |
 */
@Suppress("ExperimentalAnnotationRetention")
@RequiresOptIn(message = "This API is experimental. It could change in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
)
annotation class ExperimentalComposePreviewLabApi

/**
 * The API marked with this annotation is internal and should not be used outside of the Compose Preview Lab library.
 * If for some reason you have no choice but to use the library, please take the latest precautions when updating the library.
 * It may change or be removed in future versions without notice.
 *
 * | User | Status |
 * |---|---|
 * | Users of this library | You should avoid using this API. |
 * | Intermediate users who want to customize parts of this library | Use this API with caution. Please be aware that compatibility may break between versions. |
 */
@Suppress("ExperimentalAnnotationRetention")
@RequiresOptIn(message = "This API is for internal. It could change in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_SETTER,
)
annotation class InternalComposePreviewLabApi

/**
 * The API marked with this annotation is the UI component API.
 * These UI components are designed for use within the Compose Preview Lab library,
 * but can be used by users who want to customize the UI.
 * Binary compatibility is not guaranteed and may be changed in future versions.
 *
 * | User | Status |
 * |---|---|
 * | Users of this library | You can use these UI components if you want to customize the preview UI. |
 * | Intermediate users who want to customize parts of this library | Use this API with caution. Please be aware that compatibility may break between versions. |
 */
@Suppress("ExperimentalAnnotationRetention")
@RequiresOptIn(message = "This API is for UI components. It could change in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_SETTER,
)
annotation class UiComposePreviewLabApi
