import React, { useEffect, useRef, useState } from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './UseCasesSection.module.css';
import { IconReview, IconCatalog, IconDocumentation } from '../../components/Icons';

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

export default function UseCasesSection() {
  const { ref, inView } = useIntersectionObserver({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.section, styles.useCasesSection, inView && styles.fadeIn)} ref={ref as React.RefObject<HTMLElement>}>
      <div className="container">
        <Heading as="h2" className={styles.sectionTitle}>Reimagine Your Workflow</Heading>
        <p className={styles.sectionDescription}>
          Discover how Compose Preview Lab transforms your workflow.
        </p>
        <div className={styles.grid}>
          <div className={styles.card}>
            <div className={styles.cardIcon}>
              <IconCatalog />
            </div>
            <div className={styles.cardContent}>
              <Heading as="h3">UI Catalog</Heading>
              <p>Generate a comprehensive UI catalog as documentation.</p>
            </div>
          </div>
          <div className={styles.card}>
            <div className={styles.cardIcon}>
              <IconReview />
            </div>
            <div className={styles.cardContent}>
              <Heading as="h3">Efficient PR Reviews</Heading>
              <p>Manual testing made easy directly within Pull Requests.</p>
            </div>
          </div>
          <div className={styles.card}>
            <div className={styles.cardIcon}>
              <IconDocumentation />
            </div>
            <div className={styles.cardContent}>
              <Heading as="h3">Library Documentation</Heading>
              <p>Create comprehensive documentation for your library users with interactive examples.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
