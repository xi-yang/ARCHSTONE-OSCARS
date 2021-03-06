package net.es.oscars.pce.tce;

public class CodeNumber {
	public static final byte ASN_LONG_LEN = (byte)0x80;
		
	public static final byte BOOLEAN = (byte)0x01;
	public static final byte INTEGER_NUM = (byte)0x02;
	public static final byte ENUMERATED_NUM = (byte)0x03;
	public static final byte FLOAT_NUM = (byte)0x04;
	public static final byte DOUBLE_NUM = (byte)0x05;
	public static final byte CHARACTER_STRING = (byte)0x06;
	
    public static final byte PCE_USERCONSTRAINT = (byte)0xF1;
    public static final byte PCE_RESVCONSTRAINT = (byte)0xF2;
    public static final byte PCE_OPTICONSTRAINT_COSCHEDULEREQ = (byte)0xF3;
    public static final byte PCE_MULTIPLE_PATH = (byte)0xF4;
        
    public static final byte PCE_REPLY = (byte)0xF8;
    public static final byte PCE_REGU_REPLY = (byte)0xF9;
    public static final byte PCE_OPTI_REPLY = (byte)0xFA;
    
	
	public static final byte PCE_GRI = (byte)0x81;
	public static final byte PCE_LOGIN = (byte)0x82;
	public static final byte PCE_LAYER = (byte)0x83;
	public static final byte PCE_SOURCE = (byte)0x84;
	public static final byte PCE_DESTINATION = (byte)0x85;
	public static final byte PCE_BANDWIDTH = (byte)0x86;
	public static final byte PCE_DESCRIPTION = (byte)0x87;
	public static final byte PCE_STARTTIME = (byte)0x88;
	public static final byte PCE_ENDTIME = (byte)0x89;
	public static final byte PCE_PATHSETUPMODEL = (byte)0x8A;
	public static final byte PCE_PATHTYPE = (byte)0x8B;
	public static final byte PCE_SRCVLAN = (byte)0x8C;
	public static final byte PCE_DESTVLAN = (byte)0x8D;
	public static final byte PCE_PATH = (byte)0x8E;
	public static final byte PCE_SRCIPPORT = (byte)0x8F;
	public static final byte PCE_DESTIPPORT = (byte)0x90;
	public static final byte PCE_L3_PROTOCOL = (byte)0x91;
	public static final byte PCE_L3_DSCP = (byte)0x92;
	public static final byte PCE_MPLS_BURSTLIMIT = (byte)0x93;
	public static final byte PCE_MPLS_LSPCLASS = (byte)0x94;

	
	public static final byte PCE_PATH_ID = (byte)0x95;
	public static final byte PCE_HOP_ID = (byte)0x96;
	public static final byte PCE_LINK_ID = (byte)0x97;
	public static final byte PCE_SWITCHINGCAPTYPE = (byte)0x98;
	public static final byte PCE_SWITCHINGENCTYPE = (byte)0x99;
	public static final byte PCE_SWITCHINGVLANRANGEAVAI = (byte)0x9A;
	public static final byte PCE_PATH_LENGTH = (byte)0x9B;
	public static final byte PCE_SWITCHINGVLANRANGESUGG = (byte)0x9C;
	public static final byte PCE_VLANTRANSLATION = (byte)0x9D;
	public static final byte PCE_SWITCHINGVLANRANGEASSI = (byte)0x9E;
	public static final byte PCE_CAPACITY = (byte)0x9F;
	public static final byte PCE_MTU = (byte)0xA0;
	public static final byte PCE_COMPUTE_ERROR = (byte)0xA1;
	public static final byte PCE_REMOTE_LINK = (byte)0xA2;
	public static final byte PCE_MAXRESVCAPACITY = (byte)0xA3;
	public static final byte PCE_MINRESVCAPACITY = (byte)0xA4;
	public static final byte PCE_GRANULARITY = (byte)0xA5;
	public static final byte PCE_TE_METRIC = (byte)0xA6;
	
