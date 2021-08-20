package buildutils

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import org.gradle.util.GFileUtils
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

class OfflineMavenRepository extends DefaultTask {
    @OutputDirectory
    File repoDir = new File(project.projectDir, 'dependencies/maven')

    @TaskAction
    void build() {
        // Plugin/Buildscript dependencies
        for(Configuration configuration : project.buildscript.configurations.findAll())
        {
            copyJars(configuration)
            copyPoms(configuration)
        }

        // Normal dependencies
        for(Configuration configuration : project.configurations.findAll())
        {
            copyJars(configuration)
            copyPoms(configuration)
        }
    }

    private void copyJars(Configuration configuration) {

        if (configuration.canBeResolved) {
            configuration.resolvedConfiguration.resolvedArtifacts.each { artifact ->

                def moduleVersionId = artifact.moduleVersion.id
                File moduleDir = new File(repoDir, "${moduleVersionId.group.replace('.', '/')}/${moduleVersionId.name}/${moduleVersionId.version}")
                GFileUtils.mkdirs(moduleDir)
                GFileUtils.copyFile(artifact.file, new File(moduleDir, artifact.file.name))
            }
        }
    }

    private void copyPoms(Configuration configuration) {
        if (configuration.canBeResolved) {
            def componentIds = configuration.incoming.resolutionResult.allDependencies.collect { it.selected.id }

            def result = project.dependencies.createArtifactResolutionQuery()
                    .forComponents(componentIds)
                    .withArtifacts(MavenModule, MavenPomArtifact)
                    .execute()

            for (component in result.resolvedComponents) {
                def componentId = component.id

                if (componentId instanceof ModuleComponentIdentifier) {
                    File moduleDir = new File(repoDir, "${componentId.group.replace('.', '/')}/${componentId.module}/${componentId.version}")
                    GFileUtils.mkdirs(moduleDir)
                    File pomFile = component.getArtifacts(MavenPomArtifact)[0].file
                    GFileUtils.copyFile(pomFile, new File(moduleDir, pomFile.name))
                }
            }
        }
    }
}