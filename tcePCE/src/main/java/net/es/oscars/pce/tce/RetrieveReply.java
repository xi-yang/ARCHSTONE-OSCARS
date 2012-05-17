package net.es.oscars.pce.tce;

import java.util.ArrayList;
import java.util.List;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneAdcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapVendorSpecificInfo;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfoL2Sc;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfoLsc;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfoTdm;

import net.es.oscars.resourceManager.http.WSDLTypeConverter;
import net.es.oscars.resourceManager.common.RMException;
import net.es.oscars.utils.soap.OSCARSServiceException;

import org.ogf.schema.network.topology.ctrlplane.*;

public class RetrieveReply {
	
	byte[] receivedApiMsg;
	int offset=0;
	
	
	RetrieveReply(){
		
	}
	
	public boolean checkApiMsg(byte[] apiMsg) throws OSCARSServiceException{
		int type = this.getTwoByte(apiMsg);
		int length = this.getTwoByte(apiMsg);
		int ucid = this.getFourByte(apiMsg);
		int sequenceNum = this.getFourByte(apiMsg);
		
		int checkSum = this.getFourByte(apiMsg);
		int option = this.getFourByte(apiMsg);
		int tag = this.getFourByte(apiMsg);
		
		boolean checkSumValid = this.verifyCheckSum(type, length, ucid, sequenceNum, checkSum);
		
		if(checkSumValid == false){
			
			throw new OSCARSServiceException("Checksum of API message error");
		}
		
		if((option & 0x01) != 0){
			//group messages
			if((option & 0x02) !=0){
				//group last message
				return true;
			}else{
				return false;
			}			
		}else{
			return true;
		}	
		
		
	}
	
	private boolean verifyCheckSum(int type, int length, int ucid, int seqNum, int checkSum){
		boolean valid = true;
		byte byteValue = 0;
		long value = 0;
		long fieldOne = 0;
		long fieldTwo = 0;
		long fieldThree = 0;
		long sum = 0;
		long checkSumValue = 0;
		
		for(int i=0;i<2;i++){
			byteValue = (byte) (0xFF & length);
			value = ((long)0xFF) & byteValue;
			fieldOne = (fieldOne << 8) | value;
			length = length >> 8;			
		}
		
		
		for(int i=0;i<2;i++){
			byteValue = (byte) (0xFF & type);
			value = ((long)0xFF) & byteValue;
			fieldOne = (fieldOne << 8) | value;
			type = type >> 8;
		}
		
		
		for(int i=0;i<4;i++){
			byteValue = (byte) (0xFF & ucid);
			value = ((long)0xFF) & byteValue;
			fieldTwo = (fieldTwo << 8) | value;
			ucid = ucid >> 8;			
		}
		
		
		for(int i=0;i<4;i++){
			byteValue = (byte) (0xFF & seqNum);
			value = ((long)0xFF) & byteValue;
			fieldThree = (fieldThree << 8) | value;
			seqNum = seqNum >> 8;			
		}
		
		
		sum = fieldOne + fieldTwo + fieldThree;
		sum = sum & ((long) 0xFFFFFFFFL);
		
		
		
		
		for(int i=0;i<4;i++){
			byteValue = (byte) (0xFF & checkSum);
			value = ((long)0xFF) & byteValue;
			checkSumValue = (checkSumValue << 8) | value;
			checkSum = checkSum >> 8;
		}
		
		
		
		if(sum == checkSumValue){
			valid = true;
		}else{
			valid = false;
		}	
		
		return valid;
	}
	
	private int getTwoByte(byte[] buff){
		int result = 0;
		int value = 0;
		
		for(int i=0;i<2;i++){
			value = 0xFF & buff[offset++];
			result = (result << 8) | value;
		}
		
		return result;
	}
	
	private int getFourByte(byte[] buff){
		int result = 0;
		int value = 0;
		
		for(int i=0;i<4;i++){
			value = 0xFF & buff[offset++];
			result = (result<<8) | value;
		}
		
		return result;
	}
	
