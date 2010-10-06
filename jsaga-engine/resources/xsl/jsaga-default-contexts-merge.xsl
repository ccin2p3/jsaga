<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.in2p3.fr/jsaga/session"
                xmlns:cfg="http://www.in2p3.fr/jsaga/session"
                exclude-result-prefixes="cfg">
    <xsl:output method="xml" indent="yes"
                xalan:indent-amount="4" xmlns:xalan="http://xml.apache.org/xalan"/>
    <xsl:strip-space elements="*"/>

    <xsl:variable name="SystemProperties" select="document('SystemProperties.xml')"/>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="cfg:jsaga-default">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">Missing namespace: http://www.in2p3.fr/jsaga/session</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@extends">
        <!-- ignored -->
    </xsl:template>

    <xsl:template match="@value">
        <xsl:attribute name="value">
            <xsl:choose>
                <xsl:when test="contains(., '${')">
                    <xsl:variable name="start" select="substring-before(., '${')"/>
                    <xsl:variable name="end" select="substring-after(., '}')"/>
                    <xsl:variable name="var" select="substring-before(substring-after(., '${'), '}')"/>
                    <xsl:variable name="value" select="$SystemProperties/properties/entry[@key=$var]/text()"/>
                    <xsl:choose>
                        <xsl:when test="$value">
                            <xsl:value-of select="$start"/><xsl:value-of select="$value"/><xsl:value-of select="$end"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:message terminate="yes">Undefined system property: <xsl:value-of select="$var"/></xsl:message>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:attribute name="{name()}">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="cfg:jsaga-default">
        <jsaga-default>
            <xsl:choose>
                <xsl:when test="@extends">
                    <xsl:variable name="ref" select="document(@extends)/cfg:jsaga-default"/>
                    <xsl:call-template name="MERGE">
                        <xsl:with-param name="ref" select="$ref"/>
                        <xsl:with-param name="tag">contexts</xsl:with-param>
                        <xsl:with-param name="attr"/>
                    </xsl:call-template>
                    <xsl:call-template name="MERGE">
                        <xsl:with-param name="ref" select="$ref"/>
                        <xsl:with-param name="tag">session</xsl:with-param>
                        <xsl:with-param name="attr"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="*"/>
                </xsl:otherwise>
            </xsl:choose>
        </jsaga-default>
    </xsl:template>
    <xsl:template match="cfg:contexts">
        <xsl:param name="ref"/>
        <xsl:element name="{name()}">
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">context</xsl:with-param>
                <xsl:with-param name="attr">type</xsl:with-param>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    <xsl:template match="cfg:session">
        <xsl:param name="ref"/>
        <xsl:element name="{name()}">
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">context</xsl:with-param>
                <xsl:with-param name="attr">id</xsl:with-param>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    <xsl:template match="cfg:context">
        <xsl:param name="ref"/>
        <xsl:element name="{name()}">
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">attribute</xsl:with-param>
                <xsl:with-param name="attr">name</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">data</xsl:with-param>
                <xsl:with-param name="attr">type</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">job</xsl:with-param>
                <xsl:with-param name="attr">type</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">include</xsl:with-param>
                <xsl:with-param name="attr"/>
            </xsl:call-template>
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">exclude</xsl:with-param>
                <xsl:with-param name="attr"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    <xsl:template match="cfg:data | cfg:job">
        <xsl:param name="ref"/>
        <xsl:element name="{name()}">
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">attribute</xsl:with-param>
                <xsl:with-param name="attr">name</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">alias</xsl:with-param>
                <xsl:with-param name="attr"/>
            </xsl:call-template>
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">include</xsl:with-param>
                <xsl:with-param name="attr"/>
            </xsl:call-template>
            <xsl:call-template name="MERGE">
                <xsl:with-param name="ref" select="$ref"/>
                <xsl:with-param name="tag">exclude</xsl:with-param>
                <xsl:with-param name="attr"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template match="cfg:*">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*|*|text()|comment()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="MERGE">
        <xsl:param name="ref"/>
        <xsl:param name="tag"/>
        <xsl:param name="attr"/>
        <xsl:apply-templates select="@*"/>
        <xsl:choose>
            <xsl:when test="not($ref)">
                <xsl:apply-templates select="*[name()=$tag]"/>
            </xsl:when>
            <xsl:when test="$attr">
                <xsl:variable name="user" select="."/>
                <xsl:apply-templates select="$ref/*[name()=$tag][not(@*[name()=$attr] = $user/*[name()=$tag]/@*[name()=$attr])]"/>
                <xsl:for-each select="$user/*[name()=$tag]">
                    <xsl:variable name="id" select="@*[name()=$attr]"/>
                    <xsl:apply-templates select=".">
                        <xsl:with-param name="ref" select="$ref/*[name()=$tag][@*[name()=$attr] = $id]"/>
                    </xsl:apply-templates>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="*[name()=$tag]">
                <xsl:apply-templates select="*[name()=$tag]">
                    <xsl:with-param name="ref" select="$ref/*[name()=$tag]"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="$ref/*[name()=$tag]"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>