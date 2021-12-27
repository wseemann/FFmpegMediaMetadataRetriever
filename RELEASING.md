Releasing
========

1. Change the version in `gradle.properties` to a non-SNAPSHOT version.
2. Update the `README.md` with the new version.
3. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new
   version)
4.  `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new
    version)
5.  **IN EACH MODULE** (core and native) Run `./gradlew clean` and then `./gradlew uploadArchives --no-daemon --no-parallel`
6.  Update the `gradle.properties` to the next SNAPSHOT version.
7.  `git commit -am "Prepare next development version."`
8.  `git push && git push --tags`
9.  Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the
    artifact.


Prerequisites
-------------

In `~/.gradle/gradle.properties`, set the following:

*  `SONATYPE_NEXUS_USERNAME` - Sonatype username for releasing to
   `com.github.wseemann`.
*  `SONATYPE_NEXUS_PASSWORD` - Sonatype password for releasing to
   `com.github.wseemann`.
