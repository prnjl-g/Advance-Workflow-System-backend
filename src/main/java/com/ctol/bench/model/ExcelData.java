package com.ctol.bench.model;

import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class ExcelData {
    @Id
    public ObjectId _id;    
    public Map<String,String> details;
    
	public ExcelData(ObjectId _id, Map<String,String> details) {		
		this._id = _id;
		this.details = details;
	}
	
	public ExcelData() {				
	}
		
	public String get_id() { return _id.toHexString(); }
		  
    public void set_id(ObjectId _id) { this._id = _id; }
	
	public Map<String,String> getDetails() {
		return details;
	}
	public void setDetails(Map<String,String> details) {
		this.details = details;
	}
}