package net.es.oscars.pce.tce;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneDomainContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneNodeContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePortContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.ListRequest;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.common.soap.gen.AuthConditionType;
import net.es.oscars.common.soap.gen.AuthConditions;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.utils.clients.RMClient;
import net.es.oscars.utils.sharedConstants.StateEngineValues;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.NMWGParserUtil;

import java.net.*;
import java.io.*;

public class TcePCE {
    private Logger log = Logger.getLogger(TcePCE.class);
    private String rmWsdl;
    private String rmUrl;
    //private RMPortType rmClient;
    private RMClient rmClient;
    
    final private String[] STATUSES = {
            StateEngineValues.ACTIVE, StateEngineValues.INPATHCALCULATION,
            StateEngineValues.INSETUP, StateEngineValues.INTEARDOWN, 
            StateEngineValues.INMODIFY, StateEngineValues.INCOMMIT, 
            StateEngineValues.COMMITTED, StateEngineValues.RESERVED
            };
    final private double MBPS_DENOM = 1000000.0;
    
    
    public TcePCE(String rmUrl, String rmWsdl) throws OSCARSServiceException{
        this.rmClient = null;
        this.rmUrl = rmUrl;
        if(rmWsdl == null){
            this.rmWsdl = rmUrl+"?wsdl";
        }else{
            this.rmWsdl = rmWsdl;
        }
    }
    
    public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException, MalformedURLException, IOException{
        return this.calculatePath(query, null);
    }
    
    
    public PCEDataContent calculatePath(PCEMessage query, OSCARSNetLogger netLogger) throws OSCARSServiceException, MalformedURLException, IOException{
        synchronized(this){
            if(this.rmClient == null){
                //this.rmClient = RMClient.getClient(new URL(this.rmUrl), new URL(this.rmWsdl)).getPortType();
                this.rmClient = RMClient.getClient(new URL(this.rmUrl), new URL(this.rmWsdl));
            }
        }
        
		String host="localhost";
		int tcePort=2089;
		Socket socket;
		socket=new Socket(host,tcePort);
		
		OutputStream f1 =socket.getOutputStream();
		InputStream r1 =socket.getInputStream();
		
        byte[] apiMsg = null;        
        CreateApiMsg createMsg = new CreateApiMsg();        
        int pathNum = createMsg.getPathNumber(query);
        
        for(int i=0;i<pathNum;i++){
        	if(i!=(pathNum-1)){
        		createMsg.encodeApiMsg(query,i,false);
        	}else{
        		createMsg.encodeApiMsg(query,i,true);
        	}
        	apiMsg = createMsg.getEncodedApiMsg();
        	f1.write(apiMsg);        	
        }        
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int length = -1;
		int totalReadLength = 0;
		int messageLength = -1;
		byte[] replyApiBuff;
		boolean msgLenGetFlag = false;
		int lastResidualLen = 0;
		byte[] lastResidual = new byte[0];
		byte[] msg;
		byte[] newRead;
		boolean msgEndFlag = false;
		
		ArrayList<ReplyMessageContent> replyMessageList = new ArrayList<ReplyMessageContent>();
		
		while((length = r1.read(buff))!=-1){
			
			totalReadLength = totalReadLength + length;			
			buffer.write(buff, 0, length);
			if((totalReadLength+lastResidualLen) >= 24 && msgLenGetFlag == false){
				messageLength = this.getLength(buffer.toByteArray());
				msgLenGetFlag = true;
			}
			if((totalReadLength+lastResidualLen) >= messageLength + 24){
				//break;
				replyApiBuff = buffer.toByteArray();
				
				newRead = new byte[totalReadLength+lastResidualLen];
				System.arraycopy(lastResidual, 0, newRead, 0, lastResidualLen);
				System.arraycopy(replyApiBuff, 0, newRead, lastResidualLen, totalReadLength);
				
				msg = new byte[messageLength+24];
				lastResidualLen = lastResidualLen+totalReadLength-messageLength-24;
				lastResidual = new byte[lastResidualLen]; 
				
				System.arraycopy(newRead, 0, msg, 0, messageLength+24);
				System.arraycopy(newRead, messageLength+24, lastResidual, 0, lastResidualLen);
				
				msgLenGetFlag = false;
				
				RetrieveReply retrieveMsg = new RetrieveReply();
				msgEndFlag = retrieveMsg.checkApiMsg(msg);
				ReplyMessageContent replyMessage = retrieveMsg.decodeReplyMessage(msg);
				replyMessageList.add(replyMessage);
				
				if(replyMessage.getErrorMessage() != null){
					msgEndFlag = true;
				}
				
				while(msgEndFlag == false){					
					if(lastResidualLen >= 24 && msgLenGetFlag == false){
						messageLength = this.getLength(lastResidual);
						msgLenGetFlag = true;
					}else{
						break;
					}
					
					if(lastResidualLen >= messageLength + 24){
						msg = new byte[messageLength+24];
						lastResidualLen = lastResidualLen-messageLength-24;
						newRead = lastResidual;
						lastResidual = new byte[lastResidualLen];
						
						System.arraycopy(newRead, 0, msg, 0, messageLength+24);
						System.arraycopy(newRead, messageLength+24, lastResidual, 0, lastResidualLen);
						
						msgLenGetFlag = false;
						
						retrieveMsg = new RetrieveReply();
						msgEndFlag = retrieveMsg.checkApiMsg(msg);
						replyMessage = retrieveMsg.decodeReplyMessage(msg);
						replyMessageList.add(replyMessage);
						
						if(replyMessage.getErrorMessage() != null){
							msgEndFlag = true;
						}
					}else{
						break;
					}					
				}					
				
				if(msgEndFlag == true){
					break;
				}			
				
				buffer = new ByteArrayOutputStream(); //generate a new stream buffer
				totalReadLength = 0; //reset length
			}
		}		
		
		//ReplyMessageContent replyMessage = null;
		//RetrieveReply retrieveMsg = new RetrieveReply();
		//replyApiBuff = buffer.toByteArray();
		//retrieveMsg.checkApiMsg(replyApiBuff);
		//replyMessage = retrieveMsg.decodeReplyMessage(replyApiBuff);

		socket.close();
		
		WriteResult writeRes = new WriteResult(query);

		writeRes.writeComputeRes(replyMessageList);        
		
        PCEDataContent pceData = query.getPCEDataContent();  //pceData has been updated by method writeComputeRes() if path compute result is succeed

        return pceData;
        
        
    }
    
    public PCEDataContent commitPath(PCEMessage query) throws OSCARSServiceException{

        PCEDataContent pceData = query.getPCEDataContent();
        
        return pceData;
    }
    
	private int getLength(byte[] buff){
		int result = 0;
		int value = 0;
		int offset = 2;
		
		for(int i=0;i<2;i++){
			value = 0xFF & buff[offset++];
			result = (result << 8) | value;
		}
		
		return result;
	}

}
