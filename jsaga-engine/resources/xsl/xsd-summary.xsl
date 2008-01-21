<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xsd">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:param name="root"/>

    <xsl:template match="/">
        <xsl:if test="not($root)">
            <xsl:message terminate="yes">Missing required parameter: root</xsl:message>
        </xsl:if>
        <xsl:apply-templates select="xsd:schema"/>
    </xsl:template>

    <xsl:template match="xsd:schema">
        <xsl:apply-templates select="xsd:element[@name=$root]"/>
    </xsl:template>

    <xsl:template match="xsd:element[@name]">
        <xsl:param name="maxOccurs" select="@maxOccurs"/>
        <xsl:element name="{@name}">
            <xsl:choose>
                <xsl:when test="not($maxOccurs)">
                    <xsl:attribute name="_">1</xsl:attribute>
                </xsl:when>
                <xsl:when test="$maxOccurs!='unbounded'">
                    <xsl:attribute name="_"><xsl:value-of select="$maxOccurs"/></xsl:attribute>
                </xsl:when>
            </xsl:choose>
            <xsl:if test="@name=$root">
                <xsl:attribute name="xmlns"><xsl:value-of select="@targetNamespace"/></xsl:attribute>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="@type">
                    <xsl:apply-templates select="@type"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
    <xsl:template match="xsd:element[@ref]">
        <xsl:variable name="target" select="substring-after(@ref,':')"/>
        <xsl:choose>
            <xsl:when test="ancestor-or-self::xsd:element[@name=$target]">
                <xsl:element name="{$target}">
                    <xsl:attribute name="_">*** RECURSIVE ***</xsl:attribute>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="/xsd:schema/xsd:element[@name=$target]">
                    <xsl:with-param name="maxOccurs" select="@maxOccurs"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="xsd:complexType | xsd:complexContent | xsd:simpleType | xsd:simpleContent">
        <xsl:param name="bypass"/>
        <xsl:if test="not($bypass='attribute')">
            <xsl:apply-templates select="xsd:attribute"/>
        </xsl:if>
        <xsl:if test="not($bypass='element')">
            <xsl:apply-templates select="xsd:*[local-name()!='attribute']"/>
        </xsl:if> 
    </xsl:template>
    <xsl:template match="xsd:extension">
        <xsl:variable name="target" select="substring-after(@base,':')"/>
        <xsl:apply-templates select="/xsd:schema/xsd:complexType[@name=$target]">
            <xsl:with-param name="bypass" select="string('element')"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="xsd:attribute"/>
        <xsl:apply-templates select="/xsd:schema/xsd:complexType[@name=$target]">
            <xsl:with-param name="bypass" select="string('attribute')"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="xsd:*[local-name()!='attribute']"/>
    </xsl:template>

    <xsl:template match="xsd:sequence | xsd:choice | xsd:all">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="xsd:attribute[@name]">
        <xsl:attribute name="{@name}"><xsl:apply-templates select="@type"/></xsl:attribute>
    </xsl:template>
    <xsl:template match="xsd:attribute[@ref]">
        <xsl:variable name="target" select="substring-after(@ref,':')"/>
        <xsl:apply-templates select="/xsd:schema/xsd:attribute[@name=$target]"/>
    </xsl:template>

    <xsl:template match="@type">
        <xsl:choose>
            <xsl:when test=".='string' or substring-after(.,':')='string'"/>
            <xsl:when test="starts-with(.,'xsd:') or not(contains(.,':'))">
                <xsl:value-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="target" select="substring-after(.,':')"/>
                <xsl:apply-templates select="/xsd:schema/xsd:complexType[@name=$target]"/>
                <xsl:apply-templates select="/xsd:schema/xsd:simpleType[@name=$target]"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="xsd:restriction">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="xsd:enumeration">
        <xsl:value-of select="@value"/>
        <xsl:if test="position()!=last()">
            <xsl:text>|</xsl:text>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>