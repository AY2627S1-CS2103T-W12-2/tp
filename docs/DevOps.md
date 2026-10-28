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

This project uses GitHub Actions for CI. The necessary workflow configuration files are in `.github/workflows`. No further setup is required.

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
