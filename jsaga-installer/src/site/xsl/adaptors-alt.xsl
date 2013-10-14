<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                exclude-result-prefixes="pom">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:template match="/pom:project">
        <document><body>
            <section name="Alternative Adaptors">
                <p>Alternative adaptors are not officially supported by the JSAGA project and are not packaged with the JSAGA installer.
                </p>
                <table border="1">
                    <xsl:call-template name="HEADERS"/>
                    <xsl:apply-templates select="pom:dependencies/pom:dependency[not(pom:classifier)]">
                        <xsl:sort select="pom:artifactId/text()" order="ascending"/>
                    </xsl:apply-templates>
                </table>
            </section>
        </body></document>
    </xsl:template>

    <xsl:template name="HEADERS">
        <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Developed by</th>
        </tr>
    </xsl:template>

    <xsl:template match="pom:dependency">
        <xsl:variable name="module" select="document(concat('../adaptors/',pom:artifactId/text(),'/pom.xml'))/pom:project"/>
        
        <tr>
            <td><xsl:value-of select="$module/pom:name/text()"/></td>
            <td><xsl:value-of select="$module/pom:description/text()"/></td>
            <td>
                <xsl:for-each select="$module/pom:developers/pom:developer">
                    <xsl:if test="position()>1"><br></br></xsl:if>
                    <xsl:value-of select="pom:name/text()"/>
                    <xsl:text> (</xsl:text>
                    <xsl:value-of select="pom:organization/text()"></xsl:value-of>
                    <xsl:text>)</xsl:text>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>