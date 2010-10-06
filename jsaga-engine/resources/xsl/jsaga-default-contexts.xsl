<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.in2p3.fr/jsaga/session"
                xmlns:cfg="http://www.in2p3.fr/jsaga/session"
                xmlns:adapt="http://www.in2p3.fr/jsaga"
                exclude-result-prefixes="cfg">
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:variable name="AdaptorsDescriptor" select="document('AdaptorsDescriptor.xml')"/>

    <xsl:variable name="UrlPrefix">UrlPrefix</xsl:variable>
    <xsl:variable name="BaseUrlIncludes">BaseUrlIncludes</xsl:variable>
    <xsl:variable name="BaseUrlExcludes">BaseUrlExcludes</xsl:variable>
    <xsl:variable name="ServiceAttributes">ServiceAttributes</xsl:variable>

    <xsl:template match="/cfg:jsaga-default">
        <jsaga-default>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </jsaga-default>
    </xsl:template>

    <xsl:template match="cfg:contexts">
        <contexts>
            <xsl:apply-templates/>
        </contexts>
    </xsl:template>

    <xsl:template match="cfg:session">
        <session>
            <xsl:apply-templates/>
            <xsl:copy-of select="cfg:*[name()!='context']"/>
        </session>
    </xsl:template>

    <xsl:template match="cfg:context">
        <context>
            <xsl:copy-of select="@type"/>
            <!-- config context -->
            <xsl:copy-of select="cfg:attribute[@name!=$UrlPrefix and @name!=$BaseUrlIncludes and @name!=$BaseUrlExcludes and @name!=$ServiceAttributes]"/>

            <!-- prefix -->
            <xsl:if test="parent::cfg:session">
                <attribute name="{$UrlPrefix}">
                    <xsl:attribute name="value">
                        <xsl:choose>
                            <xsl:when test="cfg:attribute[@name=$UrlPrefix]/@value">
                                <xsl:value-of select="cfg:attribute[@name=$UrlPrefix]/@value"/>
                            </xsl:when>
                            <xsl:when test="@id">
                                <xsl:value-of select="@id"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="@type"/><xsl:value-of select="position()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                </attribute>
            </xsl:if>

            <!-- includes -->
            <xsl:if test="cfg:attribute[@name=$BaseUrlIncludes] or cfg:include or cfg:data or cfg:job">
                <attribute name="{$BaseUrlIncludes}">
                    <xsl:copy-of select="cfg:attribute[@name=$BaseUrlIncludes]/cfg:item"/>
                    <xsl:for-each select="cfg:data | cfg:job">
                        <xsl:variable name="service" select="."/>
                        <xsl:for-each select="child::cfg:include | following-sibling::cfg:include">
                            <item>
                                <xsl:apply-templates select="$service"/>://<xsl:apply-templates select="."/>
                            </item>
                        </xsl:for-each>
                        <xsl:if test="not(child::cfg:include | following-sibling::cfg:include)">
                            <item>
                                <xsl:apply-templates select="$service"/>://<xsl:text/>
                            </item>
                        </xsl:if>
                    </xsl:for-each>
                </attribute>
            </xsl:if>

            <!-- excludes -->
            <xsl:if test="cfg:attribute[@name=$BaseUrlExcludes] or descendant::cfg:exclude">
                <attribute name="{$BaseUrlExcludes}">
                    <xsl:copy-of select="cfg:attribute[@name=$BaseUrlExcludes]/cfg:item"/>
                    <xsl:for-each select="cfg:data | cfg:job">
                        <xsl:variable name="service" select="."/>
                        <xsl:for-each select="child::cfg:exclude | following-sibling::cfg:exclude">
                            <item>
                                <xsl:apply-templates select="$service"/>://<xsl:apply-templates select="."/>
                            </item>
                        </xsl:for-each>
                    </xsl:for-each>
                </attribute>
            </xsl:if>

            <!-- config services -->
            <xsl:if test="cfg:attribute[@name=$ServiceAttributes] or cfg:data or cfg:job">
                <attribute name="{$ServiceAttributes}">
                    <xsl:copy-of select="cfg:attribute[@name=$ServiceAttributes]/cfg:item"/>
                    <xsl:for-each select="cfg:data | cfg:job">
                        <xsl:variable name="service" select="."/>
                        <xsl:for-each select="cfg:attribute">
                            <item>
                                <xsl:value-of select="$service/@type"/>
                                <xsl:text>.</xsl:text>
                                <xsl:value-of select="@name"/>
                                <xsl:text>=</xsl:text>
                                <xsl:choose>
                                    <xsl:when test="@value">
                                        <xsl:value-of select="@value"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:for-each select="cfg:item">
                                            <xsl:if test="position()>1">,</xsl:if>
                                            <xsl:value-of select="text()"/>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </item>
                        </xsl:for-each>
                        <xsl:for-each select="$AdaptorsDescriptor/*/*[@type=$service/@type]
                                              /adapt:attribute[not(@name=$service/cfg:attribute/@name)]">
                            <item>
                                <xsl:value-of select="$service/@type"/>
                                <xsl:text>.</xsl:text>
                                <xsl:value-of select="@name"/>
                                <xsl:text>=</xsl:text>
                                <xsl:value-of select="@value"/>
                            </item>
                        </xsl:for-each>
                    </xsl:for-each>
                </attribute>
            </xsl:if>
        </context>
    </xsl:template>

    <xsl:template match="cfg:include | cfg:exclude">
        <xsl:choose>
            <xsl:when test="@basepath"><xsl:call-template name="BASEPATH"/></xsl:when>
            <xsl:when test="@port"><xsl:call-template name="PORT"/></xsl:when>
            <xsl:when test="@domain"><xsl:call-template name="DOMAIN"/></xsl:when>
            <xsl:when test="@host"><xsl:call-template name="HOST"/></xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="BASEPATH">
        <xsl:call-template name="PORT"/>
        <xsl:if test="not(starts-with(@basepath, '/'))">/</xsl:if>
        <xsl:value-of select="@basepath"/>
    </xsl:template>
    <xsl:template name="PORT">
        <xsl:call-template name="DOMAIN"/>
        <xsl:if test="@port">
            <xsl:choose>
                <xsl:when test="@isPortOptional='true'">[:<xsl:value-of select="@port"/>]</xsl:when>
                <xsl:otherwise>:<xsl:value-of select="@port"/></xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
    <xsl:template name="DOMAIN">
        <xsl:call-template name="HOST"/>
        <xsl:if test="@domain">.*<xsl:value-of select="@domain"/></xsl:if>
    </xsl:template>
    <xsl:template name="HOST">
        <xsl:choose>
            <xsl:when test="@host"><xsl:value-of select="@host"/></xsl:when>
            <xsl:otherwise>*</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="cfg:data[not(cfg:alias)] | cfg:job[not(cfg:alias)]">
        <xsl:value-of select="@type"/>
    </xsl:template>

    <xsl:template match="cfg:*[count(cfg:alias)=1]">
        <xsl:apply-templates select="cfg:alias"/>
    </xsl:template>

    <xsl:template match="cfg:*[count(cfg:alias)>1]">
        <xsl:text>{</xsl:text>
        <xsl:for-each select="cfg:alias">
            <xsl:if test="position()>1">,</xsl:if>
            <xsl:apply-templates select="."/>
        </xsl:for-each>
        <xsl:text>}</xsl:text>
    </xsl:template>
    <xsl:template match="cfg:alias[text() != parent::cfg:*/@type]">
        <xsl:value-of select="text()"/>-><xsl:value-of select="parent::cfg:*/@type"/>
    </xsl:template>
    <xsl:template match="cfg:alias[text() = parent::cfg:*/@type]">
        <xsl:value-of select="text()"/>
    </xsl:template>
</xsl:stylesheet>
