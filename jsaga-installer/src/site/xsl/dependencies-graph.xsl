<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:template match="/project">
        <document><body>
            <section name="Dependencies of fr.in2p3.jsaga:jsaga-installer:{@version}">
                <xsl:apply-templates select="artifact[not(@classifier='tests')]"/>
            </section>
        </body></document>
    </xsl:template>

    <xsl:template match="artifact">
        <ul>
            <li>
                <xsl:choose>
                    <xsl:when test="starts-with(@id,'jsaga-adaptor-') and not(parent::project)">
                        <b><xsl:call-template name="PRINT_ARTIFACT"/></b>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="PRINT_ARTIFACT"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:apply-templates select="artifact"/>
            </li>
        </ul>
    </xsl:template>

    <xsl:template name="PRINT_ARTIFACT">
        <xsl:value-of select="@group"/>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:if test="@classifier">
            <xsl:text>:</xsl:text>
            <xsl:value-of select="@classifier"/>
        </xsl:if>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="@version"/>
    </xsl:template>
</xsl:stylesheet>