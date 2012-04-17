/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;

import net.sf.json.JSONObject;

import net.es.oscars.pce.tce.client.*;
import net.es.oscars.pce.soap.gen.v06.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 *
 * @author xyang
 */
public class QueryTCEReplyHandler  extends TCECallbackHandler {
    
    boolean isReady = false;

    PrintWriter servletWriter = null;
    void setServletWriter(PrintWriter out) {  servletWriter = out; }

    synchronized boolean ready() {
        return isReady;
    }

    @Override
    synchronized public void handleReply(String method, String globalReservationId, 
            String transactionId, PCEDataContent pceDataContent, PCEError pceError, 
            String errorCode) throws RuntimeException  {
        
        // retrieve error message
        if (pceDataContent == null && pceError != null) {
            Map<String, Object> errorMap = new HashMap<String, Object>();
            errorMap.put("success", Boolean.FALSE);
            errorMap.put("status", pceError.getMsg()+": "+pceError.getDetails());
            errorMap.put("method", method);
            JSONObject jsonObject = JSONObject.fromObject(errorMap);
            if (servletWriter != null) {
                servletWriter.println("{}&&" + jsonObject);
            }
        }

        // retrieve topology
        if (pceDataContent != null && pceDataContent.getTopology() != null && pceDataContent.getUserRequestConstraint().getPathInfo().getPathType().equalsIgnoreCase("ServiceTopology")) {
            ;
        }

        // retrieve optionalConstraint
        if (pceDataContent != null && pceDataContent.getOptionalConstraint() != null 
               && !pceDataContent.getOptionalConstraint().isEmpty()
               && pceDataContent.getOptionalConstraint().get(0).getValue().getStringValue() != null
               && !pceDataContent.getOptionalConstraint().get(0).getValue().getStringValue().isEmpty()) {
            String optionalConstraint = pceDataContent.getOptionalConstraint().get(0).getValue().getStringValue();
            System.out.println( "PCE optionalConstraint = "+optionalConstraint);
        }

        this.isReady = true;
    }   
}
