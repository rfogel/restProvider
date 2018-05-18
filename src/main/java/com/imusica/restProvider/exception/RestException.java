package com.imusica.restProvider.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RestException extends Exception
{
	private static final long serialVersionUID = -2779367138066974156L;
	
	private String message;

    public String getErrorMsg()
    {
        return message;
    }
    
    public RestException(String message)
    {
    	super(message);
    	this.message = message;
    }
    
    public RestException(Exception e)
    {
    	super(e);
    }
    
    public static String getStackTrace(final Throwable throwable) 
    {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
}
