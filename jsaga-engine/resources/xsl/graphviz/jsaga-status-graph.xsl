<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:stg="http://www.in2p3.fr/jsaga/status">
    <xsl:output method="text"/>

    <xsl:template match="/stg:workflow">
digraph G {
    graph [rankdir=LR, ratio=1];
    graph [color=black, fontname="Times-Bold"];
    node [shape=plaintext, height=0.2];

        <!-- nodes -->
        <xsl:for-each select="stg:task[@type='dummy' and @name='start']">
    "start" [shape=ellipse, style=filled, color=<xsl:call-template name="STATUS"/>];<xsl:text/>
        </xsl:for-each>
        <xsl:for-each select="stg:task[@type='job' and not(@name=preceding-sibling::stg:task/@name)]">
    subgraph "cluster_<xsl:value-of select="@name"/>" {
        label = "<xsl:value-of select="@name"/>";
        style = dashed;
        "<xsl:value-of select="@name"/>" [label="run_job", shape=ellipse, style=filled, color=<xsl:call-template
                    name="STATUS"/>];<xsl:text/>
            <xsl:variable name="inPrefix">in_<xsl:value-of select="substring-after(@name,'run_')"/>_</xsl:variable>
            <xsl:for-each select="/stg:workflow/stg:task[@type='dummy' and starts-with(@name,$inPrefix)]">
        "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="substring-after(@name,$inPrefix)"/>"];<xsl:text/>
            </xsl:for-each>
    }
        </xsl:for-each>
        <xsl:for-each select="stg:task[@type='dummy' and not(@name=preceding-sibling::stg:task/@name) and starts-with(@name,'end_')]">
    subgraph "cluster_<xsl:value-of select="@name"/>" {
        label = "<xsl:value-of select="@name"/>";
        style = dashed;
        "<xsl:value-of select="@name"/>" [label="end_job", shape=ellipse, style=filled, color=<xsl:call-template
                name="STATUS"/>];<xsl:text/>
            <xsl:variable name="outPrefix">out_<xsl:value-of select="substring-after(@name,'end_')"/>_</xsl:variable>
            <xsl:for-each select="/stg:workflow/stg:task[@type='dummy' and starts-with(@name,$outPrefix)]">
        "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="substring-after(@name,$outPrefix)"/>"];<xsl:text/>
            </xsl:for-each>
    }
        </xsl:for-each>
        <xsl:for-each select="stg:task[@type='transfer']">
    "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="@label"/>", shape=ellipse, style=filled, color=<xsl:call-template
                name="STATUS"/>];<xsl:text/>
        </xsl:for-each>

        <!-- edges -->
    edge [arrowhead=normal, arrowtail=none, style=bold, color=black];
        <xsl:for-each select="stg:task">
            <xsl:variable name="predecessor" select="@name"/>
            <xsl:for-each select="stg:successor">
    "<xsl:value-of select="$predecessor"/>" -> "<xsl:value-of select="text()"/>";<xsl:text/>
            </xsl:for-each>
        </xsl:for-each>
}
    </xsl:template>

    <xsl:template name="STATUS">
        <xsl:choose>
            <xsl:when test="@state='NEW'">gray</xsl:when>
            <xsl:when test="@state='RUNNING'">blue</xsl:when>
            <xsl:when test="@state='DONE'">green</xsl:when>
            <xsl:when test="@state='CANCELED'">orange</xsl:when>
            <xsl:when test="@state='FAILED'">red</xsl:when>
            <xsl:when test="@state='SUSPENDED'">gold</xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>