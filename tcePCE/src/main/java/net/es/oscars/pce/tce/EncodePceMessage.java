package net.es.oscars.pce.tce;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.utils.soap.OSCARSServiceException;

import org.ogf.schema.network.topology.ctrlplane.*;

import net.es.oscars.pce.tce.optionalConstraint.stornet.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import java.util.*;
import java.text.*;

public class EncodePceMessage {	
	
	byte[] pceEncodeArray;
		
	public EncodePceMessage(){
		this.pceEncodeArray = null;		
	}
	
	
	public void encodeUserConstraint (String gri, UserRequestConstraintType userCons)throws OSCARSServiceException
	{		
		byte[] encodeBuffHead;
		byte[] encodeBuffBody;
		byte[] encodeBuff;
		byte pceType = CodeNumber.PCE_USERCONSTRAINT;
		PrimitiveEncoder priEncoder = new PrimitiveEncoder();
		int buffLen;
	    long startTime = userCons.getStartTime();
	    long endTime = userCons.getEndTime();
	    long bandwidth = (userCons.getBandwidth()) * 1000000L; //convert from MB to Bytes
	    PathInfo pathInf = userCons.getPathInfo();
	    String pathSetupMode = pathInf.getPathSetupMode();
	    String pathType = pathInf.getPathType();
	    
	    if((pathInf.getLayer2Info()!=null) && (pathInf.getLayer3Info()!=null)){
	    	throw new OSCARSServiceException("The request has both layer2 and layer3 information");
	    }
	    
	    priEncoder.encodeString(CodeNumber.PCE_GRI, gri);
	    priEncoder.encodeLong(CodeNumber.PCE_STARTTIME, startTime);
	    priEncoder.encodeLong(CodeNumber.PCE_ENDTIME, endTime);
	    priEncoder.encodeLong(CodeNumber.PCE_BANDWIDTH, bandwidth);	    
	    
	    if(pathInf.getLayer2Info()!=null){
	    	Layer2Info layer2Info = pathInf.getLayer2Info();
	    	VlanTag srcVtag = layer2Info.getSrcVtag();
	    	VlanTag destVtag = layer2Info.getDestVtag();
	    	String srcEndPoint = layer2Info.getSrcEndpoint();
	    	String destEndPoint = layer2Info.getDestEndpoint();
	    	String srcVtagValue = srcVtag.getValue();
	    	String destVtagValue = destVtag.getValue();

	    	priEncoder.encodeString(CodeNumber.PCE_LAYER, "2");
	    	priEncoder.encodeString(CodeNumber.PCE_SOURCE, srcEndPoint);
	    	priEncoder.encodeString(CodeNumber.PCE_DESTINATION, destEndPoint);
	    	priEncoder.encodeString(CodeNumber.PCE_SRCVLAN, srcVtagValue);
	    	priEncoder.encodeString(CodeNumber.PCE_DESTVLAN, destVtagValue);
	    }	    
	    
	    if(pathInf.getLayer3Info() != null){
	    	Layer3Info layer3Info = pathInf.getLayer3Info();
	    	String srcHost = layer3Info.getSrcHost();
	    	String destHost = layer3Info.getDestHost();
	    	String protocol = layer3Info.getProtocol();
	    	String dscp = layer3Info.getDscp();
	    	int srcIpPort = layer3Info.getSrcIpPort();
	    	int destIpPort = layer3Info.getDestIpPort();
	    	
	    	priEncoder.encodeString(CodeNumber.PCE_LAYER, "3");
	    	priEncoder.encodeString(CodeNumber.PCE_SOURCE, srcHost);
	    	priEncoder.encodeString(CodeNumber.PCE_DESTINATION, destHost);
	    	priEncoder.encodeInteger(CodeNumber.PCE_SRCIPPORT, srcIpPort);
	    	priEncoder.encodeInteger(CodeNumber.PCE_DESTIPPORT, destIpPort);
	    	priEncoder.encodeString(CodeNumber.PCE_L3_PROTOCOL, protocol);
	    	priEncoder.encodeString(CodeNumber.PCE_L3_DSCP, dscp);	    	
	    }
	    
	    priEncoder.encodeString(CodeNumber.PCE_PATHSETUPMODEL, pathSetupMode);
	    priEncoder.encodeString(CodeNumber.PCE_PATHTYPE, pathType);
	    
	    if(pathInf.getMplsInfo() != null){
	    	MplsInfo mplsInfo = pathInf.getMplsInfo();
	    	int burstLimit = mplsInfo.getBurstLimit();
	    	String lspClass = mplsInfo.getLspClass();
	    	
	    	priEncoder.encodeInteger(CodeNumber.PCE_MPLS_BURSTLIMIT, burstLimit);
	    	priEncoder.encodeString(CodeNumber.PCE_MPLS_LSPCLASS, lspClass);
	    }
	    
	    priEncoder.buffPrune();
	    encodeBuffBody = priEncoder.getBuff();
	    buffLen = encodeBuffBody.length;
	    
	    //System.out.println("usr="+buffLen);
	    encodeBuffHead = this.encodePceHeader(pceType, buffLen);
	    encodeBuff = this.mergeBuff(encodeBuffHead, encodeBuffBody);
	    
	    this.combineField(encodeBuff);	    
	    	   
	}
	
