package fr.in2p3.jsaga.adaptor.cream.job;

import org.glite.x2007.x11.ce.cream.types.BaseFaultType;
import org.glite.x2007.x11.ce.cream.types.JobInfoResult;
import org.glite.x2007.x11.ce.cream.types.JobRegisterResult;
import org.glite.x2007.x11.ce.cream.types.Result;
import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   CreamExceptionFactory
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   14 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class CreamExceptionFactory {
    public static void rethrow(JobRegisterResult[] resultArray) throws NoSuccessException {
        for (int i=0; resultArray!=null && i<resultArray.length; i++) {
            BaseFaultType fault = null;
            if(resultArray[i].getDelegationProxyFault() != null) {
                fault = resultArray[i].getDelegationProxyFault();
            } else if(resultArray[i].getDelegationIdMismatchFault() != null) {
                fault = resultArray[i].getDelegationIdMismatchFault();
            } else if(resultArray[i].getGenericFault() != null) {
                fault = resultArray[i].getGenericFault();
            } else if(resultArray[i].getLeaseIdMismatchFault() != null) {
                fault = resultArray[i].getLeaseIdMismatchFault();
            }
            if (fault != null) {
                String message = fault.getFaultCause()!=null && !fault.getFaultCause().equals("N/A")
                        ? fault.getFaultCause()
                        : fault.getClass().getName();
                throw new NoSuccessException(message, fault);
            }
        }
    }

    public static void rethrow(JobInfoResult[] resultArray) throws NoSuccessException {
        for (int i=0; resultArray!=null && i<resultArray.length; i++) {
            BaseFaultType fault = null;
            if (resultArray[i].getDateMismatchFault() != null) {
                fault = resultArray[i].getDateMismatchFault();
            } else if (resultArray[i].getDelegationIdMismatchFault() != null) {
                fault = resultArray[i].getDelegationIdMismatchFault();
            } else if (resultArray[i].getGenericFault() != null) {
                fault = resultArray[i].getGenericFault();
            } else if (resultArray[i].getJobStatusInvalidFault() != null) {
                fault = resultArray[i].getJobStatusInvalidFault();
            } else if (resultArray[i].getJobUnknownFault() != null) {
                fault = resultArray[i].getJobUnknownFault();
            } else if (resultArray[i].getLeaseIdMismatchFault() != null) {
                fault = resultArray[i].getLeaseIdMismatchFault();
            }
            if (fault != null) {
                String message = fault.getFaultCause()!=null && !fault.getFaultCause().equals("N/A")
                        ? fault.getFaultCause()
                        : fault.getClass().getName();
                throw new NoSuccessException(message, fault);
            }
        }
    }

    public static void rethrow(Result[] resultArray) throws NoSuccessException {
        for (int i=0; resultArray!=null && i<resultArray.length; i++) {
            BaseFaultType fault = null;
            if (resultArray[i].getDateMismatchFault() != null) {
                fault = resultArray[i].getDateMismatchFault();
            } else if (resultArray[i].getDelegationIdMismatchFault() != null) {
                fault = resultArray[i].getDelegationIdMismatchFault();
            } else if (resultArray[i].getGenericFault() != null) {
                fault = resultArray[i].getGenericFault();
            } else if (resultArray[i].getJobStatusInvalidFault() != null) {
                fault = resultArray[i].getJobStatusInvalidFault();
            } else if (resultArray[i].getJobUnknownFault() != null) {
                fault = resultArray[i].getJobUnknownFault();
            } else if (resultArray[i].getLeaseIdMismatchFault() != null) {
                fault = resultArray[i].getLeaseIdMismatchFault();
            }
            if (fault != null) {
                String message = fault.getFaultCause()!=null && !fault.getFaultCause().equals("N/A")
                        ? fault.getFaultCause()
                        : fault.getClass().getName();
                throw new NoSuccessException(message, fault);
            }
        }
    }
}
