<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jsdl="http://schemas.ggf.org/jsdl/2005/11/jsdl"
                xmlns:posix="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix"
                xmlns:spmd="http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd"
                xmlns:ext="http://www.in2p3.fr/jsdl-extension"
                xmlns:cfg="http://www.in2p3.fr/jsaga">
    <!-- ###########################################################################
         # Generate job wrapper script
         ###########################################################################
    -->
    <xsl:output method="text" indent="yes" encoding="latin1"/>
    <xsl:strip-space elements="*"/>

    <!-- resource -->
    <xsl:param name="resourceId"/><!-- required -->
    <xsl:param name="gridName"/><!-- required -->
    <xsl:param name="Intermediary"/><!-- optional -->
    <xsl:param name="workerMountPoint"/><!-- optional -->
    <xsl:param name="protection" select="'integrity'"/><!-- optional -->

    <!-- configuration -->
    <xsl:variable name="config" select="document('var/jsaga-merged-config.xml')/cfg:effective-config"/>
    <xsl:variable name="resourceScheme" select="substring-before($resourceId, '://')"/>
    <xsl:variable name="jobService" select="$config/cfg:execution[@scheme=$resourceScheme]/cfg:jobService[@name=$gridName]"/>

    <!-- entry point -->
    <xsl:template match="/">
        <xsl:apply-templates select="ext:Job/jsdl:JobDefinition/jsdl:JobDescription"/>
        <xsl:apply-templates select="test"/>
    </xsl:template>

    <!-- script -->
    <xsl:template match="jsdl:JobDescription">#!/bin/sh
function log() {
    LEVEL=$1
    MESSAGE=$2
    echo "$LEVEL: ["`date "+%d/%m/%Y %T,%2N"`"] - $MESSAGE"
    <xsl:for-each select="$jobService/cfg:logging/text()"><xsl:value-of select="."/></xsl:for-each>
}

function change_state() {
    STATUS=$1
    CAUSE=$2
    if test "$STATUS" = "FAILED" ; then
        log FATAL "new status: FAILED ($CAUSE)"
    else
        log INFO "new status: $STATUS"
    fi
    <xsl:for-each select="$jobService/cfg:monitoring/text()"><xsl:value-of select="."/></xsl:for-each>
}

function accounting() {
    FUNCTION=$1
    TIME="$2 $3 $4 $5"
    log INFO "accounting for $FUNCTION: $TIME"
    <xsl:for-each select="$jobService/cfg:accounting/text()"><xsl:value-of select="."/></xsl:for-each>
}

function _FAIL_() {
    CAUSE=$1
    if test "$_FAILED_" != "true" ; then
        change_state FAILED "$CAUSE"
        _FAILED_=true
    fi
    return 1    # return with failure
}

#if test -x /usr/bin/time ; then
#    declare -fx log
#    declare -fx change_state
#    declare -fx accounting
#    declare -fx _FAIL_
#fi
function run_time() {
    FUNCTION=$1
    <!-- todo: save all variables in a file and source it -->
#    if test -x /usr/bin/time ; then
#        declare -fx $FUNCTION
#        /usr/bin/time -f "real=%e user=%U sys=%S mem=%K" -o $TMPFILE.time bash -c $FUNCTION
#        RETURN_CODE=$?
#        unset $FUNCTION
#    else
        eval "time -p $FUNCTION &gt;stdout 2&gt;stderr" 2&gt;$TMPFILE.time
        RETURN_CODE=$?
        cat $TMPFILE.time | tr " " "=" | tr "\n" " " &gt; $TMPFILE.time
        cat stdout
        cat stderr 1&gt;&amp;2
        rm -f stdout stderr
#    fi
    return $RETURN_CODE
}

