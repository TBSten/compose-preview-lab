import { themes as prismThemes } from 'prism-react-renderer';
import type { Config } from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
  title: 'Compose Preview Lab',
  tagline: 'Compose Preview Lab turns @Preview into an interactive Component Playground',
  favicon: 'img/favicon.ico',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  // Set the production url of your site here
  url: 'https://tbsten.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/compose-preview-lab/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'TBSten', // Usually your GitHub org/user name.
  projectName: 'compose-preview-lab', // Usually your repo name.

  onBrokenLinks: 'throw',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en', 'ja'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          routeBasePath: 'docs',
          sidebarPath: './sidebars.ts',
          editUrl:
            'https://github.com/TBSten/compose-preview-lab/tree/main/integrationTest/web/',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/docusaurus-social-card.jpg',
    colorMode: {
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: 'Compose Preview Lab',
      logo: {
        alt: 'Compose Preview Lab Logo',
        src: 'img/logo.svg',
      },
      items: [
        {
          type: 'doc',
          docId: 'get-started',
          position: 'left',
          label: 'Docs',
        },
        {
          href: 'https://tbsten.github.io/compose-preview-lab/integrationTest/main/js/',
          label: 'Online Sample',
          position: 'left',
        },
        {
          href: 'https://tbsten.github.io/compose-preview-lab/dokka/',
          label: 'API',
          position: 'left',
        },
        {
          href: 'https://github.com/TBSten/compose-preview-lab',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Resources',
          items: [
            {
              label: 'Sample',
              href: 'https://tbsten.github.io/compose-preview-lab/integrationTest/main/js/',
            },
            {
              label: 'DeepWiki',
              href: 'https://deepwiki.com/TBSten/compose-preview-lab',
            },
            {
              label: 'GitHub',
              href: 'https://github.com/TBSten/compose-preview-lab',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Compose Preview Lab. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['kotlin'],
    },
  } satisfies Preset.ThemeConfig,
  staticDirectories: ["static", "../app/build/web-static-content"],

  markdown: {
    mermaid: true,
  },
  themes: ['@docusaurus/theme-mermaid'],
};

export default config;
