<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:template match="/project">
        <document><body>
            <section name="Dependencies of fr.in2p3.jsaga:jsaga-installer:{@version}">
                <xsl:apply-templates select="artifact"/>
            </section>
        </body></document>
    </xsl:template>

    <xsl:template match="artifact">
        <ul>
            <li><xsl:value-of select="@group"/>
                <xsl:text>:</xsl:text>
                <xsl:value-of select="@id"/>
                <xsl:if test="@classifier">
                    <xsl:text>:</xsl:text>
                    <xsl:value-of select="@classifier"/>
                </xsl:if>
                <xsl:text>:</xsl:text>
                <xsl:value-of select="@version"/>
                <xsl:apply-templates select="artifact"/>
            </li>
        </ul>
    </xsl:template>
</xsl:stylesheet>