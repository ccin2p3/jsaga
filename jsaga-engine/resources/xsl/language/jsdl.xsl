<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="xml" indent="yes"/>
    <xsl:variable name="id">
        <xsl:choose>
            <xsl:when test="/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName/text()">
                <xsl:value-of select="/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName/text()"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="generate-id(/jsdl:JobDefinition)"/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:template match="/">
        <ext:JobCollection>
            <ext:JobCollectionDescription>
                <ext:JobCollectionIdentification>
                    <ext:JobCollectionName><xsl:value-of select="$id"/></ext:JobCollectionName>
                </ext:JobCollectionIdentification>
                <ext:Parametric start="1" step="1" count="1"/>
            </ext:JobCollectionDescription>
            <ext:Job>
                <xsl:copy-of select="jsdl:JobDefinition"/>
            </ext:Job>
        </ext:JobCollection>
    </xsl:template>
</xsl:stylesheet>