package net.es.oscars.pce.tce;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGTopoBuilder;

import org.ogf.schema.network.topology.ctrlplane.*;

import net.es.oscars.pce.tce.optionalConstraint.stornet.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WriteResult {
	
	private PCEMessage query;
	
	public PCEMessage getQuery(){
		return this.query;
	}
	
	public WriteResult(PCEMessage query){
		this.query = query;
		
	}
	
	public void writeComputeRes(ArrayList<ReplyMessageContent> replyMessageList) throws OSCARSServiceException{
		PCEDataContent pceData = query.getPCEDataContent();
		String gri = query.getGri();		
		ReservedConstraintType resConType = pceData.getReservedConstraint();
		

		for(ReplyMessageContent replyMessage : replyMessageList){			
			if(!gri.equals(replyMessage.getGri())){
				throw new OSCARSServiceException("Gri returned by API message is not as same as request");
			}
			
			if(replyMessage.getErrorMessage()!=null){
				throw new OSCARSServiceException("Path computation fails with message: " + replyMessage.getErrorMessage());
			}			
		}	

		if(replyMessageList==null || replyMessageList.size()==0){
			throw new OSCARSServiceException("Reply message list is empty");
		}		

		if(resConType != null){
			this.writeResvConstraint(replyMessageList, resConType);
		}
		this.writeTopology(replyMessageList, pceData);
		this.writeMultipath(replyMessageList, pceData);
		
		for(int i=0;i<replyMessageList.size();i++){
			ReplyMessageContent replyMessage = replyMessageList.get(i);
			if(replyMessage.getCoSchedulePath().getAltPathContent().size()!=0){
				CoScheduleReplyField coScheduleReply = new CoScheduleReplyField();
				this.writeOptiConstraint(replyMessage, coScheduleReply);
				//new BuildXml().generateXml(coScheduleReply);					
				String resultXml = new BuildXml().generateXmlString(coScheduleReply);
				if(resultXml==null){
					throw new OSCARSServiceException("XML string returned is none");
				}
				/*old version OptionalConstraintValue
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			        dbf.setNamespaceAware(true);
			        DocumentBuilder db = null;
			        try {
			            db = dbf.newDocumentBuilder();
			        } catch (ParserConfigurationException e) {
			             e.printStackTrace();
			             System.exit(-1);
			        }
					Document xmlDoc = db.newDocument();
			        Element xmlElem = xmlDoc.createElementNS("##other", "xml");
			        xmlElem.setTextContent(resultXml);
				 */
				OptionalConstraintValue optValue = new OptionalConstraintValue();
				/*old version OptionalConstraintValue
			        optValue.getAny().add(xmlElem);
				 */
				optValue.setStringValue(resultXml);
				OptionalConstraintType optType = new OptionalConstraintType();
				optType.setValue(optValue);
				optType.setCategory("api-experiment-stornet");
				if(i==0){
					pceData.getOptionalConstraint().clear(); //delete optional constraint request
				}
				pceData.getOptionalConstraint().add(optType);
				//System.out.println("opt list size="+pceData.getOptionalConstraint().size());
			}
		
		}
		
	}
	
	protected void writeResvConstraint(ArrayList<ReplyMessageContent> replyMessageList, ReservedConstraintType resConType){
		ReplyMessageContent replyMessage = replyMessageList.get(0);
		ReplyPathContent path = replyMessage.getReplyPathContent();
		List<ReplyLinkContent> links = path.getReplyLinkContent();
		List<CtrlPlaneHopContent> hop = resConType.getPathInfo().getPath().getHop();
		
		int linkSize = links.size();
		

		while(hop.size() !=0){			
			hop.remove(0);
		}
		
		for(int i=0;i<linkSize;i++){
			CtrlPlaneHopContent hopContent = new CtrlPlaneHopContent();
			ReplyLinkContent linkRes = links.get(i);
			writeHop(linkRes, hopContent);			
			hop.add(hopContent);
		}		
	}
	
	protected void writeTopology(ArrayList<ReplyMessageContent> replyMessageList, PCEDataContent pceData) throws OSCARSServiceException{
		ReplyMessageContent replyMessage = replyMessageList.get(0);
		ReplyPathContent path = replyMessage.getReplyPathContent();
		List<ReplyLinkContent> links = path.getReplyLinkContent();
		
		NMWGTopoBuilder topoBuilder = new NMWGTopoBuilder();
				
		int linkSize = links.size();
		
		for(int i=0;i<linkSize;i++){			
			CtrlPlaneLinkContent linkOri = new CtrlPlaneLinkContent();		
			ReplyLinkContent linkRes = links.get(i);			
			writeLink(linkRes, linkOri);
			topoBuilder.addLink(linkOri);
		}
		
		pceData.setTopology(topoBuilder.getTopology());
		
		//Do not need domain now
		while(pceData.getTopology().getDomain().size()!=0){
			CtrlPlaneDomainContent domain = pceData.getTopology().getDomain().remove(0);
		}
	}
	
	
	protected void writeMultipath(ArrayList<ReplyMessageContent> replyMessageList, PCEDataContent pceData){
		CtrlPlaneTopologyContent topology = pceData.getTopology();
		List<CtrlPlanePathContent> pathList = topology.getPath();
		
		while(pathList.size()!=0){
			pathList.remove(0);
		}
		
		for(ReplyMessageContent replyMessage : replyMessageList){			
			writePath(replyMessage,pathList);			
		}
		
		if(pceData.getUserRequestConstraint()!=null){
			if(pceData.getUserRequestConstraint().getPathInfo()!=null){
				pceData.getUserRequestConstraint().getPathInfo().setPathType("ServiceTopology");				
			}
		}
		
	}
	
	protected void writePath(ReplyMessageContent replyMessage, List<CtrlPlanePathContent> pathList){
		ReplyPathContent path = replyMessage.getReplyPathContent();
		
		CtrlPlanePathContent oriPath = new CtrlPlanePathContent();
		
		writeSinglePath(path, oriPath);
		pathList.add(oriPath);
		
		//add flex paths
		if(replyMessage.getCoSchedulePath()!=null){
			for(int i=0;i<replyMessage.getCoSchedulePath().getFlexPathContent().size();i++){
				path = replyMessage.getCoSchedulePath().getFlexPathContent().get(i);
				oriPath = new CtrlPlanePathContent();
				writeSinglePath(path, oriPath);
				pathList.add(oriPath);
			}
		}	
		
	}
	
	protected void writeSinglePath(ReplyPathContent path, CtrlPlanePathContent oriPath){
		List<Lifetime> lifetime = path.getLifetime();
		List<Lifetime> oriLifetime = oriPath.getLifetime();
						
		if(lifetime != null || lifetime.size()!=0){
			writeLifetime(lifetime, oriLifetime);
		}
		
		List<ReplyLinkContent> links = path.getReplyLinkContent();
		
		List<CtrlPlaneHopContent> hop = oriPath.getHop();
		
		for(ReplyLinkContent link: links){
			CtrlPlaneHopContent singleHop = new CtrlPlaneHopContent();
			writeHop(link, singleHop);
			hop.add(singleHop);
		}	
		
	}
	
	protected void writeLifetime(List<Lifetime> source, List<Lifetime> target){
		//copy reference, not copy field value here
		for(Lifetime lifetime: source){
			target.add(lifetime);
		}		
	}
	
	protected void writeHop(ReplyLinkContent linkRes, CtrlPlaneHopContent hopContent){		
		CtrlPlaneLinkContent linkOri = new CtrlPlaneLinkContent();	
		
		String hopId = UUID.randomUUID().toString();
		hopContent.setId(hopId);

		writeLink(linkRes, linkOri);
		hopContent.setLink(linkOri);				
	}
	
	protected void writeLink(ReplyLinkContent linkRes, CtrlPlaneLinkContent linkOri){
		String linkId = linkRes.getName();
		linkOri.setId(linkId);
		
		String remoteLinkId = linkRes.getRemoteLinkId();
		if(remoteLinkId != null){
			linkOri.setRemoteLinkId(remoteLinkId);
		}else{
			linkOri.setRemoteLinkId("urn:ogf:network:domain=*:node=*:port=*:link=*");
		}
		long maximumReservableCapacity = linkRes.getMaximumReservableCapacity();  
		linkOri.setMaximumReservableCapacity(Long.toString(maximumReservableCapacity));
		long minimumReservableCapacity = linkRes.getMinimumReservableCapacity();
		linkOri.setMinimumReservableCapacity(Long.toString(minimumReservableCapacity));
		long granularity = linkRes.getGranularity();
		linkOri.setGranularity(Long.toString(granularity));
		int trafficEngineeringMetric = linkRes.getTrafficEngineeringMetric();
		linkOri.setTrafficEngineeringMetric(Integer.toString(trafficEngineeringMetric));
		//String switchingcapType = linkRes.getSwitchingType();
		//switchingCapabilityDescriptors.setSwitchingcapType(switchingcapType);
		//String encodingType = linkRes.getEncodingType();
		//switchingCapabilityDescriptors.setEncodingType(encodingType);
		long capability = linkRes.getCapacity();						
		linkOri.setCapacity(Long.toString(capability));
		
		List<CtrlPlaneSwcapContent> switchingCapabilityDescriptors = linkOri.getSwitchingCapabilityDescriptors();
		while(switchingCapabilityDescriptors.size() != 0){
			switchingCapabilityDescriptors.remove(0);
		}
		
		List<CtrlPlaneSwcapContent> switCapDescriptors = linkRes.getSwitchingCapabilityDescriptors();
		
		for(int switListIndex=0;switListIndex<switCapDescriptors.size();switListIndex++){
			switchingCapabilityDescriptors.add(switCapDescriptors.get(switListIndex));
			if((switCapDescriptors.get(switListIndex)).getSwitchingCapabilitySpecificInfo().getVendorSpecificInfo()!=null){
				System.out.println("vendor in write find"+switListIndex);
			}
			
		}
		
		List<CtrlPlaneAdcapContent> adjustmentCapabilityDescriptor = linkOri.getAdjustmentCapabilityDescriptor();
		while(adjustmentCapabilityDescriptor.size() != 0){
			adjustmentCapabilityDescriptor.remove(0);
		}
		
		List<CtrlPlaneAdcapContent> adjustCapDescriptor = linkRes.getAdjustmentCapabilityDescriptor();
		
		for(int adjListIndex=0;adjListIndex<adjustCapDescriptor.size();adjListIndex++){
			adjustmentCapabilityDescriptor.add(adjustCapDescriptor.get(adjListIndex));
		}
		
		/*
		int interfaceMTU = linkRes.getMtu();
		switchingCapabilitySpecificInfo.setInterfaceMTU(interfaceMTU);
		String vlanRangeAvailability = linkRes.getAvailableVlanTags();
		switchingCapabilitySpecificInfo.setVlanRangeAvailability(vlanRangeAvailability);
		String suggestedVLANRange = linkRes.getSuggestedVlanTags();
		switchingCapabilitySpecificInfo.setSuggestedVLANRange(suggestedVLANRange);
		boolean vlanTranslation = linkRes.getVlanTranslation();
		switchingCapabilitySpecificInfo.setVlanTranslation(vlanTranslation);
		
		switchingCapabilityDescriptors.setSwitchingCapabilitySpecificInfo(switchingCapabilitySpecificInfo);
		//linkOri.setSwitchingCapabilityDescriptors(switchingCapabilityDescriptors);
		if (linkOri.getSwitchingCapabilityDescriptors().size() == 0) {
			linkOri.getSwitchingCapabilityDescriptors().add(switchingCapabilityDescriptors);
		} else {
			linkOri.getSwitchingCapabilityDescriptors().get(0).setSwitchingCapabilitySpecificInfo(switchingCapabilitySpecificInfo);
		}
		*/		
	}
	
	protected void writeOptiConstraint(ReplyMessageContent replyMessage, CoScheduleReplyField coScheduleReply){
		ReplyCoSchedulePathContent coSchedulePathReply = replyMessage.getCoSchedulePath();
		ReplyPathContent pathReply = coSchedulePathReply.getPath();		
		
		List<CoSchedulePathField> coSchedulePathResult = coScheduleReply.getCoSchedulePath();
		
		CoSchedulePathField coSchedulePathField = new CoSchedulePathField();
		
		writePath(pathReply, coSchedulePathField);
		
		coSchedulePathResult.add(coSchedulePathField);
		
		List<ReplyPathContent> altPaths = coSchedulePathReply.getAltPathContent();
	
		for(int i=0;i<altPaths.size();i++){
			pathReply = altPaths.get(i);
			coSchedulePathField = new CoSchedulePathField();
			writePath(pathReply, coSchedulePathField);
			coSchedulePathResult.add(coSchedulePathField);			
		}		
		
	}
	
	protected void writePath(ReplyPathContent pathReply, CoSchedulePathField coSchedulePathField){
		String CoSchedulePathId = UUID.randomUUID().toString();
		
		coSchedulePathField.setId(CoSchedulePathId);
		
		PathInfoField pathInfo = null;		
		
		
		List<ReplyLinkContent> linksReply = pathReply.getReplyLinkContent();
		List<ReplyBagInfoContent> bagInfo = pathReply.getReplyBagInfoContent();
		//List<ReplyBagSegmentContent> bagsReply = pathReply.getReplyBagSegmentContent();
		
		if(linksReply.size()!=0){
			pathInfo = new PathInfoField();
			writeLink(linksReply, pathInfo);
		}
		
		List<BagInfoField> bagInfoField = coSchedulePathField.getBagInfoField();
		
		for(int i=0;i<bagInfo.size();i++){
			ReplyBagInfoContent singleBagInfo = bagInfo.get(i);
			List<ReplyBagSegmentContent> bagSeg = singleBagInfo.getReplyBagSegmentContent();
			String bagId = singleBagInfo.getId();
			
			BagInfoField singleBagInfoField = new BagInfoField();
			
			writeBag(bagSeg, bagId, singleBagInfoField);
			
			bagInfoField.add(singleBagInfoField);			
		}		
		
		coSchedulePathField.setPathInfoField(pathInfo);			
		
	}
	
	protected void writeLink(List<ReplyLinkContent> linksReply, PathInfoField pathInfo){
		String pathId = UUID.randomUUID().toString();
		
		pathInfo.setPathId(pathId);
		
		ReplyLinkContent linkContentReply = null;
		
		List<HopField> hop = pathInfo.getHop();
		
		for(int i=0;i<linksReply.size();i++){
			linkContentReply = linksReply.get(i);
			HopField hopField = new HopField();
			String hopId = new String(UUID.randomUUID().toString());
			hopField.setHopId(hopId);
			LinkField linkResult = new LinkField();			
			writeLinkDetail(linkContentReply, linkResult);
			hopField.setLink(linkResult);
			
			hop.add(hopField);			
		}		
		
	}
	
	protected void writeBag(List<ReplyBagSegmentContent> bagsReply, String bagId, BagInfoField bagInfoField){
		
		bagInfoField.setId(bagId);
		
		List<BagSegmentField> bagSegment = bagInfoField.getBagSegment();
		
		ReplyBagSegmentContent bagSegmentContentReply = null;
		
		for(int i=0;i<bagsReply.size();i++){
			bagSegmentContentReply = bagsReply.get(i);
			BagSegmentField bagSegmentField = new BagSegmentField();
			writeBagDetail(bagSegmentContentReply, bagSegmentField);
			
			bagSegment.add(bagSegmentField);
		}
	}
	
	protected void writeLinkDetail(ReplyLinkContent linkContentReply, LinkField linkResult){
		String id;
		String name;
		String remoteLinkId;
		String switchingType;
		String encodingType;
		String assignedVlanTags;
		String availableVlanTags;
		String suggestedVlanTags;
		int mtu;
		boolean vlanTranslation;
		int capacity;
		int maximumReservableCapacity;
		int minimumReservableCapacity;
		int granularity;
		int trafficEngineeringMetric;
		
		name = linkContentReply.getName();
		linkResult.setLinkId(name);
		
		switchingType = linkContentReply.getSwitchingType();
		linkResult.setSwitchingcapType(switchingType);
		
		encodingType = linkContentReply.getEncodingType();
		linkResult.setEncodingType(encodingType);
		
		availableVlanTags = linkContentReply.getAvailableVlanTags();
		linkResult.setVlanRangeAvailability(availableVlanTags);
		
		
	}
	
	protected void writeBagDetail(ReplyBagSegmentContent bagSegmentContentReply, BagSegmentField bagSegmentField){
		long bandwidth;
		int startTime;
		int endTime;
		bandwidth = bagSegmentContentReply.getBandwidth();
		startTime = bagSegmentContentReply.getStartTime();
		endTime = bagSegmentContentReply.getEndTime();
		bagSegmentField.setSegmentBandwidth(bandwidth);
		bagSegmentField.setSegmentStartTime(startTime);
		bagSegmentField.setSegmentEndTime(endTime);		
	}

}
