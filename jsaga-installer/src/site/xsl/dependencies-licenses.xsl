<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:template match="/project">
        <document><body>
            <section name="Core engine dependencies">
                <table border="1">
                    <xsl:call-template name="HEADERS"/>
                    <xsl:apply-templates select="artifact[@id='jsaga-engine' and not(@classifier)]/descendant::artifact[not(@scope='test')]"/>
                </table>
            </section>
            <section name="Adaptors dependencies">
                <table border="1">
                    <xsl:call-template name="HEADERS"/>
                    <xsl:apply-templates select="artifact[starts-with(@id,'jsaga-adaptor-')]/descendant::artifact[not(@id=preceding::artifact/@id)]"/>
                </table>
            </section>
        </body></document>
    </xsl:template>

    <xsl:template name="HEADERS">
        <tr>
            <th>Package</th>
            <th>Organization</th>
            <th>License</th>
            <th>Full name</th>
            <th>Description</th>
        </tr>
    </xsl:template>

    <xsl:template match="artifact">
        <tr>
            <td><xsl:choose>
                <xsl:when test="@url"><a href="{@url}"><xsl:value-of select="@id"/></a></xsl:when>
                <xsl:otherwise><xsl:value-of select="@id"/></xsl:otherwise>
            </xsl:choose></td>
            <td><xsl:choose>
                <xsl:when test="@organizationUrl"><a href="{@organizationUrl}"><xsl:value-of select="@organization"/></a></xsl:when>
                <xsl:otherwise><xsl:value-of select="@organization"/></xsl:otherwise>
            </xsl:choose></td>
            <td><xsl:choose>
                <xsl:when test="@licenseUrl"><a href="{@licenseUrl}"><xsl:value-of select="@license"/></a></xsl:when>
                <xsl:otherwise><xsl:value-of select="@license"/></xsl:otherwise>
            </xsl:choose></td>
            <td><xsl:value-of select="@name"/></td>
            <td><xsl:value-of select="@description"/></td>
        </tr>
    </xsl:template>
</xsl:stylesheet>