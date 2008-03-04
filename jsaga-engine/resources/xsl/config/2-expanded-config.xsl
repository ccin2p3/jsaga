<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.in2p3.fr/jsaga"
                xmlns:cfg="http://www.in2p3.fr/jsaga"
                exclude-result-prefixes="cfg">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="cfg:UNIVERSE">
        <UNIVERSE>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*[local-name()!='domain' and local-name()!='data'] | text() | comment()"/>
        </UNIVERSE>
    </xsl:template>

    <xsl:template match="cfg:GRID">
        <xsl:variable name="this" select="."/>
        <GRID>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="cfg:attribute | cfg:SITE | text() | comment()"/>
            <SITE name="{@name}">
                <xsl:apply-templates select="*[local-name()!='attribute' and local-name()!='SITE']"/>
                <xsl:apply-templates select="ancestor::cfg:*/cfg:data[not(@scheme=$this/cfg:data/@scheme)]"/>
            </SITE>
        </GRID>
    </xsl:template>

    <xsl:template match="cfg:data">
        <data>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*[local-name()!='workerIn' and local-name()!='workerOut']"/>
        </data>
    </xsl:template>

    <xsl:template match="cfg:job">
        <xsl:variable name="this" select="."/>
        <job>
            <xsl:copy-of select="@*[local-name()!='defaultIntermediary']"/>
            <xsl:apply-templates/>
            <fileStaging>
                <xsl:copy-of select="@defaultIntermediary"/>
                <xsl:apply-templates select="ancestor::cfg:*/cfg:data[not(@scheme=$this/cfg:data/@scheme)]/cfg:workerIn"/>
                <xsl:apply-templates select="ancestor::cfg:*/cfg:data[not(@scheme=$this/cfg:data/@scheme)]/cfg:workerOut"/>
            </fileStaging>
        </job>
    </xsl:template>
    <xsl:template match="cfg:workerIn | cfg:workerOut">
        <xsl:element name="{name()}">
            <xsl:attribute name="scheme"><xsl:value-of select="parent::cfg:data/@scheme"/></xsl:attribute>
            <xsl:copy-of select="@* | text()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="comment()">
        <xsl:comment><xsl:value-of select="."/></xsl:comment>
    </xsl:template>
</xsl:stylesheet>