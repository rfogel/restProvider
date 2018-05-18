package com.imusica.restProvider;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.imusica.restProvider.entity.BodyEntity;
import com.imusica.restProvider.entity.FormEntity;
import com.imusica.restProvider.entity.HeaderEntity;
import com.imusica.restProvider.entity.RawEntity;
import com.imusica.restProvider.exception.RestException;
import com.imusica.restProvider.exception.RestExceptionMessage;
import com.imusica.restProvider.response.RestResponse;

public class RestProvider
{
	private Logger logger = Logger.getLogger(this.getClass().getSimpleName());
	
	public final static Integer DEFAULT_POOL_SIZE = 100;
	public final static Integer DEFAULT_TIMEOUT = 60000;
	
	private CloseableHttpClient httpClient;
	
	private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
	
	public static RestProvider simpleClient()
	{
		return new RestProvider();
	}
	
	public static RestProvider client()
	{
		return new RestProvider(DEFAULT_POOL_SIZE, DEFAULT_TIMEOUT);
	}
	
	public static RestProvider client(Integer connectionPoolSize)
	{
		return new RestProvider(connectionPoolSize, DEFAULT_TIMEOUT);
	}
	
	public static RestProvider client(Integer connectionPoolSize, Integer connectionTimeout)
	{
		return new RestProvider(connectionPoolSize, connectionTimeout);
	}
	
	public PoolingHttpClientConnectionManager getClientConnectionManager() {
		return poolingHttpClientConnectionManager;
	}

	public void close() 
	{
		try {
			httpClient.close();
			poolingHttpClientConnectionManager.close();
		} catch (IOException e) {
			logger.error(String.format(RestExceptionMessage.ERROR_CLOSING_CONNECTION, e.getMessage()));
		}
	}
	
	private RestProvider() {}
	
	private RestProvider(Integer connectionPoolSize, Integer timeout)
	{
		SocketConfig socket = SocketConfig.custom().setSoTimeout(timeout).build();
		
		poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
		poolingHttpClientConnectionManager.setMaxTotal(connectionPoolSize);
		poolingHttpClientConnectionManager.setDefaultMaxPerRoute(connectionPoolSize);
		poolingHttpClientConnectionManager.setDefaultSocketConfig(socket);
		poolingHttpClientConnectionManager.closeIdleConnections(timeout, TimeUnit.MILLISECONDS);
		
		httpClient = HttpClients.custom().setConnectionManager(poolingHttpClientConnectionManager).build();
	}
	
	public void setLogger(Logger logger) 
	{
		this.logger = logger;
	}
	
	protected CloseableHttpClient getClient() 
	{
		if ( httpClient != null )
		{		
			showConnectionPoolStatus();
			
			return httpClient;
		}
		
		return HttpClients.createMinimal();
	}
	
	protected void showConnectionPoolStatus()
	{
		logger.debug("\\--------- Connection Pool Stats ----------\\");
		logger.debug("Max: " + poolingHttpClientConnectionManager.getTotalStats().getMax());
		logger.debug("Available: " + poolingHttpClientConnectionManager.getTotalStats().getAvailable());
		logger.debug("Leased: " + poolingHttpClientConnectionManager.getTotalStats().getLeased());
		logger.debug("Pending: " + poolingHttpClientConnectionManager.getTotalStats().getPending());
		logger.debug("\\------------------------------------------\\");
	}
	
	public RestResponse doGet(String endpoint) throws RestException {
		return doGet(endpoint, null, Boolean.TRUE);
	}
	
	public RestResponse doGet(String endpoint, HeaderEntity header) throws RestException {
		return doGet(endpoint, header, Boolean.TRUE);
	}
	
	//-----------------------------------------------------------------------------------
	
	public RestResponse doPost(String endpoint) throws RestException {
		return doPost(endpoint, null, null, Boolean.TRUE);
	}
	
	public RestResponse doPost(String endpoint, BodyEntity bodyEntity) throws RestException {
		return doPost(endpoint, bodyEntity, null, Boolean.TRUE);
	}
	
