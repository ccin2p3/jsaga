<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.in2p3.fr/jsaga"
                xmlns:cfg="http://www.in2p3.fr/jsaga"
                exclude-result-prefixes="cfg">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <effective-config>
            <xsl:copy-of select="cfg:UNIVERSE/@*[not(local-name()='name')]"/>
            <xsl:comment> contexts </xsl:comment>
            <xsl:apply-templates select="//cfg:GRID"/>
            <xsl:comment> protocols </xsl:comment>
            <xsl:apply-templates select="//cfg:data[not(@scheme=../preceding::cfg:data/@scheme)]"/>
            <xsl:comment> execution </xsl:comment>
            <xsl:apply-templates select="//cfg:job[not(@scheme=../preceding::cfg:job/@scheme)]"/>
        </effective-config>
    </xsl:template>

    <xsl:template match="cfg:GRID">
        <context name="{@name}" type="{@contextType}">
            <xsl:copy-of select="@deactivated"/>
            <xsl:apply-templates select="cfg:attribute"/>
        </context>
    </xsl:template>

    <xsl:template match="cfg:data">
        <protocol scheme="{@scheme}">
            <xsl:variable name="scheme" select="@scheme"/>
            <xsl:apply-templates select="cfg:schemeAlias"/>
            <xsl:for-each select="//cfg:data[@scheme=$scheme and not(@deactivated='true')]">
                <dataService name="{parent::cfg:SITE/@name}" type="{@type}"
                             contextRef="{ancestor::cfg:GRID/@name}" contextType="{ancestor::cfg:GRID/@contextType}">
                    <xsl:apply-templates select="parent::cfg:SITE/cfg:domain"/>
                    <xsl:apply-templates select="*[not(local-name()='schemeAlias')] | text() | comment()"/>
                </dataService>
            </xsl:for-each>
        </protocol>
    </xsl:template>

    <xsl:template match="cfg:job">
        <execution scheme="{@scheme}">
            <xsl:variable name="scheme" select="@scheme"/>
            <xsl:apply-templates select="cfg:schemeAlias"/>
            <xsl:for-each select="//cfg:job[@scheme=$scheme and not(@deactivated='true')]">
                <jobService name="{parent::cfg:SITE/@name}" type="{@type}"
                            contextRef="{ancestor::cfg:GRID/@name}" contextType="{ancestor::cfg:GRID/@contextType}">
                    <xsl:apply-templates select="parent::cfg:SITE/cfg:domain"/>
                    <xsl:apply-templates select="*[not(local-name()='schemeAlias')] | text() | comment()"/>
                </jobService>
            </xsl:for-each>
        </execution>
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