function run() {
    FUNCTION=$1
    STATUS=$2
    log DEBUG "Entering function: $FUNCTION"
    if test "$FUNCTION" = "USER_PROCESSING" ; then
        <xsl:for-each select="jsdl:Application/*">
        run_time $FUNCTION<xsl:text/>
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
    else
        run_time $FUNCTION
    fi
    RETURN_CODE=$?
    TIME=`cat $TMPFILE.time`
    rm -f $TMPFILE.time
    if test $RETURN_CODE -eq 0 ; then
        change_state $STATUS
        accounting $FUNCTION $TIME
    else
        _FAIL_ "Failed to execute function: $FUNCTION"
        cleanup
        sleep 1     # prevent LRMS from returning before stdout and stderr are flushed
        exit 1      # exit with failure
    fi
}

function cleanup() {
    if test -f $TMPFILE.newfile ; then
        cat $TMPFILE.newfile | while read line ; do
            rm -rf $line
            if test $? -eq 0 ; then
                log DEBUG "File has been deleted: $line"
            else
                log WARN "Failed to delete file: $line"
            fi
        done
        rm -f $TMPFILE.newfile
    fi

    if test -f $TMPFILE.newdir ; then
        cat $TMPFILE.newdir | while read line ; do
            rm -rf $line
            if test $? -eq 0 ; then
                log DEBUG "Directory has been deleted: $line"
            else
                log WARN "Failed to delete directory: $line"
            fi
        done
        rm -f $TMPFILE.newdir
    fi
    log INFO "Worker has been cleaned up"
}

############### INITIALIZE ###############
function INITIALIZE() {
        <xsl:if test="not($jobService/cfg:proloque/text())">
    log DEBUG "no prologue for job: $JOBNAME"
        </xsl:if>
    <xsl:for-each select="$jobService/cfg:prologue/text()"><xsl:value-of select="."/></xsl:for-each>
}

