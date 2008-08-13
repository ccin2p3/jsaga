/*
 *
 * COPYRIGHT (C) 2003-2006 National Institute of Informatics, Japan
 *                         All Rights Reserved
 * COPYRIGHT (C) 2003-2006 Fujitsu Limited
 *                         All Rights Reserved
 * 
 * This file is part of the NAREGI Grid Super Scheduler software package.
 * For license information, see the docs/LICENSE file in the top level 
 * directory of the NAREGI Grid Super Scheduler source distribution.
 *
 *
 * Revision history:
 *      Revision: 882
 *      Id: LoggerManager.java 882 2007-03-29 10:58:42Z kawamura
 */
package org.naregi.ss.service.client.logging;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.naregi.ss.service.client.ConfigManager;
import org.naregi.ss.service.client.JobScheduleServiceException;

/**
 * Logging management class
 */
public class LoggerManager {

	private static String LHD = "ss_api_";
	private static String PFX = "log";
	private static String LOCK = ".lck";
	
    private static Logger logger;
    private static File	logdir;

    static {
    	
        try
        {
            int logFileSize = 100;
            int logFileCount = 5;
            
            File tmpdir;
            try
	        {
            	String value = ConfigManager.getProperty("log.file.location");
            	tmpdir = new File(value);
	        } catch (Exception e) {
            		String _tmpdir = System.getProperty("java.io.tmpdir","/tmp");
            		tmpdir = new File(_tmpdir);
	        }
            
        	String user;
            try
	        {
            	user = System.getProperty("user.name",	"");
	        } catch (Exception e) {
            	throw new Exception("Unable to get user name ");
	        }
	        
	        LHD += user;

            String enabled;
            try
	        {
                enabled = ConfigManager.getProperty("log.enabled", "false");
	        } catch (Exception e) {
	            enabled = "false";
	        }
            if (enabled.equals("true")) {
	        logdir = File.createTempFile(LHD, PFX, tmpdir);
		logdir.delete();
		logdir.mkdir();

            	File lockfile = new File(logdir.getAbsolutePath() + LOCK);
            	if (lockfile.createNewFile() == false) {
            		throw new IOException("Unable to create lock file " + lockfile.getAbsolutePath());
            	}
            	lockfile.deleteOnExit();
            
            	try {
                	logger = Logger.getLogger( LHD );
                    FileHandler handler = 
                    	new FileHandler( logdir.getPath() + "/" + LHD + ".log", 
                    					1024 * 1024 * logFileSize, // MB
        								logFileCount);

                    handler.setFormatter( new LoggerFormatter() );
                    logger.addHandler( handler );
            	} catch (Exception e) {
                	logger = new NullLogger(LHD, null);
            	}
            } else {
            	logger = new NullLogger(LHD, null);
            }

            try
	        {
                String level = ConfigManager.getProperty("log.level");
            	logger.setLevel(Level.parse(level));
	        } catch (Exception e) {
	        	logger.setLevel(Level.CONFIG);
	        }
            
            logger.info("Log Level is " +  logger.getLevel().toString());
            logger.info("Initialization of Logger is completed." );

            String saveOldLog;
            try
	        {
            	saveOldLog = ConfigManager.getProperty("log.file.remain_enable", "false");
	        } catch (Exception e) {
	        	saveOldLog = "false";
	        }
	        
            logger.config("Save old log is " +  saveOldLog);
            if (!saveOldLog.equals("true")) {
                logger.info("Delete old log file start." );
            	removeLogDir(tmpdir, logger);
                logger.info("Delete old log file end." );
            }
            
        }
        catch( Exception e )
        {
        	logger = new NullLogger(LHD, null);
            logdir = null;
        }
	}
    
	private static void removeLogDir(File dir, Logger logger) throws JobScheduleServiceException {
		
		File[] flist = dir.listFiles(
				new RemoveFileFilter(LHD + ".*" + PFX));
		
		for (int i=0;i < flist.length;i++) {
			File lockfile = new File(flist[i].getAbsolutePath() + LOCK);
			if (lockfile.exists() == false) {
				_removeLogDir(flist[i], logger);
				flist[i].delete();
			}
		} 
	}

