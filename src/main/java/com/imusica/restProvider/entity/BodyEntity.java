package com.imusica.restProvider.entity;

public interface BodyEntity
{
	BodyEntityType getEntityType();
	
	public enum BodyEntityType
	{
		RAW_ENTITY,
		FORM_ENTITY;
	}
}
