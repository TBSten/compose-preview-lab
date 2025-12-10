import { composePreviewLabVersion } from "../generated/libVersion"

export default function ComposePreviewLabVersion() {
  return (
    <img
      alt={composePreviewLabVersion}
      src="https://img.shields.io/maven-central/v/me.tbsten.compose.preview.lab/core"
    />
  )
}
