<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="uri:izpack">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>
    <xsl:variable name="scripts" select="document('scripts.xml')/*"/>

    <xsl:template match="/project">
        <installation version="1.0">
            <info>
                <appname>JSAGA</appname>
                <appversion><xsl:value-of select="@version"/></appversion>
                <url>http://grid.in2p3.fr/jsaga/</url>
                <authors>
                    <author name="Sylvain Reynaud" email="sreynaud@in2p3.fr"/>
                    <author name="See contributors list" email="http://grid.in2p3.fr/software/jsaga-dev/contributors.html"/>
                </authors>
                <javaversion>1.5</javaversion>
            </info>
            <conditions>
                <xsl:call-template name="LICENSED_PACKAGE_SELECTION">
                    <xsl:with-param name="licenseType">CDDL</xsl:with-param>
                </xsl:call-template>
            </conditions>
            <guiprefs width="700" height="520" resizable="yes"/>
            <locale>
                <langpack iso3="eng"/>
                <langpack iso3="fra"/>
            </locale>
            <resources>
                <!-- internationalization resources -->
                <res id="CustomLangpack.xml_eng" src="../../../assembly/langpacks/CustomLangpack_eng.xml"/>
                <res id="CustomLangpack.xml_fra" src="../../../assembly/langpacks/CustomLangpack_fra.xml"/>
                <!-- installer resources -->
                <res id="Installer.image" src="../../../assembly/logo-jsaga.png"/>
                <res id="LicencePanel.licence" src="../../../assembly/licenses/License-LGPLv3.txt"/>
                <res id="LicencePanel.licence.CDDL" src="../../../assembly/licenses/License-CDDLv1.0.txt"/>
                <res id="InfoPanel.info" src="../../../src/site/apt/index.apt"/>
                <res id="XInfoPanel.info" src="../../../assembly/Readme.txt"/>
            </resources>
            <panels>
                <panel classname="HelloPanel"/>
                <panel classname="InfoPanel"/>
                <panel classname="LicencePanel"/>
                <panel classname="TargetPanel"/>
                <panel classname="TreePacksPanel"/>
                <panel classname="OptionalLicencePanel" id="CDDL" condition="show_CDDL"/>
                <panel classname="SummaryPanel"/>
                <panel classname="InstallPanel"/>
                <panel classname="XInfoPanel"/>
                <panel classname="SimpleFinishPanel"/>
            </panels>
            <packs>
                <xsl:apply-templates select="artifact[@id='jsaga-engine' and not(@classifier)]"/>
                <pack name="Adaptors" required="no">
                    <description>The adaptors provide the support for various technologies.</description>
                </pack>
                <xsl:apply-templates select="artifact[@id='jsaga-adaptors']/artifact[not(@classifier)]">
                    <xsl:sort select="@name" order="ascending"/>
                </xsl:apply-templates>
            </packs>
        </installation>
    </xsl:template>

    <xsl:template match="/project/artifact[@id='jsaga-engine' and not(@classifier)]">
        <pack name="Core" required="yes">
            <description>The core engine (required).</description>
            <file src="License-LGPLv3.txt" targetdir="$INSTALL_PATH"/>
            <file src="License-CDDLv1.0.txt" targetdir="$INSTALL_PATH" condition="show_CDDL"/>
            <file src="Readme.txt" targetdir="$INSTALL_PATH"/>
            <parsable targetfile="$INSTALL_PATH/Readme.txt" type="plain"/>
            <file src="etc/" targetdir="$INSTALL_PATH"/>
            <file src="doc/" targetdir="$INSTALL_PATH"/>
            <xsl:for-each select="descendant-or-self::artifact[not(@scope='test')]">
                <file src="{@file}" targetdir="$INSTALL_PATH/lib"/>
            </xsl:for-each>

            <!-- unix -->
            <fileset os="unix" dir="bin/" includes="*.sh" targetdir="$INSTALL_PATH/bin/"/>
            <xsl:for-each select="$scripts/script/text()">
                <xsl:variable name="script">$INSTALL_PATH/bin/<xsl:value-of select="."/>.sh</xsl:variable>
                <parsable os="unix" targetfile="{$script}" type="shell"/>
                <executable os="unix" targetfile="{$script}" stage="never"/>
            </xsl:for-each>

            <!-- windows -->
            <fileset os="windows" dir="bin/" includes="*.bat" targetdir="$INSTALL_PATH/bin/"/>
            <xsl:for-each select="$scripts/script/text()">
                <xsl:variable name="script">$INSTALL_PATH/bin/<xsl:value-of select="."/>.bat</xsl:variable>
                <parsable os="windows" targetfile="{$script}" type="plain"/>
            </xsl:for-each>
        </pack>
        <pack name="Integration tests" required="no" preselected="no">
            <description>Install integration test suites.</description>
            <xsl:for-each select="/project/artifact[@id='saga-api-test']
                                | /project/artifact[@id='saga-api-test']/artifact[@id='junit']
                                | /project/artifact[@id='jsaga-adaptors']/artifact[@scope='test']">
                <file src="{@file}" targetdir="$INSTALL_PATH/lib-test"/>
            </xsl:for-each>
        </pack>
    </xsl:template>

    <xsl:template match="/project/artifact[starts-with(@id,'jsaga-adaptors')]/artifact[not(@classifier)]">
        <xsl:variable name="description">
            <xsl:choose>
                <xsl:when test="@description and @description!=''"><xsl:value-of select="@description"/></xsl:when>
                <xsl:otherwise>No description available for this module.</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <pack parent="Adaptors" required="no">
            <xsl:attribute name="name"><xsl:call-template name="PACKAGE_NAME"/></xsl:attribute>
            <xsl:choose>
                <xsl:when test="contains(@license,'Lesser') or contains(@license,'LGPL')">
                    <xsl:attribute name="preselected">yes</xsl:attribute>
                    <description><xsl:value-of select="$description"/></description>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="preselected">no</xsl:attribute>
                    <description><xsl:value-of select="$description"/>
