import { ComponentProps, CSSProperties, useState } from "react";
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './EmbeddedPreviewLab.module.css';
import clsx from "clsx";

const iframeSizeStyleMap = {
    small: styles.embeddedPreviewLabIframeSmall,
    medium: styles.embeddedPreviewLabIframeMedium,
    large: styles.embeddedPreviewLabIframeLarge,
} as const

const shadowStyleMap = {
    none: styles.embeddedPreviewLabShadowNone,
    small: styles.embeddedPreviewLabShadowSmall,
    medium: styles.embeddedPreviewLabShadowMedium,
    large: styles.embeddedPreviewLabShadowLarge,
} as const

export default function EmbeddedPreviewLab({
    previewId,
    size = "medium",
    shadow = "none",
    noPadding = false,
    lazy = true,
    iframeOptions = {},
}: {
    previewId: string | null,
    size: keyof typeof iframeSizeStyleMap,
    shadow?: keyof typeof shadowStyleMap,
    noPadding?: boolean,
    lazy?: boolean,
    iframeOptions?: ComponentProps<"iframe">,
}) {
    const [isLoading, setIsLoading] = useState(true);
    
    let url = "compose-preview-lab-gallery/?iframe"
    if (previewId != null) {
        url += `&previewId=${previewId}`
    }

    const iframeSrc = useBaseUrl(url)
    
    const handleLoad = () => {
        setIsLoading(false);
    };
    
    return (
        <div className={clsx(!noPadding && styles.embeddedPreviewLabContainer, styles.embeddedPreviewLabWrapper)}>
            {isLoading && (
                <div className={styles.loadingIndicator}>
                    <div className={styles.spinner} />
                </div>
            )}
            <iframe
                className={clsx(
                    styles.embeddedPreviewLabIframe,
                    iframeSizeStyleMap[size],
                    shadowStyleMap[shadow],
                    isLoading && styles.embeddedPreviewLabIframeLoading,
                )}
                src={iframeSrc}
                onLoad={handleLoad}
                loading={lazy ? "lazy" : "eager"}
                {...iframeOptions}
            />
        </div>
    )
}
