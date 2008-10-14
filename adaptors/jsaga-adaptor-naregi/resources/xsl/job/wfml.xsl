<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension"
                exclude-result-prefixes="jsdl posix spmd ext">
    <xsl:output method="xml" indent="yes"/>

    <!-- entry point (MUST BE RELATIVE) -->
    <xsl:template match="jsdl:JobDefinition">
        <definitions name="Workflow" xmlns="http://www.naregi.org/wfml/02">
            <activityModel>
                <activity name="hostname#1">
                    <jsdl>
                        <old_jsdl:JobDefinition xmlns:old_jsdl="http://schemas.ggf.org/jsdl/2005/06/jsdl"
                                       xmlns:naregi="http://www.naregi.org/ws/2005/08/jsdl-naregi-draft-02">
                            <xsl:apply-templates select="jsdl:JobDescription"/>
                        </old_jsdl:JobDefinition>
                    </jsdl>
                </activity>
            </activityModel>
            <compositionModel>
                <importModel/>
                <exportModel>
                    <exportedActivity>
                        <exportedActivityInfo name="Workflow"/>
                        <controlModel controlIn="Workflow">
                            <controlLink label="WFTGEN" source="hostname#1"/>
                        </controlModel>
                    </exportedActivity>
                </exportModel>
            </compositionModel>
        </definitions>
    </xsl:template>
    <xsl:template match="jsdl:*">
        <xsl:copy-of select="@*"/>
        <xsl:element name="old_jsdl:{local-name()}" namespace="http://schemas.ggf.org/jsdl/2005/06/jsdl">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="posix:POSIXApplication">
        <old_posix:POSIXApplication xmlns:old_posix="http://schemas.ggf.org/jsdl/2005/06/jsdl-posix">
            <xsl:apply-templates/>
            <!-- Executable -->
            <xsl:if test="not(posix:Executable)">
                <xsl:message terminate="yes">Missing required element: Executable</xsl:message>
            </xsl:if>
            <!-- WallTimeLimit -->
            <xsl:choose>
                <xsl:when test="posix:WallTimeLimit"/>
                <xsl:when test="ancestor::jsdl:JobDescription/jsdl:Resources/jsdl:TotalCPUTime">
                    <xsl:variable name="TotalCPUTime" select="ancestor::jsdl:JobDescription/jsdl:Resources/jsdl:TotalCPUTime/*/text()"/>
                    <xsl:variable name="TotalCPUCount">
                        <xsl:choose>
                            <xsl:when test="ancestor::jsdl:JobDescription/jsdl:Resources/jsdl:TotalCPUCount">
                                <xsl:value-of select="ancestor::jsdl:JobDescription/jsdl:Resources/jsdl:TotalCPUCount/*/text()"/>
                            </xsl:when>
                            <xsl:otherwise>1</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="CPUTime" select="$TotalCPUTime div $TotalCPUCount"/>
                    <old_posix:WallTimeLimit>
                        <xsl:value-of select="$CPUTime + 0.1*$CPUTime"/>
                    </old_posix:WallTimeLimit>
                </xsl:when>
                <xsl:otherwise>
                    <old_posix:WallTimeLimit>300</old_posix:WallTimeLimit>
                </xsl:otherwise>
            </xsl:choose>
        </old_posix:POSIXApplication>
    </xsl:template>
    <xsl:template match="posix:*">
        <xsl:copy-of select="@*"/>
        <xsl:element name="old_posix:{local-name()}" namespace="http://schemas.ggf.org/jsdl/2005/06/jsdl-posix">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="spmd:SPMDApplication">
        <spmd:SPMDApplication>
            <xsl:message terminate="yes">SPMDApplication not supported, please create a POSIXApplication instead</xsl:message>
        </spmd:SPMDApplication>
    </xsl:template>

    <!-- default template rules -->
    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="comment()">
        <xsl:comment><xsl:value-of select="."/></xsl:comment>
    </xsl:template>
</xsl:stylesheet>