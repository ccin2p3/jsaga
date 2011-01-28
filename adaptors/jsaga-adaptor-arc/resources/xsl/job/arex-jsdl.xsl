<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:jsdl-arc="http://www.nordugrid.org/ws/schemas/jsdl-arc"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:jsaga="http://www.in2p3.fr/jsdl-extension">
	<xsl:output method="xml"/>
	
	<!-- A-REX needs Source to be empty to know it has to wait until input files are uploaded 
	before job is submitted to batch system -->
	<xsl:template match="jsdl:DataStaging/jsdl:Source">
		<jsdl:Source/>
	</xsl:template>
	
	<!-- A-REX needs Target to be empty to know that client will download output files at the end of the job -->
	<xsl:template match="jsdl:DataStaging/jsdl:Target">
		<jsdl:Target/>
	</xsl:template>
	
	<!-- Create jsaga:Source and jsaga:Target to be able to construct transfers URI for pre and post datastaging -->
	<xsl:template match="jsdl:DataStaging">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
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
	  </xsl:copy>
	</xsl:template>

    <xsl:template match="jsdl:JobDescription">
	  <xsl:copy>
	    <!--  copy JobIdentification and Application -->
	    <xsl:apply-templates select="@* | jsdl:JobIdentification"/>
	    <xsl:apply-templates select="@* | jsdl:Application"/>
	    <!-- create Resources to add A-REX extensions -->
		<jsdl:Resources>
		    <xsl:copy-of select="jsdl:Resources/*"/>
			<!-- If JobAnnotation, the queue name is sent to A-REX via JSDL ARC extension -->
		    <xsl:if test="jsdl:JobIdentification/jsdl:JobAnnotation">
		      <jsdl-arc:CandidateTarget>
		        <jsdl-arc:QueueName><xsl:value-of select="jsdl:JobIdentification/jsdl:JobAnnotation/text()"/></jsdl-arc:QueueName>
		      </jsdl-arc:CandidateTarget>
		    </xsl:if>
			<!-- If posix:WallTimeLimit, build <TotalWallTime> for A-REX -->
		    <xsl:if test="jsdl:Application/posix:POSIXApplication/posix:WallTimeLimit">
		      <jsdl:TotalWallTime>
		        <jsdl:Value>
		          <jsdl:Max><xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:WallTimeLimit/text()"/></jsdl:Max>
		        </jsdl:Value>
		      </jsdl:TotalWallTime>
		    </xsl:if>
		</jsdl:Resources>
		<!-- copy remaining nodes: DataStaging and extensions -->
	    <xsl:apply-templates select="@* | *[local-name()!='JobIdentification' and local-name()!='Application' and local-name()!='Resources']"/>
	  </xsl:copy>
    </xsl:template>

	<!-- A-REX does not use JobAnnotation which contains queue name -->
	<xsl:template match="jsdl:JobAnnotation"></xsl:template>

	<xsl:template match="/ | @* | node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@* | node()"/>
	  </xsl:copy>
	</xsl:template>

</xsl:stylesheet>