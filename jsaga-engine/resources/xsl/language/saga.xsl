<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="xml" indent="yes"/>
    <xsl:variable name="id">
        <xsl:choose>
            <xsl:when test="/attributes/JobName/@value">
                <xsl:value-of select="/attributes/JobName/@value"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="generate-id(/attributes)"/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:template match="/">
        <ext:JobCollection>
            <ext:JobCollectionDescription>
                <ext:JobCollectionIdentification>
                    <ext:JobCollectionName><xsl:value-of select="$id"/></ext:JobCollectionName>
                </ext:JobCollectionIdentification>
                <ext:Parametric start="1" step="1" count="1"/>
            </ext:JobCollectionDescription>
            <ext:Job>
                <xsl:apply-templates select="attributes"/>
            </ext:Job>
        </ext:JobCollection>
    </xsl:template>

    <xsl:template match="attributes">
        <!-- check unsupported attributes -->
        <xsl:if test="Interactive/@value='true'">
            <xsl:message terminate="yes">Interactive jobs are currently not supported by JSAGA</xsl:message>
        </xsl:if>
        <xsl:if test="JobStartTime/@value">
            <xsl:message terminate="yes">Unsupported attribute key: JobStartTime</xsl:message>
        </xsl:if>
        <xsl:if test="JobContact/value/text()">
            <xsl:message terminate="yes">Unsupported attribute key: JobContact</xsl:message>
        </xsl:if>

        <!-- job definition -->
        <jsdl:JobDefinition>
            <jsdl:JobDescription>
                <jsdl:JobIdentification>
                    <jsdl:JobName><xsl:value-of select="$id"/></jsdl:JobName>
                    <xsl:for-each select="Queue/@value">
                        <jsdl:JobAnnotation><xsl:value-of select="."/></jsdl:JobAnnotation>
                    </xsl:for-each>
                </jsdl:JobIdentification>
                <jsdl:Application>
                    <posix:POSIXApplication>
                        <xsl:for-each select="Executable/@value">
                            <posix:Executable filesystemName="WorkingDirectory"><xsl:value-of select="."/></posix:Executable>
                        </xsl:for-each>
                        <xsl:for-each select="WorkingDirectory/@value">
                            <posix:WorkingDirectory filesystemName="WorkingDirectory"><xsl:value-of select="."/></posix:WorkingDirectory>
                        </xsl:for-each>
                        <xsl:for-each select="Input/@value">
                            <posix:Input filesystemName="WorkingDirectory"><xsl:value-of select="."/></posix:Input>
                        </xsl:for-each>
                        <xsl:for-each select="Output/@value">
                            <posix:Output filesystemName="WorkingDirectory"><xsl:value-of select="."/></posix:Output>
                        </xsl:for-each>
                        <xsl:for-each select="Error/@value">
                            <posix:Error filesystemName="WorkingDirectory"><xsl:value-of select="."/></posix:Error>
                        </xsl:for-each>
                        <xsl:for-each select="Arguments/value/text()">
                            <posix:Argument><xsl:value-of select="."/></posix:Argument>
                        </xsl:for-each>
                        <xsl:for-each select="Environment/value/text()">
                            <posix:Environment name="{substring-before(.,'=')}"><xsl:value-of select="substring-after(.,'=')"/></posix:Environment>
                        </xsl:for-each>
                    </posix:POSIXApplication>
                    <xsl:if test="NumberOfProcesses | ProcessesPerHost | ThreadsPerProcess | SPMDVariation">
                        <spmd:SPMDApplication>
                            <xsl:for-each select="NumberOfProcesses/@value">
                                <spmd:NumberOfProcesses><xsl:value-of select="."/></spmd:NumberOfProcesses>
                            </xsl:for-each>
                            <xsl:for-each select="ProcessesPerHost/@value">
                                <spmd:ProcessesPerHost><xsl:value-of select="."/></spmd:ProcessesPerHost>
                            </xsl:for-each>
                            <xsl:for-each select="ThreadsPerProcess/@value">
                                <spmd:ThreadsPerProcess><xsl:value-of select="."/></spmd:ThreadsPerProcess>
                            </xsl:for-each>
                            <xsl:for-each select="SPMDVariation/@value">
                                <spmd:SPMDVariation><xsl:value-of select="."/></spmd:SPMDVariation>
                            </xsl:for-each>
                        </spmd:SPMDApplication>
                    </xsl:if>
                </jsdl:Application>
                <jsdl:Resources>
                    <jsdl:FileSystem name="WorkingDirectory">
                        <jsdl:FileSystemType>temporary</jsdl:FileSystemType>
                        <jsdl:MountPoint><xsl:value-of select="."/></jsdl:MountPoint>
                    </jsdl:FileSystem>
                    <xsl:for-each select="TotalCPUCount/@value">
                        <jsdl:TotalCPUCount>
                            <jsdl:Exact><xsl:value-of select="."/></jsdl:Exact>
                        </jsdl:TotalCPUCount>
                    </xsl:for-each>
                    <xsl:for-each select="TotalCPUTime/@value">
                        <jsdl:TotalCPUTime>
                            <jsdl:UpperBoundedRange exclusiveBound="false"><xsl:value-of select="."/></jsdl:UpperBoundedRange>
                        </jsdl:TotalCPUTime>
                    </xsl:for-each>
                    <xsl:for-each select="TotalPhysicalMemory/@value">
                        <jsdl:TotalPhysicalMemory>
                            <jsdl:UpperBoundedRange exclusiveBound="false"><xsl:value-of select="."/></jsdl:UpperBoundedRange>
                        </jsdl:TotalPhysicalMemory>
                    </xsl:for-each>
                    <xsl:for-each select="CPUArchitecture/@value"><!-- deviation from SAGA specification (for consistency with JSDL) -->
                        <jsdl:CPUArchitecture>
                            <jsdl:CPUArchitectureName><xsl:value-of select="."/></jsdl:CPUArchitectureName>
                        </jsdl:CPUArchitecture>
                    </xsl:for-each>
                    <xsl:for-each select="OperatingSystemType/@value"><!-- deviation from SAGA specification (for consistency with JSDL) -->
                        <jsdl:OperatingSystem>
                            <jsdl:OperatingSystemType>
                                <jsdl:OperatingSystemName><xsl:value-of select="."/></jsdl:OperatingSystemName>
                            </jsdl:OperatingSystemType>
                        </jsdl:OperatingSystem>
                    </xsl:for-each>
                    <xsl:for-each select="CandidateHosts">
                        <jsdl:CandidateHosts>
                            <xsl:for-each select="value/text()">
                                <jsdl:HostName><xsl:value-of select="."/></jsdl:HostName>
                            </xsl:for-each>
                        </jsdl:CandidateHosts>
                    </xsl:for-each>
                </jsdl:Resources>
                <xsl:for-each select="FileTransfer/value/text()">
                    <jsdl:DataStaging>
                        <xsl:choose>
                            <xsl:when test="contains(.,'>>')">
                                <jsdl:FileName><xsl:value-of select="substring-after(.,'>>')"/></jsdl:FileName>
                                <jsdl:FilesystemName>WorkingDirectory</jsdl:FilesystemName>
                                <jsdl:CreationFlag>Append</jsdl:CreationFlag>
                                <xsl:call-template name="CLEANUP"/>
                                <jsdl:Source>
                                    <jsdl:URI><xsl:value-of select="substring-before(.,'>>')"/></jsdl:URI>
                                </jsdl:Source>
                            </xsl:when>
                            <xsl:when test="contains(.,'>')">
                                <jsdl:FileName><xsl:value-of select="substring-after(.,'>')"/></jsdl:FileName>
                                <jsdl:FilesystemName>WorkingDirectory</jsdl:FilesystemName>
                                <jsdl:CreationFlag>Overwrite</jsdl:CreationFlag>
                                <xsl:call-template name="CLEANUP"/>
                                <jsdl:Source>
                                    <jsdl:URI><xsl:value-of select="substring-before(.,'>')"/></jsdl:URI>
                                </jsdl:Source>
                            </xsl:when>
                            <xsl:when test="contains(.,'&lt;&lt;')">
                                <jsdl:FileName><xsl:value-of select="substring-after(.,'&lt;&lt;')"/></jsdl:FileName>
                                <jsdl:FilesystemName>WorkingDirectory</jsdl:FilesystemName>
                                <jsdl:CreationFlag>Append</jsdl:CreationFlag>
                                <xsl:call-template name="CLEANUP"/>
                                <jsdl:Target>
                                    <jsdl:URI><xsl:value-of select="substring-before(.,'&lt;&lt;')"/></jsdl:URI>
                                </jsdl:Target>
                            </xsl:when>
                            <xsl:when test="contains(.,'&lt;')">
                                <jsdl:FileName><xsl:value-of select="substring-after(.,'&lt;')"/></jsdl:FileName>
                                <jsdl:FilesystemName>WorkingDirectory</jsdl:FilesystemName>
                                <jsdl:CreationFlag>Overwrite</jsdl:CreationFlag>
                                <xsl:call-template name="CLEANUP"/>
                                <jsdl:Target>
                                    <jsdl:URI><xsl:value-of select="substring-before(.,'&lt;')"/></jsdl:URI>
                                </jsdl:Target>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:message terminate="yes">BadParameter: syntax error for attribute FileTransfer: <xsl:value-of select="."/></xsl:message>
                            </xsl:otherwise>
                        </xsl:choose>
                    </jsdl:DataStaging>
                </xsl:for-each>
            </jsdl:JobDescription>
        </jsdl:JobDefinition>
    </xsl:template>

    <xsl:template name="CLEANUP">
        <xsl:for-each select="/attributes/Cleanup/@value">
            <jsdl:DeleteOnTermination><xsl:value-of select="."/></jsdl:DeleteOnTermination>
        </xsl:for-each>        
    </xsl:template>
</xsl:stylesheet>