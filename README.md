![sirius](https://raw.githubusercontent.com/scireum/sirius-kernel/master/docs/sirius.jpg)

Welcome to the **parent plugin** of the SIRIUS OpenSource framework created by [scireum GmbH](https://www.scireum.de).
To learn more about what SIRIUS is please refer to documentation of the [kernel module](https://github.com/scireum/sirius-kernel).

# SIRIUS Parent

Contains a [Gradle Plugin](https://plugins.gradle.org/plugin/com.scireum.sirius-parent) used by all scireum and sirius projects.
This will enforce a sane build process and code style settings in addition to a common test setup (jUnit 5 & Kotlin).

## Using the plugin in a project

Simply include the latest version of the plugin as a buildscript dependency and apply it afterwards.

### Sample

```kotlin
buildscript {
    dependencies {
        classpath("com.scireum:sirius-parent:1.0")
    }
}

apply(plugin = "com.scireum.sirius-parent")
```

## Publishing to Gradle Plugin Portal

Changes to the `.idea` resources folder are automatically picked up by all projects that use the plugin during every build.
Only changes to the plugin itself or its dependencies need to be published as a new version to the plugin portal.

To publish a new version of the plugin follow these steps:

1. Make the required changes to the repository.

2. Update the version number in the [Gradle Build File](/build.gradle.kts) adhering to [SemVer](https://semver.org/).

3. Run the following command in the console (using the "Gradle Plugin Publish Key" credentials from the DevOps password vault):
```shell
./gradlew publishPlugins -Pgradle.publish.key=<key> -Pgradle.publish.secret=<secret>
```

4. Check the [Plugin Portal](https://plugins.gradle.org/plugin/com.scireum.sirius-parent) whether new version is available.
