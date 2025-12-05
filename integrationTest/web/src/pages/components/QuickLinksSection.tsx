import React, { useEffect, useRef, useState } from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import Link from '@docusaurus/Link';
import styles from './QuickLinksSection.module.css';

function useIntersectionObserver(options: { threshold?: number; triggerOnce?: boolean } = {}) {
  const { threshold = 0.1, triggerOnce = true } = options;
  const [isIntersecting, setIsIntersecting] = useState(false);
  const [hasIntersected, setHasIntersected] = useState(false);
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    const element = ref.current;
    if (!element) return;

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setIsIntersecting(true);
            if (triggerOnce) {
              setHasIntersected(true);
            }
          } else if (!triggerOnce) {
            setIsIntersecting(false);
          }
        });
      },
      { threshold }
    );

    observer.observe(element);

    return () => {
      observer.disconnect();
    };
  }, [threshold, triggerOnce]);

  return { ref, inView: triggerOnce ? hasIntersected : isIntersecting };
}

export default function QuickLinksSection() {
  const { ref, inView } = useIntersectionObserver({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.section, styles.quickLinksSection, inView && styles.fadeIn)} ref={ref as React.RefObject<HTMLElement>}>
      <div className="container">
        <Heading as="h2" className={styles.sectionTitle}>Unlock the Full Potential</Heading>
        <p className={styles.sectionDescription}>
          Explore guides to master Compose Preview Lab.
        </p>
        <div className={styles.grid}>
          <Link
            to="/docs/get-started"
            className={clsx(styles.card, styles.linkCard)}
            data-index="01">
            <Heading as="h3">Get Started</Heading>
            <p>Start your journey with Compose Preview Lab.</p>
          </Link>
          <Link
            to="/docs/install"
            className={clsx(styles.card, styles.linkCard)}
            data-index="02">
            <Heading as="h3">Installation</Heading>
            <p>Step-by-step guide to set up in your project.</p>
          </Link>
          <Link
            to="/docs/guides/fields"
            className={clsx(styles.card, styles.linkCard)}
            data-index="03">
            <Heading as="h3">Fields System</Heading>
            <p>Learn about the powerful Field API.</p>
          </Link>
          <Link
            to="/docs/guides/events"
            className={clsx(styles.card, styles.linkCard)}
            data-index="04">
            <Heading as="h3">Events System</Heading>
            <p>Visualize and debug component events.</p>
          </Link>
          <Link
            to="/docs/guides/featured-files"
            className={clsx(styles.card, styles.linkCard)}
            data-index="05">
            <Heading as="h3">Preview Gallery</Heading>
            <p>Organize and browse your previews.</p>
          </Link>
        </div>
      </div>
    </section>
  );
}
