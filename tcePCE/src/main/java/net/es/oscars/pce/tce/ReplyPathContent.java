package net.es.oscars.pce.tce;

import java.util.ArrayList;
import java.util.List;

import org.ogf.schema.network.topology.ctrlplane.Lifetime;

public class ReplyPathContent {
	
	protected List<ReplyLinkContent> links;
	protected String id;
	protected List<ReplyBagInfoContent> bagInfo;
	protected List<Lifetime> lifetime;
	
	public List<ReplyLinkContent> getReplyLinkContent(){
		if(links==null){
			links = new ArrayList<ReplyLinkContent>();
		}
		return this.links;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String value){
		this.id = value;
	}
	
	public List<ReplyBagInfoContent> getReplyBagInfoContent(){
		if(bagInfo==null){
			bagInfo = new ArrayList<ReplyBagInfoContent>();
		}
		return this.bagInfo;
	}
	
	public List<Lifetime> getLifetime() {
        if (lifetime == null) {
            lifetime = new ArrayList<Lifetime>();
        }
        return this.lifetime;
    }

}
