package fr.in2p3.jsaga.adaptor.ourgrid.job;

import fr.in2p3.jsaga.adaptor.job.BadResource;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/

public class OurGridNotFoundJobException extends BadResource {

	private static final long serialVersionUID = 1L;

	public OurGridNotFoundJobException(String messenge) {
		
		super(messenge);
	}
}
