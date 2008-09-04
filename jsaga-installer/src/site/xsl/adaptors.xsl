<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:template match="/project">
        <document><body>
            <section name="Adaptors">
                <table border="1">
                    <xsl:call-template name="HEADERS"/>
                    <xsl:apply-templates select="artifact[starts-with(@id,'jsaga-adaptor-')]">
                        <xsl:sort select="@id" order="ascending"/>
                    </xsl:apply-templates>
                    <xsl:apply-templates select="artifact[@id='jsaga-engine' and not(@classifier)]"/>
                </table>
            </section>
        </body></document>
    </xsl:template>

    <xsl:template name="HEADERS">
        <tr>
            <th>Module</th>
            <th>Description</th>
            <th>License</th>
        </tr>
    </xsl:template>

    <xsl:template match="artifact[@id='jsaga-engine' and not(@classifier)]">
        <tr>
            <td>JSAGA Core</td>
            <td>The core engine provides support for:
security mechanisms (login/password, in-memory grid proxy),
data management (local file catalog),
job description languages (SAGA job description attributes, JSDL, JSAGA job collection description language).</td>
            <td><xsl:choose>
                <xsl:when test="@licenseUrl"><a href="{@licenseUrl}"><xsl:value-of select="@license"/></a></xsl:when>
                <xsl:otherwise><xsl:value-of select="@license"/></xsl:otherwise>
            </xsl:choose></td>
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