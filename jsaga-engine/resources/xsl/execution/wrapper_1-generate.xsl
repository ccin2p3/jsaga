<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension">
    <!-- ###########################################################################
         # Generate job wrapper script
         ###########################################################################
    -->
    <xsl:output method="text" indent="yes" encoding="latin1"/>
    <xsl:strip-space elements="*"/>

    <!-- configuration attributes -->
    <xsl:param name="logger"/>
    <xsl:param name="monitor"/>
    <xsl:param name="accounting"/>
    <xsl:param name="protection" select="'integrity'"/>

    <!-- entry point -->
    <xsl:template match="/">
        <xsl:apply-templates select="ext:Job/jsdl:JobDefinition/jsdl:JobDescription"/>
        <xsl:apply-templates select="test"/>
    </xsl:template>

    <!-- script -->
    <xsl:template match="jsdl:JobDescription">#!/bin/sh
JOBNAME=<xsl:value-of select="jsdl:JobIdentification/jsdl:JobName/text()"/>

function log() {
    LEVEL=$1
    MESSAGE=$2
    echo "$LEVEL: ["`date "+%d/%m/%Y %T,%2N"`"] - $MESSAGE"
    <xsl:if test="$logger"><xsl:value-of select="$logger"/></xsl:if>
}

function change_state() {
    STATUS=$1
    CAUSE=$2
    if test "$STATUS" = "FAILED" ; then
        log FATAL "new status: FAILED ($CAUSE)"
    else
        log INFO "new status: $STATUS"
    fi
    <xsl:if test="$monitor"><xsl:value-of select="$monitor"/></xsl:if>
}

function accounting() {
    FUNCTION=$1
    TIME=$2
    log INFO "accounting for $FUNCTION: $TIME"
    <xsl:if test="$accounting"><xsl:value-of select="$accounting"/></xsl:if>
}

function _FAIL_() {
    CAUSE=$1
    change_state FAILED "$CAUSE"
    cleanup
    sleep 1     # prevent LRMS from returning before stdout and stderr are flushed
    exit 1      # exit with failure
}

function run() {
    FUNCTION=$1
    STATUS=$2
    log DEBUG "Entering function $FUNCTION"
    if test -x /usr/bin/time ; then
        /usr/bin/time -f "real=%e user=%U sys=%S mem=%K" -o $0.time $FUNCTION
        RETURN_CODE=$?
    else
        eval "time -p $FUNCTION &gt;stdout 2&gt;stderr" 2&gt;&amp;1 | tr " " "=" | tr "\n" " " &gt; $0.time
        RETURN_CODE=$?
        cat stdout
        cat stderr 1&gt;&amp;2
    fi
    TIME=`cat $0.time`
    rm -f $0.time
    if test $RETURN_CODE -eq 0 ; then
        change_state $STATUS
        accounting $FUNCTION $TIME
    else
        _FAIL_ "Failed to execute $FUNCTION"
    fi
}

function cleanup() {
    if test -f $0.newfile ; then
        cat $0.newfile | while read line ; do
            rm -rf $line
            if test $? -eq 0 ; then
                log DEBUG "File $line has been deleted"
            else
                log WARN "Failed to delete file $line"
            fi
        done
        rm -f $0.newfile
    fi

    if test -f $0.newdir ; then
        cat $0.newdir | while read line ; do
            rm -rf $line
            if test $? -eq 0 ; then
                log DEBUG "Directory $line has been deleted"
            else
                log WARN "Failed to delete directory $line"
            fi
        done
        rm -f $0.newdir
    fi
}

############### INITIALIZE ###############
function INITIALIZE() {
    . /etc/profile
}

