package com.imusica.restProvider.entity;

import org.apache.http.message.BasicNameValuePair;

public class FormEntity extends DataEntity<BasicNameValuePair> implements BodyEntity
{		
	private FormEntity() {
		super();
	}
	
	public static FormEntity getPrototype() {
		return new FormEntity();
	}

	@Override
	public void add(String key, Object value) {
		if (value == null) value = new String("");
		params.add(new BasicNameValuePair(key, value.toString()));
	}
	
	@Override
	public String toString() 
	{
		StringBuilder result = new StringBuilder();
		
		for ( BasicNameValuePair pairValue: params )
		{
			String reg = "<" + pairValue.getName() + "=" + pairValue.getValue() + ">";
			result.append(reg);
		}
		
		return result.toString();
	}
	
	@Override
	public BodyEntityType getEntityType() {
		return BodyEntityType.FORM_ENTITY;
	}
}
