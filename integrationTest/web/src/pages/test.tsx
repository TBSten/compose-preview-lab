import React from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import { useInView } from 'react-intersection-observer';
import Layout from '@theme/Layout';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './test.module.css';

// Import icons
import { IconReview, IconCatalog, IconDocumentation } from '../components/Icons';

// Feature Icons
const IconInteractive = (props: React.SVGProps<SVGSVGElement>) => (
  <svg viewBox="0 0 24 24" fill="none" width="1em" height="1em" {...props}>
    <defs>
      <linearGradient id="gradInteractive" x1="4" y1="4" x2="20" y2="20" gradientUnits="userSpaceOnUse">
        <stop stopColor="#ff6b6b" />
        <stop offset="1" stopColor="#ee5a6f" />
      </linearGradient>
    </defs>
    <rect x="3" y="11" width="18" height="2" rx="1" fill="url(#gradInteractive)" opacity="0.3" />
    <circle cx="7" cy="12" r="2" fill="#fff" />
    <circle cx="17" cy="12" r="2" fill="#fff" />
    <circle cx="7" cy="12" r="1" fill="url(#gradInteractive)" />
    <circle cx="17" cy="12" r="1" fill="url(#gradInteractive)" />
    <path d="M7 12 L17 12" stroke="url(#gradInteractive)" strokeWidth="2" strokeLinecap="round" />
  </svg>
);

const IconSetup = (props: React.SVGProps<SVGSVGElement>) => (
  <svg viewBox="0 0 24 24" fill="none" width="1em" height="1em" {...props}>
    <defs>
      <linearGradient id="gradSetup" x1="12" y1="2" x2="12" y2="22" gradientUnits="userSpaceOnUse">
        <stop stopColor="#feca57" />
        <stop offset="1" stopColor="#ff9ff3" />
      </linearGradient>
    </defs>
    <path d="M12 2L2 7l10 5 10-5-10-5z" fill="url(#gradSetup)" opacity="0.2" />
    <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="url(#gradSetup)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="url(#gradSetup)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

const IconMultiplatform = (props: React.SVGProps<SVGSVGElement>) => (
  <svg viewBox="0 0 24 24" fill="none" width="1em" height="1em" {...props}>
    <defs>
      <linearGradient id="gradMultiplatform" x1="4" y1="4" x2="20" y2="20" gradientUnits="userSpaceOnUse">
        <stop stopColor="#48dbfb" />
        <stop offset="1" stopColor="#0abde3" />
      </linearGradient>
    </defs>
    <rect x="2" y="6" width="6" height="4" rx="1" fill="url(#gradMultiplatform)" opacity="0.2" />
    <rect x="10" y="6" width="6" height="4" rx="1" fill="url(#gradMultiplatform)" opacity="0.2" />
    <rect x="18" y="6" width="4" height="4" rx="1" fill="url(#gradMultiplatform)" opacity="0.2" />
    <rect x="2" y="14" width="6" height="4" rx="1" fill="url(#gradMultiplatform)" opacity="0.2" />
    <rect x="10" y="14" width="6" height="4" rx="1" fill="url(#gradMultiplatform)" opacity="0.2" />
    <rect x="18" y="14" width="4" height="4" rx="1" fill="url(#gradMultiplatform)" opacity="0.2" />
    <rect x="2" y="6" width="6" height="4" rx="1" stroke="#fff" strokeWidth="2" />
    <rect x="10" y="6" width="6" height="4" rx="1" stroke="#fff" strokeWidth="2" />
    <rect x="18" y="6" width="4" height="4" rx="1" stroke="#fff" strokeWidth="2" />
    <rect x="2" y="14" width="6" height="4" rx="1" stroke="#fff" strokeWidth="2" />
    <rect x="10" y="14" width="6" height="4" rx="1" stroke="#fff" strokeWidth="2" />
    <rect x="18" y="14" width="4" height="4" rx="1" stroke="#fff" strokeWidth="2" />
  </svg>
);

const IconCustom = (props: React.SVGProps<SVGSVGElement>) => (
  <svg viewBox="0 0 24 24" fill="none" width="1em" height="1em" {...props}>
    <defs>
      <linearGradient id="gradCustom" x1="12" y1="2" x2="12" y2="22" gradientUnits="userSpaceOnUse">
        <stop stopColor="#a29bfe" />
        <stop offset="1" stopColor="#6c5ce7" />
      </linearGradient>
    </defs>
    <circle cx="12" cy="12" r="3" fill="url(#gradCustom)" opacity="0.2" />
    <path d="M12 1v6M12 17v6M1 12h6M17 12h6M4.22 4.22l4.24 4.24M15.54 15.54l4.24 4.24M4.22 19.78l4.24-4.24M15.54 8.46l4.24-4.24" stroke="url(#gradCustom)" strokeWidth="2" strokeLinecap="round" />
    <circle cx="12" cy="12" r="3" stroke="#fff" strokeWidth="2" />
  </svg>
);

// Layout Option 1: Asymmetric Layout
const LayoutOption1 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案1: 非対称レイアウト</Heading>
        <div className={styles.testGridAsymmetric}>
          <div className={clsx(styles.testCard, styles.testCardLarge)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent(). Go beyond static snapshots.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardSmall)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardSmall)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardSmall)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 2: Staggered Layout
