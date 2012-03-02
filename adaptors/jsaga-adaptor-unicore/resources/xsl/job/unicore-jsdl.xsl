<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:jsaga="http://www.in2p3.fr/jsdl-extension"
                xmlns:unicore="http://www.unicore.eu/unicore/jsdl-extensions">
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
        <xsl:choose>
          <xsl:when test="../jsdl:JobIdentification/jsdl:JobProject">
	      		<jsdl:ApplicationName><xsl:value-of select="../jsdl:JobIdentification/jsdl:JobProject/text()"></xsl:value-of></jsdl:ApplicationName>
          </xsl:when>
          <xsl:otherwise>
	        <!-- With this, UNICORE does the chmod +x and adds . in $PATH -->
	        <jsdl:ApplicationName>Custom executable</jsdl:ApplicationName>
          </xsl:otherwise>
        </xsl:choose>
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

	<xsl:template match="jsdl:Resources">
	  <xsl:copy>
	    	<xsl:apply-templates select="@* | node()"/>
			<xsl:if test="../jsdl:Application/spmd:SPMDApplication/spmd:ProcessesPerHost">
				<jsdl:IndividualCPUCount>
					<jsdl:Exact><xsl:value-of select="../jsdl:Application/spmd:SPMDApplication/spmd:ProcessesPerHost/text()"/></jsdl:Exact>
				</jsdl:IndividualCPUCount>
				<!-- <jsdl:TotalResourceCount>
					<jsdl:Exact>1</jsdl:Exact>
				</jsdl:TotalResourceCount>-->
			</xsl:if>
	  </xsl:copy>
	  <xsl:if test="../jsdl:Application/spmd:SPMDApplication">
	  	<unicore:ExecutionEnvironment>
	  		<xsl:if test="../jsdl:Application/spmd:SPMDApplication/spmd:SPMDVariation">
	  			<unicore:Name><xsl:value-of select="../jsdl:Application/spmd:SPMDApplication/spmd:SPMDVariation/text()"/></unicore:Name>
	  		</xsl:if>
	  		<xsl:if test="../jsdl:Application/spmd:SPMDApplication/spmd:NumberOfProcesses">
	  			<unicore:Argument>
	  				<unicore:Name>NumberOfProcesses</unicore:Name>
	  				<unicore:Value><xsl:value-of select="../jsdl:Application/spmd:SPMDApplication/spmd:NumberOfProcesses/text()"/></unicore:Value>
	  			</unicore:Argument>
	  		</xsl:if>
	  		<xsl:if test="../jsdl:Application/spmd:SPMDApplication/spmd:ThreadsPerProcess">
	  			<unicore:Argument>
	  				<unicore:Name>ThreadsPerProcess</unicore:Name>
	  				<unicore:Value><xsl:value-of select="../jsdl:Application/spmd:SPMDApplication/spmd:ThreadsPerProcess/text()"/></unicore:Value>
	  			</unicore:Argument>
	  		</xsl:if>
	  		<xsl:if test="../jsdl:Application/spmd:SPMDApplication/spmd:ProcessesPerHost">
	  			<unicore:Argument>
	  				<unicore:Name>ProcessesPerHost</unicore:Name>
	  				<unicore:Value><xsl:value-of select="../jsdl:Application/spmd:SPMDApplication/spmd:ProcessesPerHost/text()"/></unicore:Value>
	  			</unicore:Argument>
	  		</xsl:if>
	  		<!-- TODO
	  		<unicore:UserPreCommand>XXX</unicore:UserPreCommand>
	  		 -->
	  	</unicore:ExecutionEnvironment>
	  </xsl:if>
	</xsl:template>

	<!-- <xsl:template match="spmd:SPMDApplication">
	</xsl:template> -->

</xsl:stylesheet>