package com.imusica.restProvider.entity;

import java.util.ArrayList;
import java.util.List;

public abstract class DataEntity<T>
{
	protected List<T> params;
	
	public DataEntity() {
		params = new ArrayList<T>();
	}
	
	public List<T> getParams() {
		return params;
	}
	
	public abstract void add(String key, Object value);
}
