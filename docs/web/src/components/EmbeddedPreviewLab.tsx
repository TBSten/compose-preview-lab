import { ComponentProps, CSSProperties, useLayoutEffect, useRef, useState } from "react";
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './EmbeddedPreviewLab.module.css';
import clsx from "clsx";
import Link from "@docusaurus/Link";

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
    title,
    size = "medium",
    shadow = "none",
    noPadding = false,
    lazy = true,
    iframeOptions = {},
}: {
    previewId: string | null,
    title?: string,
    size: keyof typeof iframeSizeStyleMap,
    shadow?: keyof typeof shadowStyleMap,
    noPadding?: boolean,
    lazy?: boolean,
    iframeOptions?: ComponentProps<"iframe">,
}) {
    const iframeRef = useRef<HTMLIFrameElement>(null);
    const [isLoading, setIsLoading] = useState(true);

    let url = "compose-preview-lab-gallery/?iframe"
    if (previewId != null) {
        url += `&previewId=${previewId}`
    }

    const iframeSrc = useBaseUrl(url)
    const titleLinkSrc = useBaseUrl(`compose-preview-lab-gallery/?previewId=${previewId}`)

    // マウント時にiframeがすでに読み込まれているか確認（キャッシュがある場合など）
    useLayoutEffect(() => {
        const iframe = iframeRef.current;
        if (iframe) {
            // iframeのdocumentにアクセスして読み込み完了を確認
            // readyStateが"complete"の場合、すでに読み込まれている
            try {
                const iframeDoc = iframe.contentDocument || iframe.contentWindow?.document;
                if (iframeDoc && iframeDoc.readyState === 'complete') {
                    setIsLoading(false);
                }
            } catch {
                // cross-originの場合はアクセスできないのでonLoadに任せる
            }
        }
    }, []);

    const handleLoad = () => {
        setIsLoading(false);
    };

    return (
        <div>
            <div className={clsx(!noPadding && styles.embeddedPreviewLabContainer, styles.embeddedPreviewLabWrapper)}>
                {isLoading && (
                    <div className={styles.loadingIndicator}>
                        <div className={styles.spinner} />
                    </div>
                )}
                <iframe
                    ref={iframeRef}
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

                {title && (
                    <div className={styles.embeddedPreviewLabTitle}>
                        <Link to={titleLinkSrc} target="_blank">
                            {title}
                        </Link>
                    </div>
                )}
            </div>
        </div>
    )
}