const LayoutOption2 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案2: スタッガードレイアウト</Heading>
        <div className={styles.testGridStaggered}>
          <div className={clsx(styles.testCard, styles.testCardStaggered1)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent(). Go beyond static snapshots.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardStaggered2)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}. No need to migrate to a separate source set.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardStaggered3)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS. Share your UI catalog on the web.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardStaggered4)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types. Includes utilities like SelectableField for various use cases.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 3: Centered Emphasis Layout
const LayoutOption3 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案3: 中央強調レイアウト</Heading>
        <div className={styles.testGridCentered}>
          <div className={clsx(styles.testCard, styles.testCardTop)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardCenterLeft)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent(). Go beyond static snapshots.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardCenterRight)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}. No need to migrate to a separate source set.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardBottom)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 4: Icon Cards Layout
const LayoutOption4 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案4: アイコン付きカード</Heading>
        <div className={styles.testGridIconCards}>
          <div className={clsx(styles.testCard, styles.testCardWithIcon)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent(). Go beyond static snapshots.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardWithIcon)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}. No need to migrate to a separate source set.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardWithIcon)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS. Share your UI catalog on the web.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardWithIcon)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types. Includes utilities like SelectableField for various use cases.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 5: Diagonal Layout
const LayoutOption5 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案5: 斜め配置レイアウト</Heading>
        <div className={styles.testGridDiagonal}>
          <div className={clsx(styles.testCard, styles.testCardDiagonal1)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent(). Go beyond static snapshots.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardDiagonal2)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}. No need to migrate to a separate source set.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardDiagonal3)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS. Share your UI catalog on the web.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardDiagonal4)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types. Includes utilities like SelectableField for various use cases.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 6: Radial Layout (放射状レイアウト)
const LayoutOption6 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案6: 放射状レイアウト</Heading>
        <div className={styles.testGridRadial}>
          <div className={clsx(styles.testCard, styles.testCardRadial1)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent().</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardRadial2)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardRadial3)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardRadial4)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 7: Floating 3D Cards (浮遊3Dカード)
const LayoutOption7 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案7: 浮遊3Dカード</Heading>
        <div className={styles.testGridFloating}>
          <div className={clsx(styles.testCard, styles.testCardFloat1)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent(). Go beyond static snapshots.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardFloat2)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}. No need to migrate to a separate source set.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardFloat3)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS. Share your UI catalog on the web.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardFloat4)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types. Includes utilities like SelectableField for various use cases.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 8: Puzzle Layout (パズル風レイアウト)