	public void encodeResvConstraint (ReservedConstraintType resvCons)throws OSCARSServiceException
	{		
		byte[] encodeBuffHead;
		byte[] encodeBuffBody;
		byte[] encodeBuff;
		byte pceType = CodeNumber.PCE_RESVCONSTRAINT;
		PrimitiveEncoder priEncoder = new PrimitiveEncoder();
		int buffLen;
	    long startTime = resvCons.getStartTime();
	    long endTime = resvCons.getEndTime();
	    long bandwidth = (resvCons.getBandwidth()) * 1000000L;
	    PathInfo pathInf = resvCons.getPathInfo();
	    
	    if((pathInf.getLayer2Info()!=null) && (pathInf.getLayer3Info()!=null)){
	    	throw new OSCARSServiceException("The request has both layer2 and layer3 information");
	    }

	    if(pathInf!=null){
	    	//System.out.println("path info not empty");
    	
	    	
	    	if(pathInf.getPath()!=null){
	    		
	    		//System.out.println("path not empty");

	    		CtrlPlanePathContent path = pathInf.getPath();
	    		if(path.getId()!=null){
	    			String pathId = path.getId();

	    			priEncoder.encodeString(CodeNumber.PCE_PATH_ID, pathId);
	    		}

	    		
	    		if(path.getHop()!=null){
	    			List<CtrlPlaneHopContent> hop = path.getHop();
	    			priEncoder.encodeInteger(CodeNumber.PCE_PATH_LENGTH, hop.size());
	    			//System.out.println("hop="+hop.size());
	    			
	    			CtrlPlaneHopContent oneHop;
	    			for(int i=0;i<hop.size();i++){
	    				 oneHop = hop.get(i);
	    				 if(oneHop.getId()!=null){
	    					 String hopId = oneHop.getId();
	    					 //System.out.println("hop_id="+hopId);

	    					 priEncoder.encodeString(CodeNumber.PCE_HOP_ID, hopId);
	    				 }
    					 

	    				 if(oneHop.getLink()!=null){
	    					 CtrlPlaneLinkContent link = oneHop.getLink();
	    					 if(link.getId()!=null){
	    						 String linkId = link.getId();
	    						 //System.out.println("link_id="+linkId);
	    						 
	    						 priEncoder.encodeString(CodeNumber.PCE_LINK_ID, linkId);
	    					 }
	    					
	    					 if(link.getSwitchingCapabilityDescriptors().size() > 0){
	    						 CtrlPlaneSwcapContent switchingCapabilityDescriptors = link.getSwitchingCapabilityDescriptors().get(0);
	    						 if(switchingCapabilityDescriptors.getSwitchingcapType()!=null){
	    							 String switchingcapType = switchingCapabilityDescriptors.getSwitchingcapType();
	    							 //System.out.println("switching_captype="+switchingcapType);
	    							 
	    							 priEncoder.encodeString(CodeNumber.PCE_SWITCHINGCAPTYPE, switchingcapType);
	    						 }
	    						 if(switchingCapabilityDescriptors.getEncodingType()!=null){
	    							 String encodingType = switchingCapabilityDescriptors.getEncodingType();
	    							 //System.out.println("switching_enctype="+encodingType);
	    							 
	    							 priEncoder.encodeString(CodeNumber.PCE_SWITCHINGENCTYPE, encodingType);
	    						 }
	    						 if(switchingCapabilityDescriptors.getSwitchingCapabilitySpecificInfo()!=null){
	    							 CtrlPlaneSwitchingCapabilitySpecificInfo switchingCapabilitySpecificInfo = switchingCapabilityDescriptors.getSwitchingCapabilitySpecificInfo();
	    							 
	    							 if(switchingCapabilitySpecificInfo.getVlanRangeAvailability()!=null){
	    								 String vlanRangeAvailability = switchingCapabilitySpecificInfo.getVlanRangeAvailability();
	    								 //System.out.println("switching_vlan_avai"+vlanRangeAvailability);
	    								 
	    								 priEncoder.encodeString(CodeNumber.PCE_SWITCHINGVLANRANGEAVAI, vlanRangeAvailability);
	    							 }
	    							 
	    							 if(switchingCapabilitySpecificInfo.getSuggestedVLANRange()!=null){
	    								 String suggestedVLANRange = switchingCapabilitySpecificInfo.getSuggestedVLANRange();
	    								 //System.out.println("switching_vlan_sugg"+suggestedVLANRange);
	    								 
	    								 priEncoder.encodeString(CodeNumber.PCE_SWITCHINGVLANRANGESUGG, suggestedVLANRange);
	    							 }
	    							 
	    							 
	    							 if(switchingCapabilitySpecificInfo.isVlanTranslation()!=null){
		    							 if(switchingCapabilitySpecificInfo.isVlanTranslation()==true){
		    								 //System.out.println("vlan trans true");
		    								 priEncoder.encodeBoolean(CodeNumber.PCE_VLANTRANSLATION, true);
		    							 }
		    							 else{
		    								 //System.out.println("vlan trans false");
		    								 priEncoder.encodeBoolean(CodeNumber.PCE_VLANTRANSLATION, false);	    								 
		    							 }	    								 
	    								 
	    							 }

	    						 }//end if switchingCapabilityDescriptors
	    					 }//end if link.getSwitchingCapabilityDescriptors
	    				 }//end if oneHop.getLink
	    				 priEncoder.encodeInteger(CodeNumber.PCE_HOP_END_TAG, 1); //dummy field for mark end of one hop
	    				 
	    			}//end for int i=0;
	    		}//end if path.getHop
	    		
	    	}//end if pathInf.getPath
	    	
	    }
	    
	    priEncoder.buffPrune();
	    encodeBuffBody = priEncoder.getBuff();
	    buffLen = encodeBuffBody.length;
	    
	    //System.out.println("res="+buffLen);
	    encodeBuffHead = this.encodePceHeader(pceType, buffLen);
	    encodeBuff = this.mergeBuff(encodeBuffHead, encodeBuffBody);
	    
	    this.combineField(encodeBuff);	    
	    	   
	}	
	
