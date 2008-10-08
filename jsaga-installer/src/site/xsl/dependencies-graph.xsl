<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <xsl:template match="/project">
digraph G {
    graph [rankdir=LR];
    node [shape=plaintext, height=0.2];
    edge [arrowhead=normal, arrowtail=none, style=bold, color=black];

        <!-- nodes -->
        <xsl:for-each select="artifact[not(@classifier='tests')]">
    "<xsl:value-of select="@id"/>" [shape=ellipse, style=filled, color=orange];<xsl:text/>
            <xsl:for-each select="descendant::artifact[not(@id=preceding::artifact/@id) and not(starts-with(@id,'jsaga-adaptor-'))]">
    "<xsl:value-of select="@id"/>";<xsl:text/>
            </xsl:for-each>
        </xsl:for-each>

        <!-- edges -->
        <xsl:for-each select="artifact[not(@classifier='tests')]">
            <xsl:apply-templates select="."/>
            <xsl:for-each select="descendant::artifact[not(@id=preceding::artifact/@id) and not(starts-with(@id,'jsaga-adaptor-'))]">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
        </xsl:for-each>
}
    </xsl:template>

    <xsl:template match="artifact">
        <xsl:for-each select="artifact">
    "<xsl:value-of select="parent::artifact/@id"/>" -> "<xsl:value-of select="@id"/>";<xsl:text/>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>