const LayoutOption8 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案8: パズル風レイアウト</Heading>
        <div className={styles.testGridPuzzle}>
          <div className={clsx(styles.testCard, styles.testCardPuzzle1)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent().</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardPuzzle2)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardPuzzle3)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardPuzzle4)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 9: Wave Layout (波状レイアウト)
const LayoutOption9 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案9: 波状レイアウト</Heading>
        <div className={styles.testGridWave}>
          <div className={clsx(styles.testCard, styles.testCardWave1)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent(). Go beyond static snapshots.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardWave2)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}. No need to migrate to a separate source set.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardWave3)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS. Share your UI catalog on the web.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardWave4)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types. Includes utilities like SelectableField for various use cases.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 10: Spiral Layout (螺旋レイアウト)
const LayoutOption10 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案10: 螺旋レイアウト</Heading>
        <div className={styles.testGridSpiral}>
          <div className={clsx(styles.testCard, styles.testCardSpiral1)}>
            <div className={styles.testCardIcon}>
              <IconInteractive />
            </div>
            <Heading as="h3">Interactive Verification</Heading>
            <p>Manipulate values with Field API and visualize events with onEvent(). Go beyond static snapshots.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardSpiral2)}>
            <div className={styles.testCardIcon}>
              <IconSetup />
            </div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}. No need to migrate to a separate source set.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardSpiral3)}>
            <div className={styles.testCardIcon}>
              <IconMultiplatform />
            </div>
            <Heading as="h3">Compose Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS. Share your UI catalog on the web.</p>
          </div>
          <div className={clsx(styles.testCard, styles.testCardSpiral4)}>
            <div className={styles.testCardIcon}>
              <IconCustom />
            </div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types. Includes utilities like SelectableField for various use cases.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 11: Feature Stream (ストリームライン)
const LayoutOption11 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案11: ストリームライン</Heading>
        <div className={styles.testStream}>
          <div className={styles.testStreamLine}></div>
          
          <div className={styles.testStreamItem}>
            <div className={styles.testStreamIcon}>
              <IconInteractive />
            </div>
            <div className={styles.testStreamContent}>
              <Heading as="h3">Interactive Verification</Heading>
              <p>Manipulate values with Field API and visualize events with onEvent().</p>
            </div>
          </div>

          <div className={clsx(styles.testStreamItem, styles.testStreamItemRight)}>
            <div className={styles.testStreamIcon}>
              <IconSetup />
            </div>
            <div className={styles.testStreamContent}>
              <Heading as="h3">Easy Setup</Heading>
              <p>Just wrap your existing @Preview with PreviewLab.</p>
            </div>
          </div>

          <div className={styles.testStreamItem}>
            <div className={styles.testStreamIcon}>
              <IconMultiplatform />
            </div>
            <div className={styles.testStreamContent}>
              <Heading as="h3">Compose Multiplatform</Heading>
              <p>Supports Android, JVM, JS, Wasm, and iOS.</p>
            </div>
          </div>

          <div className={clsx(styles.testStreamItem, styles.testStreamItemRight)}>
            <div className={styles.testStreamIcon}>
              <IconCustom />
            </div>
            <div className={styles.testStreamContent}>
              <Heading as="h3">Customizability</Heading>
              <p>Create custom Fields and types.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 12: Neon HUD (ネオンHUD)
