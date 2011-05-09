<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>
	<xsl:param name="stagingDir"/>

    <xsl:variable name="ATTRIBUTE_SEPARATOR">;</xsl:variable>
    <!-- entry point (MUST BE RELATIVE) -->
	 <xsl:template match="jsdl:JobDefinition">
         <xsl:apply-templates select="jsdl:JobDescription"/>
    </xsl:template>

    <xsl:template match="jsdl:JobDescription">
        <xsl:variable name="lf"><xsl:text>
</xsl:text></xsl:variable>
		<xsl:if test="$stagingDir">
	        <xsl:text>#PBS -d </xsl:text><xsl:value-of select="concat($stagingDir,$lf)"/>
		</xsl:if>
        <xsl:for-each select="jsdl:JobIdentification">
            <xsl:for-each select="jsdl:JobAnnotation"
            	>#PBS -q <xsl:value-of select="concat(text(),$lf)"/>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Application">
            <xsl:for-each select="posix:POSIXApplication">
                <xsl:for-each select="posix:Output"
                        >#PBS -o <xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:for-each select="posix:Error"
                        >#PBS -e <xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:for-each select="posix:Environment"
                        ><xsl:value-of select="@name"/>=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Resources">
            <xsl:choose>
                <xsl:when test="jsdl:WallTimeLimit"
                        >#PBS -l walltime=<xsl:value-of select="concat(jsdl:WallTimeLimit/*/text(),$lf)"/>
                </xsl:when>
                <xsl:otherwise
                        >#PBS -l walltime=1:00:00<xsl:value-of select="$lf"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:for-each select="jsdl:TotalCPUCount/*"
                    >#PBS -l nodes=<xsl:value-of select="concat(text(),$lf)"/>
            </xsl:for-each>
            <xsl:for-each select="jsdl:TotalCPUTime/*"
                    >#PBS -l cput=<xsl:value-of select="concat(text(),$lf)"/>
            </xsl:for-each>
            <xsl:for-each select="jsdl:TotalPhysicalMemory/*"
                    >#PBS -l mem=<xsl:value-of select="concat(text(),'Mb',$lf)"/>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Application">
            <xsl:for-each select="posix:POSIXApplication">
                <xsl:for-each select="posix:WorkingDirectory"
                        ><xsl:text>cd </xsl:text><xsl:value-of select="text()"/><xsl:text> || exit $?
</xsl:text>
                </xsl:for-each>
                <xsl:for-each select="posix:Executable"
                        > <xsl:value-of select="text()"/><xsl:text> </xsl:text>
                </xsl:for-each>
                <xsl:if test="posix:Argument"
                        > <xsl:for-each select="posix:Argument"><xsl:value-of select="text()"/>
                    <xsl:text> </xsl:text></xsl:for-each><xsl:value-of select="$lf"/>
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="jsdl:DataStaging">
        <xsl:choose>
            <xsl:when test="jsdl:Source and jsdl:CreationFlag='Append'">
                <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/> &gt;&gt; <xsl:text/>
            </xsl:when>
            <xsl:when test="jsdl:Source">
                <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/> &gt; <xsl:text/>
            </xsl:when>
            <xsl:when test="jsdl:Target and jsdl:CreationFlag='Append'">
                <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/> &lt;&lt; <xsl:text/>
            </xsl:when>
            <xsl:when test="jsdl:Target">
                <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/> &lt; <xsl:text/>
            </xsl:when>
        </xsl:choose>
        <xsl:value-of select="jsdl:FileName/text()"/>
        <xsl:value-of select="$ATTRIBUTE_SEPARATOR"/>
    </xsl:template>
</xsl:stylesheet>