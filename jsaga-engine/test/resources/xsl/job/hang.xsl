<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>

    <!-- entry point (MUST BE RELATIVE) -->
	 <xsl:template match="jsdl:JobDefinition">
        <xsl:value-of select="jsdl:JobDescription/jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/>
    </xsl:template>
</xsl:stylesheet>