	public RestResponse doPost(String endpoint, BodyEntity bodyEntity, HeaderEntity header) throws RestException {
		return doPost(endpoint, bodyEntity, header, Boolean.TRUE);
	}
	
	public RestResponse doPost(String endpoint, BodyEntity bodyEntity, HeaderEntity header, Boolean ignoreCookies) throws RestException {
		return doMethod(new HttpPost(endpoint), endpoint, bodyEntity, header, Boolean.TRUE); 
	}
	
	//-----------------------------------------------------------------------------------
	
	public RestResponse doPut(String endpoint) throws RestException {
		return doPut(endpoint, null, null, Boolean.TRUE);
	}
	
	public RestResponse doPut(String endpoint, BodyEntity bodyEntity) throws RestException {
		return doPut(endpoint, bodyEntity, null, Boolean.TRUE);
	}
	
	public RestResponse doPut(String endpoint, BodyEntity bodyEntity, HeaderEntity header) throws RestException {
		return doPut(endpoint, bodyEntity, header, Boolean.TRUE);
	}
	
	public RestResponse doPut(String endpoint, BodyEntity bodyEntity, HeaderEntity header, Boolean ignoreCookies) throws RestException {
		return doMethod(new HttpPut(endpoint), endpoint, bodyEntity, header, Boolean.TRUE); 
	}
	
	//-----------------------------------------------------------------------------------
	
	public RestResponse doDelete(String endpoint) throws RestException {
		return doDelete(endpoint,null); 
	}
	
	public RestResponse doDelete(String endpoint, HeaderEntity header) throws RestException 
	{
		CloseableHttpResponse response = null;

		try {
			HttpDelete httpDelete = new HttpDelete(endpoint);
			
			if ( header != null ) 
				for ( Header hd : header.getHeaders() ) 
					httpDelete.addHeader(hd);
			
			long before = System.currentTimeMillis();
			
			response = getClient().execute(httpDelete);
			
			long after = System.currentTimeMillis();
			
			int statusCode = response.getStatusLine().getStatusCode();
			
			String body = ( statusCode != HttpStatus.SC_NO_CONTENT ) ? EntityUtils.toString(response.getEntity()) : null;
			
			if ( statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_ACCEPTED && statusCode != HttpStatus.SC_NO_CONTENT ) {
				logger.warn(String.format(RestExceptionMessage.STATUS_NOK, response.getStatusLine().getStatusCode()));
			}
			
			return RestResponse.getPrototype(body, statusCode, response.getAllHeaders(), after - before);

		} catch (Exception e) {
			throw new RestException(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.error(String.format(RestExceptionMessage.ERROR_CLOSING_CONNECTION, e.getMessage()));
				throw new RestException(e);
			}
		}
	}
	
	//-----------------------------------------------------------------------------------
	
	public RestResponse doGet(String endpoint, HeaderEntity header, Boolean ignoreCookies) throws RestException 
	{
		CloseableHttpResponse response = null;

		try {
			HttpGet httpGet = new HttpGet(endpoint);
			
			if ( ignoreCookies ) {
				RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();			
				httpGet.setConfig(requestConfig);
			}
			
			if ( header != null ) 
				for ( Header hd : header.getHeaders() ) 
					httpGet.addHeader(hd);
			
			long before = System.currentTimeMillis();
			
			response = getClient().execute(httpGet);
			
			long after = System.currentTimeMillis();
			
			int statusCode = response.getStatusLine().getStatusCode();
			
			String body = ( statusCode != HttpStatus.SC_NO_CONTENT ) ? EntityUtils.toString(response.getEntity()) : null;

			if ( statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_ACCEPTED && statusCode != HttpStatus.SC_NO_CONTENT ) {
				logger.warn(String.format(RestExceptionMessage.STATUS_NOK, response.getStatusLine().getStatusCode()));
			}
			
			return RestResponse.getPrototype(body, statusCode, response.getAllHeaders(), after - before);

		} catch (Exception e) {
			throw new RestException(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.error(String.format(RestExceptionMessage.ERROR_CLOSING_CONNECTION, e.getMessage()));
				throw new RestException(e);
			}
		}
	}
	
