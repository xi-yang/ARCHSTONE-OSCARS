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
import net.sf.json.JSONObject;

import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.utils.clients.AuthNPolicyClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.VlanTag;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;

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
        CoordClient coordClient = core.getCoordClient();
        AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();

        UserSession userSession = new UserSession(core);
        CheckSessionReply sessionReply =
            userSession.checkSession(out, authNPolicyClient, request,
                                     methodName);
        if (sessionReply == null) {
            this.log.warn(netLogger.error(methodName, ErrSev.MINOR,"No user session: cookies invalid"));
            return;
        }

        // test code
        String flexibleBandwidth = "flexibleBandwidth=";
        flexibleBandwidth += request.getParameter("hiddenFlexibleBandwidth");
        flexibleBandwidth += " tceDestVlan=";
        flexibleBandwidth += request.getParameter("tceDestVlan");
        flexibleBandwidth += " tceScheules=";
        flexibleBandwidth += request.getParameter("hiddenTceSchedules");

        // TODO: poll tcePCE callbackHandler

        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        outputMap.put("status", "Submitted TCE query: " + flexibleBandwidth);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info(netLogger.end(methodName));
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }

}
