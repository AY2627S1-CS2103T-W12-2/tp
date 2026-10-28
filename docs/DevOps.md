---
layout: page
title: DevOps guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Build automation

This project uses Gradle for **build automation and dependency management**. **We recommend reading [this Gradle tutorial from se-edu/guides](https://se-education.org/guides/tutorials/gradle.html).**


The following commands perform common Gradle tasks.


* **`clean`**: Deletes the files created during the previous build tasks (e.g. files in the `build` folder).<br>
  For example: `./gradlew clean`

* **`shadowJar`**: Uses the Shadow plugin to create the fat JAR file `build/libs/larperboard.jar`.<br>
  For example: `./gradlew shadowJar`

* **`run`**: Builds and runs the application.<br>
  **`runShadow`**: Builds the application as a fat JAR, then runs it.

* **`checkstyleMain`**: Runs the code style check for the main code base.<br>
  **`checkstyleTest`**: Runs the code style check for the test code base.

* **`javadoc`**: Builds the Java API documentation and validates links and markup. CI also runs this task.
  Missing-tag warnings inherited from existing code are reported separately from errors; Checkstyle enforces
  the repository's Javadoc requirements.

* **`test`**
  * `./gradlew test`: Runs all tests.
  * `./gradlew clean test`: Cleans the project before running all tests

--------------------------------------------------------------------------------------------------------------------

## Continuous integration (CI)

This project uses GitHub Actions for CI. Workflow configuration is versioned in `.github/workflows`;
Pages also requires the repository publishing setting described below.

Java CI runs on pushes to `master`, pull requests targeting `master`, and manual dispatches.
Feature-branch pushes are checked through their PR, so opening a PR does not duplicate a push build.
Tag pushes do not start another matrix: release the exact source already verified before tagging.
New commits supersede older runs for the same PR or branch. A failure on one platform does not
cancel the other platforms, so their results remain available.

The matrix uses Linux, Windows and the explicit `macos-15-intel` runner. `macos-latest` selects
the arm64 pool, which could not allocate runners for several v1.4.1 jobs. The Intel label avoids
that pool while retaining macOS checks; this does not guarantee runner availability or replace
Apple Silicon coverage. Revisit the pin when GitHub deprecates the image or arm64 capacity recovers.
See the [supported runner labels](https://docs.github.com/en/actions/reference/runners/github-hosted-runners).

### Documentation deployment

Set **Settings → Pages → Build and deployment → Source** to **GitHub Actions** (`build_type: workflow`).
Do not also enable branch-based Pages publishing: the custom workflow generates PlantUML diagrams
and must be the only publisher. Changing the publishing method preserves the existing website URL.

The Documentation workflow validates PRs without deploying them. Pushes to `master` and manual
dispatches on `master` build and deploy the site. Runs for the same ref share a concurrency group.
An active production build/deployment finishes before another starts; stale PR builds can be cancelled.
This prevents competing custom deployments, while the Pages source setting removes the competing
legacy publisher. See [workflow concurrency](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/control-workflow-concurrency).

### Recovering infrastructure failures

If a macOS job fails with `The job was not acquired by Runner of type hosted even after multiple attempts`,
the build never started. Retry failed jobs after capacity recovers. GitHub reruns use the original run's
workflow revision, so retrying an old `macos-latest` failure still uses arm64; new runs use the Intel pin.
A successful newer run does not erase historical failures. Do not move release tags to change old CI results.

For a Pages deployment conflict, let the active deployment finish, confirm the source setting above,
then manually run Documentation on `master`. Do not retry obsolete deployments that would publish
older documentation. Check the deployed guide after the successful run.

### Code coverage

As part of CI, Gradle generates JaCoCo coverage reports from the tests, and CI uploads the coverage data to Codecov. Codecov then provides information about test coverage.

However, because Codecov is known to run into intermittent problems (e.g., report upload fails) due to issues on the Codecov service side, the CI is configured to pass even if the Codecov task failed. Therefore, developers are advised to check the code coverage levels periodically and take corrective actions if the coverage level falls below desired levels.

To enable Codecov for forks of this project, follow the steps given in [this se-edu guide](https://se-education.org/guides/tutorials/codecov.html).

### Repository-wide checks

In addition to Gradle checks, CI runs repository-wide checks. Unlike Gradle checks, which cover files used in the build, these checks cover every repository file and enforce rules that are hard to apply on development machines, such as line-ending requirements.

These checks are implemented as POSIX shell scripts, and thus can only be run on POSIX-compliant operating systems such as macOS and Linux. To run all checks locally on these operating systems, execute the following in the repository root directory:

`./.github/run-checks.sh`

Any warnings or errors will be printed out to the console.

**If adding new checks:**

* Checks are implemented as executable `check-*` scripts within the `.github` directory. The `run-checks.sh` script will automatically pick up and run files named as such. That is, you can add more such files if you need and the CI will do the rest.

* Check scripts should print out errors in the format `SEVERITY:FILENAME:LINE: MESSAGE`
  * SEVERITY is either ERROR or WARN.
  * FILENAME is the path to the file relative to the current directory.
  * LINE is the line of the file where the error occurred and MESSAGE is the message explaining the error.

* Check scripts must exit with a non-zero exit code if any errors occur.

--------------------------------------------------------------------------------------------------------------------

## Making a release

Prepare each release from the latest merged `master`, on a meaningful kebab-case branch such as
`release-v1-4-1`. Preserve local test data. Use imperative commit subjects of at most 72 characters,
a blank line before the explanatory body, and body lines wrapped at 72 characters.

1. Update `MainApp.VERSION`, the UG, landing page, README and versioned release notes together.
1. Run `./gradlew clean check coverage javadoc shadowJar`, `./.github/run-checks.sh` and `git diff --check`.
   Checkstyle includes the project's Java and Javadoc conventions. Fix Javadoc generation errors;
   do not disable link/markup checking to hide them.
1. Smoke-test the exact `build/libs/larperboard.jar` in a clean writable folder with Java 25,
   using `java -ea -jar larperboard.jar`. Check core commands, invalid input, photo storage,
   undo/redo, restart and legacy JSON. Confirm actual window layout and identify any scaling
   checks that were simulated rather than native OS checks.
1. Commit only release sources/docs, open a PR and wait for Java CI and documentation builds.
   Merge the reviewed release change; do not include unmerged feature branches implicitly.
1. Create an annotated version tag on that merged commit, e.g. `git tag -a v1.4.1 -m "Release LarperBoard v1.4.1"`.
   Do not move an existing release tag.
1. Publish a GitHub release for that tag with the exact tested JAR, `SHA256SUMS`, accurate notes
   and verification evidence. Verify the uploaded JAR's SHA-256 after downloading it again.
   Link known limitations and explain any removed commands in the upgrade notes.
