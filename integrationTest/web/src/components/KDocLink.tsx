import React, { ComponentProps } from 'react'
import Link from "@docusaurus/Link"
import { composePreviewLabVersion } from "../generated/libVersion"

export default function KDocLink({
    path,
    ...linkProps
}: ComponentProps<typeof Link> & {
    to: never
    path: string,
}) {
    const baseUrl = `https://tbsten.github.io/compose-preview-lab/dokka/${composePreviewLabVersion}`

    return (
        <Link
            {...linkProps}
            to={
                baseUrl.replace(/\/+$/, '') + 
                '/' + 
                path.replace(/^\/+/, '')
            }
        >
            {linkProps.children}
        </Link>
    )
}
