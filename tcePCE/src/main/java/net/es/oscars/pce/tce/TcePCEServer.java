package net.es.oscars.pce.tce;


import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.pce.PCEMessage;
import net.es.oscars.pce.SimplePCEServer;
import net.es.oscars.pce.soap.gen.v06.PCEDataContent;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.config.ConfigDefaults;

/**
 * Standalone server that reads in configuration file and publishes a 
 * Tce PCE service.
 * 
 * @author Andy Lake <andy@es.net>
 */
@OSCARSService (
        serviceName = ServiceNames.SVC_PCE_TCE,
        config = ConfigDefaults.CONFIG,
        implementor = "net.es.oscars.pce.tce.TcePCEProtocolHandler"
)
@OSCARSNetLoggerize(moduleName = "net.es.oscars.pce.Tce")
public class TcePCEServer extends SimplePCEServer{
    static private ContextConfig cc = ContextConfig.getInstance();
    
    final private String PROP_RM_URL = "rmUrl";
    final private String PROP_RM_WSDL = "rmWsdl";

    final private Logger log = Logger.getLogger(this.getClass());
    private TcePCE pce;
    private String moduleName;
    private Map config =  null;
    private Map rmConfig = null;
    
    public TcePCEServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_PCE_TCE);
        String rmUrl =  null;
        String rmWsdl = null;
        //get the name of the module from netLogger.Use annotation to be 
        //consistent with protocol handler even though variable would probably do
        this.moduleName = 
            this.getClass().getAnnotation(OSCARSNetLoggerize.class).moduleName();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        this.log.info(netLogger.start("init"));
        try{
            //String configAlias=
            //    this.getClass().getAnnotation(OSCARSService.class).config();
            //String configFilename = cc.getFilePath(configAlias);
            //Map config = ConfigHelper.getConfiguration(configFilename);

            String configFilename = cc.getFilePath(ConfigDefaults.CONFIG);
            this.log.debug("configFilename is " + configFilename);
            config = ConfigHelper.getConfiguration(configFilename);
            if(config.containsKey(PROP_RM_URL)){
                // if resourceManager service runs on a different, put the url in ConnectivityPCS's config.yaml
                rmUrl = (String) config.get(PROP_RM_URL);
            } 
            else { // if RM service runs on same host, get the url from RM Service's config.yaml
                configFilename = cc.getFilePath(ServiceNames.SVC_RM,cc.getContext(),
                        ConfigDefaults.CONFIG);
                rmConfig = (HashMap<String,Object>)ConfigHelper.getConfiguration(configFilename);
                Map soap = (HashMap<String,Object>) this.rmConfig.get("soap");
                if (soap == null ){
                    throw new ConfigException("soap stanza not found in resourceManager.yaml");
                }
                rmUrl = (String)soap.get("publishTo");
            }
            if (rmUrl == null ){
                this.log.error(netLogger.error("initialization",ErrSev.MAJOR, "unable to find resourceManager URL"));
                throw new OSCARSServiceException("unable to find resourceManager URL");
            }
            this.log.debug(netLogger.start("init","resourceManager running on " + rmUrl));
            if (config.containsKey(PROP_RM_WSDL)) {
                rmWsdl = (String) config.get(PROP_RM_WSDL);
            } else { // get it from manifest
                rmWsdl = "file:" + cc.getFilePath(ServiceNames.SVC_RM,cc.getContext(),
                        ConfigDefaults.WSDL);
            }
            this.log.debug(netLogger.end("init", "rmurl is " + rmUrl + " rmWsdl is " + rmWsdl));
            this.pce = new TcePCE(rmUrl, rmWsdl);
            this.log.info(netLogger.end("init"));
        }catch(Exception e){
            this.log.error(netLogger.error("init", ErrSev.CRITICAL, e.getMessage()));
            System.err.println(e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public PCEDataContent calculatePath(PCEMessage query) throws OSCARSServiceException{
        PCEDataContent pceData = null;
        OSCARSNetLogger netLogger = new OSCARSNetLogger(this.moduleName,query.getTransactionId());
        netLogger.setGRI(query.getGri());
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.debug(netLogger.start("calculatePath"));
        try{
            pceData = this.pce.calculatePath(query, netLogger);
            this.log.debug(netLogger.end("calculatePath"));
        }catch(OSCARSServiceException e){
            this.log.debug(netLogger.error("calculatePath", ErrSev.MAJOR, e.getMessage()));
            throw e;
        }catch(Exception e){
            this.log.debug(netLogger.error("calculatePath", ErrSev.MAJOR, e.getMessage()));
            throw new OSCARSServiceException(e);
        }
        
        return pceData;
    }
  
    /*
    public void communicateMXTCE(PCEMessage query) throws OSCARSServiceException{
        PCEDataContent pceData = null;
        OSCARSNetLogger netLogger = new OSCARSNetLogger(this.moduleName,query.getTransactionId());
        netLogger.setGRI(query.getGri());
        OSCARSNetLogger.setTlogger(netLogger);
        this.log.debug(netLogger.start("comToTce"));
        try{
            this.pce.communicateMXTCE(query, netLogger);
            this.log.debug(netLogger.end("comToTce"));
        }catch(OSCARSServiceException e){
            this.log.debug(netLogger.error("comToTce", ErrSev.MAJOR, e.getMessage()));
            throw e;
        }catch(Exception e){
            this.log.debug(netLogger.error("comToTce", ErrSev.MAJOR, e.getMessage()));
            throw new OSCARSServiceException(e);
        }
        
        //return pceData;
    }
    */
}
