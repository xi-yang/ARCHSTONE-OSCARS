/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.oscars.wbui.servlets;

import java.io.*;
import java.util.*;

import net.sf.json.*;

import net.es.oscars.pce.tce.client.*;
import net.es.oscars.pce.soap.gen.v06.*;
import org.ogf.schema.network.topology.ctrlplane.*;

import java.text.SimpleDateFormat;

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
            ArrayList<HashMap<String,String>> pathList = new ArrayList<HashMap<String,String>>();
            CtrlPlaneTopologyContent topology = pceDataContent.getTopology();
            List<CtrlPlanePathContent> paths = topology.getPath();
            for (CtrlPlanePathContent path: paths) {
                HashMap<String,String> pathMap = new HashMap<String,String>();
                pathMap.put("id", path.getId());
                List<HashMap<String,String>> hopList = getPathHops(path);
                pathMap.put("hops", JSONArray.fromObject(hopList).toString());
                String hopsText = pathHops2String(path);
                pathMap.put("hopText", hopsText);
                List<HashMap<String,String>> scheduleList = getSchedules(path);
                pathMap.put("schedules", JSONArray.fromObject(scheduleList).toString());
                String schedulsText = pathSchedules2String(path);
                pathMap.put("scheduleText", schedulsText);
                getVlans(path, pathMap);
                pathList.add(pathMap);
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
    
    private List<HashMap<String,String>> getPathHops(CtrlPlanePathContent path) {
        ArrayList<HashMap<String,String>> hopList = null;
        ArrayList<CtrlPlaneHopContent> hops = (ArrayList<CtrlPlaneHopContent>) path.getHop();
        if (hops.size() > 0) {
            hopList = new ArrayList<HashMap<String,String>>();
            for (CtrlPlaneHopContent ctrlHop : hops) {
                HashMap<String,String> linkObj = new HashMap<String,String>();
                CtrlPlaneLinkContent link = ctrlHop.getLink();
                linkObj.put("id", link.getId());
                linkObj.put("bandwidth", Long.toBinaryString(Long.valueOf(link.getMaximumReservableCapacity())/1000000));
                linkObj.put("remoteLinkId", link.getRemoteLinkId());
                CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors().get(0);
                linkObj.put("swcapType", swcap.getSwitchingcapType());
                linkObj.put("encoding", swcap.getEncodingType());
                CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = swcap.getSwitchingCapabilitySpecificInfo();
                if (specInfo != null) {
                    linkObj.put("vlanRange", specInfo.getVlanRangeAvailability());
                    linkObj.put("suggestedVlan", specInfo.getSuggestedVLANRange());
                    // TODO: more layer-specific info to add
                }
                hopList.add(linkObj);
            }            
        }
        return hopList;
    }

    private List<HashMap<String,String>> getSchedules(CtrlPlanePathContent path) {
        ArrayList<HashMap<String,String>> scheduleList = null;
        ArrayList<Lifetime> lfts = (ArrayList<Lifetime>) path.getLifetime();
        if (lfts.size() > 0) {
            scheduleList = new ArrayList<HashMap<String,String>>();
            int i = 0;
            for (Lifetime lft : lfts) {
                i++;
                HashMap<String,String> lftObj = new HashMap<String,String>();
                lftObj.put("id", Integer.toBinaryString(i));
                Date start = new Date(Long.valueOf(lft.getStart().getValue())*1000);
                Date end = new Date(Long.valueOf(lft.getEnd().getValue())*1000);
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                String startStr = formatter.format(start);
                String startDateTime[] = startStr.split(" ");
                String endStr = formatter.format(end);
                String endDateTime[] = endStr.split(" ");
                lftObj.put("startDate", startDateTime[0]);
                lftObj.put("startTime", startDateTime[1]);
                lftObj.put("endDate", endDateTime[0]);
                lftObj.put("endTime", endDateTime[1]);
                lftObj.put("duration", lft.getDuration().getValue());
                scheduleList.add(lftObj);
            }
        }
        return scheduleList;
    }
    
    private void getVlans(CtrlPlanePathContent path, HashMap<String,String> pathMap) {
        ArrayList<CtrlPlaneHopContent> hops = (ArrayList<CtrlPlaneHopContent>) path.getHop();
        if (hops.size() == 0) {
            pathMap.put("srcVlan", "any");
        }
        if (hops.size() <= 1) {
            pathMap.put("dstVlan", "any");
        } else {
            CtrlPlaneHopContent srcHop = hops.get(0);
            CtrlPlaneHopContent dstHop = hops.get(hops.size()-1);
            CtrlPlaneLinkContent link = srcHop.getLink();
            if (link == null || link.getSwitchingCapabilityDescriptors().size() == 0 
                    || link.getSwitchingCapabilityDescriptors().get(0).getSwitchingCapabilitySpecificInfo() == null) {
                pathMap.put("srcVlan", "any");            
            } else {
                pathMap.put("srcVlan", link.getSwitchingCapabilityDescriptors().get(0).getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability());            
            }
            link = dstHop.getLink();
            if (link == null || link.getSwitchingCapabilityDescriptors().size() == 0 
                    || link.getSwitchingCapabilityDescriptors().get(0).getSwitchingCapabilitySpecificInfo() == null) {
                pathMap.put("dstVlan", "any");            
            } else {
                pathMap.put("dstVlan", link.getSwitchingCapabilityDescriptors().get(0).getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability());            
            }
        }
    }

    private String pathHops2String(CtrlPlanePathContent path) {
        String pathStr = null;
        ArrayList<CtrlPlaneHopContent> hops = (ArrayList<CtrlPlaneHopContent>) path.getHop();
        if (hops.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (CtrlPlaneHopContent ctrlHop : hops) {
                CtrlPlaneLinkContent link = ctrlHop.getLink();
                sb.append("[" + link.getId() + "] ");
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