	private static void _removeLogDir(File dir, Logger logger)  {
		
		File[] flist = dir.listFiles();
		
		for (int i=0;i < flist.length;i++) {
			if (flist[i].isDirectory() == true) {
				_removeLogDir(flist[i], logger);
			} else {
				flist[i].delete();
			}
		} 
	}

    /**
     * Get handler for managing logging
     * 
     * @return handler for managing logging
     * @throws JobScheduleServiceException
     */
    public static Logger getLogger() throws JobScheduleServiceException {
    	return logger;
    }

    /**
     * Get File type instance which has logging directory information
     * 
     * @return File type instance which has logging directory information
     * @throws JobScheduleServiceException
     */
    private static File  getLogDir() throws JobScheduleServiceException {
    	if (logdir == null) {
    		throw new JobScheduleServiceException("Unable to create temporary directory.");
    	}
    	return logdir;
    }
}

/**
 * Logging format management class
 */
class LoggerFormatter extends Formatter {
    /**
     * Format logging record
     * @param record logging record
     * @return formatted logging record
     */
    public String format(LogRecord record) {
        StringBuffer buf = null;

        buf = new StringBuffer();
//        buf.append("Sequence No.:");
//        buf.append(record.getSequenceNumber());
//        buf.append("\n");
        buf.append(getTopMessage());
        buf.append("\n");
        buf.append("Level:");
        buf.append(record.getLevel());
        buf.append("\n");

        buf.append("Thread No.:");
        buf.append(record.getThreadID());
        buf.append("\n");

        buf.append("ClassName:");
        buf.append(record.getSourceClassName());
        buf.append(" , ");
        buf.append("MethodName:");
        buf.append(record.getSourceMethodName());
        buf.append("\n");

        buf.append("Message:");
        buf.append(formatMessage(record));
        buf.append("\n\n");


        return buf.toString();
    }

    /**
     * Get header strings for logging record
     * @return header strings
     */
    public String getTopMessage() {
        Calendar   cal = null;

        cal = Calendar.getInstance(Locale.JAPAN);
 
        return "[" + cal.get(Calendar.YEAR) + "/" +
               getString(cal.get(Calendar.MONTH) + 1, 2) +
               "/" + getString(cal.get(Calendar.DATE), 2) + " " +
               getString(cal.get(Calendar.HOUR_OF_DAY), 2) + ":" +
               getString(cal.get(Calendar.MINUTE), 2) + ":" +
               getString(cal.get(Calendar.SECOND), 2) + "-" +
               cal.get(Calendar.MILLISECOND) + "]";
    }

    /**
     * Padding for restrictive digit number
     * @param value raw value
     * @param digit digit number
     * @return correction string
     */
    private String getString(int value, int digit) {
        return (value < 10) ? "0" + Integer.toString(value) : Integer.toString(value);
    }
}

/**
 * Logger class for restraint of logging.
 * This class is used for restainting logging.
 */
class NullLogger extends Logger {

	public void config(String arg0) {
	}

	public void fine(String arg0) {
	}

	public void finer(String arg0) {
	}

	public void finest(String arg0) {
	}

	public void info(String arg0) {
	}

	public void severe(String arg0) {
	}

	public void warning(String arg0) {
	}

	protected NullLogger(String name, String resourceBundleName) {
		super(name, resourceBundleName);
	}

}

/**
 * FilenameFilter class for confirming logging file or not.
 * This class is used for removing old loging files.
 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
 */
class RemoveFileFilter implements FilenameFilter {

	String lockFileReqext;
	public RemoveFileFilter(String lockFileReqext) {
		this.lockFileReqext = lockFileReqext;
	}

	/*
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File file, String fname) {
		Pattern pattern = Pattern.compile(lockFileReqext);
		Matcher matcher = pattern.matcher(fname);
		if (matcher.matches()) {
			if (file.isDirectory()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
}
