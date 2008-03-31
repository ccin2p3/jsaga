<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>
    <xsl:param name="ShellPath"/>
    
    <xsl:template match="/">
        <xsl:apply-templates select="ext:Job/jsdl:JobDefinition/jsdl:JobDescription"/>
    </xsl:template>

    <xsl:template match="jsdl:JobDescription">
Type = "Job";<xsl:text/>
<!-- Rank = other.GlueCEStateFreeCPUs;<xsl:text/>  -->
Rank = -other.GlueCEStateEstimatedResponseTime ;<xsl:text/>
        <!-- executable and arguments -->
        <xsl:choose>
            <xsl:when test="$ShellPath">
Executable = "<xsl:value-of select="$ShellPath"/>";<xsl:text/>
Arguments = "-c <xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/>
                <xsl:if test="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
                    <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
                        <xsl:text> </xsl:text><xsl:value-of select="."/>
                    </xsl:for-each>
                </xsl:if>";<xsl:text/>
            </xsl:when>
            <xsl:otherwise>
Executable = "<xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/>";<xsl:text/>
                <xsl:if test="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
Arguments = "	<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
                    <xsl:text> </xsl:text><xsl:value-of select="."/>
                </xsl:for-each>";<xsl:text/>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>

        <!-- other -->
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Output/text()">
StdOutput = "<xsl:text/><xsl:value-of select="."/>";<xsl:text/>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Error/text()">
StdError = "<xsl:value-of select="."/>";<xsl:text/>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Input/text()">
StdInput = "<xsl:value-of select="."/>";<xsl:text/>
        </xsl:for-each>
        <xsl:if test="count(jsdl:Application/posix:POSIXApplication/posix:Environment) > 0">
Environment = {<xsl:text/>
      		<xsl:for-each
               select="jsdl:Application/posix:POSIXApplication/posix:Environment">
               	<xsl:if test="position() = 1">
               		"<xsl:text/><xsl:value-of select="@name"/>=<xsl:value-of select="text()"/>"<xsl:text/>
               	</xsl:if>
                <xsl:if test="position() > 1">
                		, "<xsl:text/><xsl:value-of select="@name"/>=<xsl:value-of select="text()"/>"<xsl:text/>
                </xsl:if> 
            </xsl:for-each>
           };<xsl:text/>
          </xsl:if> 
          
<!--  For TEST, get stdout and stderr -->
        <xsl:if test="count(jsdl:Application/posix:POSIXApplication/posix:Output) = 1
        	and count(jsdl:Application/posix:POSIXApplication/posix:Error) = 1">
OutputSandbox = {"<xsl:text/>
			<xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Output/text()"/><xsl:text/>
			<xsl:text/>","<xsl:text/>
			<xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Error/text()"/>"};<xsl:text/>
		</xsl:if>
		
<!--  Requirements -->
		<xsl:choose>
			<xsl:when test="count(jsdl:Resources//text()) = 0 and 
			count(jsdl:Application/spmd:SPMDApplication/spmd:ProcessesPerHost) > 0"> 
Requirements = (other.GlueCEInfoTotalCPUs >= <xsl:value-of select="jsdl:Application/spmd:SPMDApplication/spmd:ProcessesPerHost"/>);<xsl:text/>
			</xsl:when>
			<xsl:when test="count(jsdl:Resources//text()) > 0">
Requirements = <xsl:text/>
				<xsl:for-each select="jsdl:Resources/*">					  
					<xsl:choose>
						<!-- TODO -->
               			<xsl:when test="name()= 'jsdl:TotalCPUTime'">
<!-- other.MaxCPUTime > <xsl:value-of select="jsdl:UpperBoundedRange/text()"/><xsl:text/>  -->
true
 						</xsl:when>
 						<!-- OK -->        
                		<xsl:when test="name()= 'jsdl:TotalPhysicalMemory'">
other.GlueHostMainMemoryRAMSize >= <xsl:value-of select="jsdl:UpperBoundedRange/text()"/><xsl:text/>
           				</xsl:when>
           				<!-- TODO -->
 						<xsl:when test="name()= 'jsdl:CPUArchitecture'">
other.GlueSubClusterPlatformType == "<xsl:value-of select="jsdl:CPUArchitectureName"/>"<xsl:text/>
 						</xsl:when>
 						<!-- TODO -->
						<xsl:when test="name()= 'jsdl:OperatingSystem'">
other.OperatingSystemName == "<xsl:value-of select="jsdl:OperatingSystemType/jsdl:OperatingSystemName/text()"/>"<xsl:text/>
 						</xsl:when>  
 						<!-- OK -->
 						<xsl:when test="name()= 'jsdl:CandidateHosts'">
 							<xsl:for-each select="jsdl:HostName">
other.GlueCEInfoHostName == "<xsl:value-of select="text()"/>"<xsl:text/>
								<xsl:if test="position() != last()">
	                				<xsl:text/> &amp;&amp; <xsl:text/>
	                			</xsl:if>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
	    	        		<xsl:message terminate="yes">Inconsistent resource  : <xsl:value-of select="name()"/></xsl:message>
            			</xsl:otherwise>
	    			</xsl:choose>  		
					<xsl:if test="position() != last()">
                		<xsl:text/> &amp;&amp; <xsl:text/>
                	</xsl:if>
				</xsl:for-each>
				<xsl:for-each select="jsdl:Application/spmd:SPMDApplication/spmd:ProcessesPerHost">
&amp;&amp; other.GlueCEInfoTotalCPUs >= <xsl:value-of select="."/><xsl:text/>
				</xsl:for-each>
				<!-- OK -->
				<xsl:for-each select="jsdl:JobIdentification/jsdl:JobAnnotation/text()">
&amp;&amp; other.GlueCEUniqueID == "<xsl:value-of select="."/>"<xsl:text/>
				</xsl:for-each>
				<xsl:text/>;
			</xsl:when>
		    <xsl:otherwise>
Requirements = true;<xsl:text/>
		    </xsl:otherwise>
	    </xsl:choose>
        <!-- TODO : To test when input sandbox will work -->
        <xsl:for-each select="jsdl:Application/spmd:SPMDApplication/spmd:SPMDVariation/text()[not(. = 'None')]">        
            <xsl:choose>
	            <xsl:when test=". = 'MPICH1' or . = 'MPICH2' ">
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