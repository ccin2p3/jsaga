package fr.in2p3.jsaga.impl.job.staging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   OutputDataStagingFromRemote
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   2 aout 2016
 * ***************************************************
 * Description:                                      */

public class DataStagingTest {

    @Test
    public void InputDataStaging() throws NotImplementedException, BadParameterException, NoSuccessException {
        AbstractDataStaging ads;
        
        ads = DataStagingFactory.create("/tmp/myfile > remote://myserver/myfile");
        assertTrue(ads instanceof InputDataStagingToRemote);

        ads = DataStagingFactory.create("myfile > remote://myserver/myfile");
        assertTrue(ads instanceof InputDataStagingToRemote);

        ads = DataStagingFactory.create("myfile >> remote://myserver/myfile");
        assertTrue(ads instanceof InputDataStagingToRemote);

        ads = DataStagingFactory.create("myfile >> /tmp/myfile");
        assertTrue(ads instanceof InputDataStagingToWorker);

        ads = DataStagingFactory.create("file:/tmp/myfile > remote://myserver/myfile");
        assertTrue(ads instanceof InputDataStagingToRemote);
        assertTrue(ads.isInput());
        assertEquals("file", ads.getLocalProtocol());
        assertEquals("remote", ads.getWorkerProtocol());
    }
    
    @Test
    public void OutputDataStaging() throws NotImplementedException, BadParameterException, NoSuccessException {
        AbstractDataStaging ads;
        
        ads = DataStagingFactory.create("/tmp/myfile < remote://myserver/myfile");
        assertTrue(ads instanceof OutputDataStagingFromRemote);

        ads = DataStagingFactory.create("myfile < remote://myserver/myfile");
        assertTrue(ads instanceof OutputDataStagingFromRemote);

        ads = DataStagingFactory.create("myfile << remote://myserver/myfile");
        assertTrue(ads instanceof OutputDataStagingFromRemote);

        ads = DataStagingFactory.create("myfile << /tmp/myfile");
        assertTrue(ads instanceof OutputDataStagingFromWorker);

        ads = DataStagingFactory.create("file:/tmp/myfile < remote://myserver/myfile");
        assertTrue(ads instanceof OutputDataStagingFromRemote);
        assertFalse(ads.isInput());
        assertEquals("file", ads.getLocalProtocol());
        assertEquals("remote", ads.getWorkerProtocol());
    }
    
    @Test(expected=BadParameterException.class)
    public void badOperator() throws NotImplementedException, BadParameterException, NoSuccessException {
        DataStagingFactory.create("/tmp/myfile TO remote://myserver/myfile");
    }

    @Test(expected=BadParameterException.class)
    public void badSyntax() throws NotImplementedException, BadParameterException, NoSuccessException {
        DataStagingFactory.create("/tmp/myfile");
    }
}