############### INPUT_STAGING ###############
function INPUT_STAGING() {
        <xsl:for-each select="jsdl:DataStaging[jsdl:Source]">
            <xsl:variable name="LOCAL">
                <xsl:choose>
                    <xsl:when test="jsdl:Source/ext:Process/text()">
                        <xsl:value-of select="jsdl:FilesystemName/text()"/>
                        <xsl:text>/</xsl:text>
                        <xsl:call-template name="FILENAME">
                            <xsl:with-param name="path" select="jsdl:FileName/text()"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="jsdl:FileName/text()"/>
                    </xsl:otherwise>
                </xsl:choose>                
            </xsl:variable>
    # INPUT [<xsl:value-of select="@name"/>]
    if test -z "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
            <xsl:choose>
                <xsl:when test="jsdl:Source/jsdl:URI">
        # Download remote file
        <xsl:apply-templates select="jsdl:Source/jsdl:URI">
            <xsl:with-param name="local" select="$LOCAL"/>
        </xsl:apply-templates>
        if test -f <xsl:value-of select="$LOCAL"/> ; then
            echo <xsl:value-of select="$LOCAL"/> &gt;&gt; $0.newfile
            <xsl:value-of select="@name"/>=<xsl:value-of select="jsdl:FileName/text()"/>
            IS_FOUND_<xsl:value-of select="@name"/>=<xsl:value-of select="position()"/>
        else
            log WARN "Failed to download file <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/>"
        fi
                </xsl:when>
                <xsl:otherwise>
        # Use local file
        if test -e <xsl:value-of select="$LOCAL"/> ; then
            <xsl:value-of select="@name"/>=<xsl:value-of select="jsdl:FileName/text()"/>
            IS_FOUND_<xsl:value-of select="@name"/>=<xsl:value-of select="position()"/>
        else
            log INFO "Failed to find local file <xsl:value-of select="$LOCAL"/>"
        fi
                </xsl:otherwise>
            </xsl:choose>
            <xsl:for-each select="jsdl:Source/ext:Process/text()">
        # Preprocess
        if test -n "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
            SOURCE=<xsl:value-of select="$LOCAL"/>
            <xsl:value-of select="."/>
        fi
            </xsl:for-each>
    fi
            <xsl:variable name="current" select="@name"/>
            <xsl:if test="not(following-sibling::jsdl:DataStaging[@name=$current])">
    if test -n "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
        log DEBUG "\$<xsl:value-of select="@name"/>=$<xsl:value-of select="@name"/>"
        log DEBUG "\$IS_FOUND_<xsl:value-of select="@name"/>=$IS_FOUND_<xsl:value-of select="@name"/>"
    else
        _FAIL_ "Found no file for input <xsl:value-of select="@name"/>"
    fi
            </xsl:if>
        </xsl:for-each>

        <xsl:for-each select="jsdl:DataStaging[jsdl:Target]">
    # OUTPUT [<xsl:value-of select="@name"/>]
    if test -z "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
            <xsl:if test="not(jsdl:CreationFlag/text()='overwrite')">
        # Check output file existence
        if test -f <xsl:value-of select="jsdl:FileName/text()"/> ; then
            _FAIL_ "Output file already exists <xsl:value-of select="jsdl:FileName/text()"/>
        fi
            </xsl:if>
            <xsl:choose>
                <xsl:when test="jsdl:Target/jsdl:URI">
        # Upload remote file
        echo <xsl:value-of select="jsdl:FileName/text()"/> &gt;&gt; $0.newfile<xsl:text/>
        <xsl:value-of select="@name"/>=<xsl:value-of select="jsdl:FileName/text()"/>
        IS_FOUND_<xsl:value-of select="@name"/>=<xsl:value-of select="position()"/>
                </xsl:when>
                <xsl:otherwise>
        # Use local file
        touch <xsl:value-of select="jsdl:FileName/text()"/>
        if test -f <xsl:value-of select="jsdl:FileName/text()"/> ; then
            <xsl:value-of select="@name"/>=<xsl:value-of select="jsdl:FileName/text()"/>
            IS_FOUND_<xsl:value-of select="@name"/>=<xsl:value-of select="position()"/>
        else
            _FAIL_ "Can not create file <xsl:value-of select="jsdl:FileName/text()"/>"
        fi
                </xsl:otherwise>
            </xsl:choose>
    fi
            <xsl:variable name="current" select="@name"/>
            <xsl:if test="not(following-sibling::jsdl:DataStaging[@name=$current])">
    if test -n "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
        log DEBUG "\$<xsl:value-of select="@name"/>=$<xsl:value-of select="@name"/>"
        log DEBUG "\$IS_FOUND_<xsl:value-of select="@name"/>=$IS_FOUND_<xsl:value-of select="@name"/>"
    else
        _FAIL_ "No file can be created for output <xsl:value-of select="@name"/>"
    fi
            </xsl:if>
        </xsl:for-each>
}

