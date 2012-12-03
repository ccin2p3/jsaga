<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>

	<xsl:param name="UniqId"></xsl:param>
	<xsl:param name="RootDir"></xsl:param>
	
	<xsl:variable name="WorkingDir">
		<xsl:choose>
			<xsl:when test="//jsdl:Application/posix:POSIXApplication/posix:WorkingDirectory">
		    	<xsl:value-of select="//jsdl:Application/posix:POSIXApplication/posix:WorkingDirectory/text()"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$RootDir"/><xsl:text>/</xsl:text><xsl:value-of select="$UniqId"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
    <!-- entry point (MUST BE RELATIVE) -->
	 <xsl:template match="jsdl:JobDefinition">
        <xsl:apply-templates select="jsdl:JobDescription"/>
    </xsl:template>
    
    <xsl:template match="jsdl:JobDescription">
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Environment">
<xsl:value-of select="@name"/>=<xsl:value-of select="text()"/><xsl:text>
</xsl:text>
        </xsl:for-each>

		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:WorkingDirectory/text()">
_WorkingDirectory=<xsl:value-of select="."/><xsl:text>
</xsl:text>
		</xsl:for-each>

                 <xsl:if test="jsdl:DataStaging">
_DataStaging=<xsl:for-each select="jsdl:DataStaging">
                        <xsl:if test="jsdl:Source">
                                <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/>
                                <xsl:text>&gt;</xsl:text>
                                <xsl:value-of select="jsdl:FileName/text()"/>
                        </xsl:if>
                        <xsl:if test="jsdl:Target">
                            <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/>
                                <xsl:text>&lt;</xsl:text>
                                <xsl:value-of select="jsdl:FileName/text()"/>
                        </xsl:if>
                        <xsl:text>,</xsl:text>
                </xsl:for-each>
                </xsl:if>


_Executable=cd <xsl:value-of select="$WorkingDir"/><xsl:text>;</xsl:text>
		<!-- For BASH -->
		<xsl:text>export PATH=.:$PATH</xsl:text><xsl:text> 2&gt;/dev/null; </xsl:text>
		<!-- For CSH -->
		<xsl:text>setenv PATH .:$PATH</xsl:text><xsl:text> 2&gt;/dev/null; </xsl:text>
		<!-- Make it executable -->
		<xsl:text>chmod u+x </xsl:text><xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/><xsl:text> 2&gt;/dev/null; </xsl:text>
		<!-- Run the program -->
		<xsl:text>eval &apos;</xsl:text><xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/><xsl:text/>
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
             <xsl:text> </xsl:text><xsl:value-of select="."/><xsl:text/>
        </xsl:for-each>
        
        <!-- needed when job attribute FileTransfer is not set -->
		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Input/text()">
            <xsl:text> &lt;</xsl:text><xsl:value-of select="."/><xsl:text/>
		</xsl:for-each>
		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Output/text()">
            <xsl:text> &gt;</xsl:text><xsl:value-of select="."/><xsl:text/>
		</xsl:for-each>
		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Error/text()">
            <xsl:text> 2&gt;</xsl:text><xsl:value-of select="."/><xsl:text/>
		</xsl:for-each>
<!-- 		<xsl:text> &lt;</xsl:text><xsl:value-of select="$RootDir"/><xsl:text>/</xsl:text><xsl:value-of select="$UniqId"/><xsl:text>.in</xsl:text>
		<xsl:text> &gt;</xsl:text><xsl:value-of select="$RootDir"/><xsl:text>/</xsl:text><xsl:value-of select="$UniqId"/><xsl:text>.out</xsl:text>
		<xsl:text> 2&gt;</xsl:text><xsl:value-of select="$RootDir"/><xsl:text>/</xsl:text><xsl:value-of select="$UniqId"/><xsl:text>.err</xsl:text>
 -->		<xsl:text> &amp; &apos;; </xsl:text>
 		<!-- Get the PID -->
		<xsl:text>MYPID=$!;</xsl:text>
		<!-- Store the PID -->
		<xsl:text>echo $MYPID &gt; </xsl:text><xsl:value-of select="$RootDir"/><xsl:text>/</xsl:text><xsl:value-of select="$UniqId"/><xsl:text>.pid ;</xsl:text>
		<!-- Wait until process has finished -->
		<xsl:text>wait $MYPID; </xsl:text>
		<!-- Get return code -->
		<xsl:text>errcode=$?; </xsl:text>
		<!-- Store return code -->
		<xsl:text>echo $errcode &gt; </xsl:text><xsl:value-of select="$RootDir"/><xsl:text>/</xsl:text><xsl:value-of select="$UniqId"/><xsl:text>.endcode ;</xsl:text>
		<!-- exit -->
		<xsl:text>exit $errcode; </xsl:text>
	</xsl:template>
</xsl:stylesheet>