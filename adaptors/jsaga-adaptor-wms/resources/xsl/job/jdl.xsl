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
Type = "Job";<xsl:text/>
<!-- Rank = other.GlueCEStateFreeCPUs;<xsl:text/>  -->
Rank = -other.GlueCEStateEstimatedResponseTime ;<xsl:text/>
        <!-- executable and arguments -->
Executable = "<xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/>";<xsl:text/>
        <xsl:if test="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
Arguments = "<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
                    <xsl:text> </xsl:text><xsl:value-of select="."/>
                </xsl:for-each>";<xsl:text/>
		</xsl:if>

        <!-- other -->
        <xsl:if test="count(jsdl:Application/posix:POSIXApplication/posix:Environment) > 0">
Environment = {<xsl:text/>
      		<xsl:for-each
               select="jsdl:Application/posix:POSIXApplication/posix:Environment">
               	<xsl:if test="contains(text(),' ')">
               		<xsl:message terminate="yes">Unsupported space in environment value : <xsl:value-of select="text()"/></xsl:message>
               	</xsl:if>
               	<xsl:if test="position() = 1">
"<xsl:text/><xsl:value-of select="@name"/>=<xsl:value-of select="text()"/>"<xsl:text/>
               	</xsl:if>
                <xsl:if test="position() > 1">
, "<xsl:value-of select="@name"/>=<xsl:value-of select="text()"/>"<xsl:text/>
                </xsl:if> 
            </xsl:for-each>
};<xsl:text/>
          </xsl:if> 
          		
<!--  Requirements -->
Requirements = true <xsl:text/>
		<xsl:for-each select="jsdl:Resources/jsdl:TotalPhysicalMemory/jsdl:UpperBoundedRange/text()">
&amp;&amp; other.GlueHostMainMemoryRAMSize >= <xsl:value-of select="."/> <xsl:text/>
		</xsl:for-each>
 		<xsl:for-each select="jsdl:Resources/jsdl:CandidateHosts/jsdl:HostName/text()">
&amp;&amp; other.GlueCEInfoHostName == "<xsl:value-of select="."/>" <xsl:text/>
		</xsl:for-each>
		<xsl:for-each select="jsdl:Application/spmd:SPMDApplication/spmd:ProcessesPerHost/text()">
&amp;&amp; other.GlueCEInfoTotalCPUs >= <xsl:value-of select="."/> <xsl:text/>
		</xsl:for-each>
		<xsl:for-each select="jsdl:JobIdentification/jsdl:JobAnnotation/text()">
&amp;&amp; other.GlueCEUniqueID == "<xsl:value-of select="."/>" <xsl:text/>
		</xsl:for-each>
		<!-- Value to use ? 
 		<xsl:for-each select="jsdl:Resources/jsdl:CPUArchitecture/jsdl:CPUArchitectureName/text()">
&amp;&amp;  other.GlueSubClusterPlatformType == "<xsl:value-of select="."/>" <xsl:text/>
		</xsl:for-each>
		<xsl:for-each select="jsdl:Resources/jsdl:OperatingSystem/jsdl:OperatingSystemType/jsdl:OperatingSystemName/text()">
&amp;&amp;  other.OperatingSystemName == "<xsl:value-of select="."/>" <xsl:text/>
		</xsl:for-each>   -->
<xsl:text/>;

        <!-- TODO : To test when input sandbox will work -->
        <xsl:for-each select="jsdl:Application/spmd:SPMDApplication/spmd:SPMDVariation/text()[not(. = 'None')]">        
            <xsl:choose>
	            <xsl:when test=". = 'MPI' or . = 'MPICH1' or . = 'MPICH2'">
JobType = "MPICH";<xsl:text/>
		        <xsl:for-each select="../../spmd:NumberOfProcesses/text()">
NodeNumber = <xsl:value-of select="."/>;<xsl:text/>
	    	    </xsl:for-each>
	            </xsl:when>
    	        <xsl:otherwise>
	    	        <xsl:message terminate="yes">Unsupported SPMDVariation : <xsl:value-of select="."/></xsl:message>
            	</xsl:otherwise>
        	</xsl:choose>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>