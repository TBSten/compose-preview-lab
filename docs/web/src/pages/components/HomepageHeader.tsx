import React from 'react';
import clsx from 'clsx';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Heading from '@theme/Heading';
import Link from '@docusaurus/Link';
import styles from './HomepageHeader.module.css';
import useBaseUrl from '@docusaurus/useBaseUrl';

export default function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={clsx('hero', styles.heroBanner)}>
      <div className={styles.heroBackground}>
        <div className={styles.heroOrb1} />
        <div className={styles.heroOrb2} />
        <div className={styles.heroGrid} />
      </div>
      <div className={clsx("container", styles.heroContainer)}>
        <Heading as="h1" className={styles.heroTitle}>
          <span className={styles.titleCompose}>Compose</span>
          <span className={styles.titlePreviewLab}>Preview Lab</span>
        </Heading>
        <p className={styles.heroSubtitle}>{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <Link
            className={clsx("button button--lg", styles.heroButton)}
            to="/docs/get-started">
            Get Started - 5min ⏱️
          </Link>
          <Link
            className={clsx("button button--lg", styles.heroButtonSecondary)}
            to="https://tbsten.github.io/compose-preview-lab/integrationTest/main/js/compose-preview-lab-gallery/"
          >
            View Demo 👁️
          </Link>
        </div>
      </div>
    </header>
  );
}
