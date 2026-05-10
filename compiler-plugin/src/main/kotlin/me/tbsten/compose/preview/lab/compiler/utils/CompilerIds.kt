package me.tbsten.compose.preview.lab.compiler.utils

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Builds a top-level [ClassId] from a package name and a single class name.
 *
 * **Sample call**:
 * ```kotlin
 * classIdOf("me.tbsten.compose.preview.lab", "PreviewExport")
 * // == ClassId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("PreviewExport"))
 * ```
 *
 * Nested classes are out of scope; if a use case appears, add a vararg /
 * `parent.createNestedClassId(name)` overload at that time.
 */
internal fun classIdOf(packageName: String, name: String): ClassId = ClassId(FqName(packageName), Name.identifier(name))

/**
 * Builds a top-level [CallableId] from a package name and a callable name.
 *
 * **Sample call**:
 * ```kotlin
 * callableIdOf("kotlin", "lazy")
 * // == CallableId(FqName("kotlin"), Name.identifier("lazy"))
 * ```
 */
internal fun callableIdOf(packageName: String, name: String): CallableId =
    CallableId(FqName(packageName), Name.identifier(name))