	public ReplyMessageContent decodeReplyMessage(byte[] buff) throws OSCARSServiceException{
		PrimitiveDecoder priDecoder = new PrimitiveDecoder();
		byte type = 0;
		int lengthTagSize = 0;
		int totalMsgLength = 0;
		
		
		int decodedLength = 0;
		int length = 0;
		int initialDecodeOffset = 0;
		String gri;
		String errorMessage;
		ReplyMessageContent replyMessage = new ReplyMessageContent();
		
		type = buff[offset++];
		
		if(type == CodeNumber.PCE_REPLY){
			lengthTagSize = priDecoder.getLengthTagSize(buff, offset);
			totalMsgLength = priDecoder.getLength(buff, offset);
			offset = offset + lengthTagSize;
			
			initialDecodeOffset = offset;
			
			type = buff[offset++];

			if(type == CodeNumber.PCE_GRI){
				length = this.decodeLength(priDecoder, buff);
				gri = priDecoder.decodeString(buff, offset, length);
							
				offset = offset + length;
				replyMessage.setGri(gri);
			}
			
			type = buff[offset++];
			if(type==CodeNumber.PCE_COMPUTE_ERROR){
				length = this.decodeLength(priDecoder, buff);
				errorMessage = priDecoder.decodeString(buff, offset, length);
				
				offset = offset + length;			
				replyMessage.setErrorMessage(errorMessage);
				return replyMessage;
			}
			
			if(type == CodeNumber.PCE_REGU_REPLY){
				int regFieldLength = this.decodeLength(priDecoder, buff);
				this.decodeReplyPathContent(buff, replyMessage);
				decodedLength = offset - initialDecodeOffset;
				
				if(decodedLength < totalMsgLength){
					//type = buff[offset++];
					//if(type == CodeNumber.PCE_OPTI_REPLY){
						int advancedFieldLength = this.decodeLength(priDecoder, buff);
						this.decodeAdvContent(buff, replyMessage);
						
												
					//}
				}				
			}			
		}
			
		return replyMessage;
		
		
	}
	
	void decodeAdvContent(byte[] buff, ReplyMessageContent replyMessage) throws OSCARSServiceException{
		PrimitiveDecoder priDecoder = new PrimitiveDecoder();
		ReplyPathContent replyPath = null;
		ReplyCoSchedulePathContent coSchedulePath = null;
		List<ReplyPathContent> altPaths = null;
		List<ReplyPathContent> flexPaths = null;
		
		byte type = 0;	
		int length = 0;
		
		coSchedulePath = new ReplyCoSchedulePathContent();
		replyMessage.setCoScheduleReply(coSchedulePath);  //set coSchedulePath to reply message
		
		replyPath = this.decodePath(buff);
		
		coSchedulePath.setPath(replyPath);
		
		int altPathsNumber = 0;
		altPaths = coSchedulePath.getAltPathContent();
		
		type = buff[offset++];
		if(type == CodeNumber.PCE_ALT_PATH_NUM){
			length = this.decodeLength(priDecoder, buff);
			altPathsNumber = priDecoder.decodeInteger(buff, offset, length);
			offset = offset + length;			
		}
		
		
		for(int i=0;i<altPathsNumber;i++){
			replyPath = this.decodePath(buff);
			altPaths.add(replyPath);
		}
		
		int flexPathsNumber = 0;
		flexPaths = coSchedulePath.getFlexPathContent();
		
		type = buff[offset++];
		if(type == CodeNumber.PCE_FLEX_ALT_PATH_NUM){
			length = this.decodeLength(priDecoder, buff);
			flexPathsNumber = priDecoder.decodeInteger(buff, offset, length);
			offset = offset + length;
		}
		
		for(int i=0;i<flexPathsNumber;i++){
			replyPath = this.decodePath(buff);
			flexPaths.add(replyPath);
		}
		
	}