############### INPUT_STAGING ###############
function INPUT_STAGING() {
        <xsl:if test="not(jsdl:DataStaging[jsdl:Source])">
    log INFO "no input staging for job: $JOBNAME"
        </xsl:if>
        <xsl:for-each select="jsdl:DataStaging[jsdl:Source]">
            <xsl:variable name="LOCAL">
                <xsl:choose>
                    <xsl:when test="jsdl:Source/ext:Process/text()">
                        <xsl:value-of select="jsdl:FilesystemName/text()"/>
                        <xsl:text>/</xsl:text>
                        <xsl:call-template name="FILENAME">
                            <xsl:with-param name="path" select="jsdl:Source/jsdl:URI/text()"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="jsdl:FileName/text()"/>
                    </xsl:otherwise>
                </xsl:choose>                
            </xsl:variable>
            <xsl:variable name="INTERMEDIARY">
                <xsl:if test="jsdl:Source/jsdl:URI and $Intermediary and $workerMountPoint">
                    <xsl:variable name="baseUri">
                        <xsl:call-template name="BASEURL"><xsl:with-param name="url" select="jsdl:Source/jsdl:URI/text()"/></xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="baseIntermediary">
                        <xsl:call-template name="BASEURL"><xsl:with-param name="url" select="$Intermediary"/></xsl:call-template>
                    </xsl:variable>
                    <xsl:value-of select="$workerMountPoint"/>
                    <xsl:value-of select="substring-after($baseUri, $baseIntermediary)"/>
                </xsl:if>
            </xsl:variable>
    # INPUT [<xsl:value-of select="@name"/>]
    if test -z "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
            <xsl:choose>
                <xsl:when test="jsdl:Source/jsdl:URI and $workerMountPoint and not(ext:CopyLocally='true')">
        # Access intermediary file directly
        if test -e <xsl:value-of select="$INTERMEDIARY"/> ; then
            log DEBUG "Accessing intermediary file directly via shared file system"
            <xsl:value-of select="@name"/>=<xsl:value-of select="$INTERMEDIARY"/>
            IS_FOUND_<xsl:value-of select="@name"/>=<xsl:value-of select="position()"/>
        else
            log INFO "Intermediary file not found: <xsl:value-of select="$INTERMEDIARY"/>"
        fi
                </xsl:when>
                <xsl:when test="jsdl:Source/jsdl:URI">
        # Create directory if needed
        <xsl:value-of select="@name"/>=<xsl:value-of select="jsdl:FileName/text()"/>
        if test ! -d ${<xsl:value-of select="@name"/>%/*} ; then
            echo ${<xsl:value-of select="@name"/>%/*} &gt;&gt; $TMPFILE.newdir
            mkdir -p ${<xsl:value-of select="@name"/>%/*}
        fi

                    <xsl:choose>
                        <xsl:when test="$workerMountPoint">
        # Copy <xsl:value-of select="$LOCAL"/> from <xsl:value-of select="$INTERMEDIARY"/>
                    <xsl:if test="jsdl:CreationFlag/text()='overwrite'"> (overwrite)</xsl:if>
        log DEBUG "Copying intermediary file via shared file system"
        <xsl:for-each select="jsdl:Source/jsdl:URI"><xsl:call-template name="COPY">
            <xsl:with-param name="source" select="$INTERMEDIARY"/>
            <xsl:with-param name="target" select="$LOCAL"/>
        </xsl:call-template></xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
        # Get <xsl:value-of select="$LOCAL"/> from <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/>
                    <xsl:if test="jsdl:CreationFlag/text()='overwrite'"> (overwrite)</xsl:if>
        log DEBUG "Getting file from remote server"
        <xsl:apply-templates select="jsdl:Source/jsdl:URI">
            <xsl:with-param name="local" select="$LOCAL"/>
        </xsl:apply-templates>
                        </xsl:otherwise>
                    </xsl:choose>
        if test -f <xsl:value-of select="$LOCAL"/> ; then
            echo <xsl:value-of select="$LOCAL"/> &gt;&gt; $TMPFILE.newfile
            IS_FOUND_<xsl:value-of select="@name"/>=<xsl:value-of select="position()"/>
        else
            log WARN "Failed to get file: <xsl:value-of select="jsdl:Source/jsdl:URI/text()"/>"
        fi
                </xsl:when>
                <xsl:when test="not(jsdl:Source/jsdl:URI)">
        # Access local file directly
        if test -e <xsl:value-of select="$LOCAL"/> ; then
            log DEBUG "Accessing local file directly"
            <xsl:value-of select="@name"/>=<xsl:value-of select="jsdl:FileName/text()"/>
            IS_FOUND_<xsl:value-of select="@name"/>=<xsl:value-of select="position()"/>
        else
            log INFO "Local file not found: <xsl:value-of select="$LOCAL"/>"
        fi
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">Inconsistent data staging description</xsl:message>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="jsdl:Source/ext:Process/text()">
        # Preprocess
        if test -n "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
            log DEBUG "Preprocessing <xsl:value-of select="$LOCAL"/>"
            echo <xsl:value-of select="jsdl:FileName/text()"/> &gt;&gt; $TMPFILE.newfile
            SOURCE=<xsl:value-of select="$LOCAL"/><xsl:text>
            </xsl:text><xsl:value-of select="jsdl:Source/ext:Process/text()"/>
        fi
            </xsl:if>
    fi
            <xsl:variable name="current" select="@name"/>
            <xsl:if test="not(following-sibling::jsdl:DataStaging[@name=$current])">
    if test -n "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
        log DEBUG "\$<xsl:value-of select="@name"/>=$<xsl:value-of select="@name"/>"
        log DEBUG "\$IS_FOUND_<xsl:value-of select="@name"/>=$IS_FOUND_<xsl:value-of select="@name"/>"
    else
        _FAIL_ "Found no file for input: <xsl:value-of select="@name"/>"
        return $?
    fi
            </xsl:if>
        </xsl:for-each>

        <xsl:for-each select="jsdl:DataStaging[jsdl:Target]">
            <xsl:variable name="INTERMEDIARY">
                <xsl:if test="jsdl:Target/jsdl:URI and $Intermediary and $workerMountPoint">
                    <xsl:variable name="baseUri">
                        <xsl:call-template name="BASEURL"><xsl:with-param name="url" select="jsdl:Target/jsdl:URI/text()"/></xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="baseIntermediary">
                        <xsl:call-template name="BASEURL"><xsl:with-param name="url" select="$Intermediary"/></xsl:call-template>
                    </xsl:variable>
                    <xsl:value-of select="$workerMountPoint"/>
                    <xsl:value-of select="substring-after($baseUri, $baseIntermediary)"/>
                </xsl:if>
            </xsl:variable>
    # OUTPUT [<xsl:value-of select="@name"/>]
    if test -z "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
            <xsl:choose>
                <xsl:when test="jsdl:Target/jsdl:URI and $workerMountPoint and not(ext:CopyLocally='true')">
        # Access intermediary file directly
        log DEBUG "Accessing intermediary file directly via shared file system"
        <xsl:value-of select="@name"/>=<xsl:value-of select="$INTERMEDIARY"/>
                </xsl:when>
                <xsl:when test="jsdl:Target/jsdl:URI">
        # Set file to put
        log DEBUG "Setting file to put on remote server"
        <xsl:value-of select="@name"/>=<xsl:value-of select="jsdl:FileName/text()"/>
        echo <xsl:value-of select="jsdl:FileName/text()"/> &gt;&gt; $TMPFILE.newfile

        # Create directory if needed
        if test ! -d ${<xsl:value-of select="@name"/>%/*} ; then
            echo ${<xsl:value-of select="@name"/>%/*} &gt;&gt; $TMPFILE.newdir
            mkdir -p ${<xsl:value-of select="@name"/>%/*}
        fi
                </xsl:when>
                <xsl:when test="not(jsdl:Target/jsdl:URI)">
        # Access local file directly
        log DEBUG "Accessing local file directly"
        <xsl:value-of select="@name"/>=<xsl:value-of select="jsdl:FileName/text()"/>

        # Create directory if needed (will not be removed)
        if test ! -d ${<xsl:value-of select="@name"/>%/*} ; then
            mkdir -p ${<xsl:value-of select="@name"/>%/*}
        fi
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">Inconsistent data staging description</xsl:message>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="not(jsdl:CreationFlag/text()='overwrite')">

        # Check output file does not exist
        if test -f $<xsl:value-of select="@name"/> ; then
            _FAIL_ "Output file already exists: $<xsl:value-of select="@name"/>"
            return $?
        fi
            </xsl:if>

        # Check output file can be created
        if test -d ${<xsl:value-of select="@name"/>%/*} -a -w ${<xsl:value-of select="@name"/>%/*} ; then
            IS_FOUND_<xsl:value-of select="@name"/>=<xsl:value-of select="position()"/>
        else
            log INFO "Can not create file: $<xsl:value-of select="@name"/>"
        fi
    fi
            <xsl:variable name="current" select="@name"/>
            <xsl:if test="not(following-sibling::jsdl:DataStaging[@name=$current])">
    if test -n "$IS_FOUND_<xsl:value-of select="@name"/>" ; then
        log DEBUG "\$<xsl:value-of select="@name"/>=$<xsl:value-of select="@name"/>"
        log DEBUG "\$IS_FOUND_<xsl:value-of select="@name"/>=$IS_FOUND_<xsl:value-of select="@name"/>"
    else
        _FAIL_ "No file can be created for output: <xsl:value-of select="@name"/>"
        return $?
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
    export <xsl:value-of select="@name"/>=<xsl:value-of select="text()"/>
                    </xsl:for-each><xsl:text>
    </xsl:text><xsl:value-of select="posix:Executable/text()"/>
                    <xsl:for-each select="posix:Argument">
                        <xsl:text> </xsl:text><xsl:value-of select="text()"/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="local-name()='ScriptApplication'">
                    <xsl:value-of select="ext:Script/text()"/>
                    <xsl:if test="not(ext:Script/text())">
                        <xsl:message terminate="yes">ScriptApplication requires element Script</xsl:message>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">Unsupported application type: <xsl:value-of select="local-name()"/></xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        <xsl:if test="not(jsdl:Application/*)">
            <xsl:message terminate="yes">Job has no application element</xsl:message>
        </xsl:if>
}

############### OUTPUT_STAGING ###############
function OUTPUT_STAGING() {
        <xsl:if test="not(jsdl:DataStaging[jsdl:Target])">
    log INFO "no output staging for job: $JOBNAME"
        </xsl:if>
        <xsl:for-each select="jsdl:DataStaging[jsdl:Target]">
            <xsl:variable name="LOCAL">
                <xsl:choose>
                    <xsl:when test="jsdl:Target/ext:Process/text()">
                        <xsl:value-of select="jsdl:FilesystemName/text()"/>
                        <xsl:text>/</xsl:text>
                        <xsl:call-template name="FILENAME">
                            <xsl:with-param name="path" select="jsdl:Target/jsdl:URI/text()"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="jsdl:FileName/text()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="INTERMEDIARY">
                <xsl:if test="jsdl:Target/jsdl:URI and $Intermediary and $workerMountPoint">
                    <xsl:variable name="baseUri">
                        <xsl:call-template name="BASEURL"><xsl:with-param name="url" select="jsdl:Target/jsdl:URI/text()"/></xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="baseIntermediary">
                        <xsl:call-template name="BASEURL"><xsl:with-param name="url" select="$Intermediary"/></xsl:call-template>
                    </xsl:variable>
                    <xsl:value-of select="$workerMountPoint"/>
                    <xsl:value-of select="substring-after($baseUri, $baseIntermediary)"/>
                </xsl:if>
            </xsl:variable>
    # OUTPUT [<xsl:value-of select="@name"/>]
    if test "$IS_FOUND_<xsl:value-of select="@name"/>" = "<xsl:value-of select="position()"/>" ; then
        # Check output file exists
        if test ! -e $<xsl:value-of select="@name"/> ; then
            _FAIL_ "Output file has not been created: $<xsl:value-of select="@name"/>"
            return $?
        fi
            <xsl:if test="jsdl:Target/ext:Process/text()">
        # Postprocess
        log DEBUG "Postprocessing <xsl:value-of select="$LOCAL"/>"
        echo <xsl:value-of select="$LOCAL"/> &gt;&gt; $TMPFILE.newfile
        TARGET=<xsl:value-of select="$LOCAL"/><xsl:text>
        </xsl:text><xsl:value-of select="jsdl:Target/ext:Process/text()"/><xsl:text>
        </xsl:text>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="jsdl:Target/jsdl:URI and $workerMountPoint and not(ext:CopyLocally='true')">
        # Access intermediary file directly
        log DEBUG "Intermediary file is available: <xsl:value-of select="$INTERMEDIARY"/>"
                </xsl:when>
                <xsl:when test="jsdl:Target/jsdl:URI">
                    <xsl:choose>
                        <xsl:when test="$workerMountPoint">
        # Copy <xsl:value-of select="$LOCAL"/> to <xsl:value-of select="$INTERMEDIARY"/>
                    <xsl:if test="jsdl:CreationFlag/text()='overwrite'"> (overwrite)</xsl:if>
        log DEBUG "Copying intermediary file via shared file system"
        <xsl:for-each select="jsdl:Target/jsdl:URI"><xsl:call-template name="COPY">
            <xsl:with-param name="source" select="$LOCAL"/>
            <xsl:with-param name="target" select="$INTERMEDIARY"/>
        </xsl:call-template></xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
        # Put <xsl:value-of select="$LOCAL"/> to <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/>
                    <xsl:if test="jsdl:CreationFlag/text()='overwrite'"> (overwrite)</xsl:if>
        log DEBUG "Putting file to remote server"
        <xsl:apply-templates select="jsdl:Target/jsdl:URI">
            <xsl:with-param name="local" select="$LOCAL"/>
        </xsl:apply-templates>
                        </xsl:otherwise>
                    </xsl:choose>
        log DEBUG "Remote file is available: <xsl:value-of select="jsdl:Target/jsdl:URI/text()"/>"
                </xsl:when>
                <xsl:when test="not(jsdl:Target/jsdl:URI)">
        # Access local file directly
        log DEBUG "Local file is available: <xsl:value-of select="$LOCAL"/>"
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">Inconsistent data staging description</xsl:message>
                </xsl:otherwise>
            </xsl:choose>
    fi
        </xsl:for-each>
}

############### MAIN ###############
if test -f /etc/profile ; then
    OLD_PWD=$PWD
    . /etc/profile
    cd $OLD_PWD
    log INFO "sourced file: /etc/profile"
fi
export JOBNAME=<xsl:value-of select="jsdl:JobIdentification/jsdl:JobName/text()"/>
log INFO "JOBNAME=$JOBNAME"
export TMPFILE=$PWD/$JOBNAME-$$
log INFO "TMPFILE=$TMPFILE"

change_state STARTED
run INITIALIZE      INITIALIZED
run INPUT_STAGING   INPUT_STAGED
run USER_PROCESSING USER_PROCESSED
run OUTPUT_STAGING  OUTPUT_STAGED
change_state COMPLETED

cleanup
sleep 1     # prevent LRMS from returning before stdout and stderr are flushed
exit 0      # exit with success
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
            return $?
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
            return $?
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
            return $?
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
            return $?
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
        srbHost="<xsl:call-template name="HOST"/>"
        srbPort="<xsl:call-template name="PORT"/>"
        srbUser="<xsl:call-template name="USER"/>"
        mdasDomainName="<xsl:call-template name="QUERY"/>"
        AUTH_SCHEME="GSI_AUTH"
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
            return $?
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
        <xsl:variable name="tarball"><xsl:call-template name="TARBALL"/></xsl:variable>
        <xsl:variable name="file"><xsl:call-template name="FILE_IN_TARBALL"/></xsl:variable>
        tar x<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="$file"/>
            <xsl:text> </xsl:text>-f <xsl:value-of select="$tarball"/>
            <xsl:text> </xsl:text>--to-stdout &gt; <xsl:value-of select="$local"/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'tar://')]">
        <xsl:param name="local"/>
        <xsl:variable name="tarball"><xsl:call-template name="TARBALL"/></xsl:variable>
        <xsl:variable name="file"><xsl:call-template name="FILE_IN_TARBALL"/></xsl:variable>
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
        <xsl:variable name="tarball"><xsl:call-template name="TARBALL"/></xsl:variable>
        <xsl:variable name="file"><xsl:call-template name="FILE_IN_TARBALL"/></xsl:variable>
        tar xz<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="$file"/>
            <xsl:text> </xsl:text>-f <xsl:value-of select="$tarball"/>
            <xsl:text> </xsl:text>--to-stdout &gt; <xsl:value-of select="$local"/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'tgz://')]">
        <xsl:param name="local"/>
        <xsl:variable name="tarball"><xsl:call-template name="TARBALL"/></xsl:variable>
        <xsl:variable name="file"><xsl:call-template name="FILE_IN_TARBALL"/></xsl:variable>
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
    <xsl:template name="TARBALL">
        <xsl:call-template name="DECOMPOSE_TAR_URL"><xsl:with-param name="file">tarball</xsl:with-param></xsl:call-template>
    </xsl:template>
    <xsl:template name="FILE_IN_TARBALL">
        <xsl:call-template name="DECOMPOSE_TAR_URL"><xsl:with-param name="file">file_in_tarball</xsl:with-param></xsl:call-template>
    </xsl:template>
    <xsl:template name="DECOMPOSE_TAR_URL">
        <xsl:param name="file"/>
        <xsl:variable name="scheme" select="substring-before(text(), '://')"/>
        <xsl:choose>
            <xsl:when test="$scheme='tar' and contains(text(),'.tar/')">
                <xsl:call-template name="DO_DECOMPOSE_TAR_URL">
                    <xsl:with-param name="file" select="$file"/>
                    <xsl:with-param name="extension" select="'tar'"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$scheme='tgz' and contains(text(),'.tar.gz/')">
                <xsl:call-template name="DO_DECOMPOSE_TAR_URL">
                    <xsl:with-param name="file" select="$file"/>
                    <xsl:with-param name="extension" select="'tar.gz'"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$scheme='tgz' and contains(text(),'.tgz/')">
                <xsl:call-template name="DO_DECOMPOSE_TAR_URL">
                    <xsl:with-param name="file" select="$file"/>
                    <xsl:with-param name="extension" select="'tgz'"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains(text(),'://$')">
                <xsl:variable name="path" select="substring-after(text(), '://')"/>
                <xsl:choose>
                    <xsl:when test="$file='tarball'"><xsl:value-of select="substring-before($path,'/')"/></xsl:when>
                    <xsl:when test="$file='file_in_tarball'"><xsl:value-of select="substring-after($path,'/')"/></xsl:when>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">Bad URL: <xsl:value-of select="text()"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="DO_DECOMPOSE_TAR_URL">
        <xsl:param name="file"/>
        <xsl:param name="extension"/>
        <xsl:variable name="path" select="substring-after(text(), '://')"/>
        <xsl:choose>
            <xsl:when test="$file='tarball'">
                <xsl:value-of select="substring-before($path, concat('.',$extension,'/'))"/>.<xsl:value-of select="$extension"/>
            </xsl:when>
            <xsl:when test="$file='file_in_tarball'">
                <xsl:value-of select="substring-after($path, concat('.',$extension,'/'))"/>
            </xsl:when>
        </xsl:choose>
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
            return $?
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
    
    <!-- ************************* file ************************* -->
    <xsl:template match="jsdl:Source/jsdl:URI[starts-with(text(),'file://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$local"/></xsl:call-template>
        cp<xsl:text/>
            <xsl:text> </xsl:text><xsl:call-template name="PATH"/>
            <xsl:text> </xsl:text><xsl:value-of select="$local"/>
    </xsl:template>
    <xsl:template match="jsdl:Target/jsdl:URI[starts-with(text(),'file://')]">
        <xsl:param name="local"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local"><xsl:call-template name="PATH"/></xsl:with-param></xsl:call-template>
        cp<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="$local"/>
            <xsl:text> </xsl:text><xsl:call-template name="PATH"/>
    </xsl:template>

    <!-- ************************* other... ************************* -->
    <xsl:template match="jsdl:URI">
        <xsl:message terminate="yes">Protocol not supported: <xsl:value-of select="substring-before(text(),':')"/></xsl:message>
    </xsl:template>

    <!-- ******************* explicit local copy ******************** -->
    <xsl:template name="COPY">
        <xsl:param name="source"/>
        <xsl:param name="target"/>
        <xsl:call-template name="REMOVE_LOCAL"><xsl:with-param name="local" select="$target"/></xsl:call-template>
        cp<xsl:text/>
            <xsl:text> </xsl:text><xsl:value-of select="$source"/>
            <xsl:text> </xsl:text><xsl:value-of select="$target"/>
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
            <xsl:when test="../../jsdl:CreationFlag/text()='overwrite'">"Failed to put file: <xsl:value-of select="$local"/>"</xsl:when>
            <xsl:otherwise>"File already exists: <xsl:value-of select="$local"/>, please set CreationFlag to overwrite"</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="REMOVE_LOCAL">
        <xsl:param name="local"/>
        if test -e <xsl:value-of select="$local"/> ; then<xsl:text/>
            <xsl:choose>
                <xsl:when test="../../jsdl:CreationFlag/text()='overwrite'">
            rm -f <xsl:value-of select="$local"/>
                </xsl:when>
                <xsl:otherwise>
            _FAIL_ "File already exists: <xsl:value-of select="$local"/>, please set CreationFlag to overwrite"<xsl:text/>
            return $?
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
QUERY=<xsl:call-template name="QUERY"/>
FRAGMENT=<xsl:call-template name="FRAGMENT"/>
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
                <xsl:message terminate="no">URL contains no scheme: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="USER">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="authority"><xsl:call-template name="AUTHORITY"/></xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($authority,'@')">
                <xsl:value-of select="substring-before($authority,'@')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="no">URL contains no userInfo: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="HOST">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="authority"><xsl:call-template name="AUTHORITY"/></xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($authority,'@') and contains($authority,':')">
                <xsl:value-of select="substring-before(substring-after($authority,'@'),':')"/>
            </xsl:when>
            <xsl:when test="contains($authority,'@')">
                <xsl:value-of select="substring-after($authority,'@')"/>
            </xsl:when>
            <xsl:when test="contains($authority,':')">
                <xsl:value-of select="substring-before($authority,':')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$authority"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="PORT">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="authority"><xsl:call-template name="AUTHORITY"/></xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($authority,':')">
                <xsl:value-of select="substring-after($authority,':')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="no">URL contains no port: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- [[user-info@]host[:port]] -->
    <xsl:template name="AUTHORITY">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="middle"><xsl:call-template name="MIDDLE"/></xsl:variable>
        <xsl:choose>
            <xsl:when test="starts-with($middle,'/') or starts-with($middle,'.') or starts-with($middle,'$') or starts-with($url,'file://')">
                <xsl:message terminate="no">URL contains no authority: <xsl:value-of select="$url"/></xsl:message>
            </xsl:when>
            <xsl:when test="contains($middle,'/')">
                <xsl:value-of select="substring-before($middle,'/')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$middle"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="PATH">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="middle"><xsl:call-template name="MIDDLE"/></xsl:variable>
        <xsl:choose>
            <xsl:when test="starts-with($middle,'/') or starts-with($middle,'.') or starts-with($middle,'$') or starts-with($url,'file://')">
                <xsl:value-of select="$middle"/>
            </xsl:when>
            <xsl:when test="contains($middle,'/')">
                <xsl:variable name="path" select="substring-after($middle,'/')"/>
                <xsl:if test="not(starts-with($path,'./') or starts-with($path,'../'))">
                    <xsl:text>/</xsl:text>
                </xsl:if>
                <xsl:value-of select="$path"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="no">URL contains no path: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="QUERY">
        <xsl:param name="url" select="text()"/>
        <xsl:choose>
            <xsl:when test="contains($url,'?') and contains($url,'#')">
                <xsl:value-of select="substring-before(substring-after($url,'?'),'#')"/>
            </xsl:when>
            <xsl:when test="contains($url,'?')">
                <xsl:value-of select="substring-after($url,'?')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="no">URL contains no query: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="FRAGMENT">
        <xsl:param name="url" select="text()"/>
        <xsl:choose>
            <xsl:when test="contains($url,'#')">
                <xsl:value-of select="substring-after($url,'#')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="no">URL contains no fragment: <xsl:value-of select="$url"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- [[user-info@]host[:port]][/path] -->
    <xsl:template name="MIDDLE">
        <xsl:param name="url" select="text()"/>
        <xsl:variable name="noscheme">
            <xsl:choose>
                <xsl:when test="contains($url,'://')">
                    <xsl:value-of select="substring-after($url,'://')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$url"/>
                    <xsl:message terminate="no">URL contains no scheme: <xsl:value-of select="$url"/></xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($noscheme,'?')">
                <xsl:value-of select="substring-before($noscheme,'?')"/>
            </xsl:when>
            <xsl:when test="contains($noscheme,'#')">
                <xsl:value-of select="substring-before($noscheme,'#')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$noscheme"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="BASEURL">
        <xsl:param name="url"/>
        <xsl:choose>
            <xsl:when test="contains($url,'?')">
                <xsl:value-of select="substring-before($url,'?')"/>
            </xsl:when>
            <xsl:when test="contains($url,'#')">
                <xsl:value-of select="substring-before($url,'#')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$url"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>