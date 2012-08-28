<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:jsdl-posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <xsl:output method="text"/>
 <xsl:variable name="ATTRIBUTE_SEPARATOR">;</xsl:variable>
 <xsl:param name="stagingDir"/>
	<xsl:param name="HostName"/>
	<xsl:param name="UniqId"/>
    <!-- constants -->
    <xsl:variable name="SupportedProtocols">/ourgrid/</xsl:variable>
	 <xsl:variable name="lf"></xsl:variable>
 <!-- entry point (MUST BE RELATIVE) -->
    <xsl:template match="jsdl:JobDefinition">
        <xsl:apply-templates select="jsdl:JobDescription"/>
        <xsl:apply-templates select="ext:Extension[@language='JDL']"/>
    </xsl:template>
    <!-- Beginning of the jdf job -->
     <!-- label job description tag-->

<xsl:template match="jsdl:JobDescription">
<xsl:text>job:</xsl:text> 
<xsl:text>&#xa;</xsl:text>
<xsl:text>label: </xsl:text>
<xsl:choose>
    <xsl:when test="(jsdl:JobIdentification/jsdl:JobName != '') or (jsdl:Application/jsdl:ApplicationName != '')">
        <xsl:choose>
        <xsl:when test="jsdl:JobIdentification/jsdl:JobName">
      	 		<xsl:value-of select="translate(jsdl:JobIdentification/jsdl:JobName/text(),' ' ,'' )"></xsl:value-of>
   		 </xsl:when>
         <xsl:when test="jsdl:Application/jsdl:ApplicationName">
          <xsl:value-of select="translate(jsdl:Application/jsdl:ApplicationName/text(),' ' ,'' )"></xsl:value-of>
    	</xsl:when>
    </xsl:choose>
    </xsl:when>
    <xsl:otherwise><xsl:text>DefaultJob</xsl:text><xsl:value-of select="$UniqId"></xsl:value-of>
    </xsl:otherwise>
    </xsl:choose>
    
    
    