	private RestResponse doMethod(HttpEntityEnclosingRequestBase method, String endpoint, BodyEntity bodyEntity, HeaderEntity header, Boolean ignoreCookies) throws RestException
	{
		CloseableHttpResponse response = null;

		try {

			if ( ignoreCookies ) {
				RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();			
				method.setConfig(requestConfig);
			}
			
			if ( header != null )
				for ( Header hd : header.getHeaders() ) 
					method.addHeader(hd);
			
			if ( bodyEntity != null ) 
			{
				switch ( bodyEntity.getEntityType() )
				{
					case FORM_ENTITY:
						method.setEntity(new UrlEncodedFormEntity(((FormEntity)bodyEntity).getParams()));
						break;
					case RAW_ENTITY:
						method.setEntity(new StringEntity(((RawEntity)bodyEntity).getValue(),((RawEntity)bodyEntity).getContentType()));
						break;
					default:
						break;				
				}				
			}
			
			long before = System.currentTimeMillis();

			response = getClient().execute(method);
			
			long after = System.currentTimeMillis();
			
			int statusCode = response.getStatusLine().getStatusCode();
			
			String body = ( statusCode != HttpStatus.SC_NO_CONTENT ) ? EntityUtils.toString(response.getEntity()) : null;

			if ( statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_ACCEPTED && statusCode != HttpStatus.SC_NO_CONTENT ) {
				logger.warn(String.format(RestExceptionMessage.STATUS_NOK, response.getStatusLine().getStatusCode()));
			}
			
			return RestResponse.getPrototype(body, statusCode, response.getAllHeaders(), after - before);

		} catch (Exception e) {
			throw new RestException(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.error(String.format(RestExceptionMessage.ERROR_CLOSING_CONNECTION, e.getMessage()));
				throw new RestException(e);
			}
		}
	}
	
	public static RestResponse doSimpleGet(String endpoint) throws RestException 
	{
		CloseableHttpResponse response = null;

		try {
			HttpGet httpGet = new HttpGet(endpoint);
			
			long before = System.currentTimeMillis();
			
			response = HttpClients.createMinimal().execute(httpGet);
			
			long after = System.currentTimeMillis();
			
			String body = EntityUtils.toString(response.getEntity());

			int statusCode = response.getStatusLine().getStatusCode();

			return RestResponse.getPrototype(body, statusCode, response.getAllHeaders(), after - before);

		} catch (Exception e) {
			throw new RestException(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {			
				throw new RestException(e);
			}
		}
	}
	
	public static RestResponse doSimplePost(String endpoint, BodyEntity bodyEntity) throws RestException 
	{
		CloseableHttpResponse response = null;

		try {
			HttpPost httpPost = new HttpPost(endpoint);
			
			if ( bodyEntity != null ) 
			{
				switch ( bodyEntity.getEntityType() )
				{
					case FORM_ENTITY:
						httpPost.setEntity(new UrlEncodedFormEntity(((FormEntity)bodyEntity).getParams()));
						break;
					case RAW_ENTITY:
						httpPost.setEntity(new StringEntity(((RawEntity)bodyEntity).getValue(),((RawEntity)bodyEntity).getContentType()));
						break;
					default:
						break;				
				}				
			}
			
			long before = System.currentTimeMillis();

			response = HttpClients.createMinimal().execute(httpPost);
			
			long after = System.currentTimeMillis();
			
			String body = EntityUtils.toString(response.getEntity());
			
			int statusCode = response.getStatusLine().getStatusCode();
			
			return RestResponse.getPrototype(body, statusCode, response.getAllHeaders(), after - before);

		} catch (Exception e) {
			throw new RestException(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				throw new RestException(e);
			}
		}
	}
}
