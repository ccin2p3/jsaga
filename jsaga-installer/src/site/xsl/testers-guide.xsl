<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" xalan:indent-amount="2" xmlns:xalan="http://xml.apache.org/xslt"/>

    <xsl:variable name="CR"><xsl:text>
        </xsl:text></xsl:variable>

    <xsl:template match="/jel">
        <document>
            <properties>
                <title>How to test adaptors ?</title>
            </properties>
            <body>

            <p><i>Table of content:</i>
                <ul>
                    <li><a href="#Introduction">Introduction</a></li>
                    <li><a href="#Testing a security adaptor">Testing a security adaptor</a></li>
                    <li><a href="#Testing a data adaptor">Testing a data adaptor</a></li>
                    <li><a href="#Testing a job adaptor">Testing a job adaptor</a></li>
                    <li><a href="#Testing a resource adaptor">Testing a resource adaptor</a></li>
                </ul>
            </p>


            <section name="Introduction">
                <p>In order to create a test-suite for your adaptor, follow the following instructions:
                    <ul>
                        <li>create a test class in package 'integration'.</li>
                        <li>copy-paste the appropriate code from this web page to your test class.</li>
                        <li>remove inner-classes for the test-suites that your adaptor is NOT SUPPOSED to pass.</li>
                        <li>either remove methods for the tests that your adaptor is SUPPOSED to pass, or
                            replace the text "TODO: explain why this test is ignored..." with the explanation.</li>
                    </ul>
                </p>
                <p>For information about how to configure and run your test-suite,
                    please look at the <a href="contributors-faq.html">Contributors How To</a> web page.
                </p><br/>
                <p>This document is generated from source code. It is applicable to the version
                    of JSAGA that can be downloaded <a href="download.html">here</a>.
                </p>
            </section>


            <section name="Testing a security adaptor">
                <p>The security test-suite can also be used when testing data or job adaptors, in order to initialize,
                    check and destroy the security context needed to be authentified by the remote data/job server.
                </p>
                <p>For example, in order to initialize the security context, enter:</p>
                <pre>mvn integration-test -Dtest={integration.<i>_MyGrid_</i>TestSuite\$<i>_MyGrid_</i>ContextInit}</pre>

                <i>Copy-paste this code to your test class.</i>
                <pre>package integration;
import org.junit.*;
import org.ogf.saga.context.*;

@RunWith(Suite.class)
@SuiteClasses({
    <i>_MyGrid_</i>TestSuite.<i>_MyGrid_</i>ContextInit.class, 
    <i>_MyGrid_</i>TestSuite.<i>_MyGrid_</i>ContextInfo.class, 
    <i>_MyGrid_</i>TestSuite.<i>_MyGrid_</i>ContextDestroy.class})

public class <i>_MyGrid_</i>TestSuite {

    /** test cases */
    public static class <i>_MyGrid_</i>ContextInit extends <a
        href="saga-api-test/xref/org/ogf/saga/context/ContextInitTest.html">ContextInitTest</a> {
        public <i>_MyGrid_</i>ContextInit() throws Exception {super("<i>mygrid</i>");}
    }
    public static class <i>_MyGrid_</i>ContextInfo extends <a
        href="saga-api-test/xref/org/ogf/saga/context/ContextInfoTest.html">ContextInfoTest</a> {
        public <i>_MyGrid_</i>ContextInfo() throws Exception {super("<i>mygrid</i>");}
    }
    public static class <i>_MyGrid_</i>ContextDestroy extends <a
        href="saga-api-test/xref/org/ogf/saga/context/ContextDestroyTest.html">ContextDestroyTest</a> {
        public <i>_MyGrid_</i>ContextDestroy() throws Exception {super("<i>mygrid</i>");}
    }
}
                </pre>
            </section>


            <section name="Testing a data adaptor">
                <p>If the tested adaptor only supports read operations, you must set up the test environment manually
                    (see <a href="contributors-faq.html#create-test-entries">Contributors How To</a> web page).
                </p>
                <p>If the tested adaptor supports write operations, you should first test if the test environment set up
                    properly, by using the SetUpTest class:
                </p>
                <pre>mvn integration-test -Dtest={integration.<i>_MyProtocol_</i>TestSuite\$<i>_MyProtocol_</i>SetUpTest}</pre>
                <p>If the test environment set up fails with exception AlreadyExistsException, first clean it up
                    with the DataCleanUp class.
                </p>
                <pre>package integration;

public class <i>_MyProtocol_</i>CleanUp extends <a href="saga-api-test/xref/org/ogf/saga/namespace/DataCleanUp.html">DataCleanUp</a> {
    public <i>_MyProtocol_</i>CleanUp() throws Exception {
        super("<i>myprotocol</i>", "<i>myprotocol</i>");
    }
}
                </pre>

                <subsection name="Testing a physical file adator">
                    <i>Copy-paste this code to your test class.</i>
                    <pre>package integration;

@RunWith(Suite.class)
@SuiteClasses({
    <i>_MyProtocol_</i>TestSuite.<i>_WhatEverSubClassYouWantInYourTestSuite_</i>.class,
    ...
}) 
public class <i>_MyProtocol_</i>TestSuite {

