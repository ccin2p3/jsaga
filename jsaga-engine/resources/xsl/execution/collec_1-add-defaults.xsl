<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
            xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            xmlns="http://schemas.ggf.org/jsdl/2005/11/jsdl"
            xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
            xmlns:ext="http://www.in2p3.fr/jsdl-extension"
            exclude-result-prefixes="jsdl">
    <!-- ###########################################################################
         # Add default values to attributes and simple type elements
         ###########################################################################
    -->
    <xsl:output method="xml" indent="yes"/>
    <!-- engine properties -->
    <xsl:param name="job.description.default" select="'etc/jsaga-default.jsdl'"/>

    <!-- entry point -->
    <xsl:template match="/">
        <xsl:variable name="structure" select="document('var/jsaga-job-structure.xml')"/>
        <xsl:variable name="defaults" select="document($job.description.default)"/>
        <xsl:apply-templates select="ext:JobCollection">
            <xsl:with-param name="struct" select="$structure/ext:JobCollection"/>
            <xsl:with-param name="def" select="$defaults/ext:JobCollection"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- workaround for factorizing namespace declarations -->
    <xsl:template match="jsdl:JobDefinition">
        <xsl:param name="struct"/>
        <xsl:param name="def"/>
        <JobDefinition>
            <xsl:apply-templates select="jsdl:JobDescription">
                <xsl:with-param name="struct" select="$struct/jsdl:JobDescription"/>
                <xsl:with-param name="def" select="$def/jsdl:JobDescription"/>
            </xsl:apply-templates>
        </JobDefinition>
    </xsl:template>

    <!-- main rules -->
    <xsl:template match="*">
        <xsl:param name="struct"/>
        <xsl:param name="def"/>
        <xsl:element name="{name()}">
            <xsl:variable name="this" select="."/>
            <xsl:copy-of select="$def/@*[not(local-name()='INSERT')]"/>
            <xsl:copy-of select="@*"/><!-- overwrite $def/@* -->
            <!-- copy text() if leaf -->
            <xsl:if test="not(*)">
                <xsl:copy-of select="text()"/>
            </xsl:if>
            <!-- recurse -->
            <xsl:for-each select="$struct/*">
                <xsl:variable name="name" select="local-name()"/>
                <xsl:choose>
                    <xsl:when test="$this/*[local-name()=$name]">
                        <xsl:apply-templates select="$this/*[local-name()=$name]">
                            <xsl:with-param name="struct" select="."/>
                            <xsl:with-param name="def" select="$def/*[local-name()=$name]"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$def/*[local-name()=$name and not(*)]">
                        <xsl:copy-of select="$def/*[local-name()=$name]"/>
                    </xsl:when>
                    <xsl:when test="$def/*[local-name()=$name and @INSERT='true']">
                        <xsl:for-each select="$def/*[local-name()=$name]">
                            <xsl:element name="{name()}">
                                <xsl:copy-of select="@*[not(local-name()='INSERT')] | *"/>
                            </xsl:element>
                        </xsl:for-each>
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
            <!-- copy childs that are not in $struct-->
            <xsl:for-each select="*">
                <xsl:variable name="name" select="local-name()"/>
                <xsl:if test="not($struct/*[local-name()=$name])">
                    <xsl:copy-of select="."/>
                </xsl:if>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <!-- default template rules -->
    <xsl:template match="text() | comment()">
        <xsl:copy-of select="."/>
    </xsl:template>
    <xsl:template match="processing-instruction()"/>
</xsl:stylesheet>