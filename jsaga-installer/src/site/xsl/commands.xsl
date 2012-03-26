<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/scripts">
        <document><body>
            <section name="Command Line Interfaces">
                <ul>
                    <xsl:for-each select="script">
                        <li><xsl:value-of select="text()"/></li>
                    </xsl:for-each>
                </ul>
            </section>
        </body></document>
    </xsl:template>
</xsl:stylesheet>