<xsl:choose> 
<xsl:when test="jsdl:Resources">
<xsl:choose>
	
	<xsl:when test="jsdl:Resources/jsdl:OperatingSystem/jsdl:OperatingSystemType/jsdl:OperatingSystemName">
	<xsl:choose>
    	<xsl:when test="contains(jsdl:Resources/jsdl:OperatingSystem/jsdl:OperatingSystemType/jsdl:OperatingSystemName,'LINUX')">
           <xsl:text>&#xa;</xsl:text>
           <xsl:text>requirements: </xsl:text>  
           <xsl:choose>
  	         <xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:LowerBoundedRange">
       			 <xsl:text>(os == linux and mem >= </xsl:text>   
           		 <xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:LowerBoundedRange/text()"/>
        		 <xsl:text>) </xsl:text>  
         	 </xsl:when>
         	 <xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:UpperBoundedRange">
       			 <xsl:text>(os == linux and mem </xsl:text> 
       			 <xsl:text disable-output-escaping="yes"> <![CDATA[<=]]>  </xsl:text>   
           		 <xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:UpperBoundedRange/text()"/>
        		 <xsl:text>) </xsl:text>  
         	 </xsl:when>
         	  <xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:Exact">
       			 <xsl:text>(os == linux and mem == </xsl:text>   
           		 <xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:Exact/text()"/>
        		 <xsl:text>) </xsl:text>  
         	 </xsl:when>
         	 
         	 <xsl:otherwise>(os == linux)</xsl:otherwise>
     	   </xsl:choose>
    	 </xsl:when>
             
         <xsl:when test="contains(jsdl:Resources/jsdl:OperatingSystem/jsdl:OperatingSystemType/jsdl:OperatingSystemName,'WIN95') or contains(jsdl:Resources/jsdl:OperatingSystem/jsdl:OperatingSystemType/jsdl:OperatingSystemName,'WIN98') or  contains(jsdl:Resources/jsdl:OperatingSystem/jsdl:OperatingSystemType/jsdl:OperatingSystemName,' Windows_XP')">
           <xsl:text>&#xa;</xsl:text>
           <xsl:text>requirements:</xsl:text>     
             <xsl:choose>
                 <xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:LowerBoundedRange">
       			   	<xsl:text>(os == windows and mem >=</xsl:text>   
         		 	<xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:LowerBoundedRange/text()"/>
        		 	<xsl:text>) </xsl:text>  
                 </xsl:when>
             <xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:UpperBoundedRange">
       			 <xsl:text>(os ==  windows and mem </xsl:text>   
       			 <xsl:text disable-output-escaping="yes"> <![CDATA[<=]]>  </xsl:text> 
           		 <xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:UpperBoundedRange/text()"/>
        		 <xsl:text>) </xsl:text>  
         	 </xsl:when>
         	  <xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:Exact">
       			 <xsl:text>(os == windows and mem == </xsl:text>   
           		 <xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:Exact/text()"/>
        		 <xsl:text>) </xsl:text>  
         	 </xsl:when>    
                <xsl:otherwise>(os == windows)</xsl:otherwise>
        	 </xsl:choose>      
            </xsl:when>
      	</xsl:choose>  
      </xsl:when>  
       
      <xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory">
       			 <xsl:text>&#xa;</xsl:text>
          		 <xsl:text>requirements: </xsl:text> 
          		 <xsl:choose>
       				<xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:LowerBoundedRange and jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:UpperBoundedRange ">
		       				<xsl:text>(mem >= </xsl:text>
		       				<xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:LowerBoundedRange/text()"/>
		       				<xsl:text> AND </xsl:text>  
		       				<xsl:text disable-output-escaping="yes"> mem <![CDATA[<=]]></xsl:text> 
		       				<xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:UpperBoundedRange/text()"/> 
		       				<xsl:text>) </xsl:text> 
       				</xsl:when>
       			
       				<xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:LowerBoundedRange">
	       			      <xsl:text>(mem >=</xsl:text>  
	       			      <xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:LowerBoundedRange/text()"/>
	       				  <xsl:text>) </xsl:text>  
       				</xsl:when>
       				
       				<xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:UpperBoundedRange">
       					<xsl:text> (mem </xsl:text>  
       					<xsl:text disable-output-escaping="yes"><![CDATA[<=]]></xsl:text> 
       				 	<xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:UpperBoundedRange/text()"/>
       					<xsl:text>) </xsl:text>  
       				</xsl:when>
       				
       				<xsl:when test="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:Exact">
       					<xsl:text> (mem == </xsl:text>  
       				 	<xsl:value-of select="jsdl:Resources/jsdl:IndividualPhysicalMemory/jsdl:Exact/text()"/>
       					<xsl:text>) </xsl:text>  
       				</xsl:when>
       			</xsl:choose>
       		 </xsl:when>
           </xsl:choose>
 </xsl:when>        
</xsl:choose>




<xsl:for-each select="jsdl:Application/jsdl-posix:POSIXApplication">
<xsl:text>&#xa;</xsl:text>
<xsl:text>&#xa;</xsl:text>
<xsl:text>task:</xsl:text>
<xsl:text>&#xa;</xsl:text>

<xsl:text>init: put </xsl:text><xsl:value-of select="$UniqId"/><xsl:text>-</xsl:text><xsl:value-of select="position()"/><xsl:text>.sh </xsl:text><xsl:value-of select="$UniqId"/> <xsl:text>-</xsl:text><xsl:value-of select="position()"/><xsl:text>.sh</xsl:text><xsl:text>&#xa;</xsl:text><xsl:for-each select="/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging"> <xsl:if test="jsdl:Source"><xsl:text>put </xsl:text><xsl:value-of select="jsdl:FileName/text()"/><xsl:text> </xsl:text><xsl:value-of select="jsdl:FileName/text()"/><xsl:text>&#xa;</xsl:text></xsl:if>
    </xsl:for-each>
 

<xsl:if test="jsdl-posix:Error">
<xsl:text>remote: /bin/bash </xsl:text><xsl:value-of select="$UniqId"/><xsl:text>-</xsl:text> <xsl:value-of select="position()"/><xsl:text>.sh 1> </xsl:text><xsl:text disable-output-escaping="yes"></xsl:text><xsl:value-of select="jsdl-posix:Output/text()"/> <xsl:text> 2> </xsl:text>   <xsl:value-of select="jsdl-posix:Error"></xsl:value-of>
<xsl:text>&#xa;</xsl:text>
<xsl:text>final: </xsl:text>			  
</xsl:if>
			  
			  <xsl:if test="not(jsdl-posix:Error)">
			  <xsl:text>remote: /bin/bash </xsl:text><xsl:value-of select="$UniqId"/><xsl:text>-</xsl:text> <xsl:value-of select="position()"/><xsl:text>.sh 1> </xsl:text><xsl:text disable-output-escaping="yes"></xsl:text><xsl:value-of select="jsdl-posix:Output/text()"/> <xsl:text> 2> stderr</xsl:text><xsl:value-of select="position()"/>
 <xsl:text>&#xa;</xsl:text>
