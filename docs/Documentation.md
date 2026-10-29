---
layout: page
title: Documentation guide
---

## Edit a guide

1. Choose the reader: users need commands; developers need files, steps, and checks.
1. Put the task and a working example first. Explain only the rules needed to use or change it safely.
1. Use numbered steps for a procedure, bullets for independent rules, and a small table for comparisons.
1. Keep one source for shared rules. Link to field limits, data recovery, or tests instead of repeating them under each command.
1. Verify behavior against merged code. Describe unmerged changes as pending; do not call them released.
1. Check links, render the page, and run `./.github/run-checks.sh` before opening a PR.

Follow the [Google developer documentation style guide](https://developers.google.com/style) and [SE-EDU Markdown conventions](https://se-education.org/guides/conventions/markdown.html). Use plain language, sentence-case headings, blank lines before lists, `1.` for each numbered source item, and `*` for bullets. Do not hard-wrap prose at a fixed line length.

## Choose the page

| Content | Location |
|---|---|
| Task instructions and command examples | `UserGuide.md` |
| Setup, code paths, and change procedures | `DeveloperGuide.md`, `SettingUp.md` |
| JSON fields and recovery | `DataFormat.md` |
| User stories and use cases | `Requirements.md` |
| Reproducible UI/storage checks | `ManualTesting.md` |
| Build, CI, and release | `DevOps.md` |

Keep the main guides short. Supporting pages should add reference detail, not duplicate the guide. Preserve existing anchors when moving sections so old links still lead somewhere useful.

## Preview the site

1. Install a current Ruby and Bundler compatible with `docs/Gemfile.lock`.
1. From `docs/`, run `bundle install`, then `bundle exec jekyll serve --baseurl /tp`.
1. Open `http://127.0.0.1:4000/tp/`. Check desktop and narrow layouts, tables, command blocks, links, and images.

The Documentation workflow builds PRs without publishing. Production publishing runs from `master`; Pages must use **GitHub Actions** as its source. See [DevOps](DevOps.md#documentation-deployment).

## Edit a diagram

1. Change its `.puml` source in `docs/diagrams`.
1. Render and inspect it with [PlantUML](https://se-education.org/guides/tutorials/plantUml.html).
1. Keep the guide's image synchronized. The workflow generates the four undo/redo SVGs; other committed images need their own updated render.

For PDF output, follow [Saving web documents as PDF](https://se-education.org/guides/tutorials/savingPdf.html).
