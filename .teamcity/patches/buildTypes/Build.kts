package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.MavenBuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'Build'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("Build")) {
    params {
        expect {
            param("env.JAVA_HOME", "/usr/lib/jvm/jdk-11/")
        }
        update {
            param("env.JAVA_HOME", "%env.JDK_18_x64%")
        }
    }

    expectSteps {
        maven {
            goals = "clean install -T 1C -e"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "/usr/lib/jvm/jdk-11/"
            jvmArgs = "-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
            param("org.jfrog.artifactory.selectedDeployableServer.deployArtifacts", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.deployIncludePatterns", "*.jar *.pom")
            param("org.jfrog.artifactory.selectedDeployableServer.enableReleaseManagement", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.envVarsExcludePatterns", "*password*,*secret*")
            param("org.jfrog.artifactory.selectedDeployableServer.gitReleaseBranchNamePrefix", "REL-BRANCH-")
            param("org.jfrog.artifactory.selectedDeployableServer.publishBuildInfo", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.targetRepo", "libs-release-local")
            param("org.jfrog.artifactory.selectedDeployableServer.targetSnapshotRepo", "libs-snapshot-local")
            param("org.jfrog.artifactory.selectedDeployableServer.urlId", "0")
            param("org.jfrog.artifactory.selectedDeployableServer.vcsTagsBaseUrlOrName", "v")
        }
        step {
            type = "sonar-plugin"
            param("sonarProjectBinaries", "target/classes")
            param("sonarProjectModules", "vertx-actors,vertx-caching,vertx-codec,vertx-services")
            param("sonarProjectSources", "src/main/java")
            param("sonarProjectTests", "src/test/java")
            param("sonarServer", "7ba1e2c4-b91a-4c29-bd83-7948b20ea366")
            param("target.jdk.home", "%env.JDK_18_x64%")
        }
    }
    steps {
        update<MavenBuildStep>(0) {
            jdkHome = "%env.JDK_11_x64%"
            jvmArgs = "%env.ENABLE_GRAAL_COMPILER%"
        }
    }
}