<xsl:text>final: get stderr</xsl:text><xsl:value-of select="position()"/> <xsl:text> stderr</xsl:text><xsl:value-of select="position()"/>
			  <xsl:text>&#xa;</xsl:text>
			  </xsl:if>
			<xsl:choose>
<xsl:when test="(jsdl-posix:Argument)or(jsdl-posix:Input) "><xsl:for-each select="/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging[jsdl:Target][not(contains($SupportedProtocols,concat('/',substring-before(jsdl:Target/jsdl:URI/text(),'://'),'/')))]">get <xsl:value-of select="jsdl:FileName/text()"/> <xsl:text> </xsl:text> <xsl:value-of select="jsdl:FileName/text()"/><xsl:text>&#xa;</xsl:text>
</xsl:for-each>
 			  </xsl:when>
			</xsl:choose>
			
</xsl:for-each>			
			
<xsl:for-each select="jsdl:Application/jsdl-posix:POSIXApplication">
			  <xsl:if test="jsdl-posix:Error">
			  <xsl:text>¢</xsl:text><xsl:value-of select="jsdl-posix:Error"></xsl:value-of></xsl:if>
			  <xsl:if test="not(jsdl-posix:Error)">
			  <xsl:text>¢stderr</xsl:text><xsl:value-of select="position()"/></xsl:if>
			  <xsl:text>&#xa;</xsl:text>	
			 <xsl:if test="jsdl-posix:Executable">
			  <xsl:text>¢</xsl:text><xsl:value-of select="$UniqId"/><xsl:text>-</xsl:text><xsl:value-of select="position()"/><xsl:text>.sh,</xsl:text> <xsl:value-of select="jsdl-posix:Executable"></xsl:value-of><xsl:text> </xsl:text>
			  <xsl:for-each select="jsdl-posix:Argument"><xsl:value-of select="." /><xsl:text> </xsl:text> </xsl:for-each> 
			  </xsl:if>
			   <xsl:text>&#xa;</xsl:text>	
		 
</xsl:for-each>			  		 
			  <xsl:if test="jsdl:DataStaging">#<![CDATA[<]]>TransferFiles<![CDATA[>]]></xsl:if>
        <xsl:if test="jsdl:DataStaging/jsdl:DeleteOnTermination[text()='True']">
         <xsl:text>&#xa;</xsl:text>
        	 <xsl:text disable-output-escaping="yes"><![CDATA[<]]>CleanUp<![CDATA[>]]>True<![CDATA[</]]>CleanUp<![CDATA[>]]>  </xsl:text> </xsl:if>  
  <xsl:variable name="SANDBOX_BASE_URI">ourgrid://<xsl:value-of select="$HostName"/>/</xsl:variable>
   <xsl:if test="jsdl:DataStaging">
<![CDATA[<]]>DownloadURL<![CDATA[>]]><xsl:value-of select="$SANDBOX_BASE_URI"/>download/<![CDATA[</]]>DownloadURL<![CDATA[>]]>
<![CDATA[<]]>UploadURL<![CDATA[>]]><xsl:value-of select="$SANDBOX_BASE_URI"/>upload/<![CDATA[</]]>UploadURL<![CDATA[>]]></xsl:if>
<xsl:variable name="isInteractive" select="jsdl:Application/posix:POSIXApplication/@name='interactive'"/><xsl:if test="jsdl:DataStaging[jsdl:Source]">
<![CDATA[<]]>InputSandboxPreStaging<![CDATA[>]]><xsl:for-each select="jsdl:DataStaging[jsdl:Source][not(contains($SupportedProtocols,concat('/',substring-before(jsdl:Source/jsdl:URI/text(),'://'),'/')))]">
<![CDATA[<]]>PreStaging<![CDATA[>]]>
<![CDATA[<]]>StagingIn<xsl:value-of select="position()"/><![CDATA[>]]>
<![CDATA[<]]>From<![CDATA[>]]><xsl:value-of select="translate(jsdl:Source/jsdl:URI/text(),'\','/')"/><![CDATA[</]]>From<![CDATA[>]]>
<![CDATA[<]]>To<![CDATA[>]]><xsl:value-of select="$SANDBOX_BASE_URI"/>upload/<xsl:value-of select="jsdl:FileName/text()"/><![CDATA[</]]>To<![CDATA[>]]>
<![CDATA[<]]>Append<![CDATA[>]]><xsl:value-of select="string(jsdl:CreationFlag/text()='append')"/><![CDATA[</]]>Append<![CDATA[>]]>
<![CDATA[</]]>StagingIn<xsl:value-of select="position()"/><![CDATA[>]]>
<![CDATA[</]]>PreStaging<![CDATA[>]]></xsl:for-each>
<![CDATA[</]]>InputSandboxPreStaging<![CDATA[>]]>  
</xsl:if>

