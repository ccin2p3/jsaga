<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mvn="http://maven.apache.org/POM/4.0.0">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:template match="/project">
        <document><body>
            <section name="Contributors">
                <table border="1">
                    <xsl:call-template name="HEADERS"/>
                    <xsl:apply-templates select="artifact[starts-with(@id,'jsaga-adaptor-') and not(@classifier)]">
                        <xsl:sort select="@name" order="ascending"/>
                    </xsl:apply-templates>
                </table>
            </section>
        </body></document>
    </xsl:template>

    <xsl:template name="HEADERS">
        <tr>
            <th>Module</th>
            <th>Contributors</th>
            <th>Organizations</th>
            <th>Projects</th>
        </tr>
    </xsl:template>

    <xsl:template match="artifact">
        <xsl:variable name="pom">file:///<xsl:value-of select="substring-before(@file,'.jar')"/>.pom</xsl:variable>
        <xsl:variable name="developers" select="document($pom)/mvn:project/mvn:developers"/>
        <tr>
            <td><a href="adaptors/{@id}/faq.html"><xsl:value-of select="@name"/></a></td>
            <td>
                <xsl:for-each select="$developers/mvn:developer/mvn:name">
                    <xsl:value-of select="translate(text(),'éô', 'eo')"/><br/>
                </xsl:for-each>
            </td>
            <td>
                <xsl:for-each select="$developers/mvn:developer/mvn:organizationUrl">
                    <div><a href="{text()}">
                        <img src="{../mvn:properties/mvn:organizationLogoUrl/text()}"
                             alt="{../mvn:properties/mvn:organization/text()}"
                             height="30"/>
                    </a></div>
                </xsl:for-each>
            </td>
            <td>
                <xsl:for-each select="$developers/mvn:developer/mvn:properties/mvn:projectUrl">
                    <div><a href="{text()}">
                        <img src="{../mvn:projectLogoUrl/text()}"
                             alt="{../mvn:project/text()}"
                             width="60"/>
                    </a></div>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>