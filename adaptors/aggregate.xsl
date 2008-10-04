<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0" xmlns="http://maven.apache.org/POM/4.0.0">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:template match="/pom:project">
        <project>
            <parent>
                <groupId>fr.in2p3.parent</groupId>
                <artifactId>in2p3-root-pom</artifactId>
                <version>current</version>
            </parent>
            <modelVersion>4.0.0</modelVersion>
            <groupId>fr.in2p3.jsaga.aggregator</groupId>
            <artifactId>jsaga-adaptors</artifactId>
            <version><xsl:value-of select="pom:version/text()"/></version>
            <packaging>pom</packaging>
            <dependencies>
                <xsl:apply-templates select="pom:modules/pom:module"/>
            </dependencies>
        </project>
    </xsl:template>

    <xsl:template match="pom:module">
        <xsl:variable name="module" select="document(concat(text(),'/pom.xml'))/pom:project"/>
        <dependency>
            <groupId><xsl:value-of select="$module/pom:groupId/text()"/></groupId>
            <artifactId><xsl:value-of select="$module/pom:artifactId/text()"/></artifactId>
            <version><xsl:value-of select="$module/pom:version/text()"/></version>
        </dependency>
        <dependency>
            <groupId><xsl:value-of select="$module/pom:groupId/text()"/></groupId>
            <artifactId><xsl:value-of select="$module/pom:artifactId/text()"/></artifactId>
            <version><xsl:value-of select="$module/pom:version/text()"/></version>
            <classifier>tests</classifier>
            <scope>test</scope>
        </dependency>
    </xsl:template>
</xsl:stylesheet>