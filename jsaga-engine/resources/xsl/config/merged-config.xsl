<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.in2p3.fr/jsaga"
                xmlns:cfg="http://www.in2p3.fr/jsaga"
                exclude-result-prefixes="cfg">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:variable name="descriptors" select="document('descriptors.xml')/*"/>
    <xsl:variable name="configuration" select="/*"/>

    <xsl:template match="/">
        <effective-config>
            <xsl:copy-of select="$descriptors/@*"/>
            <xsl:copy-of select="$configuration/@*"/><!-- may overwrite $descriptors attributes -->
            <xsl:comment> languages </xsl:comment>
            <xsl:apply-templates select="$descriptors/cfg:language"/>
            <xsl:comment> security </xsl:comment>
            <xsl:apply-templates select="$configuration/cfg:contextInstance[@type=$descriptors/cfg:contextInstance/@type and not(@deactivated='true')]"/>
            <xsl:apply-templates select="$descriptors/cfg:contextInstance[not(@type=$configuration/cfg:contextInstance/@type)]"/>
            <xsl:comment> protocols </xsl:comment>
            <xsl:apply-templates select="$descriptors/cfg:protocol"/>
            <xsl:comment> job services </xsl:comment>
            <xsl:apply-templates select="$configuration/cfg:jobservice[@type=$descriptors/cfg:jobservice/@type]"/>
            <xsl:apply-templates select="$descriptors/cfg:jobservice[not(@type=$configuration/cfg:jobservice/@type)]"/>
        </effective-config>
    </xsl:template>

    <xsl:template match="cfg:contextInstance">
        <xsl:variable name="conf" select="."/>
        <xsl:variable name="desc" select="$descriptors/cfg:contextInstance[@type=$conf/@type]"/>
        <contextInstance>
            <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
            <xsl:apply-templates select="$desc/cfg:attribute[not(@name=$conf/cfg:attribute/@name)] | $conf/cfg:attribute"/>
            <xsl:apply-templates select="$desc/cfg:init"/>
        </contextInstance>
    </xsl:template>

    <xsl:template match="cfg:protocol">
        <xsl:variable name="desc" select="."/>
        <xsl:variable name="conf" select="$configuration/cfg:protocol[@scheme=$desc/@scheme]"/>
        <protocol>
            <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
            <xsl:apply-templates select="$desc/cfg:attribute[not(@name=$conf/cfg:attribute/@name)] | $conf/cfg:attribute"/>
            <xsl:apply-templates select="$desc/cfg:schemeAlias[not(text()=$conf/cfg:schemeAlias/text())] | $conf/cfg:schemeAlias"/>
            <xsl:apply-templates select="$desc/cfg:supportedContextType"/>
            <xsl:apply-templates select="$conf/cfg:domain"/>
            <xsl:apply-templates select="$conf/cfg:contextInstanceRef"/>
            <xsl:for-each select="$conf/cfg:contextInstanceRef/@name">
                <xsl:variable name="name" select="."/>
                <xsl:if test="not($configuration/cfg:contextInstance[@name=$name]/@type = $desc/cfg:supportedContextType/text())">
                    <xsl:message terminate="yes">
                        <xsl:text>ERROR: Protocol '</xsl:text><xsl:value-of select="$desc/@scheme"/>
                        <xsl:text>' does not support this context: </xsl:text><xsl:value-of select="$name"/>
                    </xsl:message>
                </xsl:if>
            </xsl:for-each>
        </protocol>
    </xsl:template>

    <xsl:template match="cfg:jobservice">
        <xsl:variable name="conf" select="."/>
        <xsl:variable name="desc" select="$descriptors/cfg:jobservice[@type=$conf/@type]"/>
        <jobservice>
            <xsl:copy-of select="$desc/@*[not(name()=name($conf/@*))] | $conf/@*"/>
            <xsl:apply-templates select="$desc/cfg:attribute[not(@name=$conf/cfg:attribute/@name)] | $conf/cfg:attribute"/>
            <xsl:apply-templates select="$conf/cfg:pathAlias"/>
            <xsl:apply-templates select="$desc/cfg:monitor[not($conf/cfg:monitor)] | $conf/cfg:monitor"/>
            <xsl:apply-templates select="$desc/cfg:supportedContextType"/>
            <xsl:apply-templates select="$desc/cfg:supportedProtocolScheme"/>
            <xsl:apply-templates select="$conf/cfg:sandbox"/>
            <xsl:apply-templates select="$conf/cfg:protocolRef"/>
            <xsl:apply-templates select="$conf/cfg:contextInstanceRef"/>
            <xsl:for-each select="$conf/cfg:contextInstanceRef/@name">
                <xsl:variable name="name" select="."/>
                <xsl:if test="not($configuration/cfg:contextInstance[@name=$name]/@type = $desc/cfg:supportedContextType/text())">
                    <xsl:message terminate="yes">
                        <xsl:text>ERROR: Job service '</xsl:text><xsl:value-of select="$desc/@scheme"/>
                        <xsl:text>' does not support this context: </xsl:text><xsl:value-of select="$name"/>
                    </xsl:message>
                </xsl:if>
            </xsl:for-each>
        </jobservice>
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