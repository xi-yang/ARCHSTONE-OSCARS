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
import org.ogf.schema.network.topology.ctrlplane.*;

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
        
        Map<String, Object> replyMap = new HashMap<String, Object>();

        // retrieve error message
        if (pceDataContent == null && pceError != null) {
            replyMap.put("success", Boolean.FALSE);
            replyMap.put("status", pceError.getMsg()+": "+pceError.getDetails());
            replyMap.put("method", method);
        }

        // retrieve topology
        if (pceDataContent != null && pceDataContent.getTopology() != null 
                && pceDataContent.getUserRequestConstraint().getPathInfo().getPathType().equalsIgnoreCase("ServiceTopology")) {
            replyMap.put("success", Boolean.TRUE);
            replyMap.put("status", "PCEData replied for TCE Query");
            replyMap.put("method", method);

            ArrayList<HashMap<String,Object>> pathList = new ArrayList<HashMap<String,Object>>();
            CtrlPlaneTopologyContent topology = pceDataContent.getTopology();
            List<CtrlPlanePathContent> paths = topology.getPath();
            for (CtrlPlanePathContent path: paths) {
                HashMap<String,Object> pathMap = new HashMap<String,Object>();
                pathMap.put("id", path.getId());
                List<HashMap<String,Object>> hopList = getPathHops(path);
                pathMap.put("hops", hopList);
                String hopsText = pathHops2String(path);
                pathMap.put("hopText", hopsText);
                List<HashMap<String,Object>> scheduleList = getSchedules(path);
                pathMap.put("schedules", scheduleList);
                String schedulsText = pathSchedules2String(path);
                pathMap.put("scheduleText", schedulsText);
            }
            replyMap.put("pathData", pathList);
        }

        // retrieve optionalConstraint
        if (pceDataContent != null && pceDataContent.getOptionalConstraint() != null 
               && !pceDataContent.getOptionalConstraint().isEmpty()
               && pceDataContent.getOptionalConstraint().get(0).getValue().getStringValue() != null
               && !pceDataContent.getOptionalConstraint().get(0).getValue().getStringValue().isEmpty()) {
            String optionalConstraint = pceDataContent.getOptionalConstraint().get(0).getValue().getStringValue();
            System.out.println( "PCE optionalConstraint = "+optionalConstraint);
        }

        JSONObject jsonObject = JSONObject.fromObject(replyMap);
        if (servletWriter != null) {
            servletWriter.println("{}&&" + jsonObject);
        }
        this.isReady = true;
    }   
    
    private List<HashMap<String,Object>> getPathHops(CtrlPlanePathContent path) {
        ArrayList<HashMap<String,Object>> scheduleList = null;
        ArrayList<CtrlPlaneHopContent> hops = (ArrayList<CtrlPlaneHopContent>) path.getHop();
        if (hops.size() > 0) {
            scheduleList = new ArrayList<HashMap<String,Object>>();
            for (CtrlPlaneHopContent ctrlHop : hops) {
                HashMap<String,String> linkObj = new HashMap<String,String>();
                CtrlPlaneLinkContent link = ctrlHop.getLink();
                linkObj.put("id", link.getId());
                linkObj.put("bandwidth", link.getCapacity());
                linkObj.put("remoteLinkId", link.getRemoteLinkId());
                CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors().get(0);
                linkObj.put("swcapType", swcap.getSwitchingcapType());
                linkObj.put("encoding", swcap.getSwitchingcapType());
                CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = swcap.getSwitchingCapabilitySpecificInfo();
                if (specInfo != null) {
                    linkObj.put("vlanRange", specInfo.getVlanRangeAvailability());
                    linkObj.put("suggestedVlan", specInfo.getSuggestedVLANRange());
                    // TODO: more layer-specific info to add
                }
            }            
        }
        return scheduleList;
    }

    private List<HashMap<String,Object>> getSchedules(CtrlPlanePathContent path) {
        ArrayList<HashMap<String,Object>> hopList = null;
        ArrayList<Lifetime> lfts = (ArrayList<Lifetime>) path.getLifetime();
        if (lfts.size() > 0) {
            ArrayList<HashMap<String,Object>> scheduleList = new ArrayList<HashMap<String,Object>>();
            int i = 0;
            for (Lifetime lft : lfts) {
                i++;
                HashMap<String,Object> lftObj = new HashMap<String,Object>();
                lftObj.put("id", Integer.toBinaryString(i));
                Date start = new Date(Long.valueOf(lft.getStart().getValue())*1000);
                Date end = new Date(Long.valueOf(lft.getEnd().getValue())*1000);
                lftObj.put("start", start.toString());
                lftObj.put("end", end.toString());
                lftObj.put("duration", lft.getDuration().getValue());
                scheduleList.add(lftObj);
            }
        }
        return hopList;
    }

    private String pathHops2String(CtrlPlanePathContent path) {
        String pathStr = null;
        ArrayList<CtrlPlaneHopContent> hops = (ArrayList<CtrlPlaneHopContent>) path.getHop();
        if (hops.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (CtrlPlaneHopContent ctrlHop : hops) {
                CtrlPlaneLinkContent link = ctrlHop.getLink();
                sb.append(" " + link.getId() + ":");
                // VLAN etc.
            }
            pathStr = sb.toString();
        }  // end hops.size > 0
        return pathStr;
    }

    private String pathSchedules2String(CtrlPlanePathContent path) {
        String pathStr = null;
        ArrayList<Lifetime> lfts = (ArrayList<Lifetime>) path.getLifetime();
        if (lfts.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Lifetime lft : lfts) {
                Date start = new Date(Long.valueOf(lft.getStart().getValue())*1000);
                Date end = new Date(Long.valueOf(lft.getEnd().getValue())*1000);
                sb.append("["+start.toString());
                sb.append("--"+end.toString()+"] ");
            }
            pathStr = sb.toString();
        }  // end hops.size > 0
        return pathStr;
    }

}