<xsl:if test="jsdl:DataStaging[jsdl:Source]">
<![CDATA[<]]>InputSandboxPreStagingOut<![CDATA[>]]><xsl:for-each select="jsdl:DataStaging[jsdl:Source][not(contains($SupportedProtocols,concat('/',substring-before(jsdl:Source/jsdl:URI/text(),'://'),'/')))]">
<![CDATA[<]]>PreStagingOut<![CDATA[>]]>
<![CDATA[<]]>StagingInOut<xsl:value-of select="position()"/><![CDATA[>]]>
<![CDATA[<]]>From<![CDATA[>]]><xsl:value-of select="translate(jsdl:Source/jsdl:URI/text(),'\','/')"/><![CDATA[</]]>From<![CDATA[>]]>
<![CDATA[<]]>To<![CDATA[>]]><xsl:value-of select="$SANDBOX_BASE_URI"/>download/<xsl:value-of select="jsdl:FileName/text()"/><![CDATA[</]]>To<![CDATA[>]]>
<![CDATA[<]]>Append<![CDATA[>]]><xsl:value-of select="string(jsdl:CreationFlag/text()='append')"/><![CDATA[</]]>Append<![CDATA[>]]>
<![CDATA[</]]>StagingInOut<xsl:value-of select="position()"/><![CDATA[>]]>
<![CDATA[</]]>PreStagingOut<![CDATA[>]]></xsl:for-each>
<![CDATA[</]]>InputSandboxPreStagingOut<![CDATA[>]]>
  
</xsl:if>

<xsl:if test="jsdl:DataStaging[jsdl:Target] or $isInteractive"><![CDATA[<]]>OutputSandboxPostStaging<![CDATA[>]]><xsl:for-each select="jsdl:DataStaging[jsdl:Target][not(contains($SupportedProtocols,concat('/',substring-before(jsdl:Target/jsdl:URI/text(),'://'),'/')))]">
<![CDATA[<]]>PostStaging<![CDATA[>]]>
<![CDATA[<]]>StagingOut<xsl:value-of select="position()"/><![CDATA[>]]>
<![CDATA[<]]>From<![CDATA[>]]><xsl:value-of select="$SANDBOX_BASE_URI"/>download/<xsl:value-of select="jsdl:FileName/text()"/><![CDATA[</]]>From<![CDATA[>]]>
<![CDATA[<]]>To<![CDATA[>]]><xsl:value-of select="translate(jsdl:Target/jsdl:URI/text(),'\','/')"/><![CDATA[</]]>To<![CDATA[>]]>
<![CDATA[<]]>Append<![CDATA[>]]><xsl:value-of select="string(jsdl:CreationFlag/text()='append')"/><![CDATA[</]]>Append<![CDATA[>]]>
<![CDATA[</]]>StagingOut<xsl:value-of select="position()"/><![CDATA[>]]>
<![CDATA[</]]>PostStaging<![CDATA[>]]></xsl:for-each>
<![CDATA[</]]>OutputSandboxPostStaging<![CDATA[>]]></xsl:if>		

<xsl:if test="jsdl:DataStaging"><xsl:text>&#xa;</xsl:text><![CDATA[</]]>TransferFiles<![CDATA[>]]></xsl:if>
</xsl:template>  






    
       </xsl:stylesheet>