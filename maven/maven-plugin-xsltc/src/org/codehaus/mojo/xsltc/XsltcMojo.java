/*
 The MIT License

 Copyright (c) 2004, The Codehaus

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
/*
 * Created on Aug 17, 2006
 */
package org.codehaus.mojo.xsltc;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Vector;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.xalan.xsltc.compiler.XSLTC;

/**
 * Compiles XSLT stylesheets into Xalan translets.
 *
 * @goal compile
 * @phase compile
 * @author Matt Whitlock
 */
public class XsltcMojo extends AbstractMojo
{

    /**
     * XSLT stylesheets to be compiled.
     *
     * @parameter
     * @required
     */
    private File[] stylesheets;

    /**
     * Package name for compiled translets.
     *
     * @parameter
     */
    private String packageName;

    /**
     * Directory in which to place compiled translets.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        XSLTC xsltc = new XSLTC();
        xsltc.init();
        xsltc.setDebug( true );
        xsltc.setPackageName( packageName );
        xsltc.setDestDirectory( outputDirectory.getAbsolutePath() );
        Log log = getLog();
        Vector v = new Vector();
        for (int i=0; i<stylesheets.length; i++)
        {
            File stylesheet = (File) stylesheets[i];
            log.info( "Adding " + stylesheet + " to compilation vector");
            try
            {
                v.add(stylesheet.toURL());
            }
            catch ( MalformedURLException e )
            {
                throw new MojoExecutionException( "Could not convert file to URL", e );
            }
        }
        boolean success = xsltc.compile( v );
        for (int j=0; j<xsltc.getWarnings().size(); j++)
        {
            Object warning = xsltc.getWarnings().get(j);
            log.warn( warning.toString() );
        }
        for (int j=0; j<xsltc.getErrors().size(); j++)
        {
            Object error = xsltc.getErrors().get(j);
            log.error( error.toString() );
        }
        if ( !success )
        {
            throw new MojoFailureException( "There were XSLTC errors" );
        }
    }

}
