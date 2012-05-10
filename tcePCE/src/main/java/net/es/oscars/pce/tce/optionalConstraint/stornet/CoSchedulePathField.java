package net.es.oscars.pce.tce.optionalConstraint.stornet;

import java.util.ArrayList;
import java.util.List;

public class CoSchedulePathField {
	
	protected String id;
	protected PathInfoField pathInfo;
	protected List<BagInfoField> bagInfo;
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String value){
		this.id = value;
	}
	
	public PathInfoField getPathInfoField(){
		return this.pathInfo;
	}
	
	public void setPathInfoField(PathInfoField value){
		this.pathInfo = value;
	}
	/*
	public BagInfoField getBagInfoField(){
		return this.bagInfo;
	}
	
	public void setBagInfoField(BagInfoField value){
		this.bagInfo = value;
	}
	*/
	
	public List<BagInfoField> getBagInfoField(){
		if(this.bagInfo == null){
			this.bagInfo = new ArrayList<BagInfoField>();
		}
		return this.bagInfo;		
	}

}