	public static final byte PCE_OPT_COSCHEDREQID = (byte)0xA7;
	public static final byte PCE_OPT_COSCHREQ_STARTTIME = (byte)0xA8;
	public static final byte PCE_OPT_COSCHREQ_ENDTIME = (byte)0xA9;
	public static final byte PCE_OPT_COSCHREQ_MINBANDWIDTH = (byte)0xAA;
	public static final byte PCE_OPT_COSCHREQ_MAXNUMOFALTPATHS = (byte)0xAB;
	public static final byte PCE_OPT_COSCHREQ_BANDWIDTHAVAIGRAPH = (byte)0xAC;
	public static final byte PCE_OPT_COSCHREQ_CONTIGUOUSVLAN = (byte)0xAD;
	public static final byte PCE_OPT_COSCHREQ_MAXDURATION = (byte)0xAE;
	public static final byte PCE_OPT_COSCHREQ_MAXBANDWIDTH = (byte)0xAF;
	public static final byte PCE_OPT_COSCHREQ_DATASIZEBYTES = (byte)0xB0;
	
	public static final byte PCE_HOP_END_TAG = (byte)0xB1;
	public static final byte PCE_ALT_PATH_NUM = (byte)0xB2;
	public static final byte PCE_OPT_BAG_BANDWIDTH = (byte)0xB3;
	public static final byte PCE_OPT_BAG_STARTTIME = (byte)0xB4;
	public static final byte PCE_OPT_BAG_ENDTIME = (byte)0xB5;
	public static final byte PCE_PATH_END_TAG = (byte)0xB6;
	
	public static final byte PCE_CHANNELREPRESENTATION = (byte)0xB7;
	public static final byte PCE_SWITCHINGWAVELENAVAI = (byte)0xB8;
	public static final byte PCE_SWITCHINGWAVELENASSI = (byte)0xB9;
	public static final byte PCE_SWITCHINGWAVELENSUGG = (byte)0xBA;
	public static final byte PCE_WAVELENGTHCONVERSION = (byte)0xBB;

	public static final byte PCE_CONCATENATIONTYPE = (byte)0xBC;
	public static final byte PCE_SWITCHINGTIMESLOTAVAI = (byte)0xBD;
	public static final byte PCE_SWITCHINGTIMESLOTASSI = (byte)0xBE;
	public static final byte PCE_SWITCHINGTIMESLOTSUGG = (byte)0xBF;
	public static final byte PCE_TSIENABLED = (byte)0xC0;
	public static final byte PCE_VCATENABLED = (byte)0xC1;

	public static final byte PCE_VENDORSPECIFICINFO = (byte)0xC2;
	public static final byte PCE_IACD_START = (byte)0xC3;
	public static final byte PCE_LOWERLAYERSWITCHINGTYPE = (byte)0xC4;
	public static final byte PCE_LOWERLAYERENCODINGTYPE = (byte)0xC5;
	public static final byte PCE_UPPERLAYERSWITCHINGTYPE = (byte)0xC6;
	public static final byte PCE_UPPERLAYERENCODINGTYPE = (byte)0xC7;
	public static final byte PCE_MAXADAPTBANDWIDTH = (byte)0xC8;
	
	public static final byte PCE_NEXTHOP_LENGTH = (byte)0xC9;
	public static final byte PCE_NEXTHOP = (byte)0xCA;
	public static final byte PCE_LIFETIME_NUM = (byte)0xCB;
	public static final byte PCE_LIFETIME = (byte)0xCC;
	public static final byte PCE_LIFETIME_START = (byte)0xCD;
	public static final byte PCE_LIFETIME_END = (byte)0xCE;
	public static final byte PCE_LIFETIME_DUR = (byte)0xCF;
	
	public static final byte PCE_FLEX_ALT_PATH_NUM = (byte)0xD0;
	public static final byte PCE_OPT_BAG_INFO_NUM = (byte)0xD1;
	public static final byte PCE_OPT_BAG_SEG_NUM = (byte)0xD2;
	
	public static final byte PCE_OPT_REQ_LINK_BAG = (byte)0xD3;
	public static final byte PCE_OPT_BAG_TYPE = (byte)0xD4;
	public static final byte PCE_OPT_BAG_ID = (byte)0xD5;

	

}