const LayoutOption12 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案12: ネオンHUD</Heading>
        <div className={styles.testHudGrid}>
          <div className={styles.testHudItem}>
            <div className={styles.testHudFrame}>
              <div className={styles.testHudCornerTopLeft}></div>
              <div className={styles.testHudCornerTopRight}></div>
              <div className={styles.testHudCornerBottomLeft}></div>
              <div className={styles.testHudCornerBottomRight}></div>
              <div className={styles.testHudContent}>
                <div className={styles.testHudIcon}><IconInteractive /></div>
                <Heading as="h3">Interactive</Heading>
                <div className={styles.testHudData}>STATUS: ACTIVE</div>
                <p>Manipulate values with Field API.</p>
              </div>
            </div>
          </div>
          <div className={styles.testHudItem}>
            <div className={styles.testHudFrame}>
              <div className={styles.testHudCornerTopLeft}></div>
              <div className={styles.testHudCornerTopRight}></div>
              <div className={styles.testHudCornerBottomLeft}></div>
              <div className={styles.testHudCornerBottomRight}></div>
              <div className={styles.testHudContent}>
                <div className={styles.testHudIcon}><IconSetup /></div>
                <Heading as="h3">Easy Setup</Heading>
                <div className={styles.testHudData}>TIME: 5MIN</div>
                <p>Just wrap your existing @Preview.</p>
              </div>
            </div>
          </div>
          <div className={styles.testHudItem}>
            <div className={styles.testHudFrame}>
              <div className={styles.testHudCornerTopLeft}></div>
              <div className={styles.testHudCornerTopRight}></div>
              <div className={styles.testHudCornerBottomLeft}></div>
              <div className={styles.testHudCornerBottomRight}></div>
              <div className={styles.testHudContent}>
                <div className={styles.testHudIcon}><IconMultiplatform /></div>
                <Heading as="h3">Multiplatform</Heading>
                <div className={styles.testHudData}>TARGET: ALL</div>
                <p>Supports Android, JVM, JS, Wasm, iOS.</p>
              </div>
            </div>
          </div>
          <div className={styles.testHudItem}>
            <div className={styles.testHudFrame}>
              <div className={styles.testHudCornerTopLeft}></div>
              <div className={styles.testHudCornerTopRight}></div>
              <div className={styles.testHudCornerBottomLeft}></div>
              <div className={styles.testHudCornerBottomRight}></div>
              <div className={styles.testHudContent}>
                <div className={styles.testHudIcon}><IconCustom /></div>
                <Heading as="h3">Custom</Heading>
                <div className={styles.testHudData}>TYPE: FLEXIBLE</div>
                <p>Create custom Fields and types.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 13: Giant Typography (タイポグラフィ)
const LayoutOption13 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案13: タイポグラフィ</Heading>
        <div className={styles.testTypoGrid}>
          <div className={styles.testTypoItem}>
            <div className={styles.testTypoBg}>01</div>
            <div className={styles.testTypoContent}>
              <Heading as="h3">Interactive</Heading>
              <p>Manipulate values with Field API and visualize events with onEvent().</p>
            </div>
          </div>
          <div className={styles.testTypoItem}>
            <div className={styles.testTypoBg}>02</div>
            <div className={styles.testTypoContent}>
              <Heading as="h3">Setup</Heading>
              <p>Just wrap your existing @Preview with PreviewLab.</p>
            </div>
          </div>
          <div className={styles.testTypoItem}>
            <div className={styles.testTypoBg}>03</div>
            <div className={styles.testTypoContent}>
              <Heading as="h3">KMP</Heading>
              <p>Supports Android, JVM, JS, Wasm, and iOS.</p>
            </div>
          </div>
          <div className={styles.testTypoItem}>
            <div className={styles.testTypoBg}>04</div>
            <div className={styles.testTypoContent}>
              <Heading as="h3">Custom</Heading>
              <p>Create custom Fields and types.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 14: Hover Expand (伸縮リスト)
