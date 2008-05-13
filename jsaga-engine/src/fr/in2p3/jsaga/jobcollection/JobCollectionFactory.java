package fr.in2p3.jsaga.jobcollection;

import fr.in2p3.jsaga.impl.SagaFactoryImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 * Factory for objects from the jobcollection package.
 */
public abstract class JobCollectionFactory {

    private static JobCollectionFactory factory;

    private static synchronized void initializeFactory()
        throws NotImplemented, NoSuccess {
        if (factory == null) {
            factory = new SagaFactoryImpl().createJobCollectionFactory();
        }
    }

    /**
     * Creates a job collection description. To be provided by the implementation.
     * @param language the job description language.
     * @param jobDescStream the job collection description stream.
     * @return the job collection description.
     */
    protected abstract JobCollectionDescription doCreateJobCollectionDescription(String language, InputStream jobDescStream)
        throws NotImplemented, BadParameter, NoSuccess;

    /**
     * Creates a job collection description. To be provided by the implementation.
     * @param language the job description language.
     * @param jobDescStream the job collection description stream.
     * @param collectionName the name of the job collection.
     * @return the job collection description.
     */
    protected abstract JobCollectionDescription doCreateJobCollectionDescription(String language, InputStream jobDescStream, String collectionName)
        throws NotImplemented, BadParameter, NoSuccess;

    /**
     * Creates a job collection manager. To be provided by the implementation.
     * @param session the session handle.
     * @return the job collection manager.
     */
    protected abstract JobCollectionManager doCreateJobCollectionManager(Session session)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            Timeout, NoSuccess;

    /**
     * Creates a job collection description.
     * @param language the job description language.
     * @param jobDescFile the job collection description file.
     * @return the job collection description.
     */
    public static JobCollectionDescription createJobCollectionDescription(String language, File jobDescFile)
        throws NotImplemented, BadParameter, NoSuccess {
        initializeFactory();
        try {
            InputStream jobDescStream = new FileInputStream(jobDescFile);
            JobCollectionDescription jobDesc = factory.doCreateJobCollectionDescription(language, jobDescStream);
            jobDescStream.close();
            return jobDesc;
        } catch (IOException e) {
            throw new NoSuccess("Failed to load job collection description: "+jobDescFile.getPath(), e);
        }
    }

    /**
     * Creates a job collection description.
     * @param language the job description language.
     * @param jobDescFile the job collection description file.
     * @param collectionName the name of the job collection.
     * @return the job collection description.
     */
    public static JobCollectionDescription createJobCollectionDescription(String language, File jobDescFile, String collectionName)
        throws NotImplemented, BadParameter, NoSuccess {
        initializeFactory();
        try {
            InputStream jobDescStream = new FileInputStream(jobDescFile);
            JobCollectionDescription jobDesc = factory.doCreateJobCollectionDescription(language, jobDescStream, collectionName);
            jobDescStream.close();
            return jobDesc;
        } catch (IOException e) {
            throw new NoSuccess("Failed to load job collection description: "+jobDescFile.getPath(), e);
        }
    }

    /**
     * Creates a job collection description.
     * @param language the job description language.
     * @param jobDescStream the job collection description stream.
     * @return the job collection description.
     */
    public static JobCollectionDescription createJobCollectionDescription(String language, InputStream jobDescStream)
        throws NotImplemented, BadParameter, NoSuccess {
        initializeFactory();
        return factory.doCreateJobCollectionDescription(language, jobDescStream);
    }

    /**
     * Creates a job collection description.
     * @param language the job description language.
     * @param jobDescStream the job collection description stream.
     * @param collectionName the name of the job collection.
     * @return the job collection description.
     */
    public static JobCollectionDescription createJobCollectionDescription(String language, InputStream jobDescStream, String collectionName)
        throws NotImplemented, BadParameter, NoSuccess {
        initializeFactory();
        return factory.doCreateJobCollectionDescription(language, jobDescStream, collectionName);
    }

    /**
     * Creates a job collection manager.
     * @param session the session handle.
     * @return the job collection manager.
     */
    public static JobCollectionManager createJobCollectionManager(Session session)
        throws NotImplemented, IncorrectURL,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied,
            Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateJobCollectionManager(session);
    }
}
