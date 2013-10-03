<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>
    
    <!-- entry point (MUST BE RELATIVE) -->
	 <xsl:template match="jsdl:JobDefinition">
        <xsl:apply-templates select="jsdl:JobDescription"/>
    </xsl:template>
    
    <xsl:template match="jsdl:JobDescription">
    
    <xsl:text>
manifest={ </xsl:text>
        <!-- <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Environment">
<xsl:value-of select="@name"/>='<xsl:value-of select="text()"/><xsl:text>'
</xsl:text>
        </xsl:for-each> -->

		<!-- <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:WorkingDirectory/text()">
_WorkingDirectory=<xsl:value-of select="."/><xsl:text>
</xsl:text>
		</xsl:for-each> -->

        <xsl:text>"Executable": &quot;</xsl:text>
            <xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/>
            <xsl:text>&quot;</xsl:text>

<!-- TODO: arguments is optional -->
<!-- <xsl:text>
Arguments: </xsl:text>
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
             <xsl:text> &quot;<xsl:value-of select="."/>&quot;,</xsl:text>
        </xsl:for-each> -->
        
        <!-- needed when job attribute FileTransfer is not set -->
 		<!-- <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Input/text()">
            <xsl:text> &lt;</xsl:text><xsl:value-of select="."/><xsl:text/>
		</xsl:for-each> -->
		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Output/text()">
            <xsl:text>,"StdOutput": &quot;</xsl:text>
                <xsl:value-of select="."/>
                <xsl:text>&quot;</xsl:text>
		</xsl:for-each>
		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Error/text()">
            <xsl:text>,"StdError": &quot;</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text>&quot;</xsl:text>
		</xsl:for-each>

<!-- Input data -->
<xsl:if test="jsdl:DataStaging/jsdl:Source">
    <xsl:text>
,"InputSandbox": [</xsl:text>
    <xsl:for-each select="jsdl:DataStaging/jsdl:Source">
        <xsl:text>&quot;</xsl:text>
        <!-- <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/> -->
        <xsl:value-of select="jsdl:FileName/text()"/>
        <xsl:text>&quot;</xsl:text>
        <!-- <xsl:text>,</xsl:text> -->
    </xsl:for-each>
    <xsl:text>],</xsl:text>
</xsl:if>
<!-- Output data -->
<xsl:if test="jsdl:DataStaging/jsdl:Target">
    <xsl:text>
,"OutputSandbox": [</xsl:text>
    <xsl:for-each select="jsdl:DataStaging/jsdl:Target">
        <xsl:text>&quot;</xsl:text>
        <xsl:value-of select="jsdl:FileName/text()"/>
        <xsl:text>&quot;</xsl:text>
        <!-- <xsl:text>,</xsl:text> -->
    </xsl:for-each>
    <xsl:text>],</xsl:text>
</xsl:if>
    

    <xsl:text>}</xsl:text>

	</xsl:template>
</xsl:stylesheet>