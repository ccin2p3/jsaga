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
                <p>All the adaptors implement the <code>fr.in2p3.jsaga.adaptor.Adaptor</code> interface.
                    All the data and job adaptors also implement the <code>fr.in2p3.jsaga.adaptor.ClientAdaptor</code> interface.
                </p>
                <xsl:apply-templates select="jelclass[@type='Adaptor' or @type='ClientAdaptor']"/>
            </section>
            <section name="Developing a security adaptor">
                <p>A security adaptor must implement the <code>fr.in2p3.jsaga.adaptor.security.SecurityAdaptor</code> interface.
                </p>
                <i>Copy-paste these methods to your adaptor class, and implement them.</i>
                <pre>
                    <xsl:for-each select="jelclass[@type='Adaptor' or @type='SecurityAdaptorBuilder']">
                        <xsl:call-template name="jelclass"/>
                    </xsl:for-each>
                </pre>
            </section>
            <section name="Developing a data adaptor">
                <p>A data adaptor is either a physical file adaptor, or a logical file adaptor.
                </p>
                <subsection name="Developing a physical file adator">
                    <p>A physical file adaptor must implement the <code>fr.in2p3.jsaga.adaptor.data.read.FileReader</code>
                        and/or <code>fr.in2p3.jsaga.adaptor.data.write.FileWriter</code> interfaces.
                    </p>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='DataAdaptor'
                                or @type='DataReaderAdaptor' or @type='FileReader'
                                or @type='DataWriterAdaptor' or @type='FileWriter']">
                            <xsl:call-template name="jelclass"/>
                        </xsl:for-each>
                    </pre>
                </subsection>
                <subsection name="Developing a logical file adator">
                    <p>A logical file adaptor must implement the <code>fr.in2p3.jsaga.adaptor.data.read.LogicalReader</code>
                        and/or <code>fr.in2p3.jsaga.adaptor.data.write.LogicalWriter</code> interfaces.
                    </p>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='DataAdaptor'
                                or @type='DataReaderAdaptor' or @type='LogicalReader'
                                or @type='DataWriterAdaptor' or @type='LogicalWriter']">
                            <xsl:call-template name="jelclass"/>
                        </xsl:for-each>
                    </pre>
                </subsection>
            </section>
            <section name="Developing a job adaptor">
                <p>A job adaptor is composed of two classes (that can extend a common abstract class):
                    the job control adaptor and the job monitor adaptor.
                    This increases flexibility (advanced user can use an alternative monitoring implementation),
                    and enforces development of adaptors that support offline monitoring.
                </p>
                <subsection name="Developing a job control adaptor">
                    <p>A job control adaptor must implement the <code>fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor</code> interface.
                    </p>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='JobAdaptor' or @type='JobControlAdaptor']">
                            <xsl:call-template name="jelclass"/>
                        </xsl:for-each>
                    </pre>
                </subsection>
                <subsection name="Developing a job monitor adaptor">
                    <p>A job monitor adaptor must implement the <code>fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor</code> interface.
                    </p>
                    <pre>
                        <xsl:for-each select="jelclass[@type='Adaptor' or @type='ClientAdaptor'
                                or @type='JobAdaptor' or @type='JobMonitorAdaptor']">
                            <xsl:call-template name="jelclass"/>
                        </xsl:for-each>
                    </pre>
                </subsection>
            </section>
        </body></document>
    </xsl:template>

    <xsl:template match="jelclass">
        <table>
            <tr>
                <th>Interface <xsl:value-of select="@type"/></th>
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