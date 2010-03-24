<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="xml"/>

    <!-- JSAGA parameters -->
    <xsl:param name="HostName">host</xsl:param>
    <xsl:param name="UniqId">staging</xsl:param>

    <!-- constants -->
    <xsl:variable name="SupportedProtocols">/gsiftp/</xsl:variable>
    <xsl:variable name="IntermediaryURL">
        <xsl:text/>gsiftp://<xsl:value-of select="$HostName"/>:2811/tmp/<xsl:value-of select="$UniqId"/>
    </xsl:variable>

    <!-- entry point (MUST BE RELATIVE) -->
    <xsl:template match="jsdl:JobDefinition">
        <job>
            <xsl:apply-templates select="jsdl:JobDescription"/>
        </job>
    </xsl:template>

    <xsl:template match="jsdl:JobDescription">
        <!-- executable and arguments -->
        <executable><xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/></executable>
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
            <argument><xsl:value-of select="."/></argument>
        </xsl:for-each>

        <!-- streams -->
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Output/text()">
            <stdout><xsl:value-of select="."/></stdout>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Error/text()">
            <stderr><xsl:value-of select="."/></stderr>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Input/text()">
            <stdin><xsl:value-of select="."/></stdin>
        </xsl:for-each>

        <!-- data staging -->
        <xsl:if test="jsdl:DataStaging">
            <xsl:if test="jsdl:DataStaging[jsdl:Source]">
                <fileStageIn>
                    <xsl:for-each select="jsdl:DataStaging[jsdl:Source]">
                        <transfer>
                            <sourceUrl>
                                <xsl:choose>
                                    <xsl:when test="contains($SupportedProtocols,concat('/',substring-before(jsdl:Source/jsdl:URI/text(),'://'),'/'))">
                                        <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$IntermediaryURL"/>/<xsl:value-of select="jsdl:FileName/text()"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </sourceUrl>
                            <destinationUrl>file:///${GLOBUS_USER_HOME}/<xsl:value-of select="jsdl:FileName/text()"/></destinationUrl>
                        </transfer>
                    </xsl:for-each>
                </fileStageIn>
            </xsl:if>
            <xsl:if test="jsdl:DataStaging[jsdl:Target]">
                <fileStageOut>
                    <xsl:for-each select="jsdl:DataStaging[jsdl:Target]">
                        <transfer>
                            <sourceUrl>file:///${GLOBUS_USER_HOME}/<xsl:value-of select="jsdl:FileName/text()"/></sourceUrl>
                            <destinationUrl>
                                <xsl:choose>
                                    <xsl:when test="contains($SupportedProtocols,concat('/',substring-before(jsdl:Target/jsdl:URI/text(),'://'),'/'))">
                                        <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$IntermediaryURL"/>/<xsl:value-of select="jsdl:FileName/text()"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </destinationUrl>
                        </transfer>
                    </xsl:for-each>
                </fileStageOut>
            </xsl:if>

            <extensions>
                <stageDirectory><xsl:value-of select="$IntermediaryURL"/></stageDirectory>
                <preStageIn>
                    <xsl:variable name="UnsupportedURI" select="jsdl:DataStaging[jsdl:Source][
                        not(contains($SupportedProtocols,concat('/',substring-before(jsdl:Source/jsdl:URI/text(),'://'),'/')))]"/>
                    <xsl:for-each select="$UnsupportedURI">
                        <transfer>
                            <sourceUrl><xsl:value-of select="translate(jsdl:Source/jsdl:URI/text(),'\','/')"/></sourceUrl>
                            <destinationUrl><xsl:value-of select="$IntermediaryURL"/>/<xsl:value-of select="jsdl:FileName/text()"/></destinationUrl>
                            <append><xsl:value-of select="string(jsdl:CreationFlag/text()='append')"/></append>
                        </transfer>
                    </xsl:for-each>
                </preStageIn>
                <postStageOut>
                    <xsl:variable name="UnsupportedURI" select="jsdl:DataStaging[jsdl:Target][
                        not(contains($SupportedProtocols,concat('/',substring-before(jsdl:Target/jsdl:URI/text(),'://'),'/')))]"/>
                    <xsl:for-each select="$UnsupportedURI">
                        <transfer>
                            <sourceUrl><xsl:value-of select="$IntermediaryURL"/>/<xsl:value-of select="jsdl:FileName/text()"/></sourceUrl>
                            <destinationUrl><xsl:value-of select="translate(jsdl:Target/jsdl:URI/text(),'\','/')"/></destinationUrl>
                            <append><xsl:value-of select="string(jsdl:CreationFlag/text()='append')"/></append>
                        </transfer>
                    </xsl:for-each>
                </postStageOut>
            </extensions>

            <xsl:if test="jsdl:DataStaging[jsdl:DeleteOnTermination='true']">
                <fileCleanUp>
                    <xsl:for-each select="jsdl:DataStaging[jsdl:DeleteOnTermination='true']">
                        <deletion>
                            <file>file:///${GLOBUS_USER_HOME}/<xsl:value-of select="jsdl:FileName/text()"/></file>
                        </deletion>
                    </xsl:for-each>
                </fileCleanUp>
            </xsl:if>
        </xsl:if>

        <!-- other -->
        <xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Environment">
            <environment>
                <name><xsl:value-of select="@name"/></name>
                <value><xsl:value-of select="text()"/></value>
            </environment>
        </xsl:for-each>
		<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:WorkingDirectory/text()">
            <directory><xsl:value-of select="."/></directory>
        </xsl:for-each>
        <!--  Memory in Mb (must be an integer) -->
       <xsl:for-each select="jsdl:Resources/jsdl:TotalPhysicalMemory/jsdl:UpperBoundedRange/text()">
            <maxMemory><xsl:value-of select="."/></maxMemory>
        </xsl:for-each>
         <!--  CPU time request in minutes (must be an integer) -->
        <xsl:for-each select="jsdl:Resources/jsdl:TotalCPUTime/jsdl:UpperBoundedRange/text()">
            <maxCpuTime><xsl:value-of select="ceiling(. div 60)"/></maxCpuTime>
        </xsl:for-each>
         <xsl:for-each select="jsdl:Application/spmd:SPMDApplication/spmd:SPMDVariation/text()[not(. = 'None')]">
        	<xsl:choose>
           		<xsl:when test=". = 'MPI'">
                    <jobType>mpi</jobType>
           		</xsl:when>
            	<xsl:otherwise>
	            	<xsl:message terminate="yes">Unsupported SPMDVariation : <xsl:value-of select="."/></xsl:message>
            	</xsl:otherwise>
        	</xsl:choose>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Application/spmd:SPMDApplication/spmd:NumberOfProcesses/text()">
            <count><xsl:value-of select="."/></count>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Application/spmd:SPMDApplication">
	        <xsl:if test="spmd:ProcessesPerHost/text()">
                <hostCount><xsl:value-of select="ceiling(spmd:NumberOfProcesses/text() div spmd:ProcessesPerHost/text())"/></hostCount>
        	</xsl:if>
        </xsl:for-each>
        <xsl:for-each select="jsdl:JobIdentification/jsdl:JobAnnotation/text()">
            <queue><xsl:value-of select="."/></queue>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>