############### USER_PROCESSING ###############
function USER_PROCESSING() {
        <xsl:for-each select="jsdl:Application/*">
            <xsl:choose>
                <xsl:when test="local-name()='POSIXApplication' or local-name()='SPMDApplication'">
                    <xsl:for-each select="posix:Environment">
                        <xsl:value-of select="@name"/>=<xsl:value-of select="text()"/><xsl:text>
</xsl:text>
                    </xsl:for-each>
                    <xsl:value-of select="posix:Executable/text()"/>
                    <xsl:for-each select="posix:Argument">
                        <xsl:text> </xsl:text><xsl:value-of select="text()"/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="local-name()='ScriptApplication'">
                    <xsl:value-of select="ext:Script/text()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">Unsupported application type: <xsl:value-of select="local-name()"/></xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
}

############### OUTPUT_STAGING ###############
function OUTPUT_STAGING() {
        <xsl:for-each select="jsdl:DataStaging[jsdl:Target]">
            <xsl:variable name="LOCAL">
                <xsl:choose>
                    <xsl:when test="jsdl:Target/ext:Process/text()">
                        <xsl:value-of select="jsdl:FilesystemName/text()"/>
                        <xsl:text>/</xsl:text>
                        <xsl:call-template name="FILENAME">
                            <xsl:with-param name="path" select="jsdl:FileName/text()"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="jsdl:FileName/text()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
    # OUTPUT [<xsl:value-of select="@name"/>]
    if test "$IS_FOUND_<xsl:value-of select="@name"/>" = "<xsl:value-of select="position()"/>" ; then
            <xsl:for-each select="jsdl:Target/ext:Process/text()">
        # Postprocess
        TARGET=<xsl:value-of select="$LOCAL"/><xsl:text>
        </xsl:text><xsl:value-of select="."/>
            </xsl:for-each>
            <xsl:choose>
                <xsl:when test="jsdl:Target/jsdl:URI">
        # Upload remote file
        <xsl:apply-templates select="jsdl:Target/jsdl:URI">
            <xsl:with-param name="local" select="$LOCAL"/>
        </xsl:apply-templates>
        log DEBUG "Output uploaded: <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/>"
                </xsl:when>
                <xsl:otherwise>
        # Use local file
        log DEBUG "Output ready: <xsl:value-of select="$LOCAL"/>"
                </xsl:otherwise>
            </xsl:choose>
    fi
        </xsl:for-each>
}

############### COMPLETE ###############
function COMPLETE() {
    cleanup
    sleep 1     # prevent LRMS from returning before stdout and stderr are flushed
    exit 0      # exit with success
}

############### MAIN ###############
change_state STARTED
run INITIALIZE      INITIALIZED
run INPUT_STAGING   INPUT_STAGED
        <xsl:for-each select="jsdl:Application/*">
run USER_PROCESSING USER_PROCESSED<xsl:text/>
            <xsl:for-each select="posix:Input">
                <xsl:text> &lt;</xsl:text><xsl:value-of select="text()"/>
            </xsl:for-each>
            <xsl:for-each select="posix:Output">
                <xsl:text> &gt;</xsl:text><xsl:value-of select="text()"/>
            </xsl:for-each>
            <xsl:for-each select="posix:Error">
                <xsl:text> 2&gt;</xsl:text><xsl:value-of select="text()"/>
            </xsl:for-each>
        </xsl:for-each><xsl:text>
