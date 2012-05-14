/*
 * Copyright 1999-2006 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.globus.io.urlcopy;

import org.globus.io.urlcopy.UrlCopy;
import java.io.PrintStream;
import java.util.Calendar;


public class XferBars
    implements  UrlCopyListener, Runnable
{
    protected long                      totalBytes = -1;
    protected long                      sentBytes = -1;
    protected PrintStream               outStream;
    protected int                       colCount;
    protected int                       freq;
    protected boolean                   pretty;
    protected boolean                   done = false;
    protected boolean                   first = true;
    protected Thread                    lT;
    protected Calendar                  nextUpdate = null;

    protected void init(
        boolean                         pretty, 
        PrintStream                     out, 
        int                             cols,
        int                             freq)
    {
        this.pretty = pretty;
        this.outStream = out;
        this.colCount = cols;
        this.freq = freq;
    }

    public XferBars()
    {
        this.init(true, System.out, 80, 250);
    }

    public XferBars(boolean pretty)
    {
        this.init(pretty, System.out, 80, 250);
    }

    public XferBars(boolean pretty, PrintStream out)
    {
        this.init(pretty, out, 80, 250);
    }

    public XferBars(boolean pretty, PrintStream out, int cols)
    {
        this.init(pretty, out, cols, 250);
    }

    public XferBars(boolean pretty, PrintStream out, int cols, int freq)
    {
        this.init(pretty, out, cols, freq);
    }

    protected void printOut()
    {
        String                          outS;

        if(this.pretty && this.totalBytes > 0 && this.sentBytes >= 0)
        {   
            outS = makeBar(this.sentBytes, this.totalBytes);
        }
        else
        {
            outS = prettyCount(this.sentBytes, 6);
        }

        this.outStream.print("\r");
        this.outStream.print(outS);
        this.outStream.flush();
    }

    public synchronized void transfer(long transferedBytes, long totalBytes)
    {

        this.totalBytes = totalBytes;
        this.sentBytes = transferedBytes;

        if(this.first)
        {
            this.outStream.print("\n");
            this.first = false;
        }
        else
        {
            Calendar now = Calendar.getInstance();

            if(now.before(this.nextUpdate))
            {
                return;
            }
        }
        this.nextUpdate = Calendar.getInstance();
        this.nextUpdate.add(Calendar.MILLISECOND, this.freq);

        this.printOut();
    }

    public synchronized void transferError(Exception exception)
    {
        this.printOut();
        this.outStream.print("\n");
        this.outStream.println("Error");
        this.outStream.flush();
    }

    public synchronized void transferCompleted()
    {
        this.printOut();
        this.outStream.print("\n");
        this.outStream.println("Done\n");
        this.outStream.flush();
    }

    // return a string with the long value properly suffixed 
    protected String prettyCount(long tb, int maxLen)
    {
        int                             ndx = 0;
        String suffix[] = {" B", "KB", "MB", "GB"};

        if(tb < 0)
        {
            return "Unknown";
        }

        float tbF = (float) tb;
        while(tbF > 10240.0f && ndx < suffix.length - 1)
        {
            ndx++;
            tbF = tbF / 1024.0f;
        }
        tb = (int)tbF;

        String rc =  new Long(tb).toString();

        if(rc.length() > maxLen)
        {
            int endNdx = maxLen - 3;
            rc = rc.substring(0, endNdx) + "...";
        }
        else
        {
            // some silliness to get to 2 decimals
            if(rc.length() < maxLen - 2)
            {
                tb = (int)(tbF * 100.0f);
                tbF = (float)tb / 100.0f;
                rc = new Float(tbF).toString();
            }
            int spaceCount = maxLen - rc.length();
            for(int i = 0; i < spaceCount; i++)
            {
                rc = " " + rc;
            }
        }

        rc = rc + suffix[ndx];

        return rc;
    }


    // make progress bar of the format:
    // <count> | [XXXXXX..............] XX%
    protected String makeBar(long sofar, long total)
    {
        String byteString = this.prettyCount(sofar, 6); 
        String                          doneCh = "X";
        String                          notDoneCh = ".";

        // there are 9 pad characters: <sp>[]<sp>PPP%<sp>
        int pad = 9;
        int pgLen = this.colCount - byteString.length() - pad;

        // this will be rounded down, but who cares?
        int percent = (int)((sofar * 100) / total);
        int xCount = (percent * this.colCount) / 100;

        String bar = byteString + " [";
        for(int i = 0; i < pgLen; i++)
        {
            if(i < xCount)
            {
                bar = bar + doneCh;
            }
            else
            {
                bar = bar + notDoneCh;
            }
        }
        bar = bar + "] " + percent + "% ";

        return bar;
    }


    public synchronized void run()
    {

    }
}
