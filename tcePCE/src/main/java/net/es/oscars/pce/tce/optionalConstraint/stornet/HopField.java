package net.es.oscars.pce.tce.optionalConstraint.stornet;

public class HopField {
	
	protected String hopId;
	protected LinkField link;
	protected String nextHop;
	
	public String getHopId(){
		return this.hopId;
	}
	
	public void setHopId(String value){
		this.hopId = value;
	}
	
	public LinkField getLink(){
		return this.link;
	}
	
	public void setLink(LinkField value){
		this.link = value;
	}
	
	public String getNextHop(){
		return this.nextHop;
	}
	
	public void setNextHop(String value){
		this.nextHop = value;
	}

}
