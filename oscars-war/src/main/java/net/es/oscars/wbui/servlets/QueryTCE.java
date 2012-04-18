package net.es.oscars.wbui.servlets;

/**
 * QueryTCE servlet
 *
 * @author Xi Yang
 *
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;

import java.net.URL;

import net.es.oscars.pce.soap.gen.v06.*;

import net.es.oscars.pce.tce.client.*;
import org.ogf.schema.network.topology.ctrlplane.*;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

/**
 * Servlet to handle a QueryTCE request
 * Parses the servletRequest, check the session validity, gets the
 * user's attributes and calls the Coordinator client to create the reservation.
 * @author davidr,mrt
 *
 */
public class QueryTCE extends HttpServlet {
    private Logger log = Logger.getLogger(QueryTCE.class);

    /**
     * doGet
     *
     * @param request HttpServlerRequest - contains start and end times, bandwidth, description,
     *          productionType, pathinfo
     * @param response HttpServler Response - contain gri and success or error status
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        String methodName= "QueryTCE";
        String transId  = PathTools.getLocalDomainId() + "-WBUI-" +
                          UUID.randomUUID().toString();
        OSCARSNetLogger netLogger = new OSCARSNetLogger();
        netLogger.init(ServiceNames.SVC_WBUI,transId);
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.info(netLogger.start(methodName));

        PrintWriter out = response.getWriter();
        ServletCore core = (ServletCore)
              getServletContext().getAttribute(ServletCore.CORE);
        if (core == null) {
            ServletUtils.fatalError(out, methodName);
        }
        /* put transId in core so that the queryReservationStatus servlets that are run as a consequence 
           of QueryTCE will use same transId.*/
        core.setTransId(transId);
        TCEApiClient apiClient  = core.getTceClient();
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
        UserSession userSession = new UserSession(core);
        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, request,
                                     methodName);
        if (sessionReply == null) {
            this.log.warn(netLogger.error(methodName, ErrSev.MINOR,"No user session: cookies invalid"));
            return;
        }

        URL hostUrl = new URL("http://localhost:9020/tcePCE");
        URL wsdlUrl = new URL("file://" + System.getenv("OSCARS_HOME") + "/PCERuntimeService/api/pce-0.6.wsdl");
        try {
            ((QueryTCEReplyHandler)apiClient.getReplyHandler()).setServletWriter(out);
            CtrlPlaneTopologyContent topology = new CtrlPlaneTopologyContent();
            String topoId = "TCE-topology-" + UUID.randomUUID().toString();
            topology.setId(topoId);
            String idcId  = "https://localhost:8443/OSCARS"; 
            topology.setIdcId(idcId);
            List<CtrlPlanePathContent> pathList = topology.getPath();
            CtrlPlanePathContent path = this.assemblePath(request);
            pathList.add(path);
            List<Lifetime> scheduleList = topology.getLifetime();
            assembleSchedules(request, scheduleList);
            PCEDataContent pceData = apiClient.assemblePceData(topology, transId);
            apiClient.sendPceCreate(transId, pceData);
            for (int t = 0; t < 10; t++) {
                Thread.sleep(3000); // 3 secs
                if (((QueryTCEReplyHandler)apiClient.getReplyHandler()).ready()) {
                    return;
                }
            }
            throw new OSCARSServiceException("Timeout: no response from TcePCE server.");
        } catch (Exception ex) {
            ServletUtils.handleFailure(out, log, ex, methodName);
            return;
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }
    
    private CtrlPlanePathContent assemblePath(HttpServletRequest request)
            throws OSCARSServiceException {
        String source = null;
        String destination = null;
        String strParam = request.getParameter("tceSource");
        if ((strParam != null) && !strParam.trim().equals("")) {
            source = strParam.trim();
        } else {
            throw new OSCARSServiceException("error:  source is a required parameter");
        }
        strParam = request.getParameter("tceDestination");
        if ((strParam != null) && !strParam.trim().equals("")) {
            destination = strParam.trim();
        } else {
            throw new OSCARSServiceException("error:  destination is a required parameter");
        }
        CtrlPlanePathContent path = new CtrlPlanePathContent ();
        String pathId= "TCE-path-" +UUID.randomUUID().toString();
        path.setId(pathId);
        List<CtrlPlaneHopContent> pathHops = path.getHop();
        String srcVlan = "";
        strParam = request.getParameter("tceSrcVlan");
        if (strParam != null && !strParam.trim().equals("")) {
            srcVlan = strParam.trim();
        }else {
            throw new OSCARSServiceException("error:  source vlan is a required parameter");
        }
        String dstVlan = "";
        strParam = request.getParameter("tceDestVlan");
        if (strParam != null && !strParam.trim().equals("")) {
            dstVlan = strParam.trim();
        }else {
            throw new OSCARSServiceException("error:  destination vlan is a required parameter");
        }

        long lbw=0, lmax=0, lmin=0;
        String bandwidth = "";
        strParam = request.getParameter("tceBandwidth");
        if ((strParam != null) && !strParam.trim().equals("")) {
            bandwidth = strParam.trim();
            lbw = Long.valueOf(bandwidth);
            //bandwidth = Long.toString(lbw*1000000);
        } else {
            throw new OSCARSServiceException("error:  bandwidth is a required parameter");
        }
        String maxBandwidth = "0";
        strParam = request.getParameter("maxBandwidth");
        if ((strParam != null) && !strParam.trim().equals("")) {
            maxBandwidth = strParam.trim();
            lmax = Long.valueOf(maxBandwidth);
            //maxBandwidth = Long.toString(lmax*1000000);
        } 
        String minBandwidth = "0";
        strParam = request.getParameter("minBandwidth");
        if ((strParam != null) && !strParam.trim().equals("")) {
            minBandwidth = strParam.trim();
            lmin = Long.valueOf(minBandwidth);
            //minBandwidth = Long.toString(lmin*1000000);
        } 
        if (lmax > 0 && lmax < lbw) {
            throw new OSCARSServiceException("'max. bandwidth' must be either 0 or greater than 'bandwidth'");
        }
        if (lmin > 0 && lmin > lbw) {
            throw new OSCARSServiceException("'min. bandwidth' must be either 0 or smaller than 'bandwidth'");
        }

        CtrlPlaneHopContent sourceHop = new CtrlPlaneHopContent();
        sourceHop.setId("source");
        //sourceHop.setLinkIdRef(source);
        CtrlPlaneLinkContent sourceLink = new CtrlPlaneLinkContent();
        sourceLink.setId(source);
        sourceLink.setCapacity(bandwidth);
        sourceLink.setTrafficEngineeringMetric("1");
        sourceLink.setMaximumReservableCapacity(maxBandwidth);
        sourceLink.setMinimumReservableCapacity(minBandwidth);
        sourceLink.setRemoteLinkId(destination);
        CtrlPlaneSwcapContent swcap = new CtrlPlaneSwcapContent();
        swcap.setCapacity(bandwidth);
        swcap.setSwitchingcapType("l2sc");
        swcap.setEncodingType("ethernet");
        CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = new CtrlPlaneSwitchingCapabilitySpecificInfo();
        specInfo.setCapability(bandwidth);
        specInfo.setInterfaceMTU(9200); // ?
        specInfo.setVlanTranslation(true); // ?
        specInfo.setVlanRangeAvailability(srcVlan);
        specInfo.setSuggestedVLANRange(srcVlan);
        swcap.setSwitchingCapabilitySpecificInfo(specInfo);
        sourceLink.getSwitchingCapabilityDescriptors().add(swcap);
        sourceHop.setLink(sourceLink);
        pathHops.add(sourceHop);

        CtrlPlaneHopContent destHop = new CtrlPlaneHopContent();
        destHop.setId("destination");
        //destHop.setLinkIdRef(destination);
        CtrlPlaneLinkContent destLink = new CtrlPlaneLinkContent();
        destLink.setId(destination);
        destLink.setCapacity(bandwidth);
        destLink.setGranularity("0");
        destLink.setTrafficEngineeringMetric("1");
        destLink.setMaximumReservableCapacity(maxBandwidth);
        destLink.setMinimumReservableCapacity(minBandwidth);
        destLink.setRemoteLinkId(source);
        swcap = new CtrlPlaneSwcapContent();
        swcap.setCapacity(bandwidth);
        swcap.setSwitchingcapType("l2sc");
        swcap.setEncodingType("ethernet");
        specInfo = new CtrlPlaneSwitchingCapabilitySpecificInfo();
        specInfo.setCapability(bandwidth);
        specInfo.setInterfaceMTU(9200); // ?
        specInfo.setVlanTranslation(true); // ?
        specInfo.setVlanRangeAvailability(srcVlan);
        specInfo.setSuggestedVLANRange(srcVlan);
        swcap.setSwitchingCapabilitySpecificInfo(specInfo);
        destLink.getSwitchingCapabilityDescriptors().add(swcap);
        destHop.setLink(destLink);
        pathHops.add(destHop);

        return path;
    }

    private void assembleSchedules(HttpServletRequest request, List<Lifetime> scheduleList)
            throws OSCARSServiceException {
        String strParam = request.getParameter("hiddenTceSchedules");
        String tecScheudles;
        if ((strParam != null) && !strParam.trim().equals("")) {
            tecScheudles = strParam.trim();
        } else {
            throw new OSCARSServiceException("at least one time-window entry is required");
        }
        strParam = request.getParameter("tceDuration");
        String duration;
        if ((strParam != null) && !strParam.trim().equals("")) {
            duration = strParam.trim();
        } else {
            throw new OSCARSServiceException("duration is a required parameter");
        }        
        String schedules[] = tecScheudles.split(";");
        for (String schedule: schedules) {
            Pattern p = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})--(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})");
            Matcher matcher = p.matcher(schedule);
            if (!matcher.find()) {
                throw new OSCARSServiceException("invalid time-window format: " + schedule);
            }
            String start = matcher.group(1);
            String end = matcher.group(2);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                Date currentTime = new Date();
                Date startDate = formatter.parse(start);
                Date endDate = formatter.parse(end);    
                long tnow = currentTime.getTime()/1000;
                long tstart = startDate.getTime()/1000;
                long tend = endDate.getTime()/1000;
                start = Long.toString(tstart);
                end = Long.toString(tend);
                Lifetime lft = new Lifetime();
                TimeContent ts = new TimeContent();
                ts.setType("timestamp");
                ts.setValue(start);
                lft.setStart(ts);
                TimeContent te = new TimeContent();
                te.setType("timestamp");
                te.setValue(end);
                lft.setEnd(te);
                Duration du = new Duration();
                du.setType("timestamp");
                long tduration = 60;
                tduration = tduration*Long.valueOf(duration);
                du.setValue(Long.toString(tduration));
                lft.setDuration(du);
                scheduleList.add(lft);
                // verify
                if (tstart < tnow) {
                    throw new OSCARSServiceException("Start date/time has passed in time-window " + schedule);
                }
                if (tend <= tstart) {
                    throw new OSCARSServiceException("Start time must be earlier than end time in time-window " + schedule);
                }
                if ((tend - tstart) < tduration) {
                    throw new OSCARSServiceException("Requested duration " + duration + " (min) cannot be longer than time-window " + schedule);
                }
            
            } catch (Exception e) {
                throw new OSCARSServiceException(e.getMessage());
            }
            
        }
    }
}
