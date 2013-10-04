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
{ </xsl:text>
        <!-- <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Environment">
<xsl:value-of select="@name"/>='<xsl:value-of select="text()"/><xsl:text>'
</xsl:text>
        </xsl:for-each> -->

		<!-- <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:WorkingDirectory/text()">
_WorkingDirectory=<xsl:value-of select="."/><xsl:text>
</xsl:text>
		</xsl:for-each> -->

        <xsl:text>"Executable": "</xsl:text>
        <xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/>
        <xsl:text>"</xsl:text>
        
        
        <xsl:if test="jsdl:Application/posix:POSIXApplication/posix:Argument">
            <xsl:text>,"Arguments": [</xsl:text>
            <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
                <xsl:text>"</xsl:text>
                <xsl:value-of select="."/>
                <xsl:text>"</xsl:text>
                <xsl:if test="position() &lt; last()">
                    <xsl:text>,</xsl:text>
                </xsl:if>
            </xsl:for-each>
            <xsl:text>]</xsl:text>
        </xsl:if>
        <!-- needed when job attribute FileTransfer is not set -->
 		<!-- <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Input/text()">
            <xsl:text> &lt;</xsl:text><xsl:value-of select="."/><xsl:text/>
		</xsl:for-each> -->
		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Output/text()">
            <xsl:text>,"StdOutput": "</xsl:text>
                <xsl:value-of select="."/>
                <xsl:text>"</xsl:text>
		</xsl:for-each>
		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Error/text()">
            <xsl:text>,"StdError": &quot;</xsl:text>
            <xsl:value-of select="."/>
            <xsl:text>"</xsl:text>
		</xsl:for-each>

<!-- Input data -->
<xsl:if test="jsdl:DataStaging/jsdl:Source">
    <!-- InputSandbox for DIRAC -->
    <xsl:text>,"InputSandbox": [</xsl:text>
    <xsl:for-each select="jsdl:DataStaging/jsdl:Source">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="../jsdl:FileName/text()"/>
        <xsl:text>"</xsl:text>
        <xsl:if test="position() &lt; last()">
            <xsl:text>,</xsl:text>
        </xsl:if>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
    <!-- JSAGA internal data staging info -->
    <xsl:text>,"JSAGADataStagingIn": [</xsl:text>
    <xsl:for-each select="jsdl:DataStaging/jsdl:Source">
        <xsl:text>{"Source": </xsl:text>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="./jsdl:URI/text()"/>
        <xsl:text>"</xsl:text>
        <xsl:text>,"Dest": </xsl:text>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="../jsdl:FileName/text()"/>
        <xsl:text>"}</xsl:text>
        <xsl:if test="position() &lt; last()">
            <xsl:text>,</xsl:text>
        </xsl:if>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
</xsl:if>
<!-- Output data -->
<xsl:if test="jsdl:DataStaging/jsdl:Target">
    <!-- TODO: generate JSAGADataStagingOut -->
    <xsl:text>,"OutputSandbox": [</xsl:text>
    <xsl:for-each select="jsdl:DataStaging/jsdl:Target">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="../jsdl:FileName/text()"/>
        <xsl:text>"</xsl:text>
        <xsl:if test="position() &lt; last()">
            <xsl:text>,</xsl:text>
        </xsl:if>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
</xsl:if>
    

    <xsl:text>}</xsl:text>

	</xsl:template>
</xsl:stylesheet>