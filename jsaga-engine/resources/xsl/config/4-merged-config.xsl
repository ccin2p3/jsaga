<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.in2p3.fr/jsaga"
                xmlns:cfg="http://www.in2p3.fr/jsaga"
                exclude-result-prefixes="cfg">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:variable name="descriptors" select="document('descriptors.xml')/*"/>

    <xsl:template match="/">
        <xsl:variable name="cfg" select="/*"/>
        <effective-config>
            <xsl:copy-of select="$descriptors/@*[not(name()=name($cfg/@*))] | $cfg/@*"/>
            <xsl:comment> languages </xsl:comment>
            <xsl:apply-templates select="$descriptors/cfg:language"/>
            <xsl:comment> security </xsl:comment>
            <xsl:apply-templates select="$cfg/cfg:context[@type=$descriptors/cfg:context/@type and not(@deactivated='true')]"/>
            <xsl:copy-of select="$descriptors/cfg:context[not(@type=$cfg/cfg:context/@type)]"/>
            <xsl:comment> protocols </xsl:comment>
            <xsl:apply-templates select="$cfg/cfg:protocol[cfg:dataService/@type=$descriptors/cfg:protocol/cfg:dataService/@type]"/>
            <xsl:copy-of select="$descriptors/cfg:protocol[not(cfg:dataService/@type=$cfg/cfg:protocol/cfg:dataService/@type)]"/>
            <xsl:comment> execution </xsl:comment>
            <xsl:apply-templates select="$cfg/cfg:execution[cfg:jobService/@type=$descriptors/cfg:execution/cfg:jobService/@type]"/>
            <xsl:copy-of select="$descriptors/cfg:execution[not(cfg:jobService/@type=$cfg/cfg:execution/cfg:jobService/@type)]"/>
        </effective-config>
    </xsl:template>

    <xsl:template match="cfg:context">
        <xsl:variable name="conf" select="."/>
        <xsl:variable name="desc" select="$descriptors/cfg:context[@type=$conf/@type]"/>
        <context>
            <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
            <xsl:apply-templates select="$desc/cfg:attribute[not(@name=$conf/cfg:attribute/@name)] | $conf/cfg:attribute"/>
        </context>
    </xsl:template>

    <xsl:template match="cfg:protocol">
        <xsl:variable name="conf" select="."/>
        <xsl:variable name="desc" select="$descriptors/cfg:protocol[cfg:dataService/@type=$conf/cfg:dataService[1]/@type]"/>
        <protocol>
            <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
            <xsl:apply-templates select="$conf/cfg:schemeAlias"/>
            <xsl:apply-templates select="$conf/cfg:dataService[@type=$descriptors/cfg:protocol/cfg:dataService/@type]"/>
            <!-- add default if all context types are filtered -->
            <xsl:copy-of select="$desc/cfg:dataService[not(cfg:supportedContextType=$conf/cfg:dataService/@contextType)]"/>
        </protocol>
    </xsl:template>
    <xsl:template match="cfg:dataService">
        <xsl:variable name="conf" select="."/>
        <xsl:variable name="desc" select="$descriptors/cfg:protocol/cfg:dataService[@type=$conf/@type]"/>
        <!-- filter unsupported context types -->
        <xsl:if test="$conf/@contextType = $desc/cfg:supportedContextType">
            <dataService>
                <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*[not(name()='contextType')]"/>
                <xsl:apply-templates select="$conf/cfg:domain"/>
                <xsl:apply-templates select="$desc/cfg:attribute[not(@name=$conf/cfg:attribute/@name)] | $conf/cfg:attribute"/>
                <xsl:apply-templates select="$desc/cfg:supportedContextType"/>
            </dataService>
        </xsl:if>
    </xsl:template>

    <xsl:template match="cfg:execution">
        <xsl:variable name="conf" select="."/>
        <xsl:variable name="desc" select="$descriptors/cfg:execution[cfg:jobService/@type=$conf/cfg:jobService[1]/@type]"/>
        <execution>
            <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
            <xsl:apply-templates select="$conf/cfg:schemeAlias"/>
            <xsl:apply-templates select="$conf/cfg:jobService[@type=$descriptors/cfg:execution/cfg:jobService/@type]"/>
            <!-- add default if all context types are filtered -->
            <xsl:copy-of select="$desc/cfg:jobService[not(cfg:supportedContextType=$conf/cfg:jobService/@contextType)]"/>
        </execution>
    </xsl:template>
    <xsl:template match="cfg:jobService">
        <xsl:variable name="conf" select="."/>
        <xsl:variable name="desc" select="$descriptors/cfg:execution/cfg:jobService[@type=$conf/@type]"/>
        <!-- filter unsupported context types -->
        <xsl:if test="$conf/@contextType = $desc/cfg:supportedContextType">
            <jobService>
                <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*[not(name()='contextType')]"/>
                <xsl:apply-templates select="$conf/cfg:domain"/>
                <xsl:apply-templates select="$desc/cfg:attribute[not(@name=$conf/cfg:attribute/@name)] | $conf/cfg:attribute"/>
                <xsl:apply-templates select="$desc/cfg:monitor[not($conf/cfg:monitor)] | $conf/cfg:monitor"/>
                <xsl:apply-templates select="$desc/cfg:supportedContextType"/>
                <xsl:for-each select="$conf/cfg:fileStaging">
                    <fileStaging>
                        <xsl:copy-of select="@*"/>
                        <xsl:apply-templates select="$desc/cfg:fileStaging/cfg:supportedProtocolScheme"/>
                        <!-- filter unsupported schemes -->
                        <xsl:apply-templates select="cfg:workerIn[not(@scheme=$desc/cfg:fileStaging/cfg:supportedProtocolScheme)]"/>
                        <xsl:apply-templates select="cfg:workerOut[not(@scheme=$desc/cfg:fileStaging/cfg:supportedProtocolScheme)]"/>
                    </fileStaging>
                </xsl:for-each>
            </jobService>
        </xsl:if>
    </xsl:template>

    <xsl:template match="cfg:attribute">
        <attribute>
            <xsl:copy-of select="@*"/>
            <xsl:if test="not(@source)">
                <xsl:attribute name="source">EngineConfiguration</xsl:attribute>
            </xsl:if>
        </attribute>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>