	public void encodeOptConstraint(OptionalConstraintType optCons)throws OSCARSServiceException{
		OptionalConstraintValue value = optCons.getValue();
		String category = optCons.getCategory();
		
		String stringValue = value.getStringValue();
		if(stringValue==null){
			throw new OSCARSServiceException("String Value in OptionalConstraintValue is null");
		}
		
		/* Old version of OptionalConstraintValue
		List<Object> any = value.getAny();
		
		if(any.size()==0){
			throw new OSCARSServiceException("List size is zero in OptionalConstraintValue");
		}
		
		if(any.size()>1){
			throw new OSCARSServiceException("Only support one string in OptionalConstraintValue list now");
		}
		*/
		//String optConsXml = (String)any.get(0);
		//Element optConsXml = (Element)any.get(0);
		
		ParseXml parseXmlTool = new ParseXml();
		
		//CoScheduleRequestField coScheduleRequest= parseXmlTool.readInput(optConsXml);
		CoScheduleRequestField coScheduleRequest= parseXmlTool.readInput(stringValue);
		
		this.encodeCoScheduleRequest(coScheduleRequest);
		
	}
	
	protected void encodeCoScheduleRequest(CoScheduleRequestField coSchedReq){
		byte[] encodeBuffHead;
		byte[] encodeBuffBody;
		byte[] encodeBuff;
		byte pceType = CodeNumber.PCE_OPTICONSTRAINT_COSCHEDULEREQ;
		PrimitiveEncoder priEncoder = new PrimitiveEncoder();
		int buffLen;
		
		String coScheduleRequestId = coSchedReq.getCoScheduleRequestId();
		long startTime = coSchedReq.getStartTime();
		long endTime = coSchedReq.getEndTime();
		long minBandwidth = coSchedReq.getMinBandwidth();
		int maxNumOfAltPaths = coSchedReq.getMaxNumOfAltPaths();
		boolean bandwidthAvailabilityGraph = coSchedReq.getBandwidthAvailabilityGraph(); 
		Boolean contiguousVlan = coSchedReq.getContiguousVlan();
		int maxDuration = coSchedReq.getMaxDuration();
		long maxBandwidth = coSchedReq.getMaxBandwidth();
		long dataSizeBytes = coSchedReq.getDataSizeBytes();
		
		priEncoder.encodeString(CodeNumber.PCE_OPT_COSCHEDREQID, coScheduleRequestId);
		
		priEncoder.encodeLong(CodeNumber.PCE_OPT_COSCHREQ_STARTTIME, startTime);
		priEncoder.encodeLong(CodeNumber.PCE_OPT_COSCHREQ_ENDTIME, endTime);
		
		priEncoder.encodeLong(CodeNumber.PCE_OPT_COSCHREQ_MINBANDWIDTH, minBandwidth);
		priEncoder.encodeInteger(CodeNumber.PCE_OPT_COSCHREQ_MAXNUMOFALTPATHS, maxNumOfAltPaths);
		
		priEncoder.encodeBoolean(CodeNumber.PCE_OPT_COSCHREQ_BANDWIDTHAVAIGRAPH, bandwidthAvailabilityGraph);
		
		if(contiguousVlan!=null){
			priEncoder.encodeBoolean(CodeNumber.PCE_OPT_COSCHREQ_CONTIGUOUSVLAN, contiguousVlan);
		}
		
		if(maxDuration>=0){
			priEncoder.encodeInteger(CodeNumber.PCE_OPT_COSCHREQ_MAXDURATION, maxDuration);
		}
		
		if(maxBandwidth>=0){
			priEncoder.encodeLong(CodeNumber.PCE_OPT_COSCHREQ_MAXBANDWIDTH, maxBandwidth);
		}
		
		if(dataSizeBytes>=0){
			priEncoder.encodeLong(CodeNumber.PCE_OPT_COSCHREQ_DATASIZEBYTES, dataSizeBytes);
		}
		
	    priEncoder.buffPrune();
	    encodeBuffBody = priEncoder.getBuff();
	    buffLen = encodeBuffBody.length;
	    
	    //System.out.println("opt="+buffLen);
	    encodeBuffHead = this.encodePceHeader(pceType, buffLen);
	    encodeBuff = this.mergeBuff(encodeBuffHead, encodeBuffBody);
	    
	    this.combineField(encodeBuff);

		
		
	}
	
	
	public byte[] encodePceHeader(byte pceType, int length){
		int size = 0;
		byte[] pceMessHeadBuff;
		int offset = 0;
		
		if(length<0x80){
			size = 1;
		}
		else if(length<=0xFF){
			size = 2;
		}
		else if(length<=0xFFFF){
			size = 3;
		}
		else if(length<=0xFFFFFF){
			size = 4;
		}
		else{
			
		}
		size = size + 1;
		pceMessHeadBuff = new byte[size];
		
		pceMessHeadBuff[offset++] = pceType;
		//pceMessHeadBuff[offset++] = priType;
		
		if(length<0x80){
			pceMessHeadBuff[offset++] = (byte) (length & 0xFF);			
		}
		else if(length<=0xFF){
			pceMessHeadBuff[offset++] = (0x01 | CodeNumber.ASN_LONG_LEN);
			pceMessHeadBuff[offset++] = (byte) (length & 0xFF);			
		}
		else if (length<=0xFFFF){
			pceMessHeadBuff[offset++] = (0x02 | CodeNumber.ASN_LONG_LEN);
			pceMessHeadBuff[offset++] = (byte) ((length>>8) & 0xFF);
			pceMessHeadBuff[offset++] = (byte) (length & 0xFF);
		}
		else if (length<=0xFFFFFF){
			pceMessHeadBuff[offset++] = (0x03 | CodeNumber.ASN_LONG_LEN);
			pceMessHeadBuff[offset++] = (byte) ((length>>16) & 0xFF);
			pceMessHeadBuff[offset++] = (byte) ((length>>8) & 0xFF);
			pceMessHeadBuff[offset++] = (byte) (length & 0xFF);
		}
		else{
			
		}
		
		return pceMessHeadBuff;
		
	}
	
