<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:stg="http://www.in2p3.fr/jsaga/status">
    <xsl:output method="text"/>

    <xsl:template match="/">
        <xsl:if test="not(stg:workflow)">
            <xsl:message terminate="yes">Workflow status XML document is not namespace aware</xsl:message>
        </xsl:if>
        <xsl:apply-templates select="stg:workflow"/>
    </xsl:template>

    <xsl:template match="stg:workflow">
        <xsl:apply-templates select="stg:task[@state='DONE' and contains('transfer|source|mkdir',@type)]"/>
    </xsl:template>

    <xsl:template match="stg:task[@keep='true']">
#keep.<xsl:value-of select="position()"/>=<xsl:value-of select="@name"/>
    </xsl:template>

    <xsl:template match="stg:task[@keep='false']">
delete.<xsl:value-of select="position()"/>=<xsl:value-of select="@name"/>
    </xsl:template>

    <xsl:template match="stg:task[@type='mkdir']">
rmdir.<xsl:value-of select="position()"/>=<xsl:value-of select="substring-after(@name,'mkdir_')"/>
    </xsl:template>
</xsl:stylesheet>