const LayoutOption14 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案14: 伸縮リスト</Heading>
        <div className={styles.testExpandList}>
          <div className={styles.testExpandItem}>
            <div className={styles.testExpandHeader}>
              <span className={styles.testExpandNumber}>01</span>
              <Heading as="h3">Interactive Verification</Heading>
              <div className={styles.testExpandIcon}><IconInteractive /></div>
            </div>
            <div className={styles.testExpandBody}>
              <p>Manipulate values with Field API and visualize events with onEvent(). Go beyond static snapshots.</p>
            </div>
          </div>
          <div className={styles.testExpandItem}>
            <div className={styles.testExpandHeader}>
              <span className={styles.testExpandNumber}>02</span>
              <Heading as="h3">Easy Setup</Heading>
              <div className={styles.testExpandIcon}><IconSetup /></div>
            </div>
            <div className={styles.testExpandBody}>
              <p>Just wrap your existing @Preview with PreviewLab {"{ ... }"}. No need to migrate to a separate source set.</p>
            </div>
          </div>
          <div className={styles.testExpandItem}>
            <div className={styles.testExpandHeader}>
              <span className={styles.testExpandNumber}>03</span>
              <Heading as="h3">Compose Multiplatform</Heading>
              <div className={styles.testExpandIcon}><IconMultiplatform /></div>
            </div>
            <div className={styles.testExpandBody}>
              <p>Supports Android, JVM, JS, Wasm, and iOS. Share your UI catalog on the web.</p>
            </div>
          </div>
          <div className={styles.testExpandItem}>
            <div className={styles.testExpandHeader}>
              <span className={styles.testExpandNumber}>04</span>
              <Heading as="h3">Customizability</Heading>
              <div className={styles.testExpandIcon}><IconCustom /></div>
            </div>
            <div className={styles.testExpandBody}>
              <p>Create custom Fields and types. Includes utilities like SelectableField for various use cases.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 15: Code Terminal (コードターミナル)
const LayoutOption15 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案15: コードターミナル</Heading>
        <div className={styles.testTerminal}>
          <div className={styles.testTerminalHeader}>
            <div className={styles.testTerminalButtons}>
              <span className={styles.testTerminalButtonRed}></span>
              <span className={styles.testTerminalButtonYellow}></span>
              <span className={styles.testTerminalButtonGreen}></span>
            </div>
            <div className={styles.testTerminalTitle}>features.kt</div>
          </div>
          <div className={styles.testTerminalBody}>
            <div className={styles.testTerminalLine}>
              <span className={styles.testCodeKeyword}>fun</span> <span className={styles.testCodeFunction}>features</span>() {"{"}
            </div>
            
            <div className={styles.testTerminalBlock}>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeComment}>// 1. Interactive Verification</span>
              </div>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeCall}>manipulate</span>(Field.API)
              </div>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeCall}>visualize</span>(Events)
              </div>
            </div>

            <div className={styles.testTerminalBlock}>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeComment}>// 2. Easy Setup</span>
              </div>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeAnnotation}>@Preview</span>
              </div>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeCall}>PreviewLab</span> {"{ ... }"}
              </div>
            </div>

            <div className={styles.testTerminalBlock}>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeComment}>// 3. Multiplatform</span>
              </div>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeKeyword}>val</span> targets = <span className={styles.testCodeString}>["Android", "JVM", "JS", "Wasm", "iOS"]</span>
              </div>
            </div>

            <div className={styles.testTerminalBlock}>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeComment}>// 4. Customizability</span>
              </div>
              <div className={styles.testTerminalLine}>
                &nbsp;&nbsp;<span className={styles.testCodeKeyword}>class</span> CustomField : Field
              </div>
            </div>

            <div className={styles.testTerminalLine}>{"}"}</div>
            <div className={styles.testTerminalCursor}>_</div>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 16: Glass Stack (ガラススタック)
const LayoutOption16 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案16: ガラススタック</Heading>
        <div className={styles.testStackContainer}>
          <div className={clsx(styles.testStackCard, styles.testStackCard1)}>
            <div className={styles.testStackIcon}><IconInteractive /></div>
            <Heading as="h3">Interactive</Heading>
            <p>Manipulate values with Field API and visualize events.</p>
          </div>
          <div className={clsx(styles.testStackCard, styles.testStackCard2)}>
            <div className={styles.testStackIcon}><IconSetup /></div>
            <Heading as="h3">Easy Setup</Heading>
            <p>Just wrap your existing @Preview with PreviewLab.</p>
          </div>
          <div className={clsx(styles.testStackCard, styles.testStackCard3)}>
            <div className={styles.testStackIcon}><IconMultiplatform /></div>
            <Heading as="h3">Multiplatform</Heading>
            <p>Supports Android, JVM, JS, Wasm, and iOS.</p>
          </div>
          <div className={clsx(styles.testStackCard, styles.testStackCard4)}>
            <div className={styles.testStackIcon}><IconCustom /></div>
            <Heading as="h3">Customizability</Heading>
            <p>Create custom Fields and types.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 17: Hexagon Hive (ヘキサゴンハイヴ)