	public byte[] mergeBuff(byte[] buff1, byte[] buff2){
		int length1 = buff1.length;
		int length2 = buff2.length;
		int lengthMerge = length1 + length2;
		byte[] mergeArray = new byte[lengthMerge];
		System.arraycopy(buff1, 0, mergeArray, 0, length1);
		System.arraycopy(buff2, 0, mergeArray, length1, length2);
		
		return mergeArray;
	}
	
	public void combineField(byte[] field){
		byte[] combArray;
		if(pceEncodeArray == null){
			combArray = field;
		}
		else{
			combArray = this.mergeBuff(pceEncodeArray, field);
		}
		pceEncodeArray = combArray;
	}
	
	public byte[] getPceEncodeArray(){
		return this.pceEncodeArray;
	}
	
	public void encodeTopology(CtrlPlaneTopologyContent topology, int pathSeq)throws OSCARSServiceException
	{
		byte[] encodeBuffHead;
		byte[] encodeBuffBody;
		byte[] encodeBuff;
		byte pceType = CodeNumber.PCE_MULTIPLE_PATH;
		PrimitiveEncoder priEncoder = new PrimitiveEncoder();
		int buffLen;
		List<Lifetime> topoLifetime = null;
		
		
		
		
		List<CtrlPlanePathContent> path = topology.getPath();
		
		if(path==null || path.size()==0){
			throw new OSCARSServiceException("No path in topology to encode");
		}
		
		CtrlPlanePathContent singlePath = path.get(pathSeq);
		
		topoLifetime = topology.getLifetime();
		
		this.encodePath(priEncoder, singlePath, topoLifetime);
		
		priEncoder.buffPrune();
	    encodeBuffBody = priEncoder.getBuff();
	    buffLen = encodeBuffBody.length;
	    
	    //System.out.println("opt="+buffLen);
	    encodeBuffHead = this.encodePceHeader(pceType, buffLen);
	    encodeBuff = this.mergeBuff(encodeBuffHead, encodeBuffBody);
	    
	    this.combineField(encodeBuff);
		
		
	}
	
