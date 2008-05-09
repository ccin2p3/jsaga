<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:stg="http://www.in2p3.fr/jsaga/status">
    <xsl:output method="text"/>

    <xsl:template match="/stg:workflow">
digraph G {
    graph [rankdir=LR, ratio=1];
    graph [style=filled, fillcolor=lightgrey, color=white, fontname="Times-Bold"];
    node [shape=plaintext, height=0.2];

        <!-- nodes -->
        <xsl:for-each select="stg:task[@type='transfer' and not(@server=preceding-sibling::stg:task/@server)]">
    subgraph "cluster_<xsl:value-of select="@server"/>" {
        label = "<xsl:value-of select="@server"/>\n(<xsl:value-of select="@context"/>)";<xsl:text/>
            <xsl:variable name="server" select="@server"/>
            <xsl:for-each select="stg:task[@type='transfer' and @server=$server]">
        "<xsl:value-of select="@name"/>" [label="<xsl:value-of select="@label"/>"];<xsl:text/>
            </xsl:for-each>
    }
        </xsl:for-each>
        <xsl:for-each select="stg:task[@type='job']">
    "<xsl:value-of select="@name"/>" [shape=ellipse, style=filled, color=gold];<xsl:text/>
        </xsl:for-each>

        <!-- edges -->
    edge [arrowhead=normal, arrowtail=none, style=bold, color=darkorange, weight=1.0];
        <xsl:for-each select="stg:task[@type='transfer' and @input='true']">
            <xsl:variable name="predecessor" select="@name"/>
            <xsl:for-each select="stg:successor">
    "<xsl:value-of select="$predecessor"/>" -> "<xsl:value-of select="@name"/>";<xsl:text/>
            </xsl:for-each>
        </xsl:for-each>
    edge [arrowhead=none, arrowtail=normal, style=bold, color=cornflowerblue, weight=0.0];
        <xsl:for-each select="stg:task[@type='transfer' and @input='false']">
            <xsl:variable name="successor" select="@name"/>
            <xsl:for-each select="stg:predecessor">
    "<xsl:value-of select="@name"/>" -> "<xsl:value-of select="$successor"/>";<xsl:text/>
            </xsl:for-each>
        </xsl:for-each>
}
    </xsl:template>
</xsl:stylesheet>