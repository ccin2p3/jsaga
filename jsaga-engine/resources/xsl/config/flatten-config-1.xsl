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
            <xsl:copy-of select="/cfg:config/@*"/>
            <xsl:comment> security </xsl:comment>
            <xsl:apply-templates select="/cfg:config/cfg:security/cfg:context/cfg:instance"/>
            <xsl:comment> protocols </xsl:comment>
            <xsl:apply-templates select="/cfg:config/cfg:security/cfg:context/cfg:instance/cfg:matchProtocol[not(@scheme=preceding::cfg:matchProtocol/@scheme)]"/>
            <xsl:comment> job services </xsl:comment>
            <xsl:apply-templates select="//cfg:JOBSERVICE"/>
        </effective-config>
    </xsl:template>

    <xsl:template match="cfg:instance">
        <contextInstance>
            <xsl:call-template name="ATTRIBUTE_CONTEXT_NAME"/>
            <xsl:copy-of select="../@*"/>
            <xsl:variable name="this" select="."/>
            <xsl:for-each select="../cfg:attribute[not(@name=$this/cfg:attribute/@name)] | cfg:attribute">
                <attribute><xsl:copy-of select="@*"/></attribute>
            </xsl:for-each>
        </contextInstance>
    </xsl:template>

    <xsl:template match="cfg:matchProtocol">
        <xsl:variable name="this" select="."/>
        <protocol scheme="{@scheme}">
            <xsl:apply-templates select="/cfg:config/cfg:security/cfg:context[not(@deactivated='true')]/cfg:instance[cfg:matchProtocol/@scheme=$this/@scheme]/cfg:domain"/>
            <xsl:for-each select="/cfg:config/cfg:security/cfg:context[not(@deactivated='true')]/cfg:instance[cfg:matchProtocol/@scheme=$this/@scheme]">
                <contextInstanceRef><xsl:call-template name="ATTRIBUTE_CONTEXT_NAME"/></contextInstanceRef>
            </xsl:for-each>
        </protocol>
    </xsl:template>
    <xsl:template match="cfg:domain">
        <domain name="{@name}">
            <xsl:apply-templates select="cfg:host"/>
            <xsl:for-each select="..">
                <contextInstanceRef><xsl:call-template name="ATTRIBUTE_CONTEXT_NAME"/></contextInstanceRef>
            </xsl:for-each>
        </domain>
    </xsl:template>
    <xsl:template match="cfg:host">
        <host name="{@name}">
            <xsl:for-each select="../..">
                <contextInstanceRef><xsl:call-template name="ATTRIBUTE_CONTEXT_NAME"/></contextInstanceRef>
            </xsl:for-each>
        </host>
    </xsl:template>

    <xsl:template match="cfg:JOBSERVICE">
        <xsl:variable name="scheme">
            <xsl:choose>
                <xsl:when test="@scheme"><xsl:value-of select="@scheme"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="path">
            <xsl:for-each select="ancestor::cfg:GRID[@name]">
                <xsl:value-of select="@name"/><xsl:text>/</xsl:text>
            </xsl:for-each>
            <xsl:value-of select="$scheme"/>
        </xsl:variable>
        <jobservice scheme="{$scheme}" type="@type" path="{$path}">
            <xsl:copy-of select="@*"/>
            <xsl:for-each select="cfg:attribute">
                <attribute><xsl:copy-of select="@*"/></attribute>
            </xsl:for-each>
            <xsl:call-template name="SANDBOX_CONTAINER"/>
            <xsl:call-template name="PROTOCOL_CONTAINER"/>
            <xsl:for-each select="/cfg:config/cfg:security/cfg:context[not(@deactivated='true')]/cfg:instance[starts-with(concat($path,'/'), concat(cfg:matchJobservice/@path,'/'))]">
                <contextInstanceRef><xsl:call-template name="ATTRIBUTE_CONTEXT_NAME"/></contextInstanceRef>
            </xsl:for-each>
        </jobservice>
    </xsl:template>
    <xsl:template name="SANDBOX_CONTAINER">
        <xsl:choose>
            <xsl:when test="cfg:sandbox">
                <xsl:apply-templates select="cfg:sandbox"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="..">
                    <xsl:call-template name="SANDBOX_CONTAINER"/>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="PROTOCOL_CONTAINER">
        <xsl:param name="visited_schemes">#</xsl:param>
        <xsl:variable name="content" select="cfg:protocol[not(contains($visited_schemes,concat('#',@scheme,'#')))][1]"/>
        <xsl:choose>
            <xsl:when test="$content">
                <xsl:for-each select="$content">
                    <xsl:call-template name="PROTOCOL">
                        <xsl:with-param name="visited_schemes" select="concat($visited_schemes,@scheme,'#')"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="..">
                    <xsl:call-template name="PROTOCOL_CONTAINER">
                        <xsl:with-param name="visited_schemes" select="$visited_schemes"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="PROTOCOL">
        <xsl:param name="visited_schemes">#</xsl:param>
        <protocolRef scheme="{@scheme}">
            <xsl:apply-templates/>
        </protocolRef>
        <xsl:variable name="content" select="following-sibling::cfg:protocol[not(contains($visited_schemes,concat('#',@scheme,'#')))][1]"/>
        <xsl:choose>
            <xsl:when test="$content">
                <xsl:for-each select="$content">
                    <xsl:call-template name="PROTOCOL">
                        <xsl:with-param name="visited_schemes" select="concat($visited_schemes,@scheme,'#')"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="../..">
                    <xsl:call-template name="PROTOCOL_CONTAINER">
                        <xsl:with-param name="visited_schemes" select="$visited_schemes"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="ATTRIBUTE_CONTEXT_NAME">
        <xsl:attribute name="name">
            <xsl:choose>
                <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
                <xsl:when test="count(../cfg:instance) = 1"><xsl:value-of select="../@type"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="concat(../@type,'[',count(preceding-sibling::cfg:instance),']')"/></xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>        
    </xsl:template>
    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>