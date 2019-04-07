
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.project
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.version

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.2"

project {

    buildType(Build)
}

object Build : BuildType({
    name = "Build vertx-patterns"

    artifactRules = "**/target/*.jar"

    params {
        //disable tool options, otherwise java 8 jvm flags would be applied to jdk 11, which do not work
        param("env.JAVA_TOOL_OPTIONS", "")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            goals = "clean install"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "/usr/lib/jvm/jdk-11/"
            //enable graal compiler
            jvmArgs = "-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
            param("org.jfrog.artifactory.selectedDeployableServer.deployArtifacts", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.envVarsExcludePatterns", "*password*,*secret*")
            param("org.jfrog.artifactory.selectedDeployableServer.publishBuildInfo", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.targetRepo", "libs-release-local")
            param("org.jfrog.artifactory.selectedDeployableServer.targetSnapshotRepo", "libs-snapshot-local")
            param("org.jfrog.artifactory.selectedDeployableServer.urlId", "0")
            param("org.jfrog.artifactory.selectedDeployableServer.gitReleaseBranchNamePrefix", "REL-BRANCH-")
            param("org.jfrog.artifactory.selectedDeployableServer.enableReleaseManagement", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.vcsTagsBaseUrlOrName", "v")
            param("org.jfrog.artifactory.selectedDeployableServer.deployIncludePatterns", "*.jar *.pom")
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

    triggers {
        vcs {
            perCheckinTriggering = true
        }
    }
})
