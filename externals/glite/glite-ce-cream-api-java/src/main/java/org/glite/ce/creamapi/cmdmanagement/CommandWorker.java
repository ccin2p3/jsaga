package org.glite.ce.creamapi.cmdmanagement;


import org.apache.log4j.Logger;
import org.glite.ce.creamapi.cmdmanagement.queue.CommandQueueException;

/**
 * This class implements a WorkerThread inside the CREAMJournalManager. Worker
 * threads are used to execute tasks from the journal manager in parallel. The
 * behavior of a worker thread is very simple: it fetches a command from the
 * journal manager and executes it. Accesses to the journal manager are (of
 * course) done in mutual exclusion. We need to guarantee that commands related
 * to the same job are executed in the exact same order as they appear in the
 * journal. To do so, we use the <code>scoreboard</code> variable, which is
 * the set of ID of jobs which are currently being executed by the worker
 * threads. During the fetchCommand() method, the journal is checked one entry
 * at a time. If the entry refers to a command related to a job on which another
 * command appears in the scoreboard, that entry is skipped.
 * 
 * @author Moreno Marzolla
 */
final class CommandWorker extends Thread {
    private final static Logger logger = Logger.getLogger(CommandWorker.class.getName());

    private AbstractCommandExecutor executor = null;
    private int id;
    private boolean isProcessing = false;
    private boolean exit = false;
    

    public CommandWorker(AbstractCommandExecutor executor, int id) {
        super("Worker Thread " + id);
        setDaemon(true); // This is actually a workaround to allow clean

        this.setName("Worker Thread " + id);
        this.executor = executor;
        this.id = id;
    }

    public boolean isProcessing() {
        return isProcessing;
    }
    
    public void destroy() {
        logger.info("destroying Worker Thread " + id);
        exit = true;
        logger.info("destroyed!");
    }
    
    public void run() {
        Command cmd = null;
        
        do {
            if(!executor.getCommandQueue().isOpen()) {
                logger.debug("Worker Thread " + id + ": the queue is closed -> exit");
                return;
            }
                
            logger.debug("Worker Thread " + id + " waiting for a job");
            try {
                cmd = executor.getCommandQueue().fetchCommand();
            } catch (CommandQueueException e1) {
                if (e1.getMessage().equals("queue closed!")) {
                    exit = true;
                } else {
                    logger.debug("Worker Thread " + id + " error: " + e1.getMessage());
                }
                cmd = null;
            }

            if (cmd != null && !exit) {
                String cmdId = cmd.getId();
                try {
                    isProcessing = true;
                    cmd.setStatus(Command.PROCESSING);                    
                    executor.getCommandManager().updateCommand(cmd);
                    
                    logger.debug("Worker Thread " + id + " got cmd=" + cmd.getClass().getName() + ". Now executing.");
                    executor.execute(cmd);
                    
                    cmd.setStatus(Command.SUCCESSFULL);
                    executor.getCommandManager().updateCommand(cmd);
                } catch (Exception e) {
                    cmd.setStatus(Command.ERROR);
                    cmd.setFailureReason(e.getMessage());     
                    
                    try {
                        executor.getCommandManager().updateCommand(cmd);
                    } catch (IllegalArgumentException iae) {
                        logger.error(iae);
                    } catch (CommandException ce) {
                        logger.error(ce);
                    }
                    
                    logger.warn("Worker Thread " + id + " command failed: the reason is = " + cmd.getFailureReason());
                } finally {
                    try {
                        executor.getCommandQueue().dequeueCommand(cmdId);
                        executor.getCommandManager().deleteCommand(cmdId);
                    } catch (IllegalArgumentException e) {
                        logger.error(e);
                    } catch (CommandException e) {
                        logger.error(e);
                    } catch (CommandQueueException e) {
                        logger.error(e);
                    }
                }
                
                isProcessing = false;
            }
        } while (!exit && !isInterrupted());

        logger.debug("Worker Thread " + id + " terminating.");
    }
}