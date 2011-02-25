<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>
    <xsl:variable name="ATTRIBUTE_SEPARATOR">;</xsl:variable>

    <!-- entry point (MUST BE RELATIVE) -->
	 <xsl:template match="jsdl:JobDefinition">
         <xsl:apply-templates select="jsdl:JobDescription"/>
    </xsl:template>

    <xsl:template match="jsdl:JobDescription">
        <xsl:variable name="lf"><xsl:text>
</xsl:text></xsl:variable>
        <xsl:for-each select="jsdl:JobIdentification">
            <xsl:for-each select="jsdl:JobAnnotation"
                    >Queue=<xsl:value-of select="concat(text(),$lf)"/>
            </xsl:for-each>
            <xsl:for-each select="jsdl:JobProject"
                    >JobProject=<xsl:value-of select="concat(text(),$lf)"/>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Application">
            <xsl:for-each select="posix:POSIXApplication">
                <xsl:for-each select="posix:Executable"
                        >Executable=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:if test="posix:Argument"
                        >Arguments=<xsl:for-each select="posix:Argument"><xsl:value-of select="text()"/>
                    <xsl:text> </xsl:text></xsl:for-each><xsl:value-of select="$lf"/>
                </xsl:if>
                <xsl:for-each select="posix:Input"
                        >Input=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:for-each select="posix:Output"
                        >Output=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:for-each select="posix:Error"
                        >Error=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:for-each select="posix:WorkingDirectory"
                        >WorkingDirectory=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:if test="posix:Environment"
                        >Environment=<xsl:for-each select="posix:Environment"><xsl:value-of select="text()"/>
                    <xsl:text> </xsl:text></xsl:for-each><xsl:value-of select="$lf"/>
                </xsl:if>
                <xsl:for-each select="posix:WallTimeLimit"
                        >WallTimeLimit=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
            </xsl:for-each>
            <xsl:for-each select="spmd:SPMDApplication">
                <xsl:for-each select="spmd:NumberOfProcesses"
                        >NumberOfProcesses=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:for-each select="spmd:ProcessesPerHost"
                        >ProcessesPerHost=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:for-each select="spmd:ThreadsPerProcess"
                        >ThreadsPerProcess=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
                <xsl:for-each select="spmd:SPMDVariation"
                        >SPMDVariation=<xsl:value-of select="concat(text(),$lf)"/>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Resources">
            <xsl:for-each select="jsdl:CandidateHosts"
                    >CandidateHosts=<xsl:for-each select="jsdl:HostName"><xsl:value-of select="text()"/>
                <xsl:text> </xsl:text></xsl:for-each><xsl:value-of select="$lf"/>
            </xsl:for-each>
            <xsl:for-each select="jsdl:OperatingSystem/jsdl:OperatingSystemType/jsdl:OperatingSystemName"
                    >OperatingSystemType=<xsl:value-of select="concat(text(),$lf)"/>
            </xsl:for-each>
            <xsl:for-each select="jsdl:CPUArchitecture/jsdl:CPUArchitectureName"
                    >CPUArchitecture=<xsl:value-of select="concat(text(),$lf)"/>
            </xsl:for-each>
            <xsl:choose>
                <xsl:when test="jsdl:TotalCPUTime"
                        >TotalCPUTime=<xsl:value-of select="concat(jsdl:TotalCPUTime/*/text(),$lf)"/>
                </xsl:when>
                <xsl:otherwise
                        >TotalCPUTime=1<xsl:value-of select="$lf"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:for-each select="jsdl:TotalCPUCount/*"
                    >TotalCPUCount=<xsl:value-of select="concat(text(),$lf)"/>
            </xsl:for-each>
            <xsl:for-each select="jsdl:TotalPhysicalMemory/*"
                    >TotalPhysicalMemory=<xsl:value-of select="concat(text(),$lf)"/>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:if test="jsdl:DataStaging"
                >FileTransfer=<xsl:apply-templates select="jsdl:DataStaging"/><xsl:value-of select="$lf"/>
        </xsl:if>
        <xsl:if test="jsdl:DataStaging/jsdl:DeleteOnTermination[text()='True']"
                >Cleanup=True</xsl:if>
    </xsl:template>

    <xsl:template match="jsdl:DataStaging">
        <xsl:choose>
            <xsl:when test="jsdl:Source and jsdl:CreationFlag='Append'">
                <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/> &gt;&gt; <xsl:text/>
            </xsl:when>
            <xsl:when test="jsdl:Source">
                <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/> &gt; <xsl:text/>
            </xsl:when>
            <xsl:when test="jsdl:Target and jsdl:CreationFlag='Append'">
                <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/> &lt;&lt; <xsl:text/>
            </xsl:when>
            <xsl:when test="jsdl:Target">
                <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/> &lt; <xsl:text/>
            </xsl:when>
        </xsl:choose>
        <xsl:value-of select="jsdl:FileName/text()"/>
        <xsl:value-of select="$ATTRIBUTE_SEPARATOR"/>
    </xsl:template>
</xsl:stylesheet>