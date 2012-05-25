package net.es.oscars.pce.tce;

import java.util.ArrayList;
import java.util.List;



public class ReplyBagInfoContent {	
	
	protected String id;
	protected String type;
	protected List<ReplyBagSegmentContent> bagSeg;
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String value){
		this.id = value;
	}
	
	public List<ReplyBagSegmentContent> getReplyBagSegmentContent(){
		if(bagSeg==null){
			bagSeg = new ArrayList<ReplyBagSegmentContent>();
		}
		return this.bagSeg;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setType(String value){
		this.type = value;
	}
}
