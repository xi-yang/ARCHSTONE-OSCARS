package net.es.oscars.pss.eompls.test;

import java.util.ArrayList;
import java.util.HashMap;

import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.VlanTag;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;

public class RequestFactory {
    public static ResDetails getABC() {
        String gri = "twoHop-768";
        String srcEdge      = "urn:ogf:network:foo.net:alpha:xe-1/1/0:edge";
        String hop1Edge     = "urn:ogf:network:foo.net:alpha:xe-2/0/0:beta";
        String hop2Edge     = "urn:ogf:network:foo.net:beta:1/1/1:alpha";
        String hop3Edge     = "urn:ogf:network:foo.net:beta:2/1/1:gamma";
        String hop4Edge     = "urn:ogf:network:foo.net:gamma:TenGigabitEthernet0/1:beta";
        String dstEdge      = "urn:ogf:network:foo.net:gamma:TenGigabitEthernet3/0:edge";
        String description = "SC11-SCHEDULED: foo.bar 123";

        ArrayList<HashMap<String, String>> hops = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> srcHop = new HashMap<String, String>();
        srcHop.put("urn", srcEdge);
        srcHop.put("vlan", "999");
        HashMap<String, String> dstHop = new HashMap<String, String>();
        dstHop.put("urn", dstEdge);
        dstHop.put("vlan", "999");
        HashMap<String, String> hop1 = new HashMap<String, String>();
        hop1.put("urn", hop1Edge);
        hop1.put("vlan", "999");
        HashMap<String, String> hop2 = new HashMap<String, String>();
        hop2.put("urn", hop2Edge);
        hop2.put("vlan", "999");
        HashMap<String, String> hop3 = new HashMap<String, String>();
        hop1.put("urn", hop3Edge);
        hop1.put("vlan", "999");
        HashMap<String, String> hop4 = new HashMap<String, String>();
        hop1.put("urn", hop4Edge);
        hop1.put("vlan", "999");
        
        hops.add(srcHop);
        hops.add(hop1);
        hops.add(hop2);
        hops.add(hop3);
        hops.add(hop4);
        hops.add(dstHop);

        return makeResDetails(gri, description , hops, 100);   
    }
    public static ResDetails getAB() {
        String gri = "oneHop-311";
        String srcEdge      = "urn:ogf:network:foo.net:beta:1/1/3:edge";
        String hop1Edge     = "urn:ogf:network:foo.net:beta:2/1/1:gamma";
        String hop2Edge     = "urn:ogf:network:foo.net:gamma:TenGigabitEthernet0/1:beta";
        String dstEdge      = "urn:ogf:network:foo.net:gamma:TenGigabitEthernet3/0:edge";
        String description = "SC11-SCHEDULED: foo.bar 123";
        
        ArrayList<HashMap<String, String>> hops = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> srcHop = new HashMap<String, String>();
        srcHop.put("urn", srcEdge);
        srcHop.put("vlan", "817");
        HashMap<String, String> dstHop = new HashMap<String, String>();
        dstHop.put("urn", dstEdge);
        dstHop.put("vlan", "817");
        HashMap<String, String> hop1 = new HashMap<String, String>();
        hop1.put("urn", hop1Edge);
        hop1.put("vlan", "817");
        HashMap<String, String> hop2 = new HashMap<String, String>();
        hop2.put("urn", hop2Edge);
        hop2.put("vlan", "817");
        
        hops.add(srcHop);
        hops.add(hop1);
        hops.add(hop2);
        hops.add(dstHop);

        return makeResDetails(gri, description , hops, 100);   
    }
    
    public static ResDetails getCD() {
        String gri = "oneHopCD-311";
        String srcEdge  = "urn:ogf:network:foo.net:beta:1/1/3:edge";
        String hop1Edge = "urn:ogf:network:foo.net:beta:3/1/1:delta";
        String hop2Edge = "urn:ogf:network:foo.net:delta:1/1/1:beta";
        String dstEdge  = "urn:ogf:network:foo.net:delta:3/1/1:edge";
        String description = "SC11-SHARED: foo.bar 123";

        ArrayList<HashMap<String, String>> hops = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> srcHop = new HashMap<String, String>();
        srcHop.put("urn", srcEdge);
        srcHop.put("vlan", "999");
        HashMap<String, String> dstHop = new HashMap<String, String>();
        dstHop.put("urn", dstEdge);
        dstHop.put("vlan", "999");
        HashMap<String, String> hop1 = new HashMap<String, String>();
        hop1.put("urn", hop1Edge);
        hop1.put("vlan", "999");
        HashMap<String, String> hop2 = new HashMap<String, String>();
        hop2.put("urn", hop2Edge);
        hop2.put("vlan", "999");
        
        hops.add(srcHop);
        hops.add(hop1);
        hops.add(hop2);
        hops.add(dstHop);

        return makeResDetails(gri, description , hops, 100);
        
    }
    
    
    
