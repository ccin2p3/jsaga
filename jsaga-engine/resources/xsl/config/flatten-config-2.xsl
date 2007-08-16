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
            <xsl:copy-of select="@*"/>
            <xsl:comment> security </xsl:comment>
            <xsl:apply-templates select="/cfg:effective-config/cfg:contextInstance"/>
            <xsl:comment> protocols </xsl:comment>
            <xsl:apply-templates select="/cfg:effective-config/cfg:protocol"/>
            <xsl:comment> job services </xsl:comment>
            <xsl:apply-templates select="/cfg:effective-config/cfg:jobservice"/>
        </effective-config>
    </xsl:template>

    <xsl:template match="cfg:protocol">
        <protocol>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="cfg:attribute | cfg:shemeAlias | cfg:supportedContext"/>
            <xsl:for-each select="cfg:domain[not(@name=preceding-sibling::cfg:domain/@name)]">
                <xsl:sort select="string-length(@name)" data-type="number" order="descending"/>
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:apply-templates select="cfg:contextInstanceRef"/>
        </protocol>
    </xsl:template>

    <xsl:template match="cfg:domain">
        <xsl:variable name="this" select="."/>
        <domain name="{@name}">
            <xsl:variable name="domainsEndingWith" select="../cfg:domain[contains(concat($this/@name,'#'), concat(@name,'#'))]"/>
            <xsl:for-each select="$domainsEndingWith/cfg:host[not(@name=../preceding-sibling::cfg:domain/cfg:host/@name)]">
                <xsl:sort select="string-length(@name)" data-type="number" order="descending"/>
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:apply-templates select="$domainsEndingWith/cfg:contextInstanceRef"/>
        </domain>
    </xsl:template>

    <xsl:template match="cfg:host">
        <xsl:variable name="this" select="."/>
        <host name="{@name}">
            <xsl:variable name="hostsStartingWith" select="../cfg:host[starts-with($this/@name, @name)]"/>
            <xsl:apply-templates select="$hostsStartingWith/cfg:contextInstanceRef"/>
        </host>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>