const LayoutOption17 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案17: ヘキサゴンハイヴ</Heading>
        <div className={styles.testHiveGrid}>
          <div className={styles.testHiveItem}>
            <div className={styles.testHiveHex}>
              <div className={styles.testHiveContent}>
                <div className={styles.testHiveIcon}><IconInteractive /></div>
                <Heading as="h3">Interactive</Heading>
                <p>Manipulate values</p>
              </div>
            </div>
          </div>
          <div className={styles.testHiveItem}>
            <div className={styles.testHiveHex}>
              <div className={styles.testHiveContent}>
                <div className={styles.testHiveIcon}><IconSetup /></div>
                <Heading as="h3">Setup</Heading>
                <p>Easy to start</p>
              </div>
            </div>
          </div>
          <div className={styles.testHiveItem}>
            <div className={styles.testHiveHex}>
              <div className={styles.testHiveContent}>
                <div className={styles.testHiveIcon}><IconMultiplatform /></div>
                <Heading as="h3">KMP</Heading>
                <p>All platforms</p>
              </div>
            </div>
          </div>
          <div className={styles.testHiveItem}>
            <div className={styles.testHiveHex}>
              <div className={styles.testHiveContent}>
                <div className={styles.testHiveIcon}><IconCustom /></div>
                <Heading as="h3">Custom</Heading>
                <p>Flexible types</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 18: Elastic Split (エラスティックスプリット)
const LayoutOption18 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案18: エラスティックスプリット</Heading>
        <div className={styles.testSplitContainer}>
          <div className={styles.testSplitItem}>
            <div className={styles.testSplitBg} style={{background: 'linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%)'}}></div>
            <div className={styles.testSplitContent}>
              <IconInteractive className={styles.testSplitIcon} />
              <Heading as="h3">Interactive</Heading>
              <p>Manipulate values with Field API and visualize events.</p>
            </div>
          </div>
          <div className={styles.testSplitItem}>
            <div className={styles.testSplitBg} style={{background: 'linear-gradient(135deg, #feca57 0%, #ff9ff3 100%)'}}></div>
            <div className={styles.testSplitContent}>
              <IconSetup className={styles.testSplitIcon} />
              <Heading as="h3">Easy Setup</Heading>
              <p>Just wrap your existing @Preview with PreviewLab.</p>
            </div>
          </div>
          <div className={styles.testSplitItem}>
            <div className={styles.testSplitBg} style={{background: 'linear-gradient(135deg, #48dbfb 0%, #0abde3 100%)'}}></div>
            <div className={styles.testSplitContent}>
              <IconMultiplatform className={styles.testSplitIcon} />
              <Heading as="h3">Multiplatform</Heading>
              <p>Supports Android, JVM, JS, Wasm, and iOS.</p>
            </div>
          </div>
          <div className={styles.testSplitItem}>
            <div className={styles.testSplitBg} style={{background: 'linear-gradient(135deg, #a29bfe 0%, #6c5ce7 100%)'}}></div>
            <div className={styles.testSplitContent}>
              <IconCustom className={styles.testSplitIcon} />
              <Heading as="h3">Customizability</Heading>
              <p>Create custom Fields and types.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 19: Orbital System (オービタルシステム)
