package fr.in2p3.jsaga.adaptor.wsgram.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatusNotifier;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
import org.globus.gram.GramException;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.Timeout;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramJobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WSGramJobMonitorAdaptor extends WSGramJobAdaptorAbstract implements QueryIndividualJob {
	// ListenIndividualJob

    public String getType() {
        return "wsgram";
    }

    public Usage getUsage() {
        return null;    // no usage
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;    // no default
    }
    
    public JobStatus getStatus(String nativeJobHandle) throws Timeout, NoSuccess {
        GramJob job = this.getGramJobById(nativeJobHandle);
        try {
			job.refreshStatus();
		} catch (GramException e) {
            this.rethrowException(e);
        } catch (GSSException e) {
            throw new NoSuccess(e);
        } catch (Exception e) {
			throw new NoSuccess(e);
		}       
        return new WSGramJobStatus(job.getHandle(),job.getState(), job.getState().toString());
    }

	public void subscribeJob(String nativeJobHandle, JobStatusNotifier notifier) throws Timeout, NoSuccess {
        GramJobListener listener = new WSGramJobStatusListener(notifier);
    	GramJob job = super.getGramJobById(nativeJobHandle);
    	try {
        	//job.addListener(listener);
        	//job.bind();
        	System.err.println("End bind");
		} /*catch (GramException e) {
			job.removeListener(listener);
            if (e.getErrorCode() == GramException.CONNECTION_FAILED ||
            		e.getErrorCode() == GramException.JOB_QUERY_DENIAL ||
            		e.getErrorCode() == GramException.HTTP_UNFRAME_FAILED) {
                //WARNING: Globus does not distinguish job DONE and job manager stopped
                WSGramJobStatus status = new WSGramJobStatus(nativeJobHandle, StateEnumeration.Done, "DONE");
                notifier.notifyChange(status);
            } else {
                this.rethrowException(e);
            }
			throw new NoSuccess(e);
		}
		catch (GSSException e) {
            job.removeListener(listener);
            throw new NoSuccess(e);
        } */catch (Exception e) {
        	job.removeListener(listener);
            throw new NoSuccess(e);
		}
    }

    public void unsubscribeJob(String nativeJobHandle) throws Timeout, NoSuccess {
        GramJob job = this.getGramJobById(nativeJobHandle);
       /*try {	
        	job.unbind();
        	System.err.println("End unbind");
		} catch (GramException e) {
        	if (e.getErrorCode() == GramException.CONNECTION_FAILED ||
            		e.getErrorCode() == GramException.JOB_QUERY_DENIAL ||
            		e.getErrorCode() == GramException.HTTP_UNFRAME_FAILED) {
                // ignore (Globus does not distinguish job DONE and job manager stopped)
            } else {
                this.rethrowException(e);
            }
        } catch (GSSException e) {
            throw new NoSuccess(e);
        } catch (NoSuchResourceException e) {
        	throw new NoSuccess(e);
		} catch (Exception e) {
			throw new NoSuccess(e);
		}*/
    }
    
    private void rethrowException(GramException e) throws Timeout, NoSuccess {
        switch(e.getErrorCode()) {
            case GRAMProtocolErrorConstants.INVALID_JOB_CONTACT:
            case GRAMProtocolErrorConstants.ERROR_CONNECTION_FAILED:
                throw new Timeout(e);
            default:
                throw new NoSuccess(e);
        }
    }

}
