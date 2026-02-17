import React, { ReactNode } from 'react';
import CodeBlock from '@theme/CodeBlock';
import styles from './CompareCode.module.css';

interface CompareCodeProps {
  before: string;
  after: string;
  beforeLabel?: string;
  afterLabel?: string;
  language?: string;
}

function dedent(str: string): string {
  const lines = str.split('\n');

  // 先頭の空行を削除
  while (lines.length > 0 && lines[0].trim() === '') {
    lines.shift();
  }
  // 末尾の空行を削除
  while (lines.length > 0 && lines[lines.length - 1].trim() === '') {
    lines.pop();
  }

  if (lines.length === 0) return '';

  // 空行以外の行から最小インデントを見つける
  let minIndent = Infinity;
  for (const line of lines) {
    if (line.trim() === '') continue;
    // スペースとタブをインデントとして考慮
    const match = line.match(/^[ \t]*/);
    if (match) {
      minIndent = Math.min(minIndent, match[0].length);
    }
  }

  // 空行しかない場合はインデント0
  if (minIndent === Infinity) {
    minIndent = 0;
  }

  return lines
    .map(line => {
      // 空行は空文字にする
      if (line.trim() === '') return '';
      
      const match = line.match(/^[ \t]*/);
      const currentIndent = match ? match[0].length : 0;
      
      // 最小インデント分だけ削除
      return line.slice(Math.min(currentIndent, minIndent));
    })
    .join('\n');
}

export default function CompareCode({
  before,
  after,
  beforeLabel = "変更前",
  afterLabel = "変更後",
  language = "kt",
}: CompareCodeProps): ReactNode {
  return (
    <div className={styles.container}>
      <div className={styles.column}>
        {beforeLabel && <div className={styles.label}>{beforeLabel}</div>}
        <CodeBlock language={language}>{dedent(before)}</CodeBlock>
      </div>
      <div className={styles.column}>
        {afterLabel && <div className={styles.label}>{afterLabel}</div>}
        <CodeBlock language={language}>{dedent(after)}</CodeBlock>
      </div>
    </div>
  );
}
