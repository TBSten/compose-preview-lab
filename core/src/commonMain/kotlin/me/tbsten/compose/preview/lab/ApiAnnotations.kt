package me.tbsten.compose.preview.lab

/**
 * The API marked with this annotation is experimental and unstable.
 * Binary compatibility is not guaranteed and may be changed or removed in the future.
 */
@Suppress("ExperimentalAnnotationRetention")
@RequiresOptIn(message = "This API is experimental. It could change in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalComposePreviewLabApi

/**
 * The API m arked with this annotation is internal and should not be used outside of the Compose Preview Lab library.
 * If for some reason you have no choice but to use the library, please take the latest precautions when updating the library.
 * It may change or be removed in future versions without notice.
 */
@Suppress("ExperimentalAnnotationRetention")
@RequiresOptIn(message = "This API is for internal. It could change in the future without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class InternalComposePreviewLabApi
