pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.16.2"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    preCommit {
        tasks("detektAll", "ktlintCheck")
    }
    createHooks(overwriteExisting = true)
}

rootProject.name = "collektive"

includeBuild("plugin")
include("dsl", "alchemist-incarnation-collektive")
