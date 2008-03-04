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

    <!-- 1 occurence of UNIVERSE/@name per configuration -->
    <xsl:template match="cfg:UNIVERSE">
        <UNIVERSE>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </UNIVERSE>
    </xsl:template>

    <!-- 1 occurence of GRID/@name per configuration -->
    <xsl:template match="cfg:GRID">
        <GRID>
            <xsl:attribute name="name">
                <xsl:variable name="contextType" select="@contextType"/>
                <xsl:choose>
                    <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
                    <xsl:when test="count(//cfg:GRID[@contextType=$contextType])=1"><xsl:value-of select="@contextType"/></xsl:when>
                    <xsl:otherwise><xsl:value-of select="parent::cfg:UNIVERSE/@name"/>_<xsl:value-of select="@contextType"/></xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:copy-of select="@*[not(name()='name')]"/>
            <xsl:apply-templates/>            
        </GRID>
    </xsl:template>

    <!-- 1 occurence of job/@scheme per <GRID>, but N occurences of job/@scheme per config -->
    <xsl:template match="cfg:job">
        <job>
            <xsl:attribute name="scheme">
                <xsl:variable name="type" select="@type"/>
                <xsl:choose>
                    <xsl:when test="@scheme"><xsl:value-of select="@scheme"/></xsl:when>
                    <xsl:when test="count(../cfg:job[@type=$type and not(@scheme)])=1"><xsl:value-of select="@type"/></xsl:when>
                    <xsl:otherwise><xsl:value-of select="@type"/>-<xsl:value-of select="count(preceding-sibling::cfg:job[@type=$type and not(@scheme)])"/></xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:copy-of select="@*[not(name()='scheme')]"/>
            <xsl:apply-templates/>
        </job>
    </xsl:template>

    <!-- 1 occurence of data/@scheme per <GRID>, but N occurences of data/@scheme per config -->
    <xsl:template match="cfg:data">
        <data>
            <xsl:attribute name="scheme">
                <xsl:variable name="type" select="@type"/>
                <xsl:choose>
                    <xsl:when test="@scheme"><xsl:value-of select="@scheme"/></xsl:when>
                    <xsl:when test="count(../cfg:data[@type=$type and not(@scheme)])=1"><xsl:value-of select="@type"/></xsl:when>
                    <xsl:otherwise><xsl:value-of select="@type"/>-<xsl:value-of select="count(preceding-sibling::cfg:data[@type=$type and not(@scheme)])"/></xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:copy-of select="@*[not(name()='scheme')]"/>
            <xsl:apply-templates/>
        </data>
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