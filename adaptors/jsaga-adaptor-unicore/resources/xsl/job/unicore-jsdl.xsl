<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:jsaga="http://www.in2p3.fr/jsdl-extension">
	<xsl:output method="xml"/>
	
	<!-- JSAGA parameters -->
	
	<!-- constants -->

	<xsl:template match="/ | @* | node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
	  </xsl:copy>
	</xsl:template>

	<xsl:template match="jsdl:Application">
	  <xsl:copy>
	    <!-- With this, UNICORE does the chmod +x and adds . in $PATH -->
	    <jsdl:ApplicationName>Custom executable</jsdl:ApplicationName>
	    <xsl:apply-templates select="@* | node()"/>
	  </xsl:copy>
	</xsl:template>
		
	<xsl:template match="jsdl:DataStaging">
      <jsaga:DataStaging>
        <jsaga:FileName>
          <xsl:value-of select="jsdl:FileName/text()"/>
        </jsaga:FileName>
        <xsl:choose>
          <xsl:when test="jsdl:Source">
        	<jsaga:Source>
       			<jsaga:URI><xsl:value-of select="jsdl:Source/jsdl:URI/text()"/></jsaga:URI>
	       	</jsaga:Source>
          </xsl:when>
          <xsl:otherwise>
	       	<jsaga:Target>
       			<jsaga:URI><xsl:value-of select="jsdl:Target/jsdl:URI/text()"/></jsaga:URI>
	       	</jsaga:Target>
          </xsl:otherwise>
        </xsl:choose>
      </jsaga:DataStaging>
	</xsl:template>

</xsl:stylesheet>