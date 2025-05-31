pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // تأكد إنه موجود
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "VotingAppNew"
include(":app")