import React from 'react';

export const IconReview = (props: React.SVGProps<SVGSVGElement>) => (
  <svg viewBox="0 0 24 24" fill="none" width="1em" height="1em" {...props}>
    <defs>
      <linearGradient id="gradReview" x1="6" y1="2" x2="18" y2="18" gradientUnits="userSpaceOnUse">
        <stop stopColor="#4facfe" />
        <stop offset="1" stopColor="#00f2fe" />
      </linearGradient>
    </defs>
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" fill="url(#gradReview)" opacity="0.2" />
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="url(#gradReview)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    <polyline points="14 2 14 8 20 8" stroke="#00f2fe" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    <path d="M9 15l2 2 4-4" stroke="#fff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

export const IconShare = (props: React.SVGProps<SVGSVGElement>) => (
  <svg viewBox="0 0 24 24" fill="none" width="1em" height="1em" {...props}>
    <defs>
      <linearGradient id="gradShare" x1="6" y1="12" x2="18" y2="12" gradientUnits="userSpaceOnUse">
        <stop stopColor="#43e97b" />
        <stop offset="1" stopColor="#38f9d7" />
      </linearGradient>
    </defs>
    <circle cx="18" cy="5" r="3" fill="#38f9d7" opacity="0.2" />
    <circle cx="6" cy="12" r="3" fill="#43e97b" opacity="0.2" />
    <circle cx="18" cy="19" r="3" fill="#38f9d7" opacity="0.2" />
    <circle cx="18" cy="5" r="3" stroke="#38f9d7" strokeWidth="2" />
    <circle cx="6" cy="12" r="3" stroke="#43e97b" strokeWidth="2" />
    <circle cx="18" cy="19" r="3" stroke="#38f9d7" strokeWidth="2" />
    <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" stroke="#fff" strokeWidth="2" />
    <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" stroke="#fff" strokeWidth="2" />
  </svg>
);

export const IconCatalog = (props: React.SVGProps<SVGSVGElement>) => (
  <svg viewBox="0 0 24 24" fill="none" width="1em" height="1em" {...props}>
    <defs>
      <linearGradient id="gradCatalog" x1="3" y1="3" x2="21" y2="21" gradientUnits="userSpaceOnUse">
        <stop stopColor="#a18cd1" />
        <stop offset="1" stopColor="#fbc2eb" />
      </linearGradient>
    </defs>
    <rect x="3" y="3" width="7" height="7" rx="1" fill="#a18cd1" opacity="0.2" />
    <rect x="14" y="3" width="7" height="7" rx="1" fill="#fbc2eb" opacity="0.2" />
    <rect x="14" y="14" width="7" height="7" rx="1" fill="#a18cd1" opacity="0.2" />
    <rect x="3" y="14" width="7" height="7" rx="1" fill="#fbc2eb" opacity="0.2" />
    
    <rect x="3" y="3" width="7" height="7" rx="1" stroke="#fff" strokeWidth="2" />
    <rect x="14" y="3" width="7" height="7" rx="1" stroke="#fff" strokeWidth="2" />
    <rect x="14" y="14" width="7" height="7" rx="1" stroke="#fff" strokeWidth="2" />
    <rect x="3" y="14" width="7" height="7" rx="1" stroke="#fff" strokeWidth="2" />
  </svg>
);

export const IconDocumentation = (props: React.SVGProps<SVGSVGElement>) => (
  <svg viewBox="0 0 24 24" fill="none" width="1em" height="1em" {...props}>
    <defs>
      <linearGradient id="gradDocumentation" x1="4" y1="2" x2="20" y2="22" gradientUnits="userSpaceOnUse">
        <stop stopColor="#667eea" />
        <stop offset="1" stopColor="#764ba2" />
      </linearGradient>
    </defs>
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="url(#gradDocumentation)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" fill="url(#gradDocumentation)" opacity="0.2" />
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="url(#gradDocumentation)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    <line x1="9" y1="7" x2="15" y2="7" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
    <line x1="9" y1="11" x2="15" y2="11" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
    <line x1="9" y1="15" x2="13" y2="15" stroke="#fff" strokeWidth="2" strokeLinecap="round" />
  </svg>
);
