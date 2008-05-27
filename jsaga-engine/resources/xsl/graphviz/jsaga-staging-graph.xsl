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
    graph [style=filled, fillcolor=lightgrey, color=white];
    node [shape=plaintext, height=0.2];

        <!-- nodes -->
        <xsl:for-each select="stg:task[(@type='transfer' or @type='source')
                and not(@group=preceding-sibling::stg:task[@type='transfer' or @type='source']/@group)]">
            <xsl:variable name="group" select="@group"/>
    subgraph "cluster_<xsl:value-of select="@group"/>" {
        label = "<xsl:value-of select="@group"/><xsl:if test="@context">\n(<xsl:value-of select="@context"/>)</xsl:if>";<xsl:text/>
        labelloc=t; labeljust=c; fontname="Times-Bold"; fontsize=14.0;
            <xsl:for-each select="/stg:workflow/stg:task[(@type='transfer' or @type='source') and @group=$group and
                    not (@group='file://' and (starts-with(@label,'input-sandbox') or starts-with(@label,'output-sandbox')))]">
        "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="@label"/>"];<xsl:text/>
            </xsl:for-each>
    }
        </xsl:for-each>
        <xsl:for-each select="/stg:workflow/stg:task[(@type='transfer' or @type='source')
                    and (@group='file://' and (starts-with(@label,'input-sandbox') or starts-with(@label,'output-sandbox')))]">
    "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="@group"/>\n<xsl:value-of select="@label"/>"];<xsl:text/>
        </xsl:for-each>
        <xsl:for-each select="stg:task[@type='job']">
    "<xsl:value-of select="@label"/>" [shape=ellipse, style=filled, color=gold];<xsl:text/>
        </xsl:for-each>

        <!-- edges -->
<!--
    edge [style=invis, weight=1.0];
        <xsl:for-each select="stg:task[@type='start']/stg:successor">
    "user" -> "<xsl:value-of select="text()"/>";<xsl:text/>
        </xsl:for-each>
-->
    edge [arrowhead=normal, arrowtail=none, style=bold, color=darkorange, weight=1.0];
        <xsl:for-each select="stg:task[(@type='transfer' or @type='source') and @input='true']/stg:successor">
            <xsl:variable name="predecessorName" select="../@name"/>
            <xsl:variable name="successorName" select="text()"/>
            <xsl:for-each select="/stg:workflow/stg:task[@name=$successorName]">
                <xsl:choose>
                    <xsl:when test="@type='staged'">
    "<xsl:value-of select="$predecessorName"/>" -> "<xsl:value-of select="substring-after(stg:successor/text(),'run_')"/>";<xsl:text/>
                    </xsl:when>
                    <xsl:otherwise>
    "<xsl:value-of select="$predecessorName"/>" -> "<xsl:value-of select="$successorName"/>";<xsl:text/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:for-each>
    edge [arrowhead=none, arrowtail=normal, style=bold, color=cornflowerblue, weight=0.0];
        <xsl:for-each select="stg:task[@type='job']/stg:successor">
            <xsl:variable name="predecessorName" select="../@label"/>
            <xsl:variable name="successorName" select="text()"/>
            <xsl:for-each select="/stg:workflow/stg:task[@name=$successorName and @type!='end']">
    "<xsl:value-of select="$successorName"/>" -> "<xsl:value-of select="$predecessorName"/>";<xsl:text/>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="stg:task[(@type='transfer' or @type='source') and @input='false']/stg:successor">
            <xsl:variable name="predecessorName" select="../@name"/>
            <xsl:variable name="successorName" select="text()"/>
            <xsl:for-each select="/stg:workflow/stg:task[@name=$successorName]">
                <xsl:choose>
                    <xsl:when test="@type='staged'">
<!--
    "user" -> "<xsl:value-of select="$predecessorName"/>" [style=invis, weight=0.0];<xsl:text/>
-->
                    </xsl:when>
                    <xsl:otherwise>
    "<xsl:value-of select="$successorName"/>" -> "<xsl:value-of select="$predecessorName"/>" ;<xsl:text/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:for-each>
}
    </xsl:template>
</xsl:stylesheet>