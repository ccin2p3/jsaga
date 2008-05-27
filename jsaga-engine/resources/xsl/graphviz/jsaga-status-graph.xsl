<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:stg="http://www.in2p3.fr/jsaga/status">
    <xsl:output method="text"/>

    <xsl:template match="/">
        <xsl:if test="not(stg:workflow)">
            <xsl:message terminate="yes">Workflow status XML document is not namespace aware</xsl:message>
        </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="stg:workflow">
digraph G {
    label="<xsl:value-of select="@name"/>"; labelloc=t; labeljust=l; fontname="Times-Bold"; fontsize=36.0;
    graph [rankdir=LR];
    graph [style=dashed, color=black];
    node [shape=plaintext, height=0.2];

        <!-- nodes -->
        <xsl:for-each select="stg:task[@type='start']">
    "start" [shape=ellipse, style=filled, color=<xsl:call-template name="STATUS"/>];<xsl:text/>
        </xsl:for-each>
        <xsl:for-each select="stg:task[@type='job']">
            <xsl:variable name="jobName" select="@label"/>
            <xsl:variable name="runGroup">run_<xsl:value-of select="@label"/></xsl:variable>
            <xsl:variable name="endGroup">end_<xsl:value-of select="@label"/></xsl:variable>
    subgraph "cluster_<xsl:value-of select="$runGroup"/>" {
        label = "<xsl:value-of select="$runGroup"/>"; labelloc=t; labeljust=c; fontname="Times-Bold"; fontsize=14.0;
            <xsl:for-each select="/stg:workflow/stg:task[@type='staged' and @group=$runGroup]">
        "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="@label"/>"];<xsl:text/>
            </xsl:for-each>
            <xsl:for-each select="/stg:workflow/stg:task[@type='job' and @group=$runGroup]">
        "<xsl:value-of select="@name"/>" [label="run_job", shape=ellipse, style=filled, color=<xsl:call-template name="STATUS"/>];<xsl:text/>
            </xsl:for-each>
    }
    subgraph "cluster_<xsl:value-of select="$endGroup"/>" {
        label = "<xsl:value-of select="$endGroup"/>"; labelloc=t; labeljust=c; fontname="Times-Bold"; fontsize=14.0;
            <xsl:for-each select="/stg:workflow/stg:task[@type='staged' and @group=$endGroup and not(@name=$endGroup)]">
        "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="@label"/>"];<xsl:text/>
            </xsl:for-each>
            <xsl:for-each select="/stg:workflow/stg:task[@type='end' and @group=$endGroup and @name=$endGroup]">
        "<xsl:value-of select="@name"/>" [label="end_job", shape=ellipse, style=filled, color=<xsl:call-template name="STATUS"/>];<xsl:text/>
            </xsl:for-each>
    }
        </xsl:for-each>
        <xsl:for-each select="stg:task[@type='source']">
    "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="@group"/>\n<xsl:value-of select="@label"/>"];<xsl:text/>
        </xsl:for-each>
        <xsl:for-each select="stg:task[@type='transfer']">
    "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="@group"/>\n<xsl:value-of select="@label"/>", <xsl:text/>
            <xsl:text/>shape=ellipse, style=filled, color=<xsl:call-template name="STATUS"/>];<xsl:text/>
        </xsl:for-each>
        <xsl:for-each select="stg:task[@type='mkdir']">
    "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="@group"/>\n<xsl:value-of select="@label"/>", <xsl:text/>
            <xsl:text/>shape=box, style=filled, color=<xsl:call-template name="STATUS"/>];<xsl:text/>
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