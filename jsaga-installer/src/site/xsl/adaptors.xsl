<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml">
    <xsl:output method="html" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:template match="/project">
        <html>
            <body>
                <h1>Adaptors</h1>
                <table border="1">
                    <xsl:call-template name="HEADERS"/>
                    <xsl:apply-templates select="artifact[starts-with(@id,'jsaga-adaptor-')]">
                        <xsl:sort select="@id" order="ascending"/>
                    </xsl:apply-templates>
                </table>
            </body>
        </html>
    </xsl:template>

    <xsl:template name="HEADERS">
        <tr>
            <th>Module</th>
            <th>Description</th>
            <th>License</th>
        </tr>
    </xsl:template>

    <xsl:template match="artifact">
        <tr>
            <td><a href="{@id}/faq.html"><xsl:value-of select="@name"/></a></td>
            <td><xsl:value-of select="@description"/></td>
            <td><xsl:choose>
                <xsl:when test="@licenseUrl"><a href="{@licenseUrl}"><xsl:value-of select="@license"/></a></xsl:when>
                <xsl:otherwise><xsl:value-of select="@license"/></xsl:otherwise>
            </xsl:choose></td>
        </tr>
    </xsl:template>
</xsl:stylesheet>