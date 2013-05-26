package ch.hsr.mixtape.exception;

/**
 * This exception should only be raised the first time mixtape-core is executed
 * on a system, i.e. when no database is still available.
 * 
 * @author Stefan Derungs
 */
public class FirstRunException extends Exception {

	private static final long serialVersionUID = -6766716760400236904L;

}