    <xsl:text/>
                        <xsl:for-each select="jelclass[@package='org.ogf.saga.file' or @package='org.ogf.saga.namespace' or @package='org.ogf.saga.permissions']
                                                      [contains(@type,'Test')]
                                                      [not(comment/attribute/@name='@deprecated')]">
                            <xsl:call-template name="CODE_jelclass"/>
                        </xsl:for-each>
                        <xsl:for-each select="jelclass[@package='org.ogf.saga.file' or @package='org.ogf.saga.namespace']
                                                      [contains(@type,'Test')]
                                                      [contains(@type,'DataMovement')]
                                                      [not(comment/attribute/@name='@deprecated')]">
                            <xsl:call-template name="CODE_jelclass">
                                <xsl:with-param name="targetAdaptor">_to_Emulator</xsl:with-param>
                                <xsl:with-param name="targetProtocol">test</xsl:with-param>
                            </xsl:call-template>
                        </xsl:for-each>
}
                    </pre>
                </subsection>

                <subsection name="Testing a logical file adator">
                    <i>Copy-paste this code to your test class.</i>
                    <pre>package integration;

@RunWith(Suite.class)
@SuiteClasses({
    <i>_MyProtocol_</i>TestSuite.<i>_WhatEverSubClassYouWantInYourTestSuite_</i>.class,
    ...
}) 
public class <i>_MyProtocol_</i>TestSuite {

    <xsl:text/>
                        <xsl:for-each select="jelclass[@package='org.ogf.saga.logicalfile' or @package='org.ogf.saga.namespace' or @package='org.ogf.saga.permissions']
                                                      [contains(@type,'Test')]
                                                      [not(comment/attribute/@name='@deprecated')]">
                            <xsl:call-template name="CODE_jelclass"/>
                        </xsl:for-each>
                        <xsl:for-each select="jelclass[@package='org.ogf.saga.logicalfile' or @package='org.ogf.saga.namespace']
                                                      [contains(@type,'Test')]
                                                      [contains(@type,'Move')]
                                                      [not(comment/attribute/@name='@deprecated')]">
                            <xsl:call-template name="CODE_jelclass">
                                <xsl:with-param name="targetAdaptor">_to_Emulator</xsl:with-param>
                                <xsl:with-param name="targetProtocol">test</xsl:with-param>
                            </xsl:call-template>
                        </xsl:for-each>
}
                    </pre>
                </subsection>
            </section>


            <section name="Testing a job adaptor">
                <i>Copy-paste this code to your test class.</i>
                <pre>package integration;

@RunWith(Suite.class)
@SuiteClasses({
    <i>_MyProtocol_</i>TestSuite.<i>_WhatEverSubClassYouWantInYourTestSuite_</i>.class,
    ...
}) 
public class <i>_MyProtocol_</i>TestSuite {

    <xsl:text/>
                    <xsl:for-each select="jelclass[@package='org.ogf.saga.job.run'][contains(@type,'Test')][not(comment/attribute/@name='@deprecated')]">
                        <xsl:call-template name="CODE_jelclass"/>
                    </xsl:for-each>
}
                </pre>
            </section>

            <section name="Testing a resource adaptor">
                <i>Copy-paste this code to your test class.</i>
                <pre>package integration;

@RunWith(Suite.class)
@SuiteClasses({
    <i>_MyProtocol_</i>TestSuite.<i>_WhatEverSubClassYouWantInYourTestSuite_</i>.class,
    ...
}) 
public class <i>_MyProtocol_</i>TestSuite {

    <xsl:text/>
                    <xsl:for-each select="jelclass[@package='org.ogf.saga.resource'][contains(@type,'Test')][not(contains(@type,'ResourceBase'))][not(comment/attribute/@name='@deprecated')]">
                        <xsl:call-template name="CODE_jelclass"/>
                    </xsl:for-each>
}
                </pre>
            </section>

        </body></document>
    </xsl:template>

    <xsl:template name="CODE_jelclass">
        <xsl:param name="targetAdaptor" select="''"/>
        <xsl:param name="targetProtocol">myprotocol</xsl:param>
        <xsl:variable name="package" select="substring-before(substring-after(@fulltype,'org.ogf.saga.'),concat('.',@type))"/>
    public static class <i>_MyProtocol_</i><xsl:value-of select="$targetAdaptor"/><xsl:value-of select="@type"/> extends <a
        href="saga-api-test/xref/org/ogf/saga/{$package}/{@type}.html"><xsl:value-of select="@type"/></a> {
        public <i>_MyProtocol_</i><xsl:value-of select="$targetAdaptor"/><xsl:value-of select="@type"/>() throws Exception {super("<i>myprotocol</i>"<xsl:text/>
        <xsl:if test="contains(@type,'DataMovement')">, "<i><xsl:value-of select="$targetProtocol"/></i>"</xsl:if>);}<xsl:text/>
        <xsl:for-each select="methods/method[@visibility='public' and starts-with(@name,'test_')]"><xsl:call-template name="CODE_method"/></xsl:for-each>
    }
    </xsl:template>
    <xsl:template name="CODE_method">
        <xsl:value-of select="$CR"/>
        <xsl:text>@Override @Test @Ignore("TODO: explain why this test is ignored...")
        </xsl:text>
        <xsl:value-of select="@visibility"/><xsl:text> </xsl:text>
        <xsl:call-template name="CLASSNAME"><xsl:with-param name="FQClass" select="@fulltype"/></xsl:call-template><xsl:text> </xsl:text>
        <xsl:value-of select="@name"/>(<xsl:text/>
        <xsl:for-each select="params/param"><xsl:call-template name="CODE_param"/></xsl:for-each>)<xsl:text/>
        <xsl:for-each select="exceptions"><xsl:call-template name="CODE_exceptions"/></xsl:for-each> {}<xsl:text/>
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