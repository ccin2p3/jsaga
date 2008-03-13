<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>
    <xsl:param name="ShellPath"/>
    <xsl:template match="/jsdl:JobDefinition/jsdl:JobDescription">
        <!-- executable and arguments -->
        <xsl:choose>
            <xsl:when test="$ShellPath">
<xsl:value-of select="$ShellPath"/> -c "<xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/>
                <xsl:if test="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
                    <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
                        <xsl:text> </xsl:text><xsl:value-of select="."/>
                    </xsl:for-each>
                </xsl:if>"<xsl:text/>
            </xsl:when>
            <xsl:otherwise>
<xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/> <xsl:text/>
                <xsl:if test="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
                    <xsl:text> </xsl:text><xsl:value-of select="."/>
                </xsl:for-each> <xsl:text/>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>