package com.imusica.restProvider.entity;

import org.apache.http.entity.ContentType;

public class RawEntity implements BodyEntity
{
	private String value;
	
	private ContentType contentType;
	
	public RawEntity() {
		setContentType(ContentType.APPLICATION_JSON);
	}
	
	public static RawEntity getPrototype() {
		return new RawEntity();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public BodyEntityType getEntityType() {
		return BodyEntityType.RAW_ENTITY;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
}
