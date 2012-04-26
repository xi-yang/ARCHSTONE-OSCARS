/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.oscars.pce.tce.client.test;

import java.net.URL;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.pce.soap.gen.v06.*;

import net.es.oscars.pce.tce.client.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBElement;

import org.ogf.schema.network.topology.ctrlplane.*;

import static java.util.Arrays.asList;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author xyang
 */

/**
 * 
 * To use the TCE API client:
 *  1. Set $OSCARS_HOME to your base configuration directory
 *  2. Copy PCERuntimeService directory to under $OSCARS_HOME
 *  3. In your code, host URL to "http://tcePCE-server-name-or-IP:9020/tcePCE"
 *  4. Handle reply data in derived TCECallbackHandler::handleReply
 * 
 */


public class TCETestClient {
    static String host = "localhost";
    static String port = "9020";
    static String yamlfile = "";
    static String topofile = "";
    static String optconsfile = "";

    public static void main(String args[]) {
        try {
            parseArgs(args);
            URL hostUrl = new URL("http://"+host+":"+port+"/tcePCE");
            URL wsdlUrl = new URL("file://"+System.getenv("OSCARS_HOME")+"/PCERuntimeService/api/pce-0.6.wsdl");
            TCEApiClient apiClient = TCEApiClient.getClient(hostUrl, wsdlUrl, "MyTCEClient");
            TCEExampleReplyHandler replyHandler = new TCEExampleReplyHandler();
            apiClient.initClient(replyHandler);
            String srcUrn = "urn:ogf:network:domain=testdomain-1.net:node=node-1:port=port-1:link=link-1";
            String dstUrn = "urn:ogf:network:domain=testdomain-1.net:node=node-3:port=port-4:link=link-1";
            String vlan = "any"; 
            String descr = "my test path computation run";
            String requestTopology = "<topology id=\"service-reply-seq-1234a\""
                    // must add the namespace
                    + " xmlns=\"http://ogf.org/schema/network/topology/ctrlPlane/20110826/\">"
                    // skip the acutal contents
                    + "</topology>";
            String optionalConstraint = "";
            if (!optconsfile.isEmpty()) {
                byte[] buffer = new byte[(int) new File(optconsfile).length()];
                FileInputStream f = new FileInputStream(optconsfile);
                f.read(buffer);
                optionalConstraint = new String(buffer);
                f.close();
            } else {
                optionalConstraint = "<coScheduleRequest id=\"schedule-123456789-option-1\">"
                    + "<startTime>_starttime_</startTime>"
                    + "<endTime>_endtime_</endTime>"
                    + "<minBandwidth>_bandwidth_</minBandwidth>"
                    + "<maxNumOfAltPaths>3</maxNumOfAltPaths>"
                    + "<bandwidthAvailabilityGraph>true</bandwidthAvailabilityGraph>"
                    + "<contiguousVlan>true</contiguousVlan>"
                    + "<requireLinkBAG>true</requireLinkBAG>"
                    + "</coScheduleRequest>";
                HashMap<String, Long> times = TCEApiClient.parseTimes("now", "+00:00:30");
                optionalConstraint = optionalConstraint.replaceAll("_starttime_", Long.toString(times.get("start")));
                optionalConstraint = optionalConstraint.replaceAll("_endtime_", Long.toString(times.get("end")));
                optionalConstraint = optionalConstraint.replaceAll("_bandwidth_", Integer.toString(100));                
            }

            PCEDataContent pceData;
            ResCreateContent reqData;
            String gri =  null;
            if (yamlfile.isEmpty()) {
                if (topofile.isEmpty()) {
                    pceData = apiClient.assemblePceData(srcUrn, dstUrn, 0, 1800, 100, vlan, optionalConstraint);
                } else {
                    byte[] buffer = new byte[(int) new File(topofile).length()];
                    FileInputStream f = new FileInputStream(topofile);
                    f.read(buffer);
                    requestTopology = new String(buffer);
                    f.close();
                    pceData = apiClient.assemblePceData(requestTopology, optionalConstraint);
                }
            } else {
                reqData = apiClient.configureRequstFromYaml(yamlfile, optionalConstraint);
                gri = reqData.getGlobalReservationId();
                pceData = new PCEDataContent();
                pceData.setUserRequestConstraint(reqData.getUserRequestConstraint());
                pceData.setReservedConstraint(reqData.getReservedConstraint());
                // unmarshalling reqTopo XML from topofile and JAXB to <topology>
                if (!topofile.isEmpty()) {
                    try {
                        File xmlFile = new File(topofile);
                        JAXBContext jc = JAXBContext.newInstance("org.ogf.schema.network.topology.ctrlplane");
                        Unmarshaller unm = jc.createUnmarshaller();
                        JAXBElement<CtrlPlaneTopologyContent> jaxbTopology = (JAXBElement<CtrlPlaneTopologyContent>) unm.unmarshal(xmlFile);
                        CtrlPlaneTopologyContent topology = jaxbTopology.getValue();
                        pceData.setTopology(topology);
                    } catch (Exception e) {
                        System.err.println("Error in unmarshling RequestTopology: " + e.getMessage());
                        throw e;
                    }
                }
            }
            apiClient.sendPceCreate(gri, pceData);                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void parseArgs(String args[])  throws java.io.IOException {

        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList( "?" ), "show help then exit" );
        OptionSpec<String> optHost = parser.accepts("h", "host: default='localhost'").withRequiredArg().ofType(String.class);
        OptionSpec<String> optPort = parser.accepts("p", "port: default='9020'").withRequiredArg().ofType(String.class);
        OptionSpec<String> optYamlFile = parser.accepts("y", "yaml config file path").withRequiredArg().ofType(String.class);
        OptionSpec<String> optTopoFile = parser.accepts("t", "request topology file path").withRequiredArg().ofType(String.class);
        OptionSpec<String> optOptConsFile = parser.accepts("o", "optional constraint file path").withRequiredArg().ofType(String.class);
        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) ) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        if (options.has(optHost) ){
            host = options.valueOf(optHost);
        } 
        if (options.has(optYamlFile) ){
            yamlfile = options.valueOf(optYamlFile);
        } 
        if (options.has(optTopoFile) ){
            topofile = options.valueOf(optTopoFile);
        } 
        if (options.has(optOptConsFile) ){
            optconsfile = options.valueOf(optOptConsFile);
        } 
   }
}
 
