import type { ReactNode } from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';

import HomepageHeader from './components/HomepageHeader';
import FeaturesSection from './components/FeaturesSection';
import UseCasesSection from './components/UseCasesSection';
import QuickLinksSection from './components/QuickLinksSection';

export default function Home(): ReactNode {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title={`${siteConfig.title}`}
      description={siteConfig.tagline}>
      
      <HomepageHeader />
      
      <main>
        <FeaturesSection />
        <UseCasesSection />
        <QuickLinksSection />
      </main>

    </Layout>
  );
}
