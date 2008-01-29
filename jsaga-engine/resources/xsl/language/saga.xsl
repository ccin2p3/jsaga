<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/attributes">
        <!-- check unsupported attributes -->
        <xsl:if test="attribute[@name='Interactive']/@value='true'">
            <xsl:message terminate="yes">Unsupported value for attribute key: Interactive</xsl:message>
        </xsl:if>
        <xsl:if test="attribute[@name='JobStartTime']/@value">
            <xsl:message terminate="yes">Unsupported attribute key: JobStartTime</xsl:message>
        </xsl:if>
        <xsl:if test="vectorAttribute[@name='JobContact']/value/text()">
            <xsl:message terminate="yes">Unsupported attribute key: JobContact</xsl:message>
        </xsl:if>
        <xsl:if test="attribute[@name='Queue']/@value">
            <!-- TODO -->
            <xsl:message terminate="yes">Not implemented yet: Queue</xsl:message>
        </xsl:if>

        <!-- job definition -->
        <jsdl:JobDefinition>
            <jsdl:JobDescription>
                <jsdl:JobIdentification>
                    <xsl:for-each select="attribute[@name='JobName']/@value">
                        <jsdl:JobName><xsl:value-of select="."/></jsdl:JobName>
                    </xsl:for-each>
                </jsdl:JobIdentification>
                <jsdl:Application>
                    <posix:POSIXApplication>
                        <xsl:for-each select="attribute[@name='Executable']/@value">
                            <posix:Executable filesystemName="WorkingDirectory"><xsl:value-of select="."/></posix:Executable>
                        </xsl:for-each>
                        <xsl:for-each select="attribute[@name='Input']/@value">
                            <posix:Input filesystemName="WorkingDirectory"><xsl:value-of select="."/></posix:Input>
                        </xsl:for-each>
                        <xsl:for-each select="attribute[@name='Output']/@value">
                            <posix:Output filesystemName="WorkingDirectory"><xsl:value-of select="."/></posix:Output>
                        </xsl:for-each>
                        <xsl:for-each select="attribute[@name='Error']/@value">
                            <posix:Error filesystemName="WorkingDirectory"><xsl:value-of select="."/></posix:Error>
                        </xsl:for-each>
                        <xsl:for-each select="vectorAttribute[@name='Arguments']/value/text()">
                            <posix:Argument><xsl:value-of select="."/></posix:Argument>
                        </xsl:for-each>
                        <xsl:for-each select="vectorAttribute[@name='Environment']/value/text()">
                            <posix:Environment name="{substring-before(.,'=')}"><xsl:value-of select="substring-after(.,'=')"/></posix:Environment>
                        </xsl:for-each>
                    </posix:POSIXApplication>
                    <spmd:SPMDApplication>
                        <xsl:for-each select="attribute[@name='NumberOfProcesses']/@value">
                            <spmd:NumberOfProcesses><xsl:value-of select="."/></spmd:NumberOfProcesses>
                        </xsl:for-each>
                        <xsl:for-each select="attribute[@name='ProcessesPerHost']/@value">
                            <spmd:ProcessesPerHost><xsl:value-of select="."/></spmd:ProcessesPerHost>
                        </xsl:for-each>
                        <xsl:for-each select="attribute[@name='ThreadsPerProcess']/@value">
                            <spmd:ThreadsPerProcess><xsl:value-of select="."/></spmd:ThreadsPerProcess>
                        </xsl:for-each>
                        <xsl:for-each select="attribute[@name='SPMDVariation']/@value">
                            <spmd:SPMDVariation><xsl:value-of select="."/></spmd:SPMDVariation>
                        </xsl:for-each>
                    </spmd:SPMDApplication>
                </jsdl:Application>
                <jsdl:Resources>
                    <xsl:for-each select="attribute[@name='WorkingDirectory']/@value">
                        <jsdl:FileSystem name="WorkingDirectory">
                            <jsdl:FileSystemType>temporary</jsdl:FileSystemType>
                            <jsdl:MountPoint><xsl:value-of select="."/></jsdl:MountPoint>
                        </jsdl:FileSystem>
                    </xsl:for-each>
                    <xsl:for-each select="attribute[@name='TotalCPUCount']/@value">
                        <jsdl:TotalCPUCount>
                            <jsdl:Exact><xsl:value-of select="."/></jsdl:Exact>
                        </jsdl:TotalCPUCount>
                    </xsl:for-each>
                    <xsl:for-each select="attribute[@name='TotalCPUTime']/@value">
                        <jsdl:TotalCPUTime>
                            <jsdl:UpperBoundedRange exclusiveBound="false"><xsl:value-of select="."/></jsdl:UpperBoundedRange>
                        </jsdl:TotalCPUTime>
                    </xsl:for-each>
                    <xsl:for-each select="attribute[@name='TotalPhysicalMemory']/@value">
                        <jsdl:TotalPhysicalMemory>
                            <jsdl:UpperBoundedRange exclusiveBound="false"><xsl:value-of select="."/></jsdl:UpperBoundedRange>
                        </jsdl:TotalPhysicalMemory>
                    </xsl:for-each>
                    <xsl:for-each select="vectorAttribute[@name='CPUArchitecture']/value/text()">
                        <jsdl:CPUArchitecture>
                            <jsdl:CPUArchitectureName><xsl:value-of select="."/></jsdl:CPUArchitectureName>
                        </jsdl:CPUArchitecture>
                    </xsl:for-each>
                    <xsl:for-each select="vectorAttribute[@name='OperatingSystemType']/value/text()">
                        <jsdl:OperatingSystem>
                            <jsdl:OperatingSystemType>
                                <jsdl:OperatingSystemName><xsl:value-of select="."/></jsdl:OperatingSystemName>
                            </jsdl:OperatingSystemType>
                        </jsdl:OperatingSystem>
                    </xsl:for-each>
                    <xsl:for-each select="vectorAttribute[@name='CandidateHosts']">
                        <jsdl:CandidateHosts>
                            <xsl:for-each select="value/text()">
                                <jsdl:HostName><xsl:value-of select="."/></jsdl:HostName>
                            </xsl:for-each>
                        </jsdl:CandidateHosts>
                    </xsl:for-each>
                </jsdl:Resources>
                <xsl:for-each select="vectorAttribute[@name='FileTransfer']/value/text()">
                    <jsdl:DataStaging>
                        <xsl:choose>
                            <xsl:when test="contains(.,'>>')">
                                <jsdl:Source>
                                    <jsdl:URI><xsl:value-of select="substring-before(.,'>>')"/></jsdl:URI>
                                </jsdl:Source>
                                <jsdl:FileName><xsl:value-of select="substring-after(.,'>>')"/></jsdl:FileName>
                                <jsdl:CreationFlag>Append</jsdl:CreationFlag>
                            </xsl:when>
                            <xsl:when test="contains(.,'>')">
                                <jsdl:Source>
                                    <jsdl:URI><xsl:value-of select="substring-before(.,'>')"/></jsdl:URI>
                                </jsdl:Source>
                                <jsdl:FileName><xsl:value-of select="substring-after(.,'>')"/></jsdl:FileName>
                                <jsdl:CreationFlag>Overwrite</jsdl:CreationFlag>
                            </xsl:when>
                            <xsl:when test="contains(.,'&lt;&lt;')">
                                <jsdl:Target>
                                    <jsdl:URI><xsl:value-of select="substring-before(.,'&lt;&lt;')"/></jsdl:URI>
                                </jsdl:Target>
                                <jsdl:FileName><xsl:value-of select="substring-after(.,'&lt;&lt;')"/></jsdl:FileName>
                                <jsdl:CreationFlag>Append</jsdl:CreationFlag>
                            </xsl:when>
                            <xsl:when test="contains(.,'&lt;')">
                                <jsdl:Target>
                                    <jsdl:URI><xsl:value-of select="substring-before(.,'&lt;')"/></jsdl:URI>
                                </jsdl:Target>
                                <jsdl:FileName><xsl:value-of select="substring-after(.,'&lt;')"/></jsdl:FileName>
                                <jsdl:CreationFlag>Overwrite</jsdl:CreationFlag>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:message terminate="yes">BadParameter: syntax error for attribute FileTransfer: <xsl:value-of select="."/></xsl:message>
                            </xsl:otherwise>
                        </xsl:choose>
                        <jsdl:FilesystemName>WorkingDirectory</jsdl:FilesystemName>
                        <xsl:for-each select="/attributes/attribute[@name='Cleanup']/@value">
                            <jsdl:DeleteOnTermination><xsl:value-of select="."/></jsdl:DeleteOnTermination>
                        </xsl:for-each>
                    </jsdl:DataStaging>
                </xsl:for-each>
            </jsdl:JobDescription>
        </jsdl:JobDefinition>
    </xsl:template>
</xsl:stylesheet>