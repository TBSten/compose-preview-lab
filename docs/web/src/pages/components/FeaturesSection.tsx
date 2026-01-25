import React, { useState, useEffect, useRef } from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './FeaturesSection.module.css';
import EmbeddedPreviewLab from '../../components/EmbeddedPreviewLab';

const FEATURES = [
  {
    title: "Interactive Preview",
    description: "Transform static previews into interactive ones using PreviewLab's Field and Event APIs. Dramatically improve the efficiency of manual testing for UI components.",
    previewId: null,
  },
  {
    title: "Preview Gallery",
    description: "Automatically collect previews and easily create a multiplatform preview catalog app.",
    previewId: null,
  },
  {
    title: "Customizability",
    description: "Designed with customizability in mind from the start, supporting any parameters. Easily create Fields using utilities or build completely custom Fields.",
    previewId: null,
  },
];

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

function FeatureItem({ feature, index, setActiveIndex }: { feature: typeof FEATURES[0], index: number, setActiveIndex: (index: number) => void }) {
  const { ref, inView } = useIntersectionObserver({
    threshold: 0.5,
    triggerOnce: false,
  });

  useEffect(() => {
    if (inView) {
      setActiveIndex(index);
    }
  }, [inView, index, setActiveIndex]);

  return (
    <div ref={ref as React.RefObject<HTMLDivElement>} className={clsx(styles.card, styles.featureCard)} data-number={`0${index + 1}`}>
      <Heading as="h3">{feature.title}</Heading>
      <p>{feature.description}</p>
    </div>
  );
}

export default function FeaturesSection() {
  const [activeIndex, setActiveIndex] = useState(0);
  const { ref, inView } = useIntersectionObserver({ triggerOnce: true, threshold: 0.1 });

  return (
    <section id="features" className={clsx(styles.section, styles.featuresSection, inView && styles.fadeIn)} ref={ref as React.RefObject<HTMLElement>}>
      <div className="container">
        <Heading as="h2" className={styles.sectionTitle}>Key Features</Heading>
        <p className={styles.sectionDescription}>
          Transform static previews into interactive playgrounds.
        </p>
        
        <div className={styles.contentWrapper}>
          <div className={styles.stickyColumn}>
             <div className={styles.previewWrapper}>
                <EmbeddedPreviewLab 
                    previewId={null} 
                    size="large" 
                    noPadding
                    lazy={false}
                    shadow="large"
                    title="Preview Gallery"
                />
             </div>
          </div>
          
          <div className={styles.scrollColumn}>
            {FEATURES.map((feature, index) => (
              <FeatureItem 
                key={index} 
                feature={feature} 
                index={index} 
                setActiveIndex={setActiveIndex} 
              />
            ))}
          </div>
        </div>

      </div>
    </section>
  );
}