</xsl:text>
run OUTPUT_STAGING  OUTPUT_STAGED
run COMPLETE        COMPLETED
    </xsl:template>

    <!-- ###########################################################################
         # Command line interfaces
         ###########################################################################
    -->
    <!-- ************************* srm ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'srm://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        <xsl:call-template name="INIT_SRM"/>
        $SRMCP<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="text()"/>
            <xsl:text> </xsl:text>file:///<xsl:value-of select="$local"/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'srm://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="INIT_SRM"/>
        $SRMCP<xsl:text/>
            <xsl:text> </xsl:text>file:///<xsl:value-of select="$local"/>
            <xsl:text> </xsl:text><xsl:value-of select="text()"/>
        if test $? -eq 2 ; then
        <xsl:call-template name="OVERWRITE_REMOTE">
            <xsl:with-param name="command">
            $SRMDEL <xsl:value-of select="text()"/>
            $SRMCP<xsl:text/>
                <xsl:text> </xsl:text>file:///<xsl:value-of select="$local"/>
                <xsl:text> </xsl:text><xsl:value-of select="text()"/>
            </xsl:with-param>
        </xsl:call-template>
        fi
    </xsl:template>
    <xsl:template name="INIT_SRM">
        WHICH=`which srmcp 2>/dev/null`
        if test -n "$WHICH" ; then
            SRMCP="$WHICH"
            SRMDEL=srm-advisory-delete
        elif test -x $GLITE_LOCATION/../d-cache/srm/bin/srmcp ; then
            SRMCP="$GLITE_LOCATION/../d-cache/srm/bin/srmcp"
            SRMDEL="$GLITE_LOCATION/../d-cache/srm/bin/srm-advisory-delete"
        else
            _FAIL_ "Command not found: srmcp"
        fi
    </xsl:template>

    <!-- ************************* lfn ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'lfn://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        <xsl:call-template name="INIT_LFN_DOWNLOAD"/>
        LFC_HOST=<xsl:call-template name="HOST"/>
        $LCG_CP<xsl:text/><!-- copy only -->
            <xsl:text> </xsl:text>lfn:<xsl:call-template name="PATH"/>
            <xsl:text> </xsl:text>file:///<xsl:value-of select="$local"/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'lfn://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="INIT_LFN_UPLOAD"/>
        LFC_HOST=<xsl:call-template name="HOST"/>
        $LCG_CR<xsl:text/><!-- copy and register (warning: reverse order of arguments) -->
            <xsl:text> </xsl:text>-l lfn:<xsl:call-template name="PATH"/>
            <xsl:text> </xsl:text>file:///<xsl:value-of select="$local"/>
        if test $? -eq 1 ; then
        <xsl:call-template name="OVERWRITE_REMOTE">
            <xsl:with-param name="command">
            $LCG_DEL <xsl:value-of select="text()"/>
            $LCG_CR<xsl:text/><!-- copy and register (warning: reverse order of arguments) -->
                <xsl:text> </xsl:text>-l lfn:<xsl:call-template name="PATH"/>
                <xsl:text> </xsl:text>file:///<xsl:value-of select="$local"/>
            </xsl:with-param>
        </xsl:call-template>
        fi
    </xsl:template>
    <xsl:template name="INIT_LFN_DOWNLOAD">
        WHICH=`which lcg-cp 2>/dev/null`
        if test -n "$WHICH" ; then
            LCG_CP="$WHICH"
        elif test -x $LCG_LOCATION/bin/lcg-cp ; then
            LCG_CP="$LCG_LOCATION/bin/lcg-cp"
        else
            _FAIL_ "Command not found: lcg-cp"
        fi
    </xsl:template>
    <xsl:template name="INIT_LFN_UPLOAD">
        WHICH=`which lcg-cr 2>/dev/null`
        if test -n "$WHICH" ; then
            LCG_CR="$WHICH"
            LCG_DEL=lcg-del
        elif test -x $LCG_LOCATION/bin/lcg-cr ; then
            LCG_CR="$LCG_LOCATION/bin/lcg-cr"
        else
            _FAIL_ "Command not found: lcg-cr"
        fi
    </xsl:template>

    <!-- ************************* srb ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'srb://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="INIT_SRB"/>
        Sget<xsl:text/>
            <xsl:text> </xsl:text><xsl:call-template name="SET_OVERWRITE"><xsl:with-param name="option">-f</xsl:with-param></xsl:call-template>
            <xsl:text> </xsl:text><xsl:call-template name="PATH"/>
            <xsl:text> </xsl:text><xsl:value-of select="$local"/>
        if test $? -ne 0 ; then
            _FAIL_ <xsl:call-template name="GET_ERROR"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        fi
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'srb://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="INIT_SRB"/>
        Sput<xsl:text/>
            <xsl:text> </xsl:text><xsl:call-template name="SET_OVERWRITE"><xsl:with-param name="option">-f</xsl:with-param></xsl:call-template>
            <xsl:text> </xsl:text><xsl:value-of select="$local"/>
            <xsl:text> </xsl:text><xsl:call-template name="PATH"/>
        if test $? -ne 0 ; then
            log WARN <xsl:call-template name="GET_ERROR"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        fi
    </xsl:template>
    <xsl:template name="INIT_SRB">
        mdasCollectionName="/home/USERNAME.DOMAIN"
        srbHost="<xsl:call-template name="HOST"/>"
        srbPort="<xsl:call-template name="PORT"/>"
        srbUser="<xsl:call-template name="USER"/>"
        mdasDomainName="sdsc"
        defaultResource="du-unix"
        AUTH_SCHEME="GSI_AUTH"
        Sinit
    </xsl:template>

    <!-- ************************* irods ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'irods://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="INIT_IRODS"/>
        iget<xsl:text/>
            <xsl:text> </xsl:text><xsl:call-template name="SET_OVERWRITE"><xsl:with-param name="option">-f</xsl:with-param></xsl:call-template>
            <xsl:text> </xsl:text><xsl:call-template name="PATH"/>
            <xsl:text> </xsl:text><xsl:value-of select="$local"/>
        if test $? -ne 0 ; then
            _FAIL_ <xsl:call-template name="GET_ERROR"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        fi
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'irods://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="INIT_IRODS"/>
        iput<xsl:text/>
            <xsl:text> </xsl:text><xsl:call-template name="SET_OVERWRITE"><xsl:with-param name="option">-f</xsl:with-param></xsl:call-template>
            <xsl:text> </xsl:text><xsl:value-of select="$local"/>
            <xsl:text> </xsl:text><xsl:call-template name="PATH"/>
        if test $? -ne 0 ; then
            log WARN <xsl:call-template name="GET_ERROR"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        fi
    </xsl:template>
    <xsl:template name="INIT_IRODS">
        <!-- todo -->
        iinit
    </xsl:template>

    <!-- ************************* http ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'http://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        wget<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="text()"/>
            <xsl:text> </xsl:text>--output-document=<xsl:value-of select="$local"/>
            <xsl:text> </xsl:text>--tries=1<xsl:text/>
    </xsl:template>

    <!-- ************************* https ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'https://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        wget<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="text()"/>
            <xsl:text> </xsl:text>--output-document=<xsl:value-of select="$local"/>
            <xsl:text> </xsl:text>--tries=1<xsl:text/>
            <xsl:text> </xsl:text>--no-check-certificate<xsl:text/>
    </xsl:template>

    <!-- ************************* ftp ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'ftp://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        wget<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="text()"/>
            <xsl:text> </xsl:text>--output-document=<xsl:value-of select="$local"/>
            <xsl:text> </xsl:text>--tries=1<xsl:text/>
            <xsl:text> </xsl:text>--ftp-user=<xsl:value-of select="'anonymous'"/>
            <xsl:text> </xsl:text>--ftp-password=<xsl:value-of select="'anon'"/>
    </xsl:template>

    <!-- ************************* gzip ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'gzip://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        gzip --decompress<xsl:text/>
            <xsl:text> </xsl:text><xsl:call-template name="PATH"/>
            <xsl:text> </xsl:text>--stdout &gt; <xsl:value-of select="$local"/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'gzip://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local"><xsl:call-template name="PATH"/></xsl:with-param></xsl:call-template>
        gzip<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="$local"/>
            <xsl:text> </xsl:text>--stdout &gt; <xsl:call-template name="PATH"/>
    </xsl:template>

    <!-- ************************* tar ************************* -->    
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'tar://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        <xsl:variable name="tarball" select="concat(substring-after(substring-before(text(),'.tar/'),'://'),'.tar')"/>
        <xsl:variable name="file" select="substring-after(text(),'.tar/')"/>
        tar x<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="$file"/>
            <xsl:text> </xsl:text>-f <xsl:value-of select="$tarball"/>
            <xsl:text> </xsl:text>--to-stdout &gt; <xsl:value-of select="$local"/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'tar://')]">
        <xsl:param name="local"/>
        <xsl:variable name="tarball" select="concat(substring-after(substring-before(text(),'.tar/'),'://'),'.tar')"/>
        <xsl:variable name="file" select="substring-after(text(),'.tar/')"/>
        <xsl:if test="not(../../jsdl:CreationFlag/text()='overwrite')">
        tar tf <xsl:value-of select="$tarball"/> | grep "^<xsl:value-of select="$file"/>$"
        if test $? -eq 0 ; then
            log WARN "File already exists: <xsl:value-of select="text()"/>, please set CreationFlag to overwrite"
        fi
        </xsl:if>
        mv <xsl:value-of select="$local"/> <xsl:value-of select="$file"/>
        tar r <xsl:value-of select="$file"/> -f <xsl:value-of select="$tarball"/>
        mv <xsl:value-of select="$file"/> <xsl:value-of select="$local"/>
    </xsl:template>

    <!-- ************************* tgz ************************* -->    
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'tgz://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        <xsl:variable name="tarball" select="concat(substring-after(substring-before(text(),'.tar.gz/'),'://'),'.tar')"/>
        <xsl:variable name="file" select="substring-after(text(),'.tar.gz/')"/>
        tar xz<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="$file"/>
            <xsl:text> </xsl:text>-f <xsl:value-of select="$tarball"/>
            <xsl:text> </xsl:text>--to-stdout &gt; <xsl:value-of select="$local"/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'tgz://')]">
        <xsl:param name="local"/>
        <xsl:variable name="tarball" select="concat(substring-after(substring-before(text(),'.tar.gz/'),'://'),'.tar')"/>
        <xsl:variable name="file" select="substring-after(text(),'.tar.gz/')"/>
        <xsl:if test="not(../../jsdl:CreationFlag/text()='overwrite')">
        tar tfz <xsl:value-of select="$tarball"/> | grep "^<xsl:value-of select="$file"/>$"
        if test $? -eq 0 ; then
            log WARN "File already exists: <xsl:value-of select="text()"/>, please set CreationFlag to overwrite"
        fi
        </xsl:if>
        mv <xsl:value-of select="$local"/> <xsl:value-of select="$file"/>
        tar rz <xsl:value-of select="$file"/> -f <xsl:value-of select="$tarball"/>
        mv <xsl:value-of select="$file"/> <xsl:value-of select="$local"/>
    </xsl:template>

    <!-- ************************* gsiftp ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'gsiftp://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        <xsl:call-template name="INIT_GSIFTP"/>
        $GLOBUS_URL_COPY -notpt<xsl:text/>
            <xsl:text> </xsl:text><xsl:call-template name="SET_PROTECTION"/>
            <xsl:text> </xsl:text>"<xsl:call-template name="FIXED_URL"/>"<xsl:text/>
            <xsl:text> </xsl:text>"file://<xsl:value-of select="$local"/>"<xsl:text/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'gsiftp://')]">
        <xsl:param name="local"/>
        <xsl:if test="not(../../jsdl:CreationFlag/text()='overwrite')">
            <xsl:message terminate="yes">Upload with gsiftp requires setting CreationFlag to overwrite</xsl:message>
        </xsl:if>
        <xsl:call-template name="INIT_GSIFTP"/>
        $GLOBUS_URL_COPY -notpt<xsl:text/>
            <xsl:text> </xsl:text><xsl:call-template name="SET_PROTECTION"/>
            <xsl:text> </xsl:text>"file://<xsl:value-of select="$local"/>"<xsl:text/>
            <xsl:text> </xsl:text>"<xsl:call-template name="FIXED_URL"/>"<xsl:text/>
    </xsl:template>
    <xsl:template name="SET_PROTECTION"><!-- not supported by gsiftp-v1 -->
        <xsl:choose>
            <xsl:when test="$protection='none'">-nodcau</xsl:when>
            <xsl:when test="$protection='integrity'">-dcsafe</xsl:when>
            <xsl:when test="$protection='confidentiality'">-dcpriv</xsl:when>
            <xsl:otherwise/>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="FIXED_URL"><!-- java-based command requires 2 slash for absolute path -->
        <xsl:variable name="host"><xsl:call-template name="HOST"/></xsl:variable>
        <xsl:variable name="port"><xsl:call-template name="PORT"/></xsl:variable>
        <xsl:variable name="path"><xsl:call-template name="PATH"/></xsl:variable>
        <xsl:text>gsiftp://</xsl:text>
        <xsl:value-of select="$host"/>
        <xsl:if test="$port">:<xsl:value-of select="$port"/></xsl:if>
        <xsl:text>/</xsl:text>
        <xsl:choose>
            <xsl:when test="starts-with($path,'./')"><xsl:value-of select="substring-after($path,'./')"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="$path"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="INIT_GSIFTP">
        WHICH=`which globus-url-copy 2>/dev/null`
        if test -n "$WHICH" ; then
            GLOBUS_URL_COPY="$WHICH"
        elif test -x $GLOBUS_LOCATION/bin/globus-url-copy ; then
            GLOBUS_URL_COPY="$GLOBUS_LOCATION/bin/globus-url-copy"
        elif test -x $GLOBUS_LOCATION/bin/globus-url-copy.bat ; then
            GLOBUS_URL_COPY="$GLOBUS_LOCATION/bin/globus-url-copy.bat"
        else
            _FAIL_ "Command not found: globus-url-copy"
        fi
    </xsl:template>

    <!-- ************************* gsiftp v1 ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'gsiftp-v1://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        <xsl:call-template name="INIT_GSIFTP"/>
        $GLOBUS_URL_COPY -notpt<xsl:text/>
            <xsl:text> </xsl:text>"<xsl:call-template name="FIXED_URL"/>"<xsl:text/>
            <xsl:text> </xsl:text>"file://<xsl:value-of select="$local"/>"<xsl:text/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'gsiftp-v1://')]">
        <xsl:param name="local"/>
        <xsl:if test="not(../../jsdl:CreationFlag/text()='overwrite')">
            <xsl:message terminate="yes">Upload with gsiftp requires setting CreationFlag to overwrite</xsl:message>
        </xsl:if>
        <xsl:call-template name="INIT_GSIFTP"/>
        $GLOBUS_URL_COPY -notpt<xsl:text/>
            <xsl:text> </xsl:text>"file://<xsl:value-of select="$local"/>"<xsl:text/>
            <xsl:text> </xsl:text>"<xsl:call-template name="FIXED_URL"/>"<xsl:text/>
    </xsl:template>
    
    <!-- ************************* other... ************************* -->
    <xsl:template match="jsdl:URI">
        <xsl:message terminate="yes">Protocol not supported: <xsl:value-of select="substring-before(text(),':')"/></xsl:message>
    </xsl:template>

    <xsl:template name="SET_OVERWRITE">
        <xsl:param name="option"/>
        <xsl:if test="../../jsdl:CreationFlag/text()='overwrite'">
            <xsl:value-of select="$option"/>
        </xsl:if>
    </xsl:template>
    <xsl:template name="GET_ERROR">
        <xsl:param name="local"/>
        <xsl:choose>
            <xsl:when test="../../jsdl:CreationFlag/text()='overwrite'">"Failed to upload file: <xsl:value-of select="$local"/>"</xsl:when>
            <xsl:otherwise>"File already exists: <xsl:value-of select="$local"/>, please set CreationFlag to overwrite"</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="REMOVE_LOCAL">
        <xsl:param name="local"/>
        if test -e <xsl:value-of select="$local"/> ; then
            <xsl:choose>
                <xsl:when test="../../jsdl:CreationFlag/text()='overwrite'">
            rm -f <xsl:value-of select="$local"/>
                </xsl:when>
                <xsl:otherwise>
            _FAIL_ "File already exists: <xsl:value-of select="$local"/>, please set CreationFlag to overwrite"
                </xsl:otherwise>
            </xsl:choose>
        fi
    </xsl:template>

    <xsl:template name="OVERWRITE_REMOTE">
        <xsl:param name="command"/>
        <xsl:choose>
            <xsl:when test="../../jsdl:CreationFlag/text()='overwrite'">
                <xsl:value-of select="$command"/>
            </xsl:when>
            <xsl:otherwise>
            log WARN "File already exists: <xsl:value-of select="text()"/>, please set CreationFlag to overwrite"
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ###########################################################################
         # For testing only
         ###########################################################################
    -->
    <xsl:template match="test">
