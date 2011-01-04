<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:jsaga="http://www.in2p3.fr/jsdl-extension">
	<xsl:output method="xml"/>
	
	<!-- JSAGA parameters -->
	<xsl:param name="HostName">host</xsl:param>
	<xsl:param name="UniqId">staging</xsl:param>
	<xsl:param name="Protocol">file</xsl:param>
	<xsl:param name="Port"></xsl:param>
	
	<!-- constants -->
	<!-- <xsl:variable name="SupportedProtocols">/gsiftp/</xsl:variable> -->
	<xsl:variable name="IntermediaryURL">
		<xsl:choose>
          <xsl:when test="$Protocol = 'file'">
		    <xsl:text/>file:/tmp/<xsl:value-of select="$UniqId"/>
          </xsl:when>
          <xsl:otherwise>
		    <xsl:value-of select="$Protocol"/>://<xsl:value-of select="$HostName"/>:<xsl:value-of select="$Port"/>/tmp/<xsl:value-of select="$UniqId"/>
          </xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:template match="/ | @* | node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
	  </xsl:copy>
	</xsl:template>

	<xsl:template match="jsdl:JobDescription">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
		<jsaga:StagingDirectory><jsaga:URI><xsl:value-of select="$IntermediaryURL"/></jsaga:URI></jsaga:StagingDirectory>
	  </xsl:copy>
	</xsl:template>
		
	<xsl:template match="jsdl:DataStaging/jsdl:Source">
		<jsdl:Source>
			<jsdl:URI><xsl:value-of select="$IntermediaryURL"/>/<xsl:value-of select="../jsdl:FileName/text()"/></jsdl:URI>
		</jsdl:Source>
	</xsl:template>
	
	<xsl:template match="jsdl:DataStaging/jsdl:Target">
		<jsdl:Target>
			<jsdl:URI><xsl:value-of select="$IntermediaryURL"/>/<xsl:value-of select="../jsdl:FileName/text()"/></jsdl:URI>
		</jsdl:Target>
	</xsl:template>
	
	<xsl:template match="jsdl:DataStaging">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
	  </xsl:copy>
      <jsaga:DataStaging>
        <xsl:choose>
          <xsl:when test="jsdl:Source">
     			<jsaga:PreStagingIn/>
          </xsl:when>
          <xsl:otherwise>
     			<jsaga:PostStagingOut/>
          </xsl:otherwise>
        </xsl:choose>
       	<jsaga:Source>
          <xsl:choose>
            <xsl:when test="jsdl:Source">
       			<jsaga:URI><xsl:value-of select="jsdl:Source/jsdl:URI/text()"/></jsaga:URI>
            </xsl:when>
            <xsl:otherwise>
	       		<jsaga:URI><xsl:value-of select="$IntermediaryURL"/>/<xsl:value-of select="jsdl:FileName/text()"/></jsaga:URI>
            </xsl:otherwise>
          </xsl:choose>
       	</jsaga:Source>
       	<jsaga:Target>
          <xsl:choose>
            <xsl:when test="jsdl:Source">
	       		<jsaga:URI><xsl:value-of select="$IntermediaryURL"/>/<xsl:value-of select="jsdl:FileName/text()"/></jsaga:URI>
            </xsl:when>
            <xsl:otherwise>
       			<jsaga:URI><xsl:value-of select="jsdl:Target/jsdl:URI/text()"/></jsaga:URI>
            </xsl:otherwise>
          </xsl:choose>
       	</jsaga:Target>
      </jsaga:DataStaging>
	</xsl:template>

</xsl:stylesheet>