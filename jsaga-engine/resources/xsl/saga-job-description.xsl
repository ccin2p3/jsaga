<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>

    <xsl:template match="/"># SAGA attributes<xsl:apply-templates/></xsl:template>

    <xsl:template match="jsdl:JobName">
JobName=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="jsdl:JobAnnotation">
Queue=<xsl:value-of select="text()"/></xsl:template>

    <xsl:template match="posix:Executable">
Executable=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="posix:WorkingDirectory">
WorkingDirectory=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="posix:Input">
Input=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="posix:Output">
Output=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="posix:Error">
Error=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="posix:Argument[1]">
Arguments=<xsl:value-of select="text()"/></xsl:template><xsl:template match="posix:Argument[position()>1]">,<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="posix:Environment[1]">
Environment=<xsl:value-of select="text()"/></xsl:template><xsl:template match="posix:Environment[position()>1]">,<xsl:value-of select="text()"/></xsl:template>

    <xsl:template match="spmd:NumberOfProcesses">
NumberOfProcesses=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="spmd:ProcessesPerHost">
ProcessesPerHost=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="spmd:ThreadsPerProcess">
ThreadsPerProcess=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="spmd:SPMDVariation">
SPMDVariation=<xsl:value-of select="text()"/></xsl:template>

    <xsl:template match="jsdl:FileSystem[@name='WorkingDirectory']/jsdl:MountPoint">
WorkingDirectory=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="jsdl:TotalCPUCount/jsdl:Exact">
TotalCPUCount=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="jsdl:TotalCPUTime/jsdl:UpperBoundedRange">
TotalCPUTime=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="jsdl:TotalPhysicalMemory/jsdl:UpperBoundedRange">
TotalPhysicalMemory=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="jsdl:CPUArchitectureName[1]">
CPUArchitecture=<xsl:value-of select="text()"/></xsl:template><xsl:template match="jsdl:CPUArchitectureName[position()>1]">,<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="jsdl:OperatingSystemName[1]">
OperatingSystemType=<xsl:value-of select="text()"/></xsl:template><xsl:template match="jsdl:OperatingSystemName[position()>1]">,<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="jsdl:HostName[1]">
CandidateHosts=<xsl:value-of select="text()"/></xsl:template><xsl:template match="jsdl:HostName[position()>1]">,<xsl:value-of select="text()"/></xsl:template>

    <xsl:template match="jsdl:DeleteOnTermination[1]">
Cleanup=<xsl:value-of select="text()"/></xsl:template>
    <xsl:template match="jsdl:DataStaging[1]">
FileTransfer=<xsl:call-template name="STAGING"/></xsl:template><xsl:template match="jsdl:DataStaging[position()>1]">,<xsl:call-template name="STAGING"/></xsl:template>

    <xsl:template name="STAGING">
        <xsl:variable name="op">
            <xsl:choose>
                <xsl:when test="jsdl:Source and jsdl:CreationFlag/text()='Append'">&gt;&gt;</xsl:when>
                <xsl:when test="jsdl:Source">&gt;</xsl:when>
                <xsl:when test="jsdl:Target and jsdl:CreationFlag/text()='Append'">&lt;&lt;</xsl:when>
                <xsl:when test="jsdl:Target">&lt;</xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="jsdl:*/jsdl:URI/text()"/>
        <xsl:value-of select="$op"/>
        <xsl:value-of select="jsdl:FileName/text()"/>
    </xsl:template>

    <xsl:template match="text()"/>
</xsl:stylesheet>