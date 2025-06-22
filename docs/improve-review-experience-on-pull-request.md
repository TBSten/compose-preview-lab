> [!WARNING]
> 🚨 This documentation is WIP.

# Improve Review Experience on Pull Request by using Compose Preview Lab

This documentation guides you through setting up Compose Preview Lab to improve the UI implementation review experience.
Improving the UI implementation review experience is one of the most important goals that Compose Preview Lab has been striving
for since its inception.

## Prerequisite

- Development is progressing using a GitHub and pull request development workflow
- GitHub Actions are enabled
- GitHub Pages is available

In addition, the following conditions are also required.

- The UI is built on Compose Multiplatform (i.e., the UI is located in the `src/commonMain` directory)

## Goals

- [x] Deploy the Compose Preview Lab web application to GitHub Pages so that you can check the behavior of UI components on the
  web. This eliminates the need to open an IDE for review.
- [x] Make it possible to jump to the source code for each PreviewLab. This allows reviewers to navigate to the relevant source
  code when they notice unusual behavior in a component.
- [x] Filter Pull Requests to only those with differences in files, reducing the reviewer's workload.
- [x] Filter pull requests to show only files with changes, reducing the reviewer's workload.

![image](https://placehold.jp/800x500.png)

## Step 1. JS または WasmJs target を追加する

## Step 2. Github Pages を有効にし、Pull Request で PreviewLab を表示する

## Step 3. Pull Request 上のファイルを開く OpenFileHandler を設定する

## Step 4. Preview Lab で差分があったファイルのグループを作成する
