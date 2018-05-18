package com.imusica.restProvider.response;

import org.apache.http.Header;
import org.apache.http.HttpStatus;

public class RestResponse
{
	private String body;
	
	private Integer statusCode;
	
	private Long requestDuration;
	
	private Header[] headers;
	
	public RestResponse() {
		statusCode = HttpStatus.SC_OK;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public Header[] getHeaders() {
		return headers;
	}

	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}
	
	public Boolean isResponseOK() {
		return statusCode == HttpStatus.SC_OK;
	}
	
	public Long getRequestDuration() {
		return requestDuration;
	}

	public static RestResponse getPrototype(String body, Integer statusCode, Header[] headers, Long requestDuration) 
	{
		RestResponse response = new RestResponse();
		response.body = body;
		response.statusCode = statusCode;
		response.headers = headers;
		response.requestDuration = requestDuration;
		
		return response;
	}
}
