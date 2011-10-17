<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:jsaga="http://www.in2p3.fr/jsdl-extension">
	<xsl:output method="xml"/>
	
	<!-- JSAGA parameters -->
	<xsl:param name="UniqId">staging</xsl:param>
	<xsl:param name="HostName">host</xsl:param>
	<xsl:param name="Protocol">unicore</xsl:param>
	<xsl:param name="Port">8080</xsl:param>
	<xsl:param name="Path">/DataStaging</xsl:param>
	<xsl:param name="Target">DEMO-SITE</xsl:param>
	<xsl:param name="Res">default_storage</xsl:param>
	
	<!-- constants -->
	<xsl:variable name="IntermediaryURL">
	    <xsl:value-of select="$Protocol"/>://<xsl:value-of select="$HostName"/>:<xsl:value-of select="$Port"/><xsl:value-of select="$Path"/>/<xsl:value-of select="$UniqId"/>
	</xsl:variable>
	
	<!-- constants -->
	<!-- This Intermediary URL for unicore server will be commented when Unicore will support
		URL like unicore://localhost6:8080/DEMO-SITE/... -->
	<xsl:variable name="IntermediaryUnicoreURL">
		<xsl:text/>BFT:https://<xsl:value-of select="$HostName"/>:<xsl:value-of select="$Port"/>/<xsl:value-of select="$Target"/>/services/StorageManagement?res=<xsl:value-of select="$Res"/>#<xsl:value-of select="$Path"/>/<xsl:value-of select="$UniqId"/>
	</xsl:variable>

	<xsl:template match="/ | @* | node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
	  </xsl:copy>
	</xsl:template>

	<xsl:template match="jsdl:JobDescription">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
	  </xsl:copy>
	</xsl:template>
		
	<xsl:template match="jsdl:Application">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
	  </xsl:copy>
	</xsl:template>
		
	<xsl:template match="jsdl:DataStaging/jsdl:Source">
		<jsdl:Source>
			<jsdl:URI><xsl:value-of select="$IntermediaryUnicoreURL"/>/<xsl:value-of select="../jsdl:FileName/text()"/></jsdl:URI>
		</jsdl:Source>
	</xsl:template>
	
	<xsl:template match="jsdl:DataStaging/jsdl:Target">
		<jsdl:Target>
			<jsdl:URI><xsl:value-of select="$IntermediaryUnicoreURL"/>/<xsl:value-of select="../jsdl:FileName/text()"/></jsdl:URI>
		</jsdl:Target>
	</xsl:template>
	
	<xsl:template match="jsdl:DataStaging">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
	  </xsl:copy>
	</xsl:template>

	<!-- MountPoint is not supported -->
	<xsl:template match="jsdl:MountPoint">
	</xsl:template>

	<!-- could not locate FileSystem -->
	<xsl:template match="jsdl:FileSystem">
	</xsl:template>
	
	<xsl:template match="posix:Output">
		<posix:output><xsl:value-of select="text()"/></posix:output>
	</xsl:template>
	<xsl:template match="posix:Error">
		<posix:output><xsl:value-of select="text()"/></posix:output>
	</xsl:template>

	

</xsl:stylesheet>