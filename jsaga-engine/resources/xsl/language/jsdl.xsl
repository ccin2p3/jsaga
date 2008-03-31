<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="jsdl:JobDefinition">
        <ext:JobCollection>
            <xsl:call-template name="DEFAULT_JobCollectionDescription"/>
            <ext:Job>
                <xsl:copy-of select="."/>
            </ext:Job>
        </ext:JobCollection>
    </xsl:template>

    <xsl:template match="ext:Job">
        <ext:JobCollection>
            <xsl:call-template name="DEFAULT_JobCollectionDescription"/>
            <xsl:copy-of select="."/>
        </ext:JobCollection>
    </xsl:template>

    <xsl:template match="ext:JobCollection">
        <ext:JobCollection>
            <xsl:choose>
                <xsl:when test="ext:JobCollectionDescription"><xsl:apply-templates select="ext:JobCollectionDescription"/></xsl:when>
                <xsl:otherwise><xsl:call-template name="DEFAULT_JobCollectionDescription"/></xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="ext:Job"/>
        </ext:JobCollection>
    </xsl:template>
    <xsl:template match="ext:JobCollectionDescription">
        <ext:JobCollectionDescription>
            <xsl:choose>
                <xsl:when test="ext:JobCollectionIdentification"><xsl:copy-of select="ext:JobCollectionIdentification"/></xsl:when>
                <xsl:otherwise><xsl:call-template name="DEFAULT_JobCollectionIdentification"/></xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="ext:Parametric"><xsl:copy-of select="ext:Parametric"/></xsl:when>
                <xsl:otherwise><xsl:call-template name="DEFAULT_Parametric"/></xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="ext:Termination"/>
        </ext:JobCollectionDescription>
    </xsl:template>

    <xsl:template name="DEFAULT_JobCollectionDescription">
        <ext:JobCollectionDescription>
            <xsl:call-template name="DEFAULT_JobCollectionIdentification"/>
            <xsl:call-template name="DEFAULT_Parametric"/>
        </ext:JobCollectionDescription>
    </xsl:template>
    <xsl:template name="DEFAULT_JobCollectionIdentification">
        <ext:JobCollectionIdentification>
            <ext:JobCollectionName>
                <xsl:choose>
                    <xsl:when test="/ext:JobCollection/ext:JobCollectionDescription/ext:JobCollectionIdentification/ext:JobCollectionName/text()">
                        <xsl:value-of select="/ext:JobCollection/ext:JobCollectionDescription/ext:JobCollectionIdentification/ext:JobCollectionName/text()"/>
                    </xsl:when>
                    <xsl:when test="//jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName/text()">
                        <xsl:value-of select="//jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName/text()"/>
                    </xsl:when>
                    <xsl:otherwise><xsl:value-of select="generate-id(.)"/></xsl:otherwise>
                </xsl:choose>
            </ext:JobCollectionName>
        </ext:JobCollectionIdentification>
    </xsl:template>
    <xsl:template name="DEFAULT_Parametric">
        <ext:Parametric start="1" step="1" count="1"/>
    </xsl:template>
</xsl:stylesheet>