<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:variable name="CR"><xsl:text>
    </xsl:text></xsl:variable>

    <xsl:template match="/jel">
        <document>
            <properties>
                <title>How to develop adaptors ?</title>
            </properties>
            <body>


            <section name="Introduction">
                <p>Adding to JSAGA support for new technologies can be done by developing adaptors.
                    There are 3 kind of adaptors: security adaptors, data adaptors and job adaptors.
                </p>
                <p>Adaptor interfaces are designed to be close to legacy middleware API.
                    Most adaptor interfaces are optionals. An adaptor should implement <b>only</b>
                    features that are required and optional features that are natively supported
                    by adapted middleware, other optional features should be managed by the core engine.
                    However, implemented interfaces must be <b>fully implemented</b>
                    (i.e. no NotImplementedException can be thrown).
                </p>
                <p>JSAGA adaptors API re-use Exception classes of the <code>org.ogf.saga.error</code> package
                    of the SAGA java binding API.
                    See pages 37 to 41 of the "SAGA Error Handling" chapter of the
                    <a href="http://www.ogf.org/documents/GFD.90.pdf">SAGA specification document</a>
                    for a description of each SAGA Exception class.
                </p>
                <p>This document is generated from source code. It is applicable to the version
                    of JSAGA that can be downloaded <a href="download.html">here</a>.
                </p>
            </section>


            <section name="Common interfaces">
                <p>All the adaptors implement the <code>Adaptor</code> interface.
                </p>
                <xsl:apply-templates select="jelclass[@type='Adaptor']"/>
                
                <p>All the data and job adaptors also implement the <code>ClientAdaptor</code> interface.
                </p>
                <xsl:apply-templates select="jelclass[@type='ClientAdaptor']"/>
            </section>


            <section name="Developing a security adaptor">
                <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                <pre>
                    <xsl:for-each select="jelclass[@type='Adaptor' or @type='SecurityAdaptor']">
                        <xsl:call-template name="jelclass"/>
                    </xsl:for-each>
                </pre>

                <p>A security adaptor <b>must</b> implement the <code>SecurityAdaptor</code> interface.
                </p>
                <xsl:apply-templates select="jelclass[@type='SecurityAdaptor']"/>

                <p>If your security adaptor creates expirable credentials, it should also implement
                    the <code>ExpirableSecurityAdaptor</code> interface.
                </p>
                <xsl:apply-templates select="jelclass[@type='ExpirableSecurityAdaptor']"/>

                <p>A security adaptor creates security credentials. A security credential <b>must</b>
                    implement the <code>SecurityCredential</code> interface.
                </p>
                <xsl:apply-templates select="jelclass[@type='SecurityCredential']"/>
            </section>


            <section name="Developing a data adaptor">
                <p>A data adaptor is either a physical file adaptor, or a logical file adaptor.
                    It <b>must</b> implement the <code>DataReaderAdaptor</code> <b>and/or</b>
                    the <code>DataWriterAdaptor</code> interfaces.
                </p>
                <xsl:apply-templates select="jelclass[@type='DataAdaptor'
                                or @type='DataReaderAdaptor' or @type='DataWriterAdaptor']"/>

                <p>A data reader adaptor creates file attributes container. A file attributes container <b>must</b>
                    extend the <code>FileAttributes</code> abstract class.
                </p>
                <xsl:apply-templates select="jelclass[@type='FileAttributes']"/>

                <subsection name="Developing a physical file adator">
                    <p>A physical file adaptor uses either stream methods or get/put methods.
                    </p>

                    <h4>Developing a physical file adaptor that uses stream methods</h4>
                    <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='DataAdaptor'
                                or @type='DataReaderAdaptor' or @type='FileReaderStreamFactory'
                                or @type='DataWriterAdaptor' or @type='FileWriterStreamFactory']">
                            <xsl:call-template name="jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A physical file adaptor that uses stream methods <b>must</b> implement
                        the <code>FileReaderStreamFactory</code> <b>and/or</b>
                        the <code>FileWriterStreamFactory</code> interfaces.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='FileReaderStreamFactory' or @type='FileWriterStreamFactory']"/>

                    <h4>Developing a physical file adaptor that uses get/put methods</h4>
                    <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='DataAdaptor'
                                or @type='DataReaderAdaptor' or @type='FileReaderGetter'
                                or @type='DataWriterAdaptor' or @type='FileWriterPutter']">
                            <xsl:call-template name="jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A physical file adaptor that uses get/put methods <b>must</b> implement
                        the <code>FileReaderGetter</code> <b>and/or</b>
                        the <code>FileWriterPutter</code> interfaces.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='FileReaderGetter' or @type='FileWriterPutter']"/>
                </subsection>

                <subsection name="Developing a logical file adator">
                    <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='DataAdaptor'
                                or @type='DataReaderAdaptor' or @type='LogicalReader'
                                or @type='DataWriterAdaptor' or @type='LogicalWriter']">
                            <xsl:call-template name="jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A logical file adaptor <b>must</b> implement the <code>LogicalReader</code> <b>and/or</b>
                        the <code>LogicalWriter</code> interfaces.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='LogicalReader' or @type='LogicalWriter']"/>

                    <p>A logical file adaptor <b>may</b> implement the <code>LogicalReaderMetaDataExtended</code>
                        optional interface, but this is <b>not recommended</b> because this feature can not be used
                        through the SAGA API.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='LogicalReaderMetaDataExtended']"/>
                </subsection>

                <subsection name="Data adaptor optional features">
                    <p>A data adaptor <b>may</b> implement the <code>DataWriterTimes</code>
                        optional interface in order to support preservation of last modification date when copying files.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='DataWriterTimes']"/>

                    <p>A data adaptor <b>may</b> implement the <code>LinkAdaptor</code>
                        optional interface in order to support links.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='LinkAdaptor']"/>

                    <p>A data adaptor <b>may</b> implement the <code>PermissionAdaptor</code>
                        optional interface in order to support permissions management.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='PermissionAdaptor']"/>
                </subsection>

                <subsection name="Data adaptor optional optimisations">
                    <p>A data adaptor <b>may</b> implement the <code>DataCopy</code>
                        optional interface in order to optimise data transfer (e.g. use third-party transfer).
                    </p>
                    <xsl:apply-templates select="jelclass[@type='DataCopy']"/>

                    <p>[NOT YET SUPPORTED] A data adaptor <b>may</b> implement the <code>DataCopyDelegated</code>
                        optional interface in order to delegate data transfer (e.g. to a third-party service such as gLite FTS or Globus RFT).
                    </p>
                    <xsl:apply-templates select="jelclass[@type='DataCopyDelegated']"/>

                    <p>A data adaptor <b>may</b> implement the <code>DataRename</code>
                        optional interface in order to optimize file renaming or moving (i.e. avoid copying data).
                    </p>
                    <xsl:apply-templates select="jelclass[@type='DataRename']"/>

                    <p>A data adaptor <b>may</b> implement the <code>DataFilteredList</code>
                        optional interface in order to optimize entries listing with wildcards (i.e. interpret wildcards on server-side).
                    </p>
                    <xsl:apply-templates select="jelclass[@type='DataFilteredList']"/>
                </subsection>
            </section>


            <section name="Developing a job adaptor">
                <p>A job adaptor is composed of two classes (that can extend a common abstract class):
                    the job control adaptor and the job monitor adaptor.
                    This increases flexibility (advanced user can use an alternative monitoring implementation),
                    and enforces development of adaptors that support offline monitoring.
                    Both classes <b>must</b> implement the <code>JobAdaptor</code> interface.
                </p>
                <xsl:apply-templates select="jelclass[@type='JobAdaptor']"/>
                
                <subsection name="Developing a job control adaptor">
                    <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='JobAdaptor' or @type='JobControlAdaptor']">
                            <xsl:call-template name="jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A job control adaptor <b>must</b> implement the <code>JobControlAdaptor</code> interface.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='JobControlAdaptor']"/>

                    <p>A job control adaptor creates a job description translator. A job description translator
                        <b>must</b> implement the <code>JobDescriptionTranslator</code>
                        interface.
                        Two reusable implementations are provided:
                        <ul>
                            <li><code>JobDescriptionTranslatorJSDL</code>
                                can be used when the language supported by targeted scheduler is JSDL.</li>
                            <li><code>JobDescriptionTranslatorXSLT</code>
                                can be used for any other job adaptor.</li>
                        </ul>
                        Although you have the possibility to implement your own job description translator, it is <b>recommended</b>
                        to use the JobDescriptionTranslatorXSLT in order to keep your code easy to maintain.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='JobDescriptionTranslator']"/>
                </subsection>

                <subsection name="Developing a job monitor adaptor">
                    <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='JobAdaptor' or @type='JobMonitorAdaptor']">
                            <xsl:call-template name="jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A job monitor adaptor <b>must</b> implement the <code>JobMonitorAdaptor</code>
                        interface.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='JobMonitorAdaptor']"/>

                    <p>A job monitor adaptor <b>must</b> implement at least one of the monitoring interfaces.
                        It <b>may</b> implement several if they are natively supported by the targeted scheduler.
                        Monitoring interfaces are:
                        <ul>
                            <li><code>QueryIndividualJob</code>: Query status for a single job</li>
                            <li><code>QueryListJob</code>: Query status for a list of jobs</li>
                            <li><code>QueryFilteredJob</code>: Query status for jobs maching a filter expression</li>
                            <li><code>ListenIndividualJob</code>: Listen to status changes for a single job</li>
                            <!--<li><code>ListenListJob</code>: Listen to status changes for a list of jobs</li>-->
                            <li><code>ListenFilteredJob</code>: Listen to status changes for jobs matching a filter expression</li>
                        </ul>
                    </p>
                    <xsl:apply-templates select="jelclass[
                            @type='QueryIndividualJob' or @type='QueryListJob' or @type='QueryFilteredJob' or
                            @type='ListenIndividualJob' or @type='ListenListJob' or @type='ListenFilteredJob']"/>

                    <p>Monitoring interfaces create job status objects. A job status object <b>must</b>
                        extend the <code>JobStatus</code> abstract class.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='JobStatus']"/>

                    <p>A job monitor adaptor <b>may</b> implement the <code>JobInfoAdaptor</code>
                        optional interface.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='JobInfoAdaptor']"/>
                </subsection>

                <subsection name="Job control adaptor optional features">
                    <h4>Job data (staging and I/O streams)</h4>

                    <p>A job control adaptor <b>may</b> implement a data staging optional interface,
                        in order to stage job input/output files.
                        Data staging interfaces are:
                        <ul>
                            <li><code>StagingJobAdaptorOnePhase</code>: Implement this interface if job is registered and started in one phase.</li>
                            <li><code>StagingJobAdaptorTwoPhase</code>: Implement this interface if job is registered and started in two phases.</li>
                        </ul>
                    </p>
                    <xsl:apply-templates select="jelclass[@type='StagingJobAdaptor' or
                            @type='StagingJobAdaptorOnePhase' or @type='StagingJobAdaptorTwoPhase']"/>

                    <p>The data staging interfaces create arrays of StagingTransfer instances.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='StagingTransfer']"/>

                    <p>A job control adaptor <b>may</b> implement one (and only one) of the streamable
                        job optional interfaces, in order to transfer job input/output streams.
                        These interfaces can be implemented even if the targeted scheduler
                        does not support interactive jobs.
                        Streamable job interfaces are:
                        <ul>
                            <li><code>StreamableJobBatch</code>, which creates either a <code>JobIOGetter</code>
                                or a <code>JobIOSetter</code> instance</li>
                            <li><code>StreamableJobInteractiveGet</code>, which creates a <code>JobIOGetterInteractive</code>
                                instance</li>
                            <li><code>StreamableJobInteractiveSet</code></li>
                        </ul>
                    </p>
                    <xsl:apply-templates select="jelclass[@type='StreamableJobBatch' or
                            @type='StreamableJobInteractiveGet' or @type='StreamableJobInteractiveSet']"/>


                    <h4>Job management</h4>

                    <p>A job control adaptor <b>may</b> implement the <code>ListableJobAdaptor</code> interface
                        in order to list user jobs known by the targeted scheduler service.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='ListableJobAdaptor']"/>

                    <p>A job control adaptor <b>may</b> implement the <code>PurgeableJobAdaptor</code> interface
                        in order to purge jobs that are in a final state.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='PurgeableJobAdaptor']"/>


                    <h4>Job optional features</h4>

                    <p>A job control adaptor <b>may</b> implement the <code>CheckpointableJobAdaptor</code> interface
                        in order to checkpoint running jobs.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='CheckpointableJobAdaptor']"/>

                    <p>A job control adaptor <b>may</b> implement the <code>CleanableJobAdaptor</code> interface
                        in order to clean data and files generated for the job when it is completed
                        and when its output files are retrieved.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='CleanableJobAdaptor']"/>

                    <p>A job control adaptor <b>may</b> implement the <code>HoldableJobAdaptor</code> interface
                        in order to hold/release the job when it is queued.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='HoldableJobAdaptor']"/>

                    <p>A job control adaptor <b>may</b> implement the <code>SignalableJobAdaptor</code> interface
                        in order to send a signal to a running job.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='SignalableJobAdaptor']"/>

                    <p>A job control adaptor <b>may</b> implement the <code>SuspendableJobAdaptor</code> interface
                        in order to suspend/resume the job when it is running.
                    </p>
                    <xsl:apply-templates select="jelclass[@type='SuspendableJobAdaptor']"/>
                </subsection>
            </section>
        </body></document>
    </xsl:template>

    <xsl:template match="jelclass">
        <table>
            <tr>
                <th><xsl:value-of select="@fulltype"/></th>
            </tr>
            <xsl:apply-templates select="methods/method"/>
        </table>
        <br/>
    </xsl:template>
    <xsl:template match="method">
        <tr>
            <td>
                <b><xsl:value-of select="@name"/></b>:
                <xsl:value-of select="comment/description/text()"/>
                <xsl:if test="@returncomment"><br/>
                    Returns <xsl:value-of select="@returncomment"/>
                </xsl:if>
                <ul>
                    <xsl:apply-templates select="params/param"/>
                </ul>
            </td>
            <td><xsl:apply-templates select="exceptions"/></td>
        </tr>
    </xsl:template>
    <xsl:template match="param">
        <li>
            <i><xsl:value-of select="@name"/></i>
            <xsl:if test="@comment">:
                <xsl:value-of select="@comment"/>
            </xsl:if>
        </li>
    </xsl:template>
    <xsl:template match="exceptions">
        Exceptions:
        <ul>
            <xsl:apply-templates select="exception"/>
        </ul>
    </xsl:template>
    <xsl:template match="exception">
        <li>
            <i><xsl:value-of select="@type"/></i>
            <xsl:if test="@comment">:
                <xsl:value-of select="@comment"/>
            </xsl:if>
        </li>
    </xsl:template>

    <xsl:template name="jelclass">
        <xsl:value-of select="$CR"/>
        <xsl:text/>// methods of interface <xsl:value-of select="@type"/>
        <xsl:for-each select="methods/method"><xsl:call-template name="method"/></xsl:for-each>
        <xsl:value-of select="$CR"/>
    </xsl:template>
    <xsl:template name="method">
        <xsl:value-of select="$CR"/>
        <xsl:value-of select="@visibility"/><xsl:text> </xsl:text>
        <xsl:call-template name="CLASSNAME"><xsl:with-param name="FQClass" select="@fulltype"/></xsl:call-template><xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>(<xsl:text/>
        <xsl:for-each select="params/param"><xsl:call-template name="param"/></xsl:for-each>)<xsl:text/>
        <xsl:for-each select="exceptions"><xsl:call-template name="exceptions"/></xsl:for-each> {<xsl:text/>
        <xsl:value-of select="$CR"/><xsl:text>    </xsl:text>
        <xsl:if test="@fulltype!='void'">return null; </xsl:if>
        <xsl:text>//todo: this method MUST be implemented!</xsl:text>
        <xsl:value-of select="$CR"/>}<xsl:text/>
    </xsl:template>
    <xsl:template name="param">
        <xsl:if test="position()>1">, </xsl:if>
        <xsl:call-template name="CLASSNAME"><xsl:with-param name="FQClass" select="@fulltype"/></xsl:call-template><xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
    </xsl:template>
    <xsl:template name="exceptions">
        <xsl:text> throws </xsl:text>
        <xsl:for-each select="exception"><xsl:call-template name="exception"/></xsl:for-each>
    </xsl:template>
    <xsl:template name="exception">
        <xsl:if test="position()>1">, </xsl:if>
        <xsl:value-of select="@type"/>
    </xsl:template>
    <xsl:template name="CLASSNAME">
        <xsl:param name="FQClass"/>
        <xsl:choose>
            <xsl:when test="contains($FQClass,'.')">
                <xsl:call-template name="CLASSNAME">
                    <xsl:with-param name="FQClass" select="substring-after($FQClass,'.')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$FQClass"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>