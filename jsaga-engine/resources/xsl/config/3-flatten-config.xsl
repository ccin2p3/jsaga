<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.in2p3.fr/jsaga"
                xmlns:cfg="http://www.in2p3.fr/jsaga"
                exclude-result-prefixes="cfg">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <effective-config>
            <xsl:copy-of select="cfg:UNIVERSE/@*[not(local-name()='name')]"/>
            <xsl:comment> contexts </xsl:comment>
            <xsl:apply-templates select="//cfg:GRID"/>
            <xsl:comment> protocols </xsl:comment>
            <xsl:apply-templates select="//cfg:data[not(@scheme=../preceding::cfg:data/@scheme)]"/>
            <xsl:comment> execution </xsl:comment>
            <xsl:apply-templates select="//cfg:job[not(@scheme=../preceding::cfg:job/@scheme)]"/>
        </effective-config>
    </xsl:template>

    <!-- filter redondant elements -->
    <xsl:template match="cfg:fileSystem[@name=preceding-sibling::cfg:fileSystem/@name]"/>
    <xsl:template match="cfg:workerProtocolScheme[text()=preceding-sibling::cfg:workerProtocolScheme/text()]"/>

    <xsl:template match="cfg:GRID">
        <context name="{@name}" type="{@contextType}">
            <xsl:copy-of select="@deactivated"/>
            <xsl:apply-templates select="cfg:attribute"/>
        </context>
    </xsl:template>

    <xsl:template match="cfg:data">
        <protocol scheme="{@scheme}">
            <xsl:variable name="scheme" select="@scheme"/>
            <xsl:apply-templates select="cfg:schemeAlias"/>
            <!-- filter redondant elements -->
            <xsl:for-each select="//cfg:data[@scheme=$scheme and not(@scheme=preceding-sibling::cfg:data/@scheme)]">
                <dataService name="{parent::cfg:SITE/@name}" type="{@type}"
                             contextRef="{ancestor::cfg:GRID/@name}" contextType="{ancestor::cfg:GRID/@contextType}">
                    <xsl:copy-of select="@base"/>
                    <xsl:apply-templates select="parent::cfg:SITE/cfg:domain"/>
                    <xsl:apply-templates select="*[not(local-name()='schemeAlias')] | text() | comment()"/>
                </dataService>
            </xsl:for-each>
        </protocol>
    </xsl:template>

    <xsl:template match="cfg:job">
        <execution scheme="{@scheme}">
            <xsl:variable name="scheme" select="@scheme"/>
            <xsl:apply-templates select="cfg:schemeAlias"/>
            <!-- filter redondant elements -->
            <xsl:for-each select="//cfg:job[@scheme=$scheme and not(@scheme=preceding-sibling::cfg:job/@scheme)]">
                <jobService name="{parent::cfg:SITE/@name}" type="{@type}"
                            contextRef="{ancestor::cfg:GRID/@name}" contextType="{ancestor::cfg:GRID/@contextType}">
                    <xsl:apply-templates select="cfg:attribute"/>
                    <xsl:apply-templates select="parent::cfg:SITE/cfg:domain"/>

                    <xsl:apply-templates select="parent::cfg:SITE/cfg:fileSystem"/>
                    <xsl:if test="not(parent::cfg:SITE/cfg:fileSystem[@name='WorkingDirectory'])">
                        <fileSystem name="WorkingDirectory" mountPoint="$HOME"/>
                    </xsl:if>

                    <xsl:apply-templates select="cfg:monitorService"/>
                    <xsl:apply-templates select="cfg:logging"/>
                    <xsl:if test="not(cfg:logging) and @wrapperMonitoring='true'">
                        <logging impl="fr.in2p3.jsaga.engine.jobcollection.DefaultLoggingImpl"
                                >echo "L0G: ["`date "+%d/%m/%Y %T,%2N"`"] $LEVEL: $MESSAGE"</logging>
                    </xsl:if>
                    <xsl:apply-templates select="cfg:monitoring"/>
                    <xsl:if test="not(cfg:monitoring) and @wrapperMonitoring='true'">
                        <monitoring impl="fr.in2p3.jsaga.engine.jobcollection.DefaultMonitoringImpl"
                                >echo "M0N: ["`date "+%d/%m/%Y %T,%2N"`"] $STATUS"</monitoring>
                    </xsl:if>
                    <xsl:apply-templates select="cfg:accounting"/>
                    <xsl:if test="not(cfg:accounting) and @wrapperMonitoring='true'">
                        <accounting impl="fr.in2p3.jsaga.engine.jobcollection.DefaultAccountingImpl"
                                >echo "T1M: [$TIME] $FUNCTION"</accounting>
                    </xsl:if>
                    <xsl:apply-templates select="cfg:prologue"/>

                    <fileStaging>
                        <xsl:copy-of select="@defaultIntermediary"/>
                        <xsl:for-each select="ancestor::cfg:*/cfg:data">
                            <workerProtocolScheme>
                                <xsl:copy-of select="@read | @write | @recursive | @protection"/>
                                <xsl:value-of select="@scheme"/>
                            </workerProtocolScheme>
                        </xsl:for-each>
                    </fileStaging>
                </jobService>
            </xsl:for-each>
        </execution>
    </xsl:template>

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