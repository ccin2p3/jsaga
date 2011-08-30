package fr.in2p3.maven.plugin;

/** WARNING: DO NOT TRY TO OPTIMIZE THESE IMPORTS !!! */
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.*;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTree;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.jar.classes.JarClassesAnalysis;
import org.apache.maven.model.License;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DependencyXmlMojo
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */
/**
 * Goal which dump dependencies of project as an XML file
 * @goal serialize
 * @phase generate-sources
 */
public class DependencyXmlMojo extends AbstractMojo {
    /**
     * @component
     */
    private ArtifactFactory factory;

    /**
     * @component
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * @component
     */
    private ArtifactCollector collector;

    /**
     * @component
     */
    private DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * @component
     */
    private JarClassesAnalysis classesAnalyzer;

    /**
     * <i>Maven Internal</i>: The Project descriptor.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * <i>Maven Internal</i>: Local Repository.
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * <i>Maven Internal</i>: Remote Repositories.
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    private java.util.List remoteRepositories;

    /**
     * <i>Maven Internal</i>: Location of the output directory.
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * Location of the output file.
     * @parameter expression="${project.build.directory}/dependencies.xml"
     * @required
     */
    private File outputFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // create output directory
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        // build dependencies tree
        DependencyNode root;
        try {
            DependencyTree projectTree = dependencyTreeBuilder.buildDependencyTree(
                    project, localRepository, factory, artifactMetadataSource, collector);
            root = projectTree.getRootNode();
        } catch (DependencyTreeBuilderException e) {
            throw new MojoExecutionException("Unable to build dependency tree", e);
        }

        // dump
        PrintStream out;
        try {
            out = new PrintStream(new FileOutputStream(outputFile));
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Failed to create output file", e);
        }
        out.println("<project version=\""+project.getVersion()+"\">");
        for (Iterator it=root.getChildren().iterator(); it.hasNext(); ) {
            DependencyNode child = (DependencyNode) it.next();
            dump(child, out);
        }
        out.println("</project>");
        out.close();
    }

    private void dump(DependencyNode current, PrintStream out) throws MojoExecutionException, MojoFailureException {
        String artifactId = current.getArtifact().getArtifactId();
        boolean hasClassifier = (current.getArtifact().getClassifier()!=null);
        if ( (artifactId.startsWith("jsaga-") || artifactId.startsWith("saga-"))&& !hasClassifier) {
            MavenProject module = this.getMavenProject(current.getArtifact());
            if (module != null) {
                try {
                    DependencyTree tree = dependencyTreeBuilder.buildDependencyTree(
                            module, localRepository, factory, artifactMetadataSource, collector);
                    current = tree.getRootNode();
                } catch (DependencyTreeBuilderException e) {
                    throw new MojoExecutionException("Unable to build dependency tree", e);
                }
            }
        }

        // dump (part 1)
        indent(current, out);
        out.print("<artifact");

        Artifact artifact = current.getArtifact();
        addAttribute(out, "id", artifact.getArtifactId());
        addAttribute(out, "group", artifact.getGroupId());
        addAttribute(out, "version", artifact.getVersion());
        addAttribute(out, "type", artifact.getType());
        addAttribute(out, "scope", artifact.getScope());
        addAttribute(out, "classifier", artifact.getClassifier());
        addAttribute(out, "file", this.getJarFile(artifact).toString());

        MavenProject proj = this.getMavenProject(artifact);
        if (proj != null) {
            if (!proj.getName().startsWith("Unnamed - ")) {
                addAttribute(out, "name", proj.getName());
            }
            addAttribute(out, "description", proj.getDescription());
            addAttribute(out, "url", proj.getUrl());
            if (proj.getOrganization() != null) {
                addAttribute(out, "organization", proj.getOrganization().getName());
                addAttribute(out, "organizationUrl", proj.getOrganization().getUrl());
            }
            if (proj.getLicenses().size() > 0) {
                License license = (License) proj.getLicenses().get(0);
                addAttribute(out, "license", license.getName());
                addAttribute(out, "licenseUrl", license.getUrl());
            }
        }

        out.println(">");

        // recurse
        for (Iterator it=current.getChildren().iterator(); it.hasNext(); ) {
            DependencyNode child = (DependencyNode) it.next();
            // filter dependencies with scope "test", except those with classifier "tests" (i.e. adaptor integration tests)
            if ("test".equals(child.getArtifact().getScope()) && !"tests".equals(child.getArtifact().getClassifier())) {
                Artifact c = child.getArtifact();
                getLog().debug(artifact.getArtifactId()+": ignoring dependency "+c.getGroupId()+":"+c.getArtifactId());
            } else {
                this.dump(child, out);
            }
        }

        // dump (part 2)
        indent(current, out);
        out.println("</artifact>");
    }

    private static void indent(DependencyNode current, PrintStream out) {
        for (int i=0; i<current.getDepth(); i++) {
            out.print("    ");
        }
    }

    private static void addAttribute(PrintStream out, String name, String value) {
        if (value != null) {
            if (value.contains("\"")) {
                value = value.replaceAll("\"", "'");
            }
            out.print(" "+name+"=\""+StringEscapeUtils.escapeHtml4(value)+"\"");
        }
    }

    /**
     * @return the maven project, or null if the <code>artifact</code> has a classifier attribute
     */
    private MavenProject getMavenProject(Artifact artifact) throws MojoFailureException {
        if (artifact.getClassifier() == null) {
            try {
                MavenProject mavenProject = projectBuilder.buildFromRepository(
                        artifact, remoteRepositories, localRepository);
                if (mavenProject != null) {
                    return mavenProject;
                } else {
                    throw new MojoFailureException("Failed to retrieve project: "+artifact);
                }
            } catch (ProjectBuildingException e) {
                throw new MojoFailureException("Failed to retrieve project: "+artifact);
            }
        } else {
            return null;
        }
    }

    private File getJarFile(Artifact artifact) throws MojoFailureException {
        final File basedir = new File(localRepository.getBasedir());
        File file = new File(basedir, localRepository.pathOf(artifact));
        if (file.exists()) {
            return file;
        } else {
            throw new MojoFailureException("Artifact not installed: "+file);
        }
    }
}
