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

            <p><i>Table of content:</i>
                <ul>
                    <li><a href="#Introduction">Introduction</a></li>
                    <li><a href="#Developing a security adaptor">Developing a security adaptor</a></li>
                    <li><a href="#Developing a data adaptor">Developing a data adaptor</a></li>
                    <li><a href="#Developing a job adaptor">Developing a job adaptor</a></li>
                </ul>
            </p>


            <section name="Introduction">
                <p>Adding to JSAGA support for new technologies can be done by developing adaptors.
                    There are 3 kind of adaptors: security adaptors, data adaptors and job adaptors.
                </p><br/>
                <p>Adaptor interfaces are designed to be as close to legacy middleware API as possible.
                    A few interfaces are required, most of them are optional.
                    An adaptor should implement <b>only</b> required features, and optional features
                    that can not be emulated by the core engine.
                    However, selected interfaces must be <b>fully implemented</b> and adaptor methods
                    can not throw exception "NotImplementedException".
                </p><br/>
                <p>JSAGA adaptors API re-use Exception classes of the <a href="saga-api/apidocs/org/ogf/saga/error/package-summary.html"
                        >org.ogf.saga.error</a> package of the SAGA java binding API.
                    See pages 37 to 41 of the "SAGA Error Handling" chapter of the
                    <a href="http://www.ogf.org/documents/GFD.90.pdf">SAGA specification document</a>
                    for a description of each SAGA Exception class.
                </p>
                <p>This document describes adaptor interfaces only.
                    For information about how to create a test-suite for your adaptor,
                    please look at the <a href="testers-guide.html">Testing Adaptors Guide</a> web page.
                    For information about how to create project skeleton, how to configure and run test-suite, etc.,
                    please look at the <a href="howto.html">Contributors How To</a> web page.
                </p>
                <p>This document is generated from source code. It is applicable to the version
                    of JSAGA that can be downloaded <a href="download.html">here</a>.
                </p>
            </section>


            <section name="Developing a security adaptor">
                <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                <pre>
                    <xsl:for-each select="jelclass[@type='Adaptor' or @type='SecurityAdaptor']">
                        <xsl:call-template name="CODE_jelclass"/>
                    </xsl:for-each>
                </pre>

                <p>A security adaptor <b>must</b> implement the <xsl:apply-templates select="jelclass[@type='SecurityAdaptor']"/>
                    interface, which extends the <xsl:apply-templates select="jelclass[@type='Adaptor']"/> interface.
                </p>

                <p>If your security adaptor creates expirable credentials, it <b>should</b> also implement
                    the <xsl:apply-templates select="jelclass[@type='ExpirableSecurityAdaptor']"/> interface.
                </p>

                <p>A security adaptor creates security credentials. A security credential <b>must</b> implement
                    the <xsl:apply-templates select="jelclass[@type='SecurityCredential']"/> interface.
                </p>

                <p>See <a href="jsaga-engine/xref/fr/in2p3/jsaga/adaptor/security/UserPassSecurityAdaptor.html">example</a>.
                </p>
            </section>


            <section name="Developing a data adaptor">
                <p>A data adaptor <b>must</b> implements the <xsl:apply-templates select="jelclass[@type='DataAdaptor']"/>
                    interface, which extends the <xsl:apply-templates select="jelclass[@type='ClientAdaptor']"/>
                    and the <xsl:apply-templates select="jelclass[@type='Adaptor']"/> interfaces.
                </p>

                <p>A data adaptor is either a physical file adaptor, or a logical file adaptor.
                    It <b>must</b> implement the <xsl:apply-templates select="jelclass[@type='DataReaderAdaptor']"/>
                    <b>and/or</b> the <xsl:apply-templates select="jelclass[@type='DataWriterAdaptor']"/> interfaces.
                </p>

                <p>A data reader adaptor creates file attributes container. A file attributes container <b>must</b>
                    extend the <xsl:apply-templates select="jelclass[@type='FileAttributes']"/> abstract class.
                </p>

                <p>See <a href="jsaga-engine/xref/fr/in2p3/jsaga/adaptor/data/package-summary.html">example</a>.
                </p>

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
                            <xsl:call-template name="CODE_jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A physical file adaptor that uses stream methods <b>must</b> implement
                        the <xsl:apply-templates select="jelclass[@type='FileReaderStreamFactory']"/> <b>and/or</b>
                        the <xsl:apply-templates select="jelclass[@type='FileWriterStreamFactory']"/> interfaces.
                    </p>

                    <h4>Developing a physical file adaptor that uses get/put methods</h4>
                    <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='DataAdaptor'
                                or @type='DataReaderAdaptor' or @type='FileReaderGetter'
                                or @type='DataWriterAdaptor' or @type='FileWriterPutter']">
                            <xsl:call-template name="CODE_jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A physical file adaptor that uses get/put methods <b>must</b> implement
                        the <xsl:apply-templates select="jelclass[@type='FileReaderGetter']"/> <b>and/or</b>
                        the <xsl:apply-templates select="jelclass[@type='FileWriterPutter']"/> interfaces.
                    </p>
                </subsection>

                <subsection name="Developing a logical file adator">
                    <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='DataAdaptor'
                                or @type='DataReaderAdaptor' or @type='LogicalReader'
                                or @type='DataWriterAdaptor' or @type='LogicalWriter']">
                            <xsl:call-template name="CODE_jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A logical file adaptor <b>must</b> implement the <xsl:apply-templates select="jelclass[@type='LogicalReader']"/>
                        <b>and/or</b> the <xsl:apply-templates select="jelclass[@type='LogicalWriter']"/> interfaces.
                    </p>

                    <p>A logical file adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='LogicalReaderMetaDataExtended']"/>
                        optional interface, but this is <b>not recommended</b> because this feature can not be used
                        through the SAGA API.
                    </p>
                </subsection>

                <subsection name="Data adaptor optional features">
                    <p>A data adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='DataWriterTimes']"/>
                        optional interface in order to support preservation of last modification date when copying files.
                    </p>

                    <p>A data adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='LinkAdaptor']"/>
                        optional interface in order to support links.
                    </p>

                    <p>A data adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='PermissionAdaptor']"/>
                        optional interface in order to support permissions management.
                    </p>
                </subsection>

                <subsection name="Data adaptor optional optimisations">
                    <p>A data adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='DataCopy']"/>
                        optional interface in order to optimise data transfer (e.g. use third-party transfer).
                    </p>

                    <p>[NOT YET SUPPORTED] A data adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='DataCopyDelegated']"/>
                        optional interface in order to delegate data transfer (e.g. to a third-party service such as gLite FTS or Globus RFT).
                    </p>

                    <p>A data adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='DataRename']"/>
                        optional interface in order to optimize file renaming or moving (i.e. avoid copying data).
                    </p>

                    <p>A data adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='DataFilteredList']"/>
                        optional interface in order to optimize entries listing with wildcards (i.e. interpret wildcards on server-side).
                    </p>
                </subsection>
            </section>


            <section name="Developing a job adaptor">
                <p>A job adaptor <b>must</b> implement the <xsl:apply-templates select="jelclass[@type='JobAdaptor']"/>
                    interface, which extends the <xsl:apply-templates select="jelclass[@type='ClientAdaptor']"/>
                    and the <xsl:apply-templates select="jelclass[@type='Adaptor']"/> interfaces.
                </p>

                <p>A job adaptor is composed of two classes (that can extend a common abstract class):
                    the job control adaptor and the job monitor adaptor.
                    This increases flexibility (advanced user can use an alternative monitoring implementation),
                    and enforces development of adaptors that support offline monitoring.
                </p>
                
                <p>See <a href="jsaga-engine/xref/fr/in2p3/jsaga/adaptor/job/package-summary.html">example</a>.
                </p>

                <subsection name="Developing a job control adaptor">
                    <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='JobAdaptor' or @type='JobControlAdaptor']">
                            <xsl:call-template name="CODE_jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A job control adaptor <b>must</b> implement the <xsl:apply-templates select="jelclass[@type='JobControlAdaptor']"/>
                        interface.
                    </p>

                    <p>A job control adaptor creates a job description translator. A job description translator
                        <b>must</b> implement the <xsl:apply-templates select="jelclass[@type='JobDescriptionTranslator']"/>
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
                </subsection>

                <subsection name="Developing a job monitor adaptor">
                    <i>Copy-paste required methods to your adaptor class, and implement them.</i>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='JobAdaptor' or @type='JobMonitorAdaptor']">
                            <xsl:call-template name="CODE_jelclass"/>
                        </xsl:for-each>
                    </pre>

                    <p>A job monitor adaptor <b>must</b> implement the <xsl:apply-templates select="jelclass[@type='JobMonitorAdaptor']"/>
                        interface.
                    </p>

                    <p>A job monitor adaptor <b>must</b> implement at least one of the monitoring interfaces.
                        It <b>may</b> implement several if they are natively supported by the targeted scheduler.
                        Monitoring interfaces are:
                        <ul>
                            <li><xsl:apply-templates select="jelclass[@type='QueryIndividualJob']"/>:
                                Query status for a single job</li>
                            <li><xsl:apply-templates select="jelclass[@type='QueryListJob']"/>:
                                Query status for a list of jobs</li>
                            <li><xsl:apply-templates select="jelclass[@type='QueryFilteredJob']"/>:
                                Query status for jobs maching a filter expression</li>
                            <li><xsl:apply-templates select="jelclass[@type='ListenIndividualJob']"/>:
                                Listen to status changes for a single job</li>
                            <!--<li><xsl:apply-templates select="jelclass[@type='ListenListJob']"/>:
                                Listen to status changes for a list of jobs</li>-->
                            <li><xsl:apply-templates select="jelclass[@type='ListenFilteredJob']"/>:
                                Listen to status changes for jobs matching a filter expression</li>
                        </ul>
                    </p>

                    <p>Monitoring interfaces create job status objects. A job status object <b>must</b>
                        extend the <xsl:apply-templates select="jelclass[@type='JobStatus']"/> abstract class.
                    </p>

                    <p>A job monitor adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='JobInfoAdaptor']"/>
                        optional interface.
                    </p>
                </subsection>

                <subsection name="Job control adaptor optional features">
                    <h4>Job data (staging and I/O streams)</h4>

                    <p>A job control adaptor <b>may</b> implement a data staging optional interface,
                        in order to stage job input/output files.
                        Data staging interfaces are:
                        <ul>
                            <li><xsl:apply-templates select="jelclass[@type='StagingJobAdaptorOnePhase']"/>:
                                Implement this interface if job is registered and started in one phase.</li>
                            <li><xsl:apply-templates select="jelclass[@type='StagingJobAdaptorTwoPhase']"/>:
                                Implement this interface if job is registered and started in two phases.</li>
                        </ul>
                        Both of them extends the <xsl:apply-templates select="jelclass[@type='StagingJobAdaptor']"/>
                        interface.
                    </p>

                    <p>The data staging interfaces create arrays of <xsl:apply-templates select="jelclass[@type='StagingTransfer']"/>
                        instances.
                    </p><br/>

                    <p>A job control adaptor <b>may</b> implement one (and only one) of the streamable
                        job optional interfaces, in order to transfer job input/output streams.
                        These interfaces can be implemented even if the targeted scheduler
                        does not support interactive jobs.
                        Streamable job interfaces are:
                        <ul>
                            <li><xsl:apply-templates select="jelclass[@type='StreamableJobBatch']"/>,
                                which creates either a <xsl:apply-templates select="jelclass[@type='JobIOGetter']"/>
                                or a <xsl:apply-templates select="jelclass[@type='JobIOSetter']"/> instance.</li>
                            <li><xsl:apply-templates select="jelclass[@type='StreamableJobInteractiveGet']"/>,
                                which creates a <xsl:apply-templates select="jelclass[@type='JobIOGetterInteractive']"/>
                                instance.</li>
                            <li><xsl:apply-templates select="jelclass[@type='StreamableJobInteractiveSet']"/></li>
                        </ul>
                    </p>


                    <h4>Job management</h4>

                    <p>A job control adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='ListableJobAdaptor']"/>
                        interface in order to list user jobs known by the targeted scheduler service.
                    </p><br/>

                    <p>A job control adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='PurgeableJobAdaptor']"/>
                        interface in order to purge jobs that are in a final state.
                    </p>


                    <h4>Job optional features</h4>

                    <p>A job control adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='CheckpointableJobAdaptor']"/>
                        interface in order to checkpoint running jobs.
                    </p><br/>

                    <p>A job control adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='CleanableJobAdaptor']"/>
                        interface in order to clean data and files generated for the job when it is completed
                        and when its output files are retrieved.
                    </p>

                    <p>A job control adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='HoldableJobAdaptor']"/>
                        interface in order to hold/release the job when it is queued.
                    </p>

                    <p>A job control adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='SignalableJobAdaptor']"/>
                        interface in order to send a signal to a running job.
                    </p>

                    <p>A job control adaptor <b>may</b> implement the <xsl:apply-templates select="jelclass[@type='SuspendableJobAdaptor']"/>
                        interface in order to suspend/resume the job when it is running.
                    </p>
                </subsection>
            </section>


            <section name="APPENDIX">
                <xsl:for-each select="jelclass[
                        (@interface='true' and not(comment/attribute/@name='@deprecated')) or
                        @type='FileAttributes' or @type='JobStatus' or @ type='StagingTransfer' or
                        @type='JobIOGetter' or @type='JobIOSetter' or @type='JobIOGetterInteractive']">
                    <xsl:call-template name="TABLE_jelclass"/>
                </xsl:for-each>
            </section>
        </body></document>
    </xsl:template>

    <xsl:template match="jelclass">
        <a href="#{@type}"><xsl:value-of select="@type"/></a>
    </xsl:template>

    <xsl:template name="TABLE_jelclass">
        <a id="{@type}"/>
        <table>
            <tr>
                <th><xsl:value-of select="@fulltype"/></th>
            </tr>
            <xsl:for-each select="methods/method"><xsl:call-template name="TABLE_method"/></xsl:for-each>
        </table>
        <br/>
    </xsl:template>
    <xsl:template name="TABLE_method">
        <tr>
            <td>
                <b><xsl:value-of select="@name"/></b>:
                <xsl:if test="comment/description/text()">
                    <xsl:value-of select="comment/description/text()"/>
                    <xsl:if test="@returncomment"><br/></xsl:if>
                </xsl:if>
                <xsl:if test="@returncomment">
                    Returns <xsl:value-of select="@returncomment"/>
                </xsl:if>
                <ul>
                    <xsl:for-each select="params/param"><xsl:call-template name="TABLE_param"/></xsl:for-each>
                </ul>
            </td>
            <td><xsl:for-each select="exceptions"><xsl:call-template name="TABLE_exceptions"/></xsl:for-each></td>
        </tr>
    </xsl:template>
    <xsl:template name="TABLE_param">
        <li>
            <i><xsl:value-of select="@name"/></i>
            <xsl:if test="@comment">:
                <xsl:value-of select="@comment"/>
            </xsl:if>
        </li>
    </xsl:template>
    <xsl:template name="TABLE_exceptions">
        Exceptions:
        <ul>
            <xsl:for-each select="exception"><xsl:call-template name="TABLE_exception"/></xsl:for-each>
        </ul>
    </xsl:template>
    <xsl:template name="TABLE_exception">
        <li>
            <i><xsl:value-of select="@type"/></i>
            <xsl:if test="@comment">:
                <xsl:value-of select="@comment"/>
            </xsl:if>
        </li>
    </xsl:template>

    <xsl:template name="CODE_jelclass">
        <xsl:value-of select="$CR"/>
        <xsl:text/>// methods of interface <xsl:value-of select="@type"/>
        <xsl:for-each select="methods/method"><xsl:call-template name="CODE_method"/></xsl:for-each>
        <xsl:value-of select="$CR"/>
    </xsl:template>
    <xsl:template name="CODE_method">
        <xsl:value-of select="$CR"/>
        <xsl:value-of select="@visibility"/><xsl:text> </xsl:text>
        <xsl:call-template name="CLASSNAME"><xsl:with-param name="FQClass" select="@fulltype"/></xsl:call-template><xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>(<xsl:text/>
        <xsl:for-each select="params/param"><xsl:call-template name="CODE_param"/></xsl:for-each>)<xsl:text/>
        <xsl:for-each select="exceptions"><xsl:call-template name="CODE_exceptions"/></xsl:for-each> {<xsl:text/>
        <xsl:value-of select="$CR"/><xsl:text>    </xsl:text>
        <xsl:if test="@fulltype!='void'">return null; </xsl:if>
        <xsl:text>//todo: this method MUST be implemented!</xsl:text>
        <xsl:value-of select="$CR"/>}<xsl:text/>
    </xsl:template>
    <xsl:template name="CODE_param">
        <xsl:if test="position()>1">, </xsl:if>
        <xsl:call-template name="CLASSNAME"><xsl:with-param name="FQClass" select="@fulltype"/></xsl:call-template><xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>
    </xsl:template>
    <xsl:template name="CODE_exceptions">
        <xsl:text> throws </xsl:text>
        <xsl:for-each select="exception"><xsl:call-template name="CODE_exception"/></xsl:for-each>
    </xsl:template>
    <xsl:template name="CODE_exception">
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