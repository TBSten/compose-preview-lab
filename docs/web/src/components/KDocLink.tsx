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
    const href = baseUrl.replace(/\/+$/, '') + '/' + path.replace(/^\/+/, '');

    return (
        <Link
            {...linkProps}
            to={href}
        >
            {linkProps.children}
        </Link>
    )
}
