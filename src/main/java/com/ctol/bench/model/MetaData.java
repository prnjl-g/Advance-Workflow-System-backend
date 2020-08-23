package com.ctol.bench.model;

import java.util.List;
import java.util.Map;

public class MetaData {
	public Map<String, List<String>> map;

	public Map<String, List<String>> getMap() {
		return map;
	}

	public MetaData(Map<String, List<String>> map) {
		super();
		this.map = map;
	}
	
	public MetaData() {
	}

	public void setMap(Map<String, List<String>> map) {
		this.map = map;
	}
	
}
