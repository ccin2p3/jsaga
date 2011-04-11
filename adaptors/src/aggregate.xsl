<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://maven.apache.org/POM/4.0.0"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                exclude-result-prefixes="pom">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="4" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:template match="/pom:project">
        <project>
            <parent>
                <groupId>fr.in2p3.jsaga</groupId>
                <artifactId>jsaga</artifactId>
                <version><xsl:value-of select="pom:parent/pom:version/text()"/></version>
            </parent>
            <modelVersion>4.0.0</modelVersion>
            <groupId>fr.in2p3.jsaga.poms</groupId>
            <artifactId>jsaga-adaptors</artifactId>
            <packaging>pom</packaging>
            <name>List of adaptors</name>
            <dependencies>
                <xsl:apply-templates select="pom:modules/pom:module"/>
            </dependencies>
        </project>
    </xsl:template>

    <xsl:template match="pom:module">
        <xsl:variable name="module" select="document(concat('../',text(),'/pom.xml'))/pom:project"/>
        <dependency>
            <groupId><xsl:value-of select="$module/pom:parent/pom:groupId/text()"/></groupId>
            <artifactId><xsl:value-of select="$module/pom:artifactId/text()"/></artifactId>
            <version><xsl:value-of select="$module/pom:parent/pom:version/text()"/></version>
        </dependency>
        <dependency>
            <groupId><xsl:value-of select="$module/pom:parent/pom:groupId/text()"/></groupId>
            <artifactId><xsl:value-of select="$module/pom:artifactId/text()"/></artifactId>
            <version><xsl:value-of select="$module/pom:parent/pom:version/text()"/></version>
            <classifier>tests</classifier>
            <scope>test</scope>
        </dependency>
    </xsl:template>
</xsl:stylesheet>