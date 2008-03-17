<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="uri:izpack">
    <xsl:output method="xml" indent="yes"/>
    <xsl:variable name="scripts" select="document('scripts.xml')/*"/>

    <xsl:template match="/project">
        <installation version="1.0">
            <info>
                <appname>JSAGA</appname>
                <appversion><xsl:value-of select="@version"/></appversion>
                <url>http://grid.in2p3.fr/jsaga/</url>
                <authors>
                    <author name="Sylvain Reynaud" email="sreynaud@in2p3.fr"/>
                </authors>
            </info>
            <guiprefs width="640" height="480" resizable="yes"/>
            <locale>
                <langpack iso3="eng"/>
                <langpack iso3="fra"/>
            </locale>
            <resources>
                <res id="LicencePanel.licence" src="Licence.txt"/>
                <res id="InfoPanel.info" src="../../../src/site/apt/index.apt"/>
                <res id="XInfoPanel.info" src="Readme.txt"/>
            </resources>
            <panels>
                <panel classname="HelloPanel"/>
                <panel classname="InfoPanel"/>
                <panel classname="LicencePanel"/>
                <panel classname="TargetPanel"/>
                <panel classname="PacksPanel"/>
                <panel classname="InstallPanel"/>
                <panel classname="XInfoPanel"/>
                <panel classname="FinishPanel"/>
            </panels>
            <packs>
                <xsl:apply-templates select="artifact"/>
            </packs>
        </installation>
    </xsl:template>

    <xsl:template match="/project/artifact[@id='jsaga-engine' and not(@classifier)]">
        <pack name="Core" required="yes">
            <description>The core engine</description>
            <file src="Licence.txt" targetdir="$INSTALL_PATH"/>
            <file src="Readme.txt" targetdir="$INSTALL_PATH"/>
            <parsable targetfile="$INSTALL_PATH/Readme.txt" type="plain"/>
            <file src="etc/" targetdir="$INSTALL_PATH"/>
            <xsl:for-each select="descendant-or-self::artifact">
                <file src="{@file}" targetdir="$INSTALL_PATH/lib"/>
            </xsl:for-each>

            <!-- WORKAROUND: artifact commons-logging.jar is provided by module jsaga-adaptor-srm instead of jsaga-engine -->
            <xsl:for-each select="/project/artifact[@id='jsaga-adaptor-srm']/artifact/artifact[@id='commons-logging']">
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
    </xsl:template>

    <xsl:template match="/project/artifact[@id!='jsaga-engine' and @scope!='test']">
        <pack name="{@id}" required="no">
            <description>An adaptor</description>
            <xsl:for-each select="descendant-or-self::artifact">
                <file src="{@file}" targetdir="$INSTALL_PATH/lib-adaptors"/>
            </xsl:for-each>
        </pack>
    </xsl:template>
</xsl:stylesheet>