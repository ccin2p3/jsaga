package fr.in2p3.jsaga.adaptor.arex.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.advanced.HoldableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.SuspendableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityDocumentType;
import org.nordugrid.schemas.besFactory.ActivityStateEnumeration;
import org.nordugrid.schemas.besFactory.ActivityStatusType;
import org.nordugrid.schemas.besFactory.holders.ActivityStatusTypeHolder;
import org.ggf.schemas.bes.x2006.x08.besFactory.BESFactoryPortType;

import org.nordugrid.schemas.besFactory.CantApplyOperationToCurrentStateFaultType;
import org.nordugrid.schemas.besFactory.InvalidActivityIdentifierFaultType;
import org.nordugrid.schemas.besFactory.OperationWillBeAppliedEventuallyFaultType;
import org.nordugrid.schemas.besFactory.NotAuthorizedFaultType;


import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityType;
import org.ggf.schemas.bes.x2006.x08.besFactory.FactoryResourceAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.InvalidRequestMessageFaultType;

import org.globus.wsrf.encoding.DeserializationException;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;

import org.nordugrid.schemas.arex.ARex_PortType;
import org.nordugrid.schemas.arex.ARex_ServiceLocator;
import org.nordugrid.schemas.arex.ChangeActivityStatusRequestType;
import org.nordugrid.schemas.arex.ChangeActivityStatusResponseType;

import org.ogf.saga.error.*;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ArexJobControlAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   9 déc 2010
* ***************************************************/
public class ArexJobControlAdaptor extends BesJobControlAdaptorAbstract /*implements SuspendableJobAdaptor*/ {

	protected ARex_PortType _arex_pt = null;

    public String getType() {
        return "arex";
    }
    
	public int getDefaultPort() {
		return 2010;
	}

	public Usage getUsage() {
    	return null;
    }
	
	protected Class getJobClass() {
		return ArexJob.class;
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{};
    }
    
    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new ArexJobMonitorAdaptor();
    }

	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
    	
    	if (_arex_pt != null) return;
    	
        ARex_ServiceLocator _arex_service = new ARex_ServiceLocator();
		try {
			_arex_service.setEndpointAddress("ARex", _bes_url);
			_arex_pt=(ARex_PortType) _arex_service.getARex();
		} catch (ServiceException e) {
			throw new NoSuccessException(e);
		}
    }

	public void disconnect() throws NoSuccessException {
        _arex_pt = null;
        super.disconnect();
    }

	/*
	private void changeStatus(String nativeJobId, ActivityStateEnumeration oldStatus, ActivityStateEnumeration newStatus) throws PermissionDeniedException, NoSuccessException {
		//ChangeActivityStatusRequestType request = new ChangeActivityStatusRequestType();
		//request.setActivityIdentifier(nativeId2ActivityId(nativeJobId));
		ActivityStatusType oldAst = new ActivityStatusType();
		oldAst.setState(oldStatus);
		ActivityStatusType newAst = new ActivityStatusType();
		newAst.setState(newStatus);
		//request.setNewStatus(newAst);
		try {
			_arex_pt.changeActivityStatus(nativeId2ActivityId(nativeJobId),	oldAst, new ActivityStatusTypeHolder(newAst)) ;
		} catch (OperationWillBeAppliedEventuallyFaultType e) {
			throw new NoSuccessException(e);
		} catch (CantApplyOperationToCurrentStateFaultType e) {
			throw new NoSuccessException(e);
		} catch (InvalidActivityIdentifierFaultType e) {
			throw new NoSuccessException(e);
		} catch (NotAuthorizedFaultType e) {
			throw new PermissionDeniedException(e);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}
	*/
}