	void decodeReplyPathContent(byte[] buff, ReplyMessageContent replyMessage) throws OSCARSServiceException{
		/*
		PrimitiveDecoder priDecoder = new PrimitiveDecoder();
		byte type = 0;
		int lengthTagSize = 0;
		int length = 0;
		String gri;
		String errorMessage;
		String pathId;
		int pathLength = 0;
		String linkName;
		String switchingCapType;
		String switchingEncType;
		String assignedVlanTags;
		String suggestedVlanTags;
		String availableVlanTags;
		boolean vlanTranslation;
		int capacity;
		int mtu;
		String remoteLinkId;
		int maximumReservableCapacity;
		int minimumReservableCapacity;
		int granularity;
		int trafficEngineeringMetric;
		ReplyLinkContent replyLink = null;
		List<ReplyLinkContent> linkSet = null;
		int initialDecodeOffset = 0;
		int regularReplyLength = 0;
		int optionalReplyLength = 0;
		
		ReplyCoSchedulePathContent coSchedulePath = null;
		List<ReplyPathContent> altPaths = null;
		int altPathsNumber = 0;
		*/
		ReplyPathContent replyPath = null;
		
		replyPath = this.decodePath(buff);
		replyMessage.setReplyPathContent(replyPath);
		/*
		initialDecodeOffset = this.offset;		
		
				
		type = buff[offset++];
		if(type == CodeNumber.PCE_GRI){
			lengthTagSize = priDecoder.getLengthTagSize(buff, offset);
			length = priDecoder.getLength(buff, offset);
			offset = offset + lengthTagSize;
			gri = priDecoder.decodeString(buff, offset, length);
						
			offset = offset + length;
			replyMessage.setGri(gri);
		}
		
		type = buff[offset++];
		if(type==CodeNumber.PCE_COMPUTE_ERROR){
			length = this.decodeLength(priDecoder, buff);
			errorMessage = priDecoder.decodeString(buff, offset, length);
			offset = offset + length;			
			replyMessage.setErrorMessage(errorMessage);
			return;
		}
		
		if(type==CodeNumber.PCE_REGU_REPLY){
			regularReplyLength = this.decodeLength(priDecoder, buff);
			replyPath = this.decodePath(buff);
			replyMessage.setReplyPathContent(replyPath);
		}else{
			
		}
		
		if((this.offset - initialDecodeOffset) < totalLength)
		{
			type = buff[offset++];
			if(type==CodeNumber.PCE_OPTI_REPLY){
				optionalReplyLength = this.decodeLength(priDecoder, buff);
				coSchedulePath = new ReplyCoSchedulePathContent();
				replyMessage.setCoScheduleReply(coSchedulePath);  //set coSchedulePath to reply message
				
				replyPath = this.decodePath(buff);
				
				coSchedulePath.setPath(replyPath);
				
				altPaths = coSchedulePath.getAltPathContent();
				
				type = buff[offset++];
				length = this.decodeLength(priDecoder, buff);
				altPathsNumber = priDecoder.decodeInteger(buff, offset, length);
				offset = offset + length;
				
				for(int i=0;i<altPathsNumber;i++){
					replyPath = this.decodePath(buff);
					altPaths.add(replyPath);
				}				
			}else{
				
			}
		}
		*/
		/*
		if(type == CodeNumber.PCE_PATH_ID){
			length = this.decodeLength(priDecoder, buff);
			pathId = priDecoder.decodeString(buff, offset, length);
						
			offset = offset + length;
			ReplyPathContent replyPath = new ReplyPathContent();
			replyMessage.setReplyPathContent(replyPath);  //set replyPathContent to object variable
			replyMessage.getReplyPathContent().setId(pathId);
			
			type = buff[offset++];			
			if(type == CodeNumber.PCE_PATH_LENGTH){
				length = this.decodeLength(priDecoder, buff);
				pathLength = priDecoder.decodeInteger(buff, offset, length);
								
				offset = offset + length;
				
			}
			
			linkSet = replyMessage.getReplyPathContent().getReplyLinkContent();
			
			while((this.offset - initialDecodeOffset) < totalLength){
				type = buff[offset++];
				if(type == CodeNumber.PCE_LINK_ID){
					if(replyLink != null){
						linkSet.add(replyLink);
					}
					replyLink = new ReplyLinkContent();
					length = this.decodeLength(priDecoder, buff);
					linkName = priDecoder.decodeString(buff, offset, length);
										
					offset = offset + length;
					
					replyLink.setName(linkName);
								
				}else if(type == CodeNumber.PCE_REMOTE_LINK){
					length = this.decodeLength(priDecoder, buff);
					remoteLinkId = priDecoder.decodeString(buff, offset, length);
					
					offset = offset + length;
					
					replyLink.setRemoteLinkId(remoteLinkId);
					
				}else if(type == CodeNumber.PCE_SWITCHINGCAPTYPE){
					length = this.decodeLength(priDecoder, buff);
					switchingCapType = priDecoder.decodeString(buff, offset, length);
										
					offset = offset + length;
					
					replyLink.setSwitchingType(switchingCapType);					
				}else if(type == CodeNumber.PCE_SWITCHINGENCTYPE){
					length = this.decodeLength(priDecoder, buff);
					switchingEncType = priDecoder.decodeString(buff, offset, length);
										
					offset = offset + length;
					
					replyLink.setEncodingType(switchingEncType);
				}else if(type == CodeNumber.PCE_SWITCHINGVLANRANGEAVAI){
					length = this.decodeLength(priDecoder, buff);
					availableVlanTags = priDecoder.decodeString(buff, offset, length);
										
					offset = offset + length;
					
					replyLink.setAvailableVlanTags(availableVlanTags);
				}else if(type == CodeNumber.PCE_SWITCHINGVLANRANGESUGG){
					length = this.decodeLength(priDecoder, buff);
					suggestedVlanTags = priDecoder.decodeString(buff, offset, length);
										
					offset = offset + length;
					
					replyLink.setSuggestedVlanTags(suggestedVlanTags);
				}else if(type == CodeNumber.PCE_SWITCHINGVLANRANGEASSI){
					length = this.decodeLength(priDecoder, buff);
					assignedVlanTags = priDecoder.decodeString(buff, offset, length);
										
					offset = offset + length;
					
					replyLink.setAssignedVlanTags(assignedVlanTags);
				}else if(type == CodeNumber.PCE_VLANTRANSLATION){
					length = this.decodeLength(priDecoder, buff);
					vlanTranslation = priDecoder.decodeBoolean(buff, offset, length);
										
					offset = offset + length;
					
					replyLink.setVlanTranslation(vlanTranslation);
				}else if(type == CodeNumber.PCE_CAPACITY){
					length = this.decodeLength(priDecoder, buff);
					capacity = priDecoder.decodeInteger(buff, offset, length);
										
					offset = offset + length;
					
					replyLink.setCapacity(capacity);
				}else if(type == CodeNumber.PCE_MTU){
					length = this.decodeLength(priDecoder, buff);
					mtu = priDecoder.decodeInteger(buff, offset, length);
										
					offset = offset + length;
					
					replyLink.setMtu(mtu);
				}else if(type == CodeNumber.PCE_MAXRESVCAPACITY){
					length = this.decodeLength(priDecoder, buff);
					maximumReservableCapacity = priDecoder.decodeInteger(buff, offset, length);
					
					offset = offset + length;
					
					replyLink.setMaximumReservableCapacity(maximumReservableCapacity);					
				}else if(type == CodeNumber.PCE_MINRESVCAPACITY){
					length = this.decodeLength(priDecoder, buff);
					minimumReservableCapacity = priDecoder.decodeInteger(buff, offset, length);
					
					offset = offset + length;
					
					replyLink.setMinimumReservableCapacity(minimumReservableCapacity);
				}else if(type == CodeNumber.PCE_GRANULARITY){
					length = this.decodeLength(priDecoder, buff);
					granularity = priDecoder.decodeInteger(buff, offset, length);
					
					offset = offset + length;
					
					replyLink.setGranularity(granularity);
				}else if(type == CodeNumber.PCE_TE_METRIC){
					length = this.decodeLength(priDecoder, buff);
					trafficEngineeringMetric = priDecoder.decodeInteger(buff, offset, length);
					
					offset = offset + length;
					
					replyLink.setTrafficEngineeringMetric(trafficEngineeringMetric);
				}else{
					
				}
				
			}
			linkSet.add(replyLink); //last link			
			
		}
		*/
		
	}
	
	
	ReplyPathContent decodePath(byte[] buff) throws OSCARSServiceException{		
		PrimitiveDecoder priDecoder = new PrimitiveDecoder();
		byte type = 0;
		int lengthTagSize = 0;
		int length = 0;
		String gri;
		String errorMessage;
		String pathId = null;
		int pathLength = 0;
		String linkName;
		String switchingCapType;
		String switchingEncType;
		String assignedVlanTags;
		String suggestedVlanTags;
		String availableVlanTags;
		boolean vlanTranslation;
		long capacity;
		int mtu;
		String remoteLinkId;
		long maximumReservableCapacity;
		long minimumReservableCapacity;
		long granularity;
		int trafficEngineeringMetric;
		ReplyLinkContent replyLink = null;
		List<ReplyLinkContent> linkSet = null;
		int initialDecodeOffset = 0;
		boolean pathEndFlag = false;
		
		//ReplyBagSegmentContent bagSegment = null;
		//long bagBandwidth;
		//int bagStartTime;
		//int bagEndTime;
		List<CtrlPlaneSwcapContent> switchingCapabilityDescriptors = null;
		CtrlPlaneSwcapContent switchCap = null;
		CtrlPlaneSwitchingCapabilitySpecificInfo switchingCapabilitySpecificInfo = null;
		List<CtrlPlaneAdcapContent> adjustmentCapabilityDescriptor = null;
		CtrlPlaneAdcapContent adcapContent = null;
		
		
		
		ReplyPathContent replyPath = new ReplyPathContent();
		
		type = buff[offset++];
		if(type == CodeNumber.PCE_PATH_ID){
			length = this.decodeLength(priDecoder, buff);
			pathId = priDecoder.decodeString(buff, offset, length);

			offset = offset + length;
			
			pathEndFlag = true;
		}
		
		replyPath.setId(pathId);
		
		int lifetimeNum = 0;
		type = buff[offset++];
		if(type == CodeNumber.PCE_LIFETIME_NUM){
			length = this.decodeLength(priDecoder, buff);
			lifetimeNum = priDecoder.decodeInteger(buff, offset, length);
			offset = offset + length;
			
			for(int i=0;i<lifetimeNum;i++){
				Lifetime lifetime = new Lifetime();
				
				type = buff[offset++];
				if(type == CodeNumber.PCE_LIFETIME_START){
					length = this.decodeLength(priDecoder, buff);
					int startTime = priDecoder.decodeInteger(buff, offset, length);
					offset = offset + length;
					lifetime.getStart().setValue(Integer.toString(startTime));					
				}
				
				type = buff[offset++];
				if(type == CodeNumber.PCE_LIFETIME_END){
					length = this.decodeLength(priDecoder, buff);
					int endTime = priDecoder.decodeInteger(buff, offset, length);
					offset = offset + length;
					lifetime.getEnd().setValue(Integer.toString(endTime));
				}
				
				type = buff[offset++];
				if(type == CodeNumber.PCE_LIFETIME_DUR){
					length = this.decodeLength(priDecoder, buff);
					int duration = priDecoder.decodeInteger(buff, offset, length);
					offset = offset + length;
					lifetime.getDuration().setValue(Integer.toString(duration));
				}
				
				replyPath.getLifetime().add(lifetime);
			}
			
		}		

		type = buff[offset++];			
		if(type == CodeNumber.PCE_PATH_LENGTH){
			length = this.decodeLength(priDecoder, buff);
			pathLength = priDecoder.decodeInteger(buff, offset, length);

			offset = offset + length;

		}

		linkSet = replyPath.getReplyLinkContent();	
		
		int bagInfoNum = 0;

		while(pathEndFlag){
			type = buff[offset++];
			if(type == CodeNumber.PCE_LINK_ID){
				if(replyLink != null){
					linkSet.add(replyLink);
				}
				replyLink = new ReplyLinkContent();
				length = this.decodeLength(priDecoder, buff);
				linkName = priDecoder.decodeString(buff, offset, length);

				offset = offset + length;

				replyLink.setName(linkName);

			}else if(type == CodeNumber.PCE_REMOTE_LINK){
				length = this.decodeLength(priDecoder, buff);
				remoteLinkId = priDecoder.decodeString(buff, offset, length);

				offset = offset + length;

				replyLink.setRemoteLinkId(remoteLinkId);

			}else if(type == CodeNumber.PCE_SWITCHINGCAPTYPE){
				length = this.decodeLength(priDecoder, buff);
				switchingCapType = priDecoder.decodeString(buff, offset, length);

				offset = offset + length;

				replyLink.setSwitchingType(switchingCapType);
				
				switchingCapabilityDescriptors = replyLink.getSwitchingCapabilityDescriptors();
				
				switchCap = new CtrlPlaneSwcapContent();
								
				switchingCapabilityDescriptors.add(switchCap);
				
				switchCap.setSwitchingcapType(switchingCapType);
				
				switchingCapabilitySpecificInfo = new CtrlPlaneSwitchingCapabilitySpecificInfo();
				switchCap.setSwitchingCapabilitySpecificInfo(switchingCapabilitySpecificInfo);
				
				//according to switch cap type, create corresponding fields
				if(switchingCapType.equals("l2sc")){
					CtrlPlaneSwitchingCapabilitySpecificInfoL2Sc cpscsL2sc = new CtrlPlaneSwitchingCapabilitySpecificInfoL2Sc();
					switchingCapabilitySpecificInfo.setL2ScSpecificInfo(cpscsL2sc);
				}else if(switchingCapType.equals("tdm")){
					CtrlPlaneSwitchingCapabilitySpecificInfoTdm cpscsTdm = new CtrlPlaneSwitchingCapabilitySpecificInfoTdm();
					switchingCapabilitySpecificInfo.setTdmSpecificInfo(cpscsTdm);
				}else if(switchingCapType.equals("lsc")){
					CtrlPlaneSwitchingCapabilitySpecificInfoLsc cpscsLsc = new CtrlPlaneSwitchingCapabilitySpecificInfoLsc();
					switchingCapabilitySpecificInfo.setLscSpecificInfo(cpscsLsc);
				}else{
					throw new OSCARSServiceException("Unknow switching capability type");
				}
				
			}else if(type == CodeNumber.PCE_SWITCHINGENCTYPE){
				length = this.decodeLength(priDecoder, buff);
				switchingEncType = priDecoder.decodeString(buff, offset, length);

				offset = offset + length;

				replyLink.setEncodingType(switchingEncType);
				
				switchCap.setEncodingType(switchingEncType);
				
			}else if(type == CodeNumber.PCE_SWITCHINGVLANRANGEAVAI){
				length = this.decodeLength(priDecoder, buff);
				availableVlanTags = priDecoder.decodeString(buff, offset, length);

				offset = offset + length;

				replyLink.setAvailableVlanTags(availableVlanTags);
				
				switchingCapabilitySpecificInfo.getL2ScSpecificInfo().setVlanRangeSet(availableVlanTags);
				
				switchingCapabilitySpecificInfo.setVlanRangeAvailability(availableVlanTags); //compatible
				
			}else if(type == CodeNumber.PCE_SWITCHINGVLANRANGESUGG){
				length = this.decodeLength(priDecoder, buff);
				suggestedVlanTags = priDecoder.decodeString(buff, offset, length);

				offset = offset + length;

				replyLink.setSuggestedVlanTags(suggestedVlanTags);
				
				switchingCapabilitySpecificInfo.getL2ScSpecificInfo().setSuggestedVlanSet(suggestedVlanTags);
				
				switchingCapabilitySpecificInfo.setSuggestedVLANRange(suggestedVlanTags); //compatible
				
			}else if(type == CodeNumber.PCE_SWITCHINGVLANRANGEASSI){
				length = this.decodeLength(priDecoder, buff);
				assignedVlanTags = priDecoder.decodeString(buff, offset, length);

				offset = offset + length;

				replyLink.setAssignedVlanTags(assignedVlanTags);
				
				switchingCapabilitySpecificInfo.getL2ScSpecificInfo().setUsedVlanSet(assignedVlanTags);
				
			}else if(type == CodeNumber.PCE_VLANTRANSLATION){
				length = this.decodeLength(priDecoder, buff);
				vlanTranslation = priDecoder.decodeBoolean(buff, offset, length);

				offset = offset + length;

				replyLink.setVlanTranslation(vlanTranslation);
				
				switchingCapabilitySpecificInfo.getL2ScSpecificInfo().setVlanTranslation(vlanTranslation);
				
				switchingCapabilitySpecificInfo.setVlanTranslation(vlanTranslation); //compatible
				
			}else if(type == CodeNumber.PCE_CAPACITY){
				length = this.decodeLength(priDecoder, buff);
				capacity = priDecoder.decodeLong(buff, offset, length);

				offset = offset + length;

				replyLink.setCapacity(capacity);
				
				switchCap.setCapacity(Long.toString(capacity));
			}else if(type == CodeNumber.PCE_MTU){
				length = this.decodeLength(priDecoder, buff);
				mtu = priDecoder.decodeInteger(buff, offset, length);

				offset = offset + length;

				replyLink.setMtu(mtu);
				
				switchingCapabilitySpecificInfo.getL2ScSpecificInfo().setInterfaceMTU(mtu);
				
				switchingCapabilitySpecificInfo.setInterfaceMTU(mtu);
				
			}else if(type == CodeNumber.PCE_CONCATENATIONTYPE){
				length = this.decodeLength(priDecoder, buff);
				String concatenationType = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getTdmSpecificInfo().setConcatenationType(concatenationType);
				
			}else if(type == CodeNumber.PCE_SWITCHINGTIMESLOTAVAI){
				length = this.decodeLength(priDecoder, buff);
				String availableTimeslot = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getTdmSpecificInfo().setTimeslotRangeSet(availableTimeslot);
				
			}else if(type == CodeNumber.PCE_SWITCHINGTIMESLOTSUGG){
				length = this.decodeLength(priDecoder, buff);
				String suggestedTimeslot = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getTdmSpecificInfo().setSuggestedTimeslotSet(suggestedTimeslot);
				
			}else if(type == CodeNumber.PCE_SWITCHINGTIMESLOTASSI){
				length = this.decodeLength(priDecoder, buff);
				String assignedTimeslot = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getTdmSpecificInfo().setUsedTimeslotSet(assignedTimeslot);
				
			}else if(type == CodeNumber.PCE_TSIENABLED){
				length = this.decodeLength(priDecoder, buff);
				boolean tsiEnabled = priDecoder.decodeBoolean(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getTdmSpecificInfo().setTsiEnabled(tsiEnabled);
				
			}else if(type == CodeNumber.PCE_VCATENABLED){
				length = this.decodeLength(priDecoder, buff);
				boolean vcatEnabled = priDecoder.decodeBoolean(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getTdmSpecificInfo().setVcatEnabled(vcatEnabled);
				
			}else if(type == CodeNumber.PCE_CHANNELREPRESENTATION){
				length = this.decodeLength(priDecoder, buff);
				String channelRepresentation = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getLscSpecificInfo().setChannelRepresentation(channelRepresentation);
				
			}else if(type == CodeNumber.PCE_SWITCHINGWAVELENAVAI){
				length = this.decodeLength(priDecoder, buff);
				String availableWavelength = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getLscSpecificInfo().setWavelengthRangeSet(availableWavelength);
				
			}else if(type == CodeNumber.PCE_SWITCHINGWAVELENSUGG){
				length = this.decodeLength(priDecoder, buff);
				String suggestedWavelength = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getLscSpecificInfo().setSuggestedWavelengthSet(suggestedWavelength);
				
			}else if(type == CodeNumber.PCE_SWITCHINGWAVELENASSI){
				length = this.decodeLength(priDecoder, buff);
				String assignedWavelength = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getLscSpecificInfo().setUsedWavelengthSet(assignedWavelength);
				
			}else if(type == CodeNumber.PCE_WAVELENGTHCONVERSION){
				length = this.decodeLength(priDecoder, buff);
				boolean wavelengthConversion = priDecoder.decodeBoolean(buff, offset, length);
				offset = offset + length;
				
				switchingCapabilitySpecificInfo.getLscSpecificInfo().setWavelengthConversionEnabled(wavelengthConversion);
				
			}else if(type == CodeNumber.PCE_VENDORSPECIFICINFO){
				length = this.decodeLength(priDecoder, buff);
				String vendorSpecXmlStr = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				// test line: to be removed
				vendorSpecXmlStr="<vendorSpecificInfo xmlns=\"http://ogf.org/schema/network/topology/ctrlPlane/20110826/\"><infineraDTNSpecificInfo><tributaryInfo id=\"1\" model=\"TAM-2-10\" contain=\"1xOTU3\">        <OTU3 id=\"1\" contain=\"1x10GE\"/>        </tributaryInfo></infineraDTNSpecificInfo></vendorSpecificInfo>";
				try {
					CtrlPlaneSwcapVendorSpecificInfo vendorSpecificInfo = WSDLTypeConverter.convertVendorSpecificInfo(vendorSpecXmlStr);
					switchingCapabilitySpecificInfo.setVendorSpecificInfo(vendorSpecificInfo);
				} catch (RMException e) {
					throw new OSCARSServiceException(e.getMessage());
				}
			}else if(type == CodeNumber.PCE_IACD_START){
				length = this.decodeLength(priDecoder, buff);
				String iacdStart = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				adjustmentCapabilityDescriptor = replyLink.getAdjustmentCapabilityDescriptor();
				adcapContent = new CtrlPlaneAdcapContent();
				adjustmentCapabilityDescriptor.add(adcapContent);
								
			}else if(type == CodeNumber.PCE_LOWERLAYERSWITCHINGTYPE){
				length = this.decodeLength(priDecoder, buff);
				String lowerSwcap = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				adcapContent.setLowerSwcap(lowerSwcap);
				
			}else if(type == CodeNumber.PCE_LOWERLAYERENCODINGTYPE){
				length = this.decodeLength(priDecoder, buff);
				String lowerEncType = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				adcapContent.setLowerEncType(lowerEncType);				
				
			}else if(type == CodeNumber.PCE_UPPERLAYERSWITCHINGTYPE){
				length = this.decodeLength(priDecoder, buff);
				String upperSwcap = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				adcapContent.setUpperSwcap(upperSwcap);			
				
			}else if(type == CodeNumber.PCE_UPPERLAYERENCODINGTYPE){
				length = this.decodeLength(priDecoder, buff);
				String upperEncType = priDecoder.decodeString(buff, offset, length);
				offset = offset + length;
				
				adcapContent.setUpperEncType(upperEncType);				
				
			}else if(type == CodeNumber.PCE_MAXADAPTBANDWIDTH){
				length = this.decodeLength(priDecoder, buff);
				long maximumAdjustableCapacity = priDecoder.decodeLong(buff, offset, length);
				offset = offset + length;
				
				adcapContent.setMaximumAdjustableCapacity(Long.toString(maximumAdjustableCapacity));
				
			}else if(type == CodeNumber.PCE_MAXRESVCAPACITY){
				length = this.decodeLength(priDecoder, buff);
				maximumReservableCapacity = priDecoder.decodeLong(buff, offset, length);

				offset = offset + length;

				replyLink.setMaximumReservableCapacity(maximumReservableCapacity);					
			}else if(type == CodeNumber.PCE_MINRESVCAPACITY){
				length = this.decodeLength(priDecoder, buff);
				minimumReservableCapacity = priDecoder.decodeLong(buff, offset, length);

				offset = offset + length;

				replyLink.setMinimumReservableCapacity(minimumReservableCapacity);
			}else if(type == CodeNumber.PCE_GRANULARITY){
				length = this.decodeLength(priDecoder, buff);
				granularity = priDecoder.decodeLong(buff, offset, length);

				offset = offset + length;

				replyLink.setGranularity(granularity);
				//granularity field is per link in mxtce implementation
				//switchCap.setGranularity(Long.toString(granularity));
			}else if(type == CodeNumber.PCE_TE_METRIC){
				length = this.decodeLength(priDecoder, buff);
				trafficEngineeringMetric = priDecoder.decodeInteger(buff, offset, length);

				offset = offset + length;

				replyLink.setTrafficEngineeringMetric(trafficEngineeringMetric);
				/*
			}else if(type == CodeNumber.PCE_OPT_BAG_BANDWIDTH){				
				length = this.decodeLength(priDecoder, buff);
				bagBandwidth = priDecoder.decodeLong(buff, offset, length);
				
				offset = offset + length;
				
				if(bagSegment == null){
					bagSegment = new ReplyBagSegmentContent();
				}
				
				bagSegment.setBandwidth(bagBandwidth);					
			}else if(type == CodeNumber.PCE_OPT_BAG_STARTTIME){
				length = this.decodeLength(priDecoder, buff);
				bagStartTime = priDecoder.decodeInteger(buff, offset, length);
				
				offset = offset + length;
				
				if(bagSegment == null){
					throw new OSCARSServiceException("BagSegment should start with bandwidth in API encoded message");
				}
				bagSegment.setStartTime(bagStartTime);					
			}else if(type == CodeNumber.PCE_OPT_BAG_ENDTIME){
				length = this.decodeLength(priDecoder, buff);
				bagEndTime = priDecoder.decodeInteger(buff, offset, length);
				
				offset = offset + length;
				
				if(bagSegment == null){
					throw new OSCARSServiceException("BagSegment should start with bandwidth in API encoded message");
				}				
				bagSegment.setEndTime(bagEndTime);
				bags.add(bagSegment);
				bagSegment = null; //set back to null
				*/
			}else if(type == CodeNumber.PCE_OPT_BAG_INFO_NUM){
				length = this.decodeLength(priDecoder, buff);
				bagInfoNum = priDecoder.decodeInteger(buff, offset, length);  //get bag info number and terminate regular fields decoding
				
				offset = offset + length;
				
				linkSet.add(replyLink); //last link	
				pathEndFlag = false;

			}else{
				
			}

		}
		//linkSet.add(replyLink); //last link			

		List<ReplyBagInfoContent> bagInfo = replyPath.getReplyBagInfoContent();
		
		for(int i=0;i<bagInfoNum;i++){
			ReplyBagInfoContent singleBagInfo = new ReplyBagInfoContent();
			bagInfo.add(singleBagInfo);
			List<ReplyBagSegmentContent> bagSeg = singleBagInfo.getReplyBagSegmentContent();

			type = buff[offset++];
			if(type == CodeNumber.PCE_OPT_BAG_SEG_NUM){
				length = this.decodeLength(priDecoder, buff);
				int bagSegNum = priDecoder.decodeInteger(buff, offset, length);
				offset = offset + length;

				for(int j=0;j<bagSegNum;j++){
					ReplyBagSegmentContent bagSegment = new ReplyBagSegmentContent();
					bagSeg.add(bagSegment);

					type = buff[offset++];
					if(type == CodeNumber.PCE_OPT_BAG_BANDWIDTH){
						length = this.decodeLength(priDecoder, buff);
						long bagBandwidth = priDecoder.decodeLong(buff, offset, length);							
						offset = offset + length;

						bagSegment.setBandwidth(bagBandwidth);
					}

					type = buff[offset++];
					if(type == CodeNumber.PCE_OPT_BAG_STARTTIME){
						length = this.decodeLength(priDecoder, buff);
						int bagStartTime = priDecoder.decodeInteger(buff, offset, length);							
						offset = offset + length;

						bagSegment.setStartTime(bagStartTime);
					}

					type = buff[offset++];						
					if(type == CodeNumber.PCE_OPT_BAG_ENDTIME){
						length = this.decodeLength(priDecoder, buff);
						int bagEndTime = priDecoder.decodeInteger(buff, offset, length);							
						offset = offset + length;

						bagSegment.setEndTime(bagEndTime);						
					}						
				}					
			}else{
				throw new OSCARSServiceException("BagSegment number is missing");
			}
		}	
		
		return replyPath;

	}
	

	
	int decodeLength(PrimitiveDecoder priDecoder, byte[] buff){
		int lengthTagSize = 0;
		int length = 0;
		lengthTagSize = priDecoder.getLengthTagSize(buff, offset);
		length = priDecoder.getLength(buff, offset);
		offset = offset + lengthTagSize;
		return length;
	}

}
