<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.in2p3.fr/jsaga"
                xmlns:cfg="http://www.in2p3.fr/jsaga"
                exclude-result-prefixes="cfg">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/cfg:effective-config">
        <effective-config>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </effective-config>
    </xsl:template>

    <xsl:template match="cfg:protocol">
        <protocol>
            <xsl:copy-of select="@*"/>
            <mapping>
                <xsl:variable name="this" select="."/>
                <xsl:for-each select="$this/cfg:dataService/cfg:domain[not(@name=../preceding-sibling::cfg:dataService/cfg:domain/@name)]">
                    <domain>
                        <xsl:copy-of select="@name"/>
                        <xsl:variable name="domain" select="@name"/>
                        <xsl:for-each select="$this/cfg:dataService/cfg:domain[@name=$domain or (not(@name) and not($domain))]/cfg:host">
                            <host prefix="{@prefix}">
                                <serviceRef name="{ancestor::cfg:dataService/@name}"/>
                            </host>
                        </xsl:for-each>
                        <xsl:for-each select="$this/cfg:dataService/cfg:domain[@name=$domain or (not(@name) and not($domain))][not(cfg:host)]">
                            <serviceRef name="{parent::cfg:dataService/@name}"/>
                        </xsl:for-each>
                    </domain>
                </xsl:for-each>
                <xsl:for-each select="$this/cfg:dataService[not(cfg:domain)]">
                    <serviceRef name="{@name}"/>
                </xsl:for-each>
            </mapping>
            <xsl:apply-templates/>
        </protocol>
    </xsl:template>
    <xsl:template match="cfg:dataService">
        <dataService>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*[not(name()='domain')] | text() | comment()"/>
        </dataService>
    </xsl:template>

    <xsl:template match="cfg:execution">
        <execution>
            <xsl:copy-of select="@*"/>
            <mapping>
                <xsl:variable name="this" select="."/>
                <xsl:for-each select="$this/cfg:jobService/cfg:domain[not(@name=../preceding-sibling::cfg:jobService/cfg:domain/@name)]">
                    <domain>
                        <xsl:copy-of select="@name"/>
                        <xsl:variable name="domain" select="@name"/>
                        <xsl:for-each select="$this/cfg:jobService/cfg:domain[@name=$domain or (not(@name) and not($domain))]/cfg:host">
                            <host prefix="{@prefix}">
                                <serviceRef name="{ancestor::cfg:jobService/@name}"/>
                            </host>
                        </xsl:for-each>
                        <xsl:for-each select="$this/cfg:jobService/cfg:domain[@name=$domain or (not(@name) and not($domain))][not(cfg:host)]">
                            <serviceRef name="{parent::cfg:jobService/@name}"/>
                        </xsl:for-each>
                    </domain>
                </xsl:for-each>
                <xsl:for-each select="$this/cfg:jobService[not(cfg:domain)]">
                    <serviceRef name="{@name}"/>
                </xsl:for-each>
            </mapping>
            <xsl:apply-templates/>
        </execution>
    </xsl:template>
    <xsl:template match="cfg:jobService">
        <jobService>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*[not(name()='domain')] | text() | comment()"/>
        </jobService>
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