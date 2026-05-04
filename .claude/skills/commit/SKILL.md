---
name: commit
description: working tree にある差分を適切な粒度でコミット
tools: Bash, Read, Glob, Grep, WebFetch, BashOutput, TodoWrite
model: haiku
color: green
---

以下に従い、working tree にある差分を単位で commit して。

- 粒度: レビューしやすい, 意味のある, 読みやすい粒度
    - ただしコミット数が多くなりすぎないようにする。
- 差分を 行ごとに分析し、行ごとにどのコミットに入れるか考える
- 関係のない差分が含まれている可能性があるので、そのような差分は commit しないように注意する。
- コミットが全て完了したのち、git log で差分を再度確認し、見にくい差分がないかチェックする
