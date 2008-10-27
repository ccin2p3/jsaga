package fr.in2p3.jsaga.adaptor.job.monitor;

import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobWrapperException
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 juin 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobWrapperException extends NoSuccessException {
    private static final int _ERROR_INPUT_DOES_NOT_EXIST_=90;
    private static final int _ERROR_INPUT_ALREADY_EXISTS_LOCALLY_=91;
    private static final int _ERROR_INPUT_FAIL_TO_TRANSFER_=92;
    private static final int _ERROR_OUTPUT_DOES_NOT_EXIST_=93;
    private static final int _ERROR_OUTPUT_ALREADY_EXISTS_LOCALLY_=94;
    private static final int _ERROR_OUTPUT_FAIL_TO_CREATE_=95;
    private static final int _ERROR_COMMAND_NOT_FOUND_=96;
    private static final int _ERROR_RESERVED_RETURN_CODE_=97;

    public JobWrapperException(String nativeJobId, int returnCode) {
        super("Job '"+nativeJobId+"': "+getMessage(returnCode));
    }

    public static String getMessage(int returnCode) {
        switch(returnCode) {
            case _ERROR_INPUT_DOES_NOT_EXIST_:
                return "Input file does not exist";
            case _ERROR_INPUT_ALREADY_EXISTS_LOCALLY_:
                return "Input file already exists locally";
            case _ERROR_INPUT_FAIL_TO_TRANSFER_:
                return "Input file can not be transfered";
            case _ERROR_OUTPUT_DOES_NOT_EXIST_:
                return "Output file does not exist";
            case _ERROR_OUTPUT_ALREADY_EXISTS_LOCALLY_:
                return "Output file already exists locally";
            case _ERROR_OUTPUT_FAIL_TO_CREATE_:
                return "Output file can not be created";
            case _ERROR_COMMAND_NOT_FOUND_:
                return "File transfer command not found";
            case _ERROR_RESERVED_RETURN_CODE_:
                return "Application returned code between 90 and 97";
            default:
                return "Application returned code: "+returnCode;
        }
    }
}
