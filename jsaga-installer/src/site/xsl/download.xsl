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
                            <td>Download and install <a href="http://www.oracle.com/technetwork/indexes/downloads/index.html">JDK 1.6</a> or later versions.</td>
                        </tr>
                    </table>
                </subsection>
                <subsection name="Current Release">
                <xsl:choose>
                    <xsl:when test="contains($project.version, 'SNAPSHOT')">
                        <p>If you want to use JSAGA in a stable environment, you might prefer the latest release. 
                        Please visit <a href="../latest-release/download.html">this page</a></p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="dir">http://maven.in2p3.fr/fr/in2p3/jsaga/jsaga-installer/<xsl:value-of select="$project.version"/></xsl:variable>
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
                        <p>See <a href="changes-report.html">release notes</a>.</p>
                    </xsl:otherwise>
                </xsl:choose>
                </subsection>
                <subsection name="Current development snapshot">
                        <p>Download the current snapshot from this <a href="http://maven.in2p3.fr/fr/in2p3/jsaga/jsaga-installer/?C=M;O=D">directory</a>:</p>
                        <ul>
                            <li>Choose one of the 3 biggest files: tar.gz, zip or the Graphical Installer (jar)</li>
                            <li>Download it</li>
                        </ul>
                </subsection>
                <subsection name="Installation Instructions">
                    <ul>
                        <li><p><b>Windows &#174;</b></p>
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
                                    "Modify" to add at the end of variable PATH: <source>;%JSAGA_HOME%\examples</source></li>
                            </ol>
                        </li>
                        <li><p><b>Unix-based Operating Systems (Linux, Solaris and Mac OS X):</b></p>
                            <ol>
                                <li>Execute installer or extract binaries package to the directory you
                                    wish to install JSAGA.</li>
                                <li>Set the JSAGA_HOME environment variable (optional):
                                    <source>export JSAGA_HOME=&lt;path_to_your_directory&gt;</source></li>
                                <li>Update the PATH environment variable (optional):
                                    <source>export PATH=$PATH:JSAGA_HOME/examples</source></li>
                                <li>Execute the post-install.sh script:
                                    <source>./post-install.sh</source></li>
                            </ol>
                        </li>
                    </ul>
                </subsection>
                <subsection name="Previous Releases (CAUTION: none of these are supported yet)">
                    <p>Previous releases of JSAGA can be downloaded from the <a href="http://grid.in2p3.fr/maven2/fr/in2p3/jsaga/jsaga-installer/">archives</a>.</p>
                </subsection>
            </section>
        </body></document>
    </xsl:template>
</xsl:stylesheet>