const LayoutOption19 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案19: オービタルシステム</Heading>
        <div className={styles.testOrbitSystem}>
          <div className={styles.testOrbitCore}>
            <div className={styles.testOrbitLogo}>Lab</div>
          </div>
          
          <div className={styles.testOrbitRing}>
            <div className={clsx(styles.testOrbitPlanet, styles.testOrbitPlanet1)}>
              <div className={styles.testOrbitIcon}><IconInteractive /></div>
              <div className={styles.testOrbitLabel}>Interactive</div>
            </div>
            <div className={clsx(styles.testOrbitPlanet, styles.testOrbitPlanet2)}>
              <div className={styles.testOrbitIcon}><IconSetup /></div>
              <div className={styles.testOrbitLabel}>Easy Setup</div>
            </div>
            <div className={clsx(styles.testOrbitPlanet, styles.testOrbitPlanet3)}>
              <div className={styles.testOrbitIcon}><IconMultiplatform /></div>
              <div className={styles.testOrbitLabel}>KMP</div>
            </div>
            <div className={clsx(styles.testOrbitPlanet, styles.testOrbitPlanet4)}>
              <div className={styles.testOrbitIcon}><IconCustom /></div>
              <div className={styles.testOrbitLabel}>Custom</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

// Layout Option 20: Architectural Lines (アーキテクチュアルライン)
const LayoutOption20 = () => {
  const { ref, inView } = useInView({ triggerOnce: true, threshold: 0.1 });
  return (
    <section className={clsx(styles.testSection, inView && styles.fadeIn)} ref={ref}>
      <div className="container">
        <Heading as="h2" className={styles.testSectionTitle}>案20: アーキテクチュアルライン</Heading>
        <div className={styles.testArchList}>
          <div className={styles.testArchItem}>
            <div className={styles.testArchLine}></div>
            <div className={styles.testArchMeta}>01</div>
            <div className={styles.testArchContent}>
              <Heading as="h3">Interactive Verification <IconInteractive style={{marginLeft: '10px', verticalAlign: 'middle'}} /></Heading>
              <p>Manipulate values with Field API and visualize events.</p>
            </div>
          </div>
          <div className={styles.testArchItem}>
            <div className={styles.testArchLine}></div>
            <div className={styles.testArchMeta}>02</div>
            <div className={styles.testArchContent}>
              <Heading as="h3">Easy Setup <IconSetup style={{marginLeft: '10px', verticalAlign: 'middle'}} /></Heading>
              <p>Just wrap your existing @Preview with PreviewLab.</p>
            </div>
          </div>
          <div className={styles.testArchItem}>
            <div className={styles.testArchLine}></div>
            <div className={styles.testArchMeta}>03</div>
            <div className={styles.testArchContent}>
              <Heading as="h3">Compose Multiplatform <IconMultiplatform style={{marginLeft: '10px', verticalAlign: 'middle'}} /></Heading>
              <p>Supports Android, JVM, JS, Wasm, and iOS.</p>
            </div>
          </div>
          <div className={styles.testArchItem}>
            <div className={styles.testArchLine}></div>
            <div className={styles.testArchMeta}>04</div>
            <div className={styles.testArchContent}>
              <Heading as="h3">Customizability <IconCustom style={{marginLeft: '10px', verticalAlign: 'middle'}} /></Heading>
              <p>Create custom Fields and types.</p>
            </div>
          </div>
          <div className={styles.testArchLine}></div>
        </div>
      </div>
    </section>
  );
};

export default function TestPage(): React.ReactNode {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout title="Layout Options Test" description="Testing different layout options for FeaturesSection">
      <main style={{ paddingTop: '6rem' }}>
        <LayoutOption1 />
        <LayoutOption2 />
        <LayoutOption3 />
        <LayoutOption4 />
        <LayoutOption5 />
        <LayoutOption6 />
        <LayoutOption7 />
        <LayoutOption8 />
        <LayoutOption9 />
        <LayoutOption10 />
        <LayoutOption11 />
        <LayoutOption12 />
        <LayoutOption13 />
        <LayoutOption14 />
        <LayoutOption15 />
        <LayoutOption16 />
        <LayoutOption17 />
        <LayoutOption18 />
        <LayoutOption19 />
        <LayoutOption20 />
      </main>
    </Layout>
  );
}
