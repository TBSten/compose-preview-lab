package me.tbsten.compose.preview.lab.ksp.plugin

internal class InvalidPreviewsListPackageException(previewsListPackage: String) :
    IllegalStateException(
        "ksp arg `composePreviewLab.previewsListPackage` (\"${previewsListPackage}\") is invalid. " +
            "Please set not empty value.\n" +
            "\n" +
            "  // build.gradle.kts\n" +
            "  ksp {\n" +
            "      arg(\"composePreviewLab.previewsListPackage\", \"myModule\")" +
            "  }\n" +
            "",
    )

internal class NotConfiguredPreviewsListPackageException :
    IllegalStateException(
        "ksp arg `composePreviewLab.previewsListPackage` is not set. Please set it.\n" +
            "\n" +
            "  // build.gradle.kts\n" +
            "  ksp {\n" +
            "      arg(\"composePreviewLab.previewsListPackage\", \"myModule\")\n" +
            "  }\n" +
            "",
    )
