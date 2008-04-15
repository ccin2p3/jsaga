<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.in2p3.fr/jsaga"
                xmlns:cfg="http://www.in2p3.fr/jsaga"
                exclude-result-prefixes="cfg">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:param name="ignore.missing.adaptor" select="'true'"/>
    <xsl:variable name="descriptors" select="document('descriptors.xml')/*"/>

    <xsl:template match="/">
        <xsl:variable name="cfg" select="/*"/>
        <effective-config>
            <xsl:copy-of select="$descriptors/@*[not(name()=name($cfg/@*))] | $cfg/@*"/>
            <xsl:comment> languages </xsl:comment>
            <xsl:apply-templates select="$descriptors/cfg:language"/>
            <xsl:comment> security </xsl:comment>
            <xsl:apply-templates select="$cfg/cfg:context[not(@deactivated='true')]"/>
            <xsl:copy-of select="$descriptors/cfg:context[not(@type=$cfg/cfg:context/@type)]"/>
            <xsl:comment> protocols </xsl:comment>
            <xsl:apply-templates select="$cfg/cfg:protocol[not(@deactivated='true')]"/>
            <xsl:copy-of select="$descriptors/cfg:protocol[not(cfg:dataService/@type=$cfg/cfg:protocol/cfg:dataService/@type)]"/>
            <xsl:comment> execution </xsl:comment>
            <xsl:apply-templates select="$cfg/cfg:execution[not(@deactivated='true')]"/>
            <xsl:copy-of select="$descriptors/cfg:execution[not(cfg:jobService/@type=$cfg/cfg:execution/cfg:jobService/@type)]"/>
        </effective-config>
    </xsl:template>

    <xsl:template match="cfg:context">
        <xsl:variable name="conf" select="."/>
        <xsl:variable name="desc" select="$descriptors/cfg:context[@type=$conf/@type]"/>
        <xsl:choose>
            <xsl:when test="not($desc) and $ignore.missing.adaptor='true'">
                <xsl:message terminate="no">Missing plugin for: <xsl:value-of select="@type"/></xsl:message>
            </xsl:when>
            <xsl:when test="not($desc) and $ignore.missing.adaptor='false'">
                <xsl:message terminate="yes">Missing plugin for: <xsl:value-of select="@type"/></xsl:message>
            </xsl:when>
            <xsl:otherwise>
                <context>
                    <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
                    <xsl:apply-templates select="$desc/cfg:attribute[not(@name=$conf/cfg:attribute/@name)] | $conf/cfg:attribute"/>
                </context>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="cfg:protocol">
        <xsl:variable name="conf" select="."/>
        <xsl:variable name="desc" select="$descriptors/cfg:protocol[cfg:dataService/@type=$conf/cfg:dataService[1]/@type]"/>
        <xsl:choose>
            <xsl:when test="not($desc) and $ignore.missing.adaptor='true'">
                <xsl:message terminate="no">Missing plugin for: <xsl:value-of select="cfg:dataService[1]/@type"/></xsl:message>
            </xsl:when>
            <xsl:when test="not($desc) and $ignore.missing.adaptor='false'">
                <xsl:message terminate="yes">Missing plugin for: <xsl:value-of select="cfg:dataService[1]/@type"/></xsl:message>                
            </xsl:when>
            <xsl:otherwise>
                <protocol>
                    <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
                    <xsl:apply-templates select="$conf/cfg:schemeAlias"/>
                    <xsl:apply-templates select="$conf/cfg:dataService[@type=$descriptors/cfg:protocol/cfg:dataService/@type]"/>
                    <!-- add default if all context types are filtered -->
                    <xsl:copy-of select="$desc/cfg:dataService[not(cfg:supportedContextType=$conf/cfg:dataService/@contextType)]"/>
                </protocol>
            </xsl:otherwise>
        </xsl:choose>
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
        <xsl:choose>
            <xsl:when test="not($desc) and $ignore.missing.adaptor='true'">
                <xsl:message terminate="no">Missing plugin for: <xsl:value-of select="cfg:jobService[1]/@type"/></xsl:message>
            </xsl:when>
            <xsl:when test="not($desc) and $ignore.missing.adaptor='false'">
                <xsl:message terminate="yes">Missing plugin for: <xsl:value-of select="cfg:jobService[1]/@type"/></xsl:message>                
            </xsl:when>
            <xsl:otherwise>
                <execution>
                    <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
                    <xsl:apply-templates select="$conf/cfg:schemeAlias"/>
                    <xsl:apply-templates select="$conf/cfg:jobService[@type=$descriptors/cfg:execution/cfg:jobService/@type]"/>
                    <!-- add default if all context types are filtered -->
                    <xsl:copy-of select="$desc/cfg:jobService[not(cfg:supportedContextType=$conf/cfg:jobService/@contextType)]"/>
                </execution>                
            </xsl:otherwise>
        </xsl:choose>
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
                <xsl:apply-templates select="$desc/cfg:monitorService">
                    <xsl:with-param name="conf" select="$conf/cfg:monitorService"/>
                </xsl:apply-templates>
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
    <xsl:template match="cfg:monitorService">
        <xsl:param name="conf"/>
        <xsl:variable name="desc" select="."/>
        <monitorService>
            <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
            <xsl:apply-templates select="$desc/cfg:attribute[not(@name=$conf/cfg:attribute/@name)] | $conf/cfg:attribute"/>
        </monitorService>
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