URL=<xsl:value-of select="text()"/>
SCHEME=<xsl:call-template name="SCHEME"/>
USER=<xsl:call-template name="USER"/>
HOST=<xsl:call-template name="HOST"/>
PORT=<xsl:call-template name="PORT"/>
PATH=<xsl:call-template name="PATH"/>
    </xsl:template>

    <!-- ###########################################################################
         # Named templates
         ###########################################################################
    -->
    <xsl:template name="FILENAME">
        <xsl:param name="path"/>
        <xsl:choose>
            <xsl:when test="contains($path,'/')">
                <xsl:call-template name="FILENAME">
                    <xsl:with-param name="path" select="substring-after($path,'/')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$path"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="SCHEME">
        <xsl:param name="url" select="text()"/>
        <xsl:choose>
            <xsl:when test="contains($url,'://')">
                <xsl:value-of select="substring-before($url,'://')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">URL contains no scheme: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="USER">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="suffix">
            <xsl:choose>
                <xsl:when test="contains($url,'://')">
                    <xsl:value-of select="substring-after($url,'://')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">URL contains no scheme: <xsl:value-of select="$url"/></xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($suffix,'@')">
                <xsl:value-of select="substring-before($suffix,'@')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="no">URL contains no userInfo: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="HOST">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="suffix">
            <xsl:choose>
                <xsl:when test="contains($url,'@')">
                    <xsl:value-of select="substring-after($url,'@')"/>
                </xsl:when>
                <xsl:when test="contains($url,'://')">
                    <xsl:value-of select="substring-after($url,'://')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">URL contains no scheme: <xsl:value-of select="$url"/></xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($suffix,':') and not(starts-with($url,'file://'))">
                <xsl:value-of select="substring-before($suffix,':')"/>
            </xsl:when>
            <xsl:when test="contains($suffix,'/') and not(starts-with($suffix,'.')) and not(starts-with($suffix,'$'))">
                <xsl:value-of select="substring-before($suffix,'/')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="no">URL contains no host: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="PORT">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="suffix">
            <xsl:choose>
                <xsl:when test="contains($url,'://')">
                    <xsl:value-of select="substring-after($url,'://')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">URL contains no scheme: <xsl:value-of select="$url"/></xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($suffix,':')">
                <xsl:variable name="suffix2" select="substring-after($suffix,':')"/>
                <xsl:choose>
                    <xsl:when test="contains($suffix2,'/')">
                        <xsl:value-of select="substring-before($suffix2,'/')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$suffix2"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="no">URL contains no port: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="PATH">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="suffix">
            <xsl:choose>
                <xsl:when test="contains($url,'://')">
                    <xsl:value-of select="substring-after($url,'://')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">URL contains no scheme: <xsl:value-of select="$url"/></xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="starts-with($suffix,'/') or starts-with($suffix,'.') or starts-with($suffix,'$')">
                <xsl:value-of select="$suffix"/>
            </xsl:when>
            <xsl:when test="contains($suffix,'/')">
                <xsl:variable name="suffix2" select="substring-after($suffix,'/')"/>
                <xsl:if test="not(starts-with($suffix2,'./') or starts-with($suffix2,'../'))">
                    <xsl:text>/</xsl:text>
                </xsl:if>
                <xsl:value-of select="substring-after($suffix,'/')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="no">URL contains no path: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>