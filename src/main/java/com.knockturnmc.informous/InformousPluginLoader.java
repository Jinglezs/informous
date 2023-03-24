package com.knockturnmc.informous;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class InformousPluginLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        final var mavenLibraryResolver = new MavenLibraryResolver();

        // Kotlin dependencies
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
        mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:1.8.10"), null));
        mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-reflect:1.8.10"), null));
        mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4"), null));
        mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0"), null));

        // Kord dependencies
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("sonatype01", "default", "https://s01.oss.sonatype.org/content/repositories/snapshots").build());
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("sonatype", "default", "https://oss.sonatype.org/content/repositories/snapshots/").build());

        final var kord = new Dependency(new DefaultArtifact("com.kotlindiscord.kord.extensions:kord-extensions:1.5.6-SNAPSHOT"), null);
        mavenLibraryResolver.addDependency(kord);

        classpathBuilder.addLibrary(mavenLibraryResolver);
    }

}