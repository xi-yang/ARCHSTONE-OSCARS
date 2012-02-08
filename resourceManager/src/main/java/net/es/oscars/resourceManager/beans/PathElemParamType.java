package net.es.oscars.resourceManager.beans;

public class PathElemParamType {
    // Pre-ARCHSTONE backward compatible 
    public static final String ENCODING_TYPE = "encodingType";
    public static final String L2SC_VLAN_RANGE = "vlanRangeAvailability";
    public static final String L2SC_SUGGESTED_VLAN = "suggestedVlan";
    public static final String MPLS_VLAN_RANGE = "vlanRangeAvailability";
    public static final String MPLS_SUGGESTED_VLAN = "suggestedVlan";
    // The following are extended for ARCHSTONE
    // pscSpecificInfo
    public static final String INTERFACE_MTU = "interfaceMTU"; // also used by L2SC
    public static final String LSP_HIERARCHY = "lspHierarchy";
    // l2scSpecificInfo
    public static final String VLAN_RANGE_SET = "vlanRangeSet";
    public static final String SUGGESTED_VLAN_SET = "suggestedVlanSet";
    public static final String VLAN_TRANSLATION = "vlanTranslation";
    // tdmSpecificInfo
    public static final String TIMESLOT_RANGE_SET = "timeslotRangeSet";
    public static final String SUGGESTED_TIMESLOT_SET = "suggestedTimeslotSet";
    public static final String TSI_ENABLED = "tsiEnabled";
    public static final String VCAT_ENABLED = "vcatEnabled";
    // lscSpecificInfo
    public static final String WAVELENGTH_RANGE_SET = "wavelengthRangeSet";
    public static final String SUGGESTED_WAVELENGTH_SET = "wavelengthTimeslotSet";
    public static final String WAVELENGTH_CONVERSION = "wavelengthConversion";
    // openflowSpecificInfo
        //TDB
    // vendorSpecificInfo
    public static final String VENDOR_SPECIFIC_INFO = "vendorSpecificInfo"; // also used by ADJUST
    // adjustmentCapability
    public static final String LOWER_SWCAP = "lowerSwcap";
    public static final String LOWER_ENC_TYPE = "lowerEncType";
    public static final String UPPER_SWCAP = "upperSwcap";
    public static final String UPPER_ENC_TYPE = "upperEncType";
    public static final String MAX_ADJUST_CAPACITY = "maxAdjustCapacity";

    static public boolean isValid(String type){
        if(L2SC_VLAN_RANGE.equals(type)){
            return true;
        }else if(L2SC_SUGGESTED_VLAN.equals(type)){
            return true;
        }else if(MPLS_VLAN_RANGE.equals(type)){
            return true;
        }else if(MPLS_SUGGESTED_VLAN.equals(type)){
            return true;
        }else if(ENCODING_TYPE.equals(type)){
            return true;
        }else if(INTERFACE_MTU.equals(type)){
            return true;
        }else if(LSP_HIERARCHY.equals(type)){
            return true;
        }else if(VLAN_RANGE_SET.equals(type)){
            return true;
        }else if(SUGGESTED_VLAN_SET.equals(type)){
            return true;
        }else if(VLAN_TRANSLATION.equals(type)){
            return true;
        }else if(TIMESLOT_RANGE_SET.equals(type)){
            return true;
        }else if(SUGGESTED_TIMESLOT_SET.equals(type)){
            return true;
        }else if(TSI_ENABLED.equals(type)){
            return true;
        }else if(VCAT_ENABLED.equals(type)){
            return true;
        }else if(WAVELENGTH_RANGE_SET.equals(type)){
            return true;
        }else if(SUGGESTED_WAVELENGTH_SET.equals(type)){
            return true;
        }else if(WAVELENGTH_CONVERSION.equals(type)){
            return true;
        }else if(VENDOR_SPECIFIC_INFO.equals(type)){
            return true;
        }else if(LOWER_SWCAP.equals(type)){
            return true;
        }else if(LOWER_ENC_TYPE.equals(type)){
            return true;
        }else if(UPPER_SWCAP.equals(type)){
            return true;
        }else if(UPPER_ENC_TYPE.equals(type)){
            return true;
        }else if(MAX_ADJUST_CAPACITY.equals(type)){
            return true;
        }
        return false;
    }
}