	public void encodePath(PrimitiveEncoder priEncoder, CtrlPlanePathContent path, 
			List<Lifetime> topoLifetime)throws OSCARSServiceException
	{
		List<Lifetime> pathLifetime = path.getLifetime();
		int topoLifetimeNum = 0;
		int pathLifetimeNum = 0;

		if(path.getId()!=null){
			String pathId = path.getId();

			priEncoder.encodeString(CodeNumber.PCE_PATH_ID, pathId);
		}
		
		if(topoLifetime != null && topoLifetime.size()!=0 ){
			topoLifetimeNum = topoLifetime.size();			
			
		}
		
		if(pathLifetime != null && pathLifetime.size()!=0 ){
			pathLifetimeNum = pathLifetime.size();
			
		}
		
		if((topoLifetimeNum + pathLifetimeNum) != 0){
			priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_NUM, (topoLifetimeNum + pathLifetimeNum));
			
			Lifetime singleLifetime = null;
			String startTime = null;
			String endTime = null;
			String duration = null;
			
			for(int i=0;i<(topoLifetimeNum+pathLifetimeNum);i++){
				if(i<topoLifetimeNum){
					singleLifetime = topoLifetime.get(i);
				}else{
					singleLifetime = pathLifetime.get(i-topoLifetimeNum);
				}
				
				//priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME, 0); //dummy
				
				startTime = singleLifetime.getStart().getValue();
				if(startTime != null && startTime.length() != 0){
					try{
						int startTimeNum = Integer.parseInt(startTime);
						priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_START, startTimeNum);
					}catch(NumberFormatException nfe){
						try{
							DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss:SSS");
							Date date = formatter.parse(startTime);
							int startTimeNum = (int)(date.getTime()/1000L);
							priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_START, startTimeNum);
						}catch(ParseException e){
							throw new OSCARSServiceException("Lifetime start is not in correct format");
						}
					}					
					//priEncoder.encodeString(CodeNumber.PCE_LIFETIME_START, startTime);
				}else{
					priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_START, -1);
					//priEncoder.encodeString(CodeNumber.PCE_LIFETIME_START, "NO_VALUE");
				}
				
				endTime = singleLifetime.getEnd().getValue();
				if(endTime != null && endTime.length() !=  0){
					try{
						int endTimeNum = Integer.parseInt(endTime);
						priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_END, endTimeNum);
					}catch(NumberFormatException nfe){
						try{
							DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss:SSS");
							Date date = formatter.parse(endTime);
							int endTimeNum = (int)(date.getTime()/1000L);
							priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_END, endTimeNum);
						}catch(ParseException e){
							throw new OSCARSServiceException("Lifetime end is not in correct format");
						}
					}
					//priEncoder.encodeString(CodeNumber.PCE_LIFETIME_END, endTime);
				}else{
					priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_END, -1);
					//priEncoder.encodeString(CodeNumber.PCE_LIFETIME_END, "NO_VALUE");
				}
				
				duration = singleLifetime.getDuration().getValue();
				if(duration != null && duration.length() != 0){
					try{
						int durationNum = Integer.parseInt(duration);
						priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_DUR, durationNum);
					}catch(NumberFormatException nfe){				
						String[] durStr = duration.split(":");
						int hour=0, min=0, second=0;
						if(durStr.length==4){
							try{
								hour = Integer.parseInt(durStr[0]);
								min = Integer.parseInt(durStr[1]);
								second = Integer.parseInt(durStr[2]);
							}catch(NumberFormatException numfe){
								throw new OSCARSServiceException("Lifetime duration is not in correct format");
							}
						}else if(durStr.length==3){
							try{									
								min = Integer.parseInt(durStr[0]);
								second = Integer.parseInt(durStr[1]);
							}catch(NumberFormatException numfe){
								throw new OSCARSServiceException("Lifetime duration is not in correct format");
							}
						}else{
							throw new OSCARSServiceException("Lifetime duration is not in correct format");
						}
						int durationNum = hour*60*60 + min*60 + second;
						priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_DUR, durationNum);
					}
					//priEncoder.encodeString(CodeNumber.PCE_LIFETIME_DUR, duration);
				}else{
					priEncoder.encodeInteger(CodeNumber.PCE_LIFETIME_DUR, -1);
					//priEncoder.encodeString(CodeNumber.PCE_LIFETIME_DUR, "NO_VALUE");
				}
			}			
		}
		
		
		if(path.getHop()==null || path.getHop().size()==0){
			throw new OSCARSServiceException("Hop is empty");
		}
		
		CtrlPlaneHopContent firstHop = path.getHop().get(0);
		
		if(firstHop.getLink()==null){
			throw new OSCARSServiceException("Link in first hop is null");
		}	
		
		if(firstHop.getLink().getMaximumReservableCapacity()!=null && firstHop.getLink().getMaximumReservableCapacity().length()!=0){
			String maximumReservableCapacity = firstHop.getLink().getMaximumReservableCapacity();
			try{
				long maximumReservableCapacityNum = Long.parseLong(maximumReservableCapacity);
				priEncoder.encodeLong(CodeNumber.PCE_MAXRESVCAPACITY, maximumReservableCapacityNum);
			}catch(NumberFormatException nfe){
				throw new OSCARSServiceException("MaximumReservableCapacity end is not a number");
			}						
		}
		
		if(firstHop.getLink().getMinimumReservableCapacity()!=null && firstHop.getLink().getMinimumReservableCapacity().length()!=0){
			String minimumReservableCapacity = firstHop.getLink().getMinimumReservableCapacity();
			try{
				long minimumReservableCapacityNum = Long.parseLong(minimumReservableCapacity);
				priEncoder.encodeLong(CodeNumber.PCE_MINRESVCAPACITY, minimumReservableCapacityNum);
			}catch(NumberFormatException nfe){
				throw new OSCARSServiceException("MinimumReservableCapacity end is not a number");
			}						
		}
		
		if(firstHop.getLink().getGranularity()!=null && firstHop.getLink().getGranularity().length()!=0){
			String granularity = firstHop.getLink().getGranularity();
			try{
				long granularityNum = Long.parseLong(granularity);
				priEncoder.encodeLong(CodeNumber.PCE_GRANULARITY, granularityNum);
			}catch(NumberFormatException nfe){
				throw new OSCARSServiceException("Granularity end is not a number");
			}						
		}

		if(path.getHop()!=null){
			List<CtrlPlaneHopContent> hop = path.getHop();
			priEncoder.encodeInteger(CodeNumber.PCE_PATH_LENGTH, hop.size());
			//System.out.println("hop="+hop.size());

			CtrlPlaneHopContent oneHop;
			for(int i=0;i<hop.size();i++){
				oneHop = hop.get(i);
				if(oneHop.getId()!=null){
					String hopId = oneHop.getId();
					//System.out.println("hop_id="+hopId);

					priEncoder.encodeString(CodeNumber.PCE_HOP_ID, hopId);
				}


				if(oneHop.getLink()!=null){
					CtrlPlaneLinkContent link = oneHop.getLink();
					if(link.getId()!=null){
						String linkId = link.getId();
						//System.out.println("link_id="+linkId);

						priEncoder.encodeString(CodeNumber.PCE_LINK_ID, linkId);
					}
					
					/*
					if(link.getMaximumReservableCapacity()!=null && link.getMaximumReservableCapacity().length()!=0){
						String maximumReservableCapacity = link.getMaximumReservableCapacity();
						try{
							long maximumReservableCapacityNum = Long.parseLong(maximumReservableCapacity);
							priEncoder.encodeLong(CodeNumber.PCE_MAXRESVCAPACITY, maximumReservableCapacityNum);
						}catch(NumberFormatException nfe){
							throw new OSCARSServiceException("MaximumReservableCapacity end is not a number");
						}						
					}
					
					if(link.getMinimumReservableCapacity()!=null && link.getMinimumReservableCapacity().length()!=0){
						String minimumReservableCapacity = link.getMinimumReservableCapacity();
						try{
							long minimumReservableCapacityNum = Long.parseLong(minimumReservableCapacity);
							priEncoder.encodeLong(CodeNumber.PCE_MINRESVCAPACITY, minimumReservableCapacityNum);
						}catch(NumberFormatException nfe){
							throw new OSCARSServiceException("MinimumReservableCapacity end is not a number");
						}						
					}
					
					if(link.getGranularity()!=null && link.getGranularity().length()!=0){
						String granularity = link.getGranularity();
						try{
							long granularityNum = Long.parseLong(granularity);
							priEncoder.encodeLong(CodeNumber.PCE_GRANULARITY, granularityNum);
						}catch(NumberFormatException nfe){
							throw new OSCARSServiceException("Granularity end is not a number");
						}						
					}
					*/

					if(link.getSwitchingCapabilityDescriptors().size() > 0){
						CtrlPlaneSwcapContent switchingCapabilityDescriptors = link.getSwitchingCapabilityDescriptors().get(0);
						if(switchingCapabilityDescriptors.getSwitchingcapType()!=null){
							String switchingcapType = switchingCapabilityDescriptors.getSwitchingcapType();
							//System.out.println("switching_captype="+switchingcapType);

							priEncoder.encodeString(CodeNumber.PCE_SWITCHINGCAPTYPE, switchingcapType);
						}
						if(switchingCapabilityDescriptors.getEncodingType()!=null){
							String encodingType = switchingCapabilityDescriptors.getEncodingType();
							//System.out.println("switching_enctype="+encodingType);

							priEncoder.encodeString(CodeNumber.PCE_SWITCHINGENCTYPE, encodingType);
						}
						if(switchingCapabilityDescriptors.getSwitchingCapabilitySpecificInfo()!=null){
							CtrlPlaneSwitchingCapabilitySpecificInfo switchingCapabilitySpecificInfo = switchingCapabilityDescriptors.getSwitchingCapabilitySpecificInfo();

							if(switchingCapabilitySpecificInfo.getVlanRangeAvailability()!=null){
								String vlanRangeAvailability = switchingCapabilitySpecificInfo.getVlanRangeAvailability();
								//System.out.println("switching_vlan_avai"+vlanRangeAvailability);

								priEncoder.encodeString(CodeNumber.PCE_SWITCHINGVLANRANGEAVAI, vlanRangeAvailability);
							}

							if(switchingCapabilitySpecificInfo.getSuggestedVLANRange()!=null){
								String suggestedVLANRange = switchingCapabilitySpecificInfo.getSuggestedVLANRange();
								//System.out.println("switching_vlan_sugg"+suggestedVLANRange);

								priEncoder.encodeString(CodeNumber.PCE_SWITCHINGVLANRANGESUGG, suggestedVLANRange);
							}


							if(switchingCapabilitySpecificInfo.isVlanTranslation()!=null){
								if(switchingCapabilitySpecificInfo.isVlanTranslation()==true){
									//System.out.println("vlan trans true");
									priEncoder.encodeBoolean(CodeNumber.PCE_VLANTRANSLATION, true);
								}
								else{
									//System.out.println("vlan trans false");
									priEncoder.encodeBoolean(CodeNumber.PCE_VLANTRANSLATION, false);	    								 
								}	    								 

							}

						}//end if switchingCapabilityDescriptors
					}//end if link.getSwitchingCapabilityDescriptors
				}//end if oneHop.getLink

				List<CtrlPlaneNextHopContent> nextHop = oneHop.getNextHop();

				if(nextHop !=null && nextHop.size()!=0){
					priEncoder.encodeInteger(CodeNumber.PCE_NEXTHOP_LENGTH, nextHop.size());

					for(CtrlPlaneNextHopContent singleNextHop: nextHop){
						String nextHopValue = singleNextHop.getValue();						
						priEncoder.encodeString(CodeNumber.PCE_NEXTHOP, nextHopValue);						
					}	

				}
				
				priEncoder.encodeInteger(CodeNumber.PCE_HOP_END_TAG, 1); //dummy field for mark end of one hop

			}//end for int i=0;
		}//end if path.getHop
		
		
	}

}
