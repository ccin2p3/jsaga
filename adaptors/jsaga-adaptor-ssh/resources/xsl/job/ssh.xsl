<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>
    
	<xsl:param name="RootDir"></xsl:param>

    <!-- entry point (MUST BE RELATIVE) -->
	 <xsl:template match="jsdl:JobDefinition">
        <xsl:apply-templates select="jsdl:JobDescription"/>
    </xsl:template>
    
    <xsl:template match="jsdl:JobDescription">
    
<!--  <xsl:text>echo "</xsl:text>-->
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Environment">
<xsl:value-of select="@name"/>='<xsl:value-of select="text()"/><xsl:text>'
</xsl:text>
        </xsl:for-each>
		
_Script=<xsl:text>errcode=0;</xsl:text>
		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:WorkingDirectory/text()">
			<xsl:text> cd </xsl:text><xsl:value-of select="."/><xsl:text> ; errcode=$?;</xsl:text>
		</xsl:for-each>
		<xsl:text> if [ $errcode -eq 0 ] ; then </xsl:text>
		<xsl:text> eval &apos; /bin/cat | </xsl:text>
		<xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/><xsl:text/>
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
             <xsl:text> </xsl:text><xsl:value-of select="."/><xsl:text/>
        </xsl:for-each>
		<xsl:text>  &amp;  &apos;; </xsl:text>
		<xsl:text>MYPID=$!;</xsl:text>
		<xsl:text>echo $MYPID &gt; $HOME/</xsl:text><xsl:value-of select="$RootDir"/><xsl:text>/${JOBID}.pid ;</xsl:text>
		<xsl:text>wait $MYPID ; </xsl:text>
		<xsl:text>errcode=$?; </xsl:text>
		<xsl:text>fi ; </xsl:text>
		<xsl:text>echo $errcode &gt; $HOME/</xsl:text><xsl:value-of select="$RootDir"/><xsl:text>/${JOBID}.endcode ;</xsl:text>
		<xsl:text>exit $errcode; </xsl:text>
<!--<xsl:text>" | sh</xsl:text>-->

	</xsl:template>
</xsl:stylesheet>