*****************************************************************************
*** WARNING: If you check this package, you will have to accept the terms of the agreement
***    of the <xsl:value-of select="@license"/>.
*****************************************************************************</description>
                </xsl:otherwise>
            </xsl:choose>
            <file src="{@file}" targetdir="$INSTALL_PATH/lib-adaptors"/>
            <xsl:for-each select="artifact">
                <xsl:call-template name="PACKAGE_DEPENDENCY"/>
            </xsl:for-each>
        </pack>
    </xsl:template>

    <!-- set condition "show_${licenseType}" if one or more packages under license "${licenseType}" is selected -->
    <xsl:template name="LICENSED_PACKAGE_SELECTION">
        <xsl:param name="licenseType"/>
        <xsl:variable name="nbPacks" select="count(artifact[contains(@license,$licenseType)])"/>
        <xsl:choose>
            <xsl:when test="$nbPacks = 1">
                <xsl:for-each select="artifact[contains(@license,$licenseType)]">
                    <condition type="packselection" id="show_{$licenseType}">
                        <packid><xsl:call-template name="PACKAGE_NAME"/></packid>
                    </condition>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="$nbPacks > 1">
                <xsl:for-each select="artifact[contains(@license,$licenseType)]">
                    <condition type="packselection" id="${@id}_SELECTED">
                        <packid><xsl:call-template name="PACKAGE_NAME"/></packid>
                    </condition>
                </xsl:for-each>
                <condition type="or" id="show_{$licenseType}">
                    <xsl:for-each select="artifact[contains(@license,$licenseType)]">
                        <condition type="ref" refid="${@id}_SELECTED"/>
                    </xsl:for-each>
                </condition>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="PACKAGE_DEPENDENCY">
        <xsl:choose>
            <xsl:when test="starts-with(@id, 'jsaga-adaptor-')">
                <depends>
                    <xsl:attribute name="packname"><xsl:call-template name="PACKAGE_NAME"/></xsl:attribute>
                </depends>
            </xsl:when>
            <xsl:otherwise>
                <file src="{@file}" targetdir="$INSTALL_PATH/lib-adaptors"/>
                <!-- recurse -->
                <xsl:for-each select="artifact">
                    <xsl:call-template name="PACKAGE_DEPENDENCY"/>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="PACKAGE_NAME">
        <xsl:choose>
            <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="@id"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>