package net.es.oscars.pce.tce;

import java.util.ArrayList;
import java.util.List;

public class ReplyCoSchedulePathContent {
	
	protected List<ReplyPathContent> altPaths;
	protected ReplyPathContent path;
	protected List<ReplyPathContent> flexPaths;
	
	public List<ReplyPathContent> getAltPathContent(){
		if(altPaths==null){
			altPaths = new ArrayList<ReplyPathContent>();
		}
		return this.altPaths;
	}
	
	public List<ReplyPathContent> getFlexPathContent(){
		if(flexPaths==null){
			flexPaths = new ArrayList<ReplyPathContent>();
		}
		return this.flexPaths;
	}
	
	public ReplyPathContent getPath(){
		return this.path;
	}
	
	public void setPath(ReplyPathContent value){
		this.path = value;
	}

}
