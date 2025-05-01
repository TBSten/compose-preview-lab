package me.tbsten.compose.preview.lab.gradle.plugin

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

abstract class ComposePreviewLabExtension @Inject constructor(objectFactory: ObjectFactory) {
    val sourceSets: SetProperty<SourceDirectorySet> =
        objectFactory.setProperty(SourceDirectorySet::class.java)
}
