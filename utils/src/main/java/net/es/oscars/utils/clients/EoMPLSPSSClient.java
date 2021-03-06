package net.es.oscars.utils.clients;

import java.net.URL;
import java.net.MalformedURLException;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.logging.OSCARSNetLoggerize;
import net.es.oscars.pss.soap.gen.PSSPortType;
import net.es.oscars.pss.soap.gen.PSSService;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.svc.ServiceNames;
import org.apache.log4j.Logger;

/**
 *
 *
 * @author haniotak
 *
 */
@OSCARSNetLoggerize(moduleName = ModuleName.PSS_EOMPLS)
@OSCARSService (
        implementor = "net.es.oscars.pss.soap.gen.PSSService",
        namespace   = "http://oscars.es.net/OSCARS/pss",
        serviceName = ServiceNames.SVC_PSS_EOMPLS
)
public class EoMPLSPSSClient extends OSCARSSoapService<PSSService, PSSPortType>  {
    static private Logger LOG = Logger.getLogger(EoMPLSPSSClient.class);

    private EoMPLSPSSClient (URL host, URL wsdlFile) throws OSCARSServiceException {
        super (host, wsdlFile, PSSPortType.class);
    }

    static public EoMPLSPSSClient getClient (URL host, URL wsdl)
        throws MalformedURLException, OSCARSServiceException {

        ContextConfig cc = ContextConfig.getInstance();
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        try {
            if (cc.getContext() != null){
                String cxfClientPath = cc.getFilePath(cc.getServiceName(), ConfigDefaults.CXF_CLIENT);
                OSCARSSoapService.setSSLBusConfiguration(new URL("file:" + cxfClientPath));
            } else {
                throw new ConfigException("ContextConfig not initialized");
            }
        } catch (ConfigException e) {
            LOG.error(netLogger.error("EoMPLSPSSClient.getClient", ErrSev.MAJOR, " caughtException: " + e.getMessage()));
            e.printStackTrace();
            throw new OSCARSServiceException(e.getMessage());
        }
        EoMPLSPSSClient client = new EoMPLSPSSClient (host, wsdl);
        return client;
    }

}




