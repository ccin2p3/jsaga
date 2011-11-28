<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>

    <!-- JSAGA parameters -->
    <xsl:param name="HostName">host</xsl:param>
    <xsl:param name="UniqId">staging</xsl:param>

    <!-- Adaptor-specific parameters (JDL attributes) -->
    <xsl:param name="LBAddress"/>
    <xsl:param name="requirements"/>
    <xsl:param name="rank"/>
    <xsl:param name="virtualorganisation"/>
    <xsl:param name="RetryCount"/>
    <xsl:param name="ShallowRetryCount"/>
    <xsl:param name="OutputStorage"/>
    <xsl:param name="ErrorStorage"/>
    <xsl:param name="AllowZippedISB"/>
    <xsl:param name="PerusalFileEnable"/>
    <xsl:param name="ListenerStorage"/>
    <xsl:param name="MyProxyServer"/>

    <!-- constants -->
    <xsl:variable name="SupportedProtocols">/gsiftp/</xsl:variable>
    <xsl:variable name="SANDBOX_BASE_URI">file:///sandbox/</xsl:variable>

    <!-- entry point (MUST BE RELATIVE) -->
    <xsl:template match="jsdl:JobDefinition">
        <xsl:apply-templates select="jsdl:JobDescription"/>
        <xsl:apply-templates select="ext:Extension[@language='JDL']"/>
    </xsl:template>

    <xsl:template match="jsdl:JobDescription">[
Type = "Job";<xsl:text/>
        <!-- executable and arguments -->
Executable = "<xsl:value-of select="jsdl:Application/posix:POSIXApplication/posix:Executable/text()"/>";<xsl:text/>
        <xsl:if test="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
Arguments = "<xsl:for-each select="jsdl:Application/posix:POSIXApplication/posix:Argument/text()">
                    <xsl:text> </xsl:text><xsl:value-of select="."/>
                </xsl:for-each>";<xsl:text/>
		</xsl:if>

        <!-- other -->
        <xsl:if test="count(jsdl:Application/posix:POSIXApplication/posix:Environment) > 0">
Environment = {<xsl:text/>
      		<xsl:for-each
               select="jsdl:Application/posix:POSIXApplication/posix:Environment">
               	<xsl:if test="contains(text(),' ')">
               		<xsl:message terminate="yes">Unsupported space in environment value : <xsl:value-of select="text()"/></xsl:message>
               	</xsl:if>
               	<xsl:if test="position() = 1">
"<xsl:text/><xsl:value-of select="@name"/>=<xsl:value-of select="text()"/>"<xsl:text/>
               	</xsl:if>
                <xsl:if test="position() > 1">
, "<xsl:value-of select="@name"/>=<xsl:value-of select="text()"/>"<xsl:text/>
                </xsl:if> 
            </xsl:for-each>
};<xsl:text/>
          </xsl:if> 
          		
<!--  Requirements -->
Requirements = true <xsl:text/>
        <xsl:if test="$requirements">
&amp;&amp; <xsl:value-of select="$requirements"/> <xsl:text/>
        </xsl:if>
        <xsl:for-each select="jsdl:JobIdentification/JDLRequirements/text()">
&amp;&amp; <xsl:value-of select="."/> <xsl:text/>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Resources/jsdl:TotalCPUTime/jsdl:UpperBoundedRange/text()">
&amp;&amp; other.GlueCEPolicyMaxCPUTime >= <xsl:value-of select="."/> <xsl:text/>
        </xsl:for-each>
		<xsl:for-each select="jsdl:Resources/jsdl:TotalPhysicalMemory/jsdl:UpperBoundedRange/text()">
&amp;&amp; other.GlueHostMainMemoryRAMSize >= <xsl:value-of select="."/> <xsl:text/>
		</xsl:for-each>
 		<xsl:for-each select="jsdl:Resources/jsdl:CandidateHosts/jsdl:HostName/text()">
             <xsl:choose>
                 <xsl:when test="contains(.,'/')">
&amp;&amp; other.GlueCEUniqueID == "<xsl:value-of select="."/>" <xsl:text/>
                 </xsl:when>
                 <xsl:otherwise>
&amp;&amp; other.GlueCEInfoHostName == "<xsl:value-of select="."/>" <xsl:text/>
                 </xsl:otherwise>
             </xsl:choose>
		</xsl:for-each>
		<xsl:for-each select="jsdl:Application/spmd:SPMDApplication/spmd:ProcessesPerHost/text()">
&amp;&amp; other.GlueHostArchitectureSMPSize  >= <xsl:value-of select="."/> <xsl:text/>
		</xsl:for-each>
		<xsl:for-each select="jsdl:JobIdentification/jsdl:JobAnnotation/text()">
&amp;&amp; other.GlueCEUniqueID == "<xsl:value-of select="."/>" <xsl:text/>
		</xsl:for-each>
 		<xsl:for-each select="jsdl:Resources/jsdl:CPUArchitecture/jsdl:CPUArchitectureName/text()">
&amp;&amp;  other.GlueHostArchitecturePlatformType == "<xsl:value-of select="."/>" <xsl:text/>
		</xsl:for-each>
		<xsl:for-each select="jsdl:Resources/jsdl:OperatingSystem/jsdl:OperatingSystemType/jsdl:OperatingSystemName/text()">
&amp;&amp;  other.GlueHostOperatingSystemName == "<xsl:value-of select="."/>" <xsl:text/>
		</xsl:for-each>
<xsl:text/>;

        <xsl:if test="$rank">
Rank = <xsl:value-of select="$rank"/>;<xsl:text/>
        </xsl:if>

        <!-- TODO : To test when input sandbox will work -->
        <xsl:for-each select="jsdl:Application/spmd:SPMDApplication/spmd:SPMDVariation/text()[not(. = 'None')]">        
            <xsl:choose>
	            <xsl:when test=". = 'MPI' or . = 'MPICH1' or . = 'MPICH2'">
JobType = "MPICH";<xsl:text/>
		        <xsl:for-each select="../../spmd:NumberOfProcesses/text()">
NodeNumber = <xsl:value-of select="."/>;<xsl:text/>
	    	    </xsl:for-each>
	            </xsl:when>
    	        <xsl:otherwise>
	    	        <xsl:message terminate="yes">Unsupported SPMDVariation : <xsl:value-of select="."/></xsl:message>
            	</xsl:otherwise>
        	</xsl:choose>
        </xsl:for-each>
        <xsl:for-each select="jsdl:Resources/jsdl:TotalCPUCount/jsdl:Exact/text()">
CPUNumber = <xsl:value-of select="."/>;<xsl:text/>
        </xsl:for-each>

        <!-- streams -->
        <xsl:variable name="isInteractive" select="jsdl:Application/posix:POSIXApplication/@name='interactive'"/>
        <xsl:choose>
            <xsl:when test="$isInteractive">
  StdOutput = "<xsl:value-of select="$UniqId"/>-output.txt";
  StdError = "<xsl:value-of select="$UniqId"/>-error.txt";
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="jsdl:Application/posix:POSIXApplication">
                    <xsl:for-each select="posix:Input">
                        <xsl:if test="starts-with(text(),'/')">
                            <xsl:message terminate="no">Absolute path in attribute Input means for JDL that file is already available on the worker node</xsl:message>
                        </xsl:if>
  StdInput = "<xsl:value-of select="text()"/>";<xsl:text/>
                    </xsl:for-each>
                    <xsl:for-each select="posix:Output">
                        <xsl:if test="starts-with(text(),'/')">
                            <xsl:message terminate="yes">Absolute path in attribute Output is not supported by JDL</xsl:message>
                        </xsl:if>
  StdOutput = "<xsl:value-of select="text()"/>";<xsl:text/>
                    </xsl:for-each>
                    <xsl:for-each select="posix:Error">
                        <xsl:if test="starts-with(text(),'/')">
                            <xsl:message terminate="yes">Absolute path in attribute Error is not supported by JDL</xsl:message>
                        </xsl:if>
  StdError = "<xsl:value-of select="text()"/>";<xsl:text/>
                    </xsl:for-each>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="jsdl:DataStaging[jsdl:Source]">
  InputSandboxPreStaging = {<xsl:text/>
            <xsl:for-each select="jsdl:DataStaging[jsdl:Source][
                    not(contains($SupportedProtocols,concat('/',substring-before(jsdl:Source/jsdl:URI/text(),'://'),'/')))]">
    [
        From = "<xsl:value-of select="translate(jsdl:Source/jsdl:URI/text(),'\','/')"/>";<xsl:text/>
        To = "<xsl:value-of select="$SANDBOX_BASE_URI"/><xsl:value-of select="jsdl:FileName/text()"/>";<xsl:text/>
        Append = "<xsl:value-of select="string(jsdl:CreationFlag/text()='append')"/>";<xsl:text/>
    ]<xsl:if test="position()!=last()">,</xsl:if>
            </xsl:for-each>
  };
  InputSandbox = {<xsl:text/>
            <xsl:for-each select="jsdl:DataStaging[jsdl:Source]">
                <xsl:if test="position()>1">,</xsl:if>
                "<xsl:text/>
                <xsl:choose>
                    <xsl:when test="contains($SupportedProtocols,concat('/',substring-before(jsdl:Source/jsdl:URI/text(),'://'),'/'))">
                        <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="jsdl:FileName/text()"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:text>"</xsl:text>
            </xsl:for-each>};
        </xsl:if>

        <xsl:if test="jsdl:DataStaging[jsdl:Target] or $isInteractive">
  OutputSandboxPostStaging = {<xsl:text/>
            <xsl:for-each select="jsdl:DataStaging[jsdl:Target][
                not(contains($SupportedProtocols,concat('/',substring-before(jsdl:Target/jsdl:URI/text(),'://'),'/')))]">
    [
        From = "<xsl:value-of select="$SANDBOX_BASE_URI"/><xsl:value-of select="jsdl:FileName/text()"/>";<xsl:text/>
        To = "<xsl:value-of select="translate(jsdl:Target/jsdl:URI/text(),'\','/')"/>";<xsl:text/>
        Append = "<xsl:value-of select="string(jsdl:CreationFlag/text()='append')"/>";<xsl:text/>
    ]<xsl:if test="position()!=last()">,</xsl:if>
            </xsl:for-each>
  };
  OutputSandbox = {<xsl:text/>
            <xsl:if test="$isInteractive">
                "<xsl:value-of select="$UniqId"/>-output.txt",
                "<xsl:value-of select="$UniqId"/>-error.txt"<xsl:text/>
            </xsl:if>
            <xsl:for-each select="jsdl:DataStaging[jsdl:Target]/jsdl:FileName">
                <xsl:if test="position()>1 or $isInteractive">,</xsl:if>
                "<xsl:value-of select="text()"/>"<xsl:text/>
            </xsl:for-each>};
  OutputSandboxDestURI = {<xsl:text/>
            <xsl:if test="$isInteractive">
                "gsiftp://<xsl:value-of select="$HostName"/>/tmp/<xsl:value-of select="$UniqId"/>-output.txt",
                "gsiftp://<xsl:value-of select="$HostName"/>/tmp/<xsl:value-of select="$UniqId"/>-error.txt"<xsl:text/>
            </xsl:if>
            <xsl:for-each select="jsdl:DataStaging[jsdl:Target]">
                <xsl:if test="position()>1 or $isInteractive">,</xsl:if>
                "<xsl:text/>
                <xsl:choose>
                    <xsl:when test="contains($SupportedProtocols,concat('/',substring-before(jsdl:Target/jsdl:URI/text(),'://'),'/'))">
                        <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="jsdl:FileName/text()"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:text>"</xsl:text>
            </xsl:for-each>};
        </xsl:if>

        <xsl:if test="$LBAddress">
LBAddress = "<xsl:value-of select="$LBAddress"/>";<xsl:text/>
        </xsl:if>
        <xsl:if test="$virtualorganisation">
virtualorganisation = "<xsl:value-of select="$virtualorganisation"/>";<xsl:text/>
        </xsl:if>
        <xsl:if test="$RetryCount">
RetryCount = <xsl:value-of select="$RetryCount"/>;<xsl:text/>
        </xsl:if>
        <xsl:if test="$ShallowRetryCount">
ShallowRetryCount = <xsl:value-of select="$ShallowRetryCount"/>;<xsl:text/>
        </xsl:if>
        <xsl:if test="$OutputStorage">
OutputStorage = "<xsl:value-of select="$OutputStorage"/>";<xsl:text/>
        </xsl:if>
        <xsl:if test="$ErrorStorage">
ErrorStorage = "<xsl:value-of select="$ErrorStorage"/>";<xsl:text/>
        </xsl:if>
        <xsl:if test="$AllowZippedISB">
AllowZippedISB = <xsl:value-of select="$AllowZippedISB"/>;<xsl:text/>
        </xsl:if>
        <xsl:if test="$PerusalFileEnable">
PerusalFileEnable = <xsl:value-of select="$PerusalFileEnable"/>;<xsl:text/>
        </xsl:if>
        <xsl:if test="$ListenerStorage">
ListenerStorage = "<xsl:value-of select="$ListenerStorage"/>";<xsl:text/>
        </xsl:if>
        <xsl:if test="$MyProxyServer">
MyProxyServer = "<xsl:value-of select="$MyProxyServer"/>";<xsl:text/>
        </xsl:if>
]
    </xsl:template>

    <xsl:template match="posix:Input | posix:Output | posix:Error">
        <!-- add path -->
        <xsl:call-template name="VALUE_OF_TEXT"/>
    </xsl:template>

    <xsl:template match="jsdl:Source/jsdl:URI">
        <!-- check file not renamed -->
        <xsl:variable name="sourceFilename">
            <xsl:call-template name="FILENAME"><xsl:with-param name="uri" select="text()"/></xsl:call-template>
        </xsl:variable>
        <xsl:variable name="targetFilename" select="ancestor::jsdl:DataStaging/jsdl:FileName/text()"/>
        <xsl:if test="$sourceFilename != $targetFilename">
            <xsl:message terminate="yes">Renaming file is not supported: <xsl:value-of
                    select="$sourceFilename"/> / <xsl:value-of select="$targetFilename"/></xsl:message>
        </xsl:if>
        <!-- add URI -->
        <xsl:call-template name="VALUE_OF_TEXT"/>
    </xsl:template>
    <xsl:template match="jsdl:DataStaging[jsdl:Target/jsdl:URI]/jsdl:FileName">
        <!-- add FileName -->
        <xsl:call-template name="VALUE_OF_TEXT"/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI">
        <!-- add URI -->
        <xsl:call-template name="VALUE_OF_TEXT"/>
    </xsl:template>

    <xsl:template match="ext:Extension"># Extension:
<xsl:value-of select="text()"/>
    </xsl:template>


    <xsl:template name="VALUE_OF_TEXT">
        <xsl:if test="position() > 1">
            <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:text>"</xsl:text><xsl:value-of select="text()"/><xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template name="FILENAME">
        <xsl:param name="uri"/>
        <xsl:choose>
            <xsl:when test="contains($uri,'/')">
                <xsl:call-template name="FILENAME">
                    <xsl:with-param name="uri" select="substring-after($uri,'/')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$uri"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
