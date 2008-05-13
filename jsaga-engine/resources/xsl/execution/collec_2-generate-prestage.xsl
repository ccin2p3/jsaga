<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
            xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            xmlns="http://schemas.ggf.org/jsdl/2005/11/jsdl"
            xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
            xmlns:ext="http://www.in2p3.fr/jsdl-extension"
            exclude-result-prefixes="jsdl">
    <!-- ###########################################################################
         # Generate pre/post-staging
         ###########################################################################
    -->
    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:param name="localIntermediary" select="'/tmp'"/>
    <xsl:param name="collectionName">
        <!-- default collection name (when parameter collectionName not given) -->
        <xsl:choose>
            <xsl:when test="/ext:JobCollection/ext:JobCollectionDescription/ext:JobCollectionIdentification/ext:JobCollectionName/text()">
                <xsl:value-of select="/ext:JobCollection/ext:JobCollectionDescription/ext:JobCollectionIdentification/ext:JobCollectionName/text()"/>
            </xsl:when>
            <xsl:when test="/ext:JobCollection/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName/text()">
                <xsl:value-of select="/ext:JobCollection/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName/text()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="generate-id(/ext:JobCollection)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:variable name="insbx" select="concat($localIntermediary,'/',$collectionName,'/input-sandbox.tar')"/>
    <xsl:variable name="outsbx" select="concat($localIntermediary,'/',$collectionName,'/output-sandbox-@{INDEX}.tar')"/>

    <!-- entry point -->
    <xsl:template match="/">
        <xsl:apply-templates select="ext:JobCollection"/>
    </xsl:template>
    <xsl:template match="ext:JobCollection">
        <ext:JobCollection>
            <xsl:comment> GENERATED BY collec_2-generate-prestage.xsl </xsl:comment>
            <xsl:apply-templates select="@* | * | text() | comment()"/>
        </ext:JobCollection>
    </xsl:template>

    <!-- workaround to namespace problems... -->
    <xsl:template match="jsdl:JobDefinition">
        <JobDefinition>
            <xsl:apply-templates/>
        </JobDefinition>
    </xsl:template>

    <!-- specific templates here -->
    <xsl:template match="jsdl:JobDescription[jsdl:DataStaging/ext:Sandbox/text()='true']">
        <JobDescription>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="jsdl:JobIdentification | jsdl:Application | jsdl:Resources"/>
            <xsl:if test="jsdl:DataStaging[ext:Sandbox/text()='true' and jsdl:Source/jsdl:URI]">
                <xsl:comment> automatically generated </xsl:comment>
                <DataStaging name="INPUT_SANDBOX">
                    <FileName>input-sandbox.tar</FileName>
                    <CreationFlag>overwrite</CreationFlag>
                    <Source>
                        <URI>file://<xsl:value-of select="$insbx"/></URI>
                    </Source>
                    <ext:Shared>true</ext:Shared><!-- always shared -->
                </DataStaging>
            </xsl:if>
            <xsl:apply-templates select="jsdl:DataStaging"/>
            <xsl:if test="jsdl:DataStaging[ext:Sandbox/text()='true' and jsdl:Target/jsdl:URI]">
                <xsl:comment> automatically generated </xsl:comment>
                <DataStaging name="OUTPUT_SANDBOX">
                    <FileName>output-sandbox.tar</FileName>
                    <CreationFlag>overwrite</CreationFlag>
                    <Target>
                        <URI>file://<xsl:value-of select="$outsbx"/></URI>
                    </Target>
                    <ext:Shared>false</ext:Shared><!-- never shared -->
                </DataStaging>
            </xsl:if>
            <xsl:apply-templates select="*[namespace-uri()!='http://schemas.ggf.org/jsdl/2005/11/jsdl']"/>
        </JobDescription>
    </xsl:template>

    <xsl:template match="jsdl:DataStaging[ext:Sandbox/text()='true' and jsdl:*/jsdl:URI]">
        <DataStaging>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="jsdl:FileName | jsdl:FilesystemName | jsdl:CreationFlag | jsdl:DeleteOnTermination"/>
            <xsl:for-each select="jsdl:Source[jsdl:URI]">
                <Source>
                    <URI>tar://$INPUT_SANDBOX/<xsl:call-template name="FILENAME"/></URI>
                    <xsl:apply-templates select="ext:*"/>
                </Source>
                <ext:Step uri="{jsdl:URI/text()}">
                    <ext:Step>
                        <xsl:attribute name="uri">tar://<xsl:value-of select="$insbx"/>/<xsl:call-template name="FILENAME"/></xsl:attribute>
                    </ext:Step>
                </ext:Step>
            </xsl:for-each>
            <xsl:for-each select="jsdl:Target[jsdl:URI]">
                <Target>
                    <URI>tar://$OUTPUT_SANDBOX/<xsl:call-template name="FILENAME"/></URI>
                    <xsl:apply-templates select="ext:*"/>
                </Target>
                <ext:Step uri="{jsdl:URI/text()}">
                    <ext:Step>
                        <xsl:attribute name="uri">tar://<xsl:value-of select="$outsbx"/>/<xsl:call-template name="FILENAME"/></xsl:attribute>
                    </ext:Step>
                </ext:Step>
            </xsl:for-each>
            <xsl:apply-templates select="*[namespace-uri()!='http://schemas.ggf.org/jsdl/2005/11/jsdl']"/>
        </DataStaging>
    </xsl:template>

    <xsl:template match="jsdl:DataStaging[not(ext:Sandbox/text()='true')]">
        <DataStaging>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*[not(local-name()='Shared')] | text() | comment()"/>
            <ext:Shared>
                <xsl:for-each select="jsdl:Source | jsdl:Target">
                    <xsl:value-of select="string(not(contains(jsdl:URI/text(),'@{')))"/>
                </xsl:for-each>
            </ext:Shared>
        </DataStaging>
    </xsl:template>

    <xsl:template name="FILENAME">
        <xsl:variable name="uri">
            <xsl:choose>
                <xsl:when test="contains(jsdl:URI/text(),'?')"><xsl:value-of select="substring-before(jsdl:URI/text(),'?')"/></xsl:when>
                <xsl:when test="contains(jsdl:URI/text(),'#')"><xsl:value-of select="substring-before(jsdl:URI/text(),'#')"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="jsdl:URI/text()"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="translate($uri,':/','.,')"/>
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