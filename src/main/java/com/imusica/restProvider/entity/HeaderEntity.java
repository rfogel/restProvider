package com.imusica.restProvider.entity;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;

public class HeaderEntity extends DataEntity<Header>
{	
	private HeaderEntity() {
		super();
	}
	
	public static HeaderEntity getPrototype() {
		return new HeaderEntity();
	}

	@Override
	public void add(String key, Object value) {
		params.add(new RestHeader(key, value.toString()));
	}
	
	public List<Header> getHeaders() {
		return params;
	}
	
	private final class RestHeader implements Header
	{
		private String name;
		private String value;
		
		public RestHeader(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public HeaderElement[] getElements() throws ParseException {
			return null;
		}		
	}
}
