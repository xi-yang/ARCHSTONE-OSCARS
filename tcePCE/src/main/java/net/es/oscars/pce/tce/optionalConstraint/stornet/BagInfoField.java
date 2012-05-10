package net.es.oscars.pce.tce.optionalConstraint.stornet;

import java.util.ArrayList;
import java.util.List;

public class BagInfoField {

	protected String id;
	protected List<BagSegmentField> bagSegment;
	
	public List<BagSegmentField> getBagSegment(){
		if(bagSegment==null){
			bagSegment = new ArrayList<BagSegmentField>();
		}
		return this.bagSegment;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String value){
		this.id = value;
	}
	

}