    public static ResDetails getSameDevice() {
        String gri = "sameDev-333";
        String srcEdge = "urn:ogf:network:foo.net:alpha:xe-1/1/0:edge";
        String dstEdge = "urn:ogf:network:foo.net:alpha:xe-2/1/0:edge";
        ArrayList<HashMap<String, String>> hops = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> srcHop = new HashMap<String, String>();
        srcHop.put("urn", srcEdge);
        srcHop.put("vlan", "999");
        HashMap<String, String> dstHop = new HashMap<String, String>();
        dstHop.put("urn", dstEdge);
        dstHop.put("vlan", "999");
        hops.add(srcHop);
        hops.add(dstHop);
        String description = "same device";
        return makeResDetails(gri, description , hops, 100);
    }
    
    
    public static ResDetails makeResDetails(String gri, String description, ArrayList<HashMap<String, String>> hops, int bandwidth) {

        String aEdge = hops.get(0).get("urn");
        String zEdge = hops.get(hops.size()-1).get("urn");
        String aVlan = hops.get(0).get("vlan");
        String zVlan = hops.get(hops.size()-1).get("vlan");
        
        
        ResDetails resDet = new ResDetails();
        ReservedConstraintType rc = new ReservedConstraintType();
        CtrlPlanePathContent path           = new CtrlPlanePathContent();

        PathInfo pathInfo                   = new PathInfo();
        Layer2Info l2Info                   = new Layer2Info();
        VlanTag srcVlan                     = new VlanTag();
        VlanTag dstVlan                     = new VlanTag();
        
        l2Info.setSrcEndpoint(aEdge);
        l2Info.setDestEndpoint(zEdge);
        l2Info.setSrcVtag(srcVlan);
        l2Info.setDestVtag(dstVlan);
        pathInfo.setLayer2Info(l2Info);

        srcVlan.setTagged(true);
        srcVlan.setValue(aVlan);
        dstVlan.setTagged(true);
        dstVlan.setValue(zVlan);
        for (int i = 0; i < hops.size(); i++) {
            CtrlPlaneHopContent cpHop;
            String urn = hops.get(i).get("urn");
            if (i == 0 || i == hops.size() - 1) {
               String vlan = hops.get(i).get("vlan");
               cpHop          = RequestFactory.makeEdgeHop(urn, vlan);
            } else {
                cpHop          = RequestFactory.makeInternalHop(urn);
            }
            path.getHop().add(cpHop);
        }
        
        pathInfo.setPath(path);
        rc.setPathInfo(pathInfo);
        rc.setBandwidth(bandwidth);
        resDet.setReservedConstraint(rc);
        resDet.setDescription(description);
        resDet.setGlobalReservationId(gri);


        
        return resDet;                
    }
    
    
    public static CtrlPlaneHopContent makeInternalHop(String linkId) {
        CtrlPlaneHopContent hop          = new CtrlPlaneHopContent();
        CtrlPlaneLinkContent link        = new CtrlPlaneLinkContent();
        hop.setLinkIdRef(linkId);
        link.setId(linkId);
        hop.setLink(link);
        return hop;
        
    }
    
    public static CtrlPlaneHopContent makeEdgeHop(String linkId, String vlan) {
        
        CtrlPlaneHopContent hop          = new CtrlPlaneHopContent();
        
        CtrlPlaneLinkContent link        = new CtrlPlaneLinkContent();
        CtrlPlaneSwcapContent scp        = new CtrlPlaneSwcapContent();
        CtrlPlaneSwitchingCapabilitySpecificInfo ssi
                                         = new CtrlPlaneSwitchingCapabilitySpecificInfo();
        
        hop.setLinkIdRef(linkId);
        link.setId(linkId);
        ssi.setSuggestedVLANRange(vlan);
        ssi.setVlanRangeAvailability(vlan);
        if (link.getSwitchingCapabilityDescriptors().size() == 0) {
            scp.setSwitchingCapabilitySpecificInfo(ssi);
            link.getSwitchingCapabilityDescriptors().add(scp);
        } else {
           link.getSwitchingCapabilityDescriptors().get(0).setSwitchingCapabilitySpecificInfo(ssi);
        }

        hop.setLink(link);
        return hop;
    }
    
}
