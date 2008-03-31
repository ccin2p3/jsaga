<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
            xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
            xmlns:ext="http://www.in2p3.fr/jsdl-extension"
            exclude-result-prefixes="xsd">
    <xsl:output method="xml" indent="yes"/>
    <xsl:variable name="root" select="document('schema/job-collection.xsd')/xsd:schema"/>
    <xsl:variable name="xjsdl" select="document('schema/jsdl-extended.xsd.xml')/xsd:schema"/>
    <xsl:variable name="jsdl" select="document('schema/jsdl.xsd')/xsd:schema"/>
    <xsl:variable name="ext" select="document('schema/jsdl-extension.xsd')/xsd:schema"/>

    <!-- entry point -->
    <xsl:template match="/">
        <xsl:apply-templates select="$root/xsd:element[@name='JobCollection']"/>
    </xsl:template>

    <xsl:template match="xsd:element[@name='JobDefinition']">
        <xsl:apply-templates select="$xjsdl/xsd:element[@name='JobDefinitionExtended']"/>
    </xsl:template>
    <xsl:template match="xsd:element[@name='JobDefinitionExtended']">
        <jsdl:JobDefinition>
            <xsl:apply-templates select="xsd:complexType"/>
        </jsdl:JobDefinition>
    </xsl:template>

    <xsl:template match="xsd:element">
        <xsl:choose>
            <xsl:when test="@name">
                <xsl:element namespace="{ancestor::xsd:schema/@targetNamespace}" name="{@name}">
                    <xsl:choose>
                        <xsl:when test="@type">
                            <xsl:call-template name="TYPE">
                                <xsl:with-param name="type" select="@type"/>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates select="xsd:complexType | xsd:simpleType"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:element>
            </xsl:when>
            <xsl:when test="@ref">
                <xsl:call-template name="ELEMENT">
                    <xsl:with-param name="ref" select="@ref"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">FAILED</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="xsd:complexType">
        <xsl:apply-templates select="xsd:complexContent | xsd:sequence"/>
    </xsl:template>
    <xsl:template match="xsd:simpleType">
        <xsl:apply-templates select="xsd:simpleContent"/>
    </xsl:template>
    <xsl:template match="xsd:complexContent">
        <xsl:apply-templates select="xsd:extension"/>
    </xsl:template>
    <xsl:template match="xsd:extension">
        <xsl:call-template name="TYPE">
            <xsl:with-param name="type" select="@base"/>
        </xsl:call-template>
        <xsl:apply-templates select="xsd:sequence/xsd:element[@ref]"/>
    </xsl:template>
    <xsl:template match="xsd:sequence">
        <xsl:apply-templates select="xsd:element"/>
    </xsl:template>

    <xsl:template name="TYPE">
        <xsl:param name="type"/>
        <xsl:variable name="prefix" select="substring-before($type,':')"/>
        <xsl:variable name="name" select="substring-after($type,':')"/>
        <xsl:choose>
            <xsl:when test="$prefix='jsdl' and $jsdl/xsd:complexType[@name=$name]">
                <xsl:call-template name="COMPLEX_TYPE">
                    <xsl:with-param name="current" select="$jsdl/xsd:complexType[@name=$name]"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$prefix='jsdl' and  $jsdl/xsd:simpleType[@name=$name]">
                <xsl:call-template name="SIMPLE_TYPE">
                    <xsl:with-param name="current" select="$jsdl/xsd:simpleType[@name=$name]"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$prefix='ext' and $ext/xsd:complexType[@name=$name]">
                <xsl:call-template name="COMPLEX_TYPE">
                    <xsl:with-param name="current" select="$ext/xsd:complexType[@name=$name]"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$prefix='ext' and $ext/xsd:simpleType[@name=$name]">
                <xsl:call-template name="SIMPLE_TYPE">
                    <xsl:with-param name="current" select="$ext/xsd:simpleType[@name=$name]"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$prefix='tns' and ancestor::xsd:schema/xsd:complexType[@name=$name]">
                <xsl:call-template name="COMPLEX_TYPE">
                    <xsl:with-param name="current" select="ancestor::xsd:schema/xsd:complexType[@name=$name]"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$prefix='tns' and ancestor::xsd:schema/xsd:simpleType[@name=$name]">
                <xsl:call-template name="SIMPLE_TYPE">
                    <xsl:with-param name="current" select="ancestor::xsd:schema/xsd:simpleType[@name=$name]"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="COMPLEX_TYPE">
        <xsl:param name="current"/>
        <xsl:choose>
            <xsl:when test="$current/xsd:complexContent">
                <xsl:call-template name="COMPLEX_CONTENT">
                    <xsl:with-param name="current" select="$current/xsd:complexContent"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$current/xsd:sequence">
                <xsl:call-template name="SEQUENCE">
                    <xsl:with-param name="current" select="$current/xsd:sequence"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="SIMPLE_TYPE">
        <xsl:param name="current"/>
        <xsl:apply-templates select="$current/xsd:simpleContent"/>
    </xsl:template>

    <xsl:template name="COMPLEX_CONTENT">
        <xsl:param name="current"/>
        <xsl:apply-templates select="$current/xsd:extension"/>
    </xsl:template>

    <xsl:template name="SEQUENCE">
        <xsl:param name="current"/>
        <xsl:variable name="extendedSequence" select="./xsd:sequence"/>
        <xsl:for-each select="$current/xsd:element">
            <xsl:variable name="id">
                <xsl:choose>
                    <xsl:when test="@ref"><xsl:value-of select="substring-after(@ref,':')"/></xsl:when>
                    <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
                </xsl:choose>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$extendedSequence/xsd:element[@name=$id]">
                    <xsl:apply-templates select="$extendedSequence/xsd:element[@name=$id]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="ELEMENT">
        <xsl:param name="ref"/>
        <xsl:variable name="prefix" select="substring-before($ref,':')"/>
        <xsl:variable name="name" select="substring-after($ref,':')"/>
        <xsl:choose>
            <xsl:when test="$prefix='jsdl'">
                <xsl:apply-templates select="$jsdl/xsd:element[@name=$name]"/>
            </xsl:when>
            <xsl:when test="$prefix='ext'">
                <xsl:apply-templates select="$ext/xsd:element[@name=$name]"/>
            </xsl:when>
            <xsl:when test="$prefix='tns' and not(ancestor::xsd:element[@name=$name])">
                <xsl:apply-templates select="ancestor::xsd:schema/xsd:element[@name=$name]"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>