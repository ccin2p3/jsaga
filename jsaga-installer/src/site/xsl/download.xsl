<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>
    <xsl:param name="project.version">ERROR</xsl:param>

    <xsl:template match="/">
        <document><body>
            <section name="Download JSAGA">
                <subsection name="System Requirements">
                    <table>
                        <tr>
                            <th>JDK</th>
                            <td>Download and install <a href="http://www.oracle.com/technetwork/java/javase/downloads/index-jdk5-jsp-142662.html">JDK 1.5</a> or later versions.</td>
                        </tr>
                        <tr>
                            <th>Operating System</th>
                            <td>Some adaptors may require a specific release of linux.</td>
                        </tr>
                    </table>
                </subsection>
                <subsection name="Installation Instructions">
                    <ul>
                        <li><p><b>Windows 2000/XP:</b></p>
                            <ol>
                                <li>Execute installer or extract binaries package to the directory you
                                    wish to install JSAGA.</li>
                                <li>Set the JSAGA_HOME environment variable (optional):
                                    Open up the system properties (WinKey + Pause), select the tab
                                    "Advanced", and the "Environment Variables" button, then click on
                                    "New" to set the JSAGA_HOME variable with the installation path.</li>
                                <li>Update the PATH environment variable (optional):
                                    Open up the system properties (WinKey + Pause), select the tab
                                    "Advanced", and the "Environment Variables" button, then click on
                                    "Modify" to add at the end of variable PATH: <source>;%JSAGA_HOME%\bin</source></li>
                            </ol>
                        </li>
                        <li><p><b>Unix-based Operating Systems (Linux, Solaris and Mac OS X):</b></p>
                            <ol>
                                <li>Execute installer or extract binaries package to the directory you
                                    wish to install JSAGA.</li>
                                <li>Set the JSAGA_HOME environment variable (optional):
                                    <source>export JSAGA_HOME=&lt;path_to_your_directory&gt;</source></li>
                                <li>Update the PATH environment variable (optional):
                                    <source>export PATH=$PATH:JSAGA_HOME/bin</source></li>
                                <li>Execute the post-install.sh script:
                                    <source>./post-install.sh</source></li>
                            </ol>
                        </li>
                    </ul>
                </subsection>
                <subsection name="Current Release">
                    <xsl:variable name="dir">/maven2/fr/in2p3/jsaga/jsaga-installer/<xsl:value-of select="$project.version"/></xsl:variable>
                    <xsl:choose>
                        <xsl:when test="contains($project.version, 'SNAPSHOT')">
                            <p>Download the latest snapshot from this <a href="{$dir}/">directory</a>:</p>
                            <ul>
                                <li>Sort by column "Last modified"</li>
                                <li>Choose one of the 3 biggest files: tar.gz, zip or the Graphical Installer (jar)</li>
                                <li>Download it</li>
                            </ul>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="prefix">
                                <xsl:value-of select="$dir"/>/jsaga-installer-<xsl:value-of select="$project.version"/>
                            </xsl:variable>
                            <p>JSAGA is distributed in several formats for your convenience.</p>
                            <ul>
                                <li>Graphical Installer:
                                    <a href="{$prefix}.jar">jsaga-installer-<xsl:value-of select="$project.version"/>.jar</a>
                                </li>
                                <li>ZIP binaries package:
                                    <a href="{$prefix}-bin.zip">jsaga-installer-<xsl:value-of select="$project.version"/>-bin.zip</a>
                                </li>
                                <li>TAR-GZ binaries package:
                                    <a href="{$prefix}-bin.tar.gz">jsaga-installer-<xsl:value-of select="$project.version"/>-bin.tar.gz</a>
                                </li>
                            </ul>
                        </xsl:otherwise>
                    </xsl:choose>
                    <p>See <a href="changes-report.html">release notes</a>.</p>
                </subsection>
                <subsection name="Previous Releases">
                    <p>Previous releases of JSAGA can be downloaded from the <a href="http://grid.in2p3.fr/software/archives/jsaga/">archives</a>.</p>
                </subsection>
            </section>
        </body></document>
    </xsl:template>
</xsl:stylesheet>