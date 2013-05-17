package org.esgf.srm;

import org.esgf.srm.scriptgen.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.esgf.datacart.DocElement;
import org.esgf.datacart.FileDownloadTemplateController;
import org.esgf.datacart.URLSElement;
import org.esgf.datacart.XmlFormatter;
import org.esgf.email.Email;
import org.esgf.email.Attachment;
import org.esgf.email.EmailUtils;
import org.esgf.filetransformer.SRMFileTransformationUtils;
import org.esgf.solr.model.Solr;
import org.esgf.solr.model.SolrRecord;
import org.esgf.solr.model.SolrResponse;
import org.esgf.srm.scriptgen.ScriptGeneratorFactory;
import org.esgf.srmcache.SRMCacheStore;
import org.esgf.srmcache.SRMCacheStoreFactory;
import org.esgf.srmcache.SRMEntry;
import org.esgf.srmworkflow.SRMWorkflow;
import org.esgf.srmworkflow.SRMWorkflowFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import esg.common.util.ESGFProperties;
import esg.node.security.UserInfo;
import esg.node.security.UserInfoCredentialedDAO;

// this is a turl -> gsiftp://esg2-sdnl1.ccs.ornl.gov//lustre/esgfs/SRM/shared/V.0.0-505553807/t341f02.FAMIPr.cam2.h0.1978-09.nc

@Controller
public class SRMProxyController {
    
    private static String MAX_LIMIT = "9999";

    private static final String DEFAULT_TYPE = "Dataset";

    private static String DB_TYPE = "postgres";

    
    
    //anything over 25 doesn't work ... must fix
    private static int NUM_FILES_LIMIT = 11125;

    private static boolean debugFlag = false;
    
    private static boolean printIDsFlag = false; 
    
    private static boolean isProduction = false;
    
    private static boolean cacheOn = false;
    
    private static boolean isBasicScript = true;
    
    private static String entry_list_file = "/esg/config/srm_entry_list_File.xml";
    
    private Email initialEmail;
    private Email confirmationEmail;
    private SRMResponse srm_response;
    
    
    
    private boolean srmproxydebugflag = true;
    private boolean emailTextflag = true;

    private static String SCRIPT_NAME = "wget";
    private static String SCRIPT_COMPLEXITY = "basic";
    
    public static int TEST_NUMFILE_LIMIT = 12;
    
    
    public static void main(String [] args) {
        
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        
       
        
        //what are the inputs?
        //- all inputs required for solr
        //- the "type" (Database/File)
        //- the openid
        //- the return type
        
        //scenario for dataset
        /*
        mockRequest.addParameter("dataset_id", SRMProxyControllerConstants.INPUT_DATASET_ID);
        mockRequest.addParameter("constraints", SRMProxyControllerConstants.INPUT_CONSTRAINTS);
        mockRequest.addParameter("file_id", SRMProxyControllerConstants.INPUT_DATASET_FILE_ID);
        mockRequest.addParameter("file_url", SRMProxyControllerConstants.INPUT_DATASET_FILE_URL);
        mockRequest.addParameter("open_id", SRMProxyControllerConstants.INPUT_OPEN_ID);
        mockRequest.addParameter("type", "File");
        
        System.out.println(mockRequest.getParameter("type"));
        //scenario for file
        */
        
        //scenario for file
        mockRequest.addParameter("dataset_id", "ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1|esg2-sdnl1.ccs.ornl.gov");//SRMProxyControllerConstants.INPUT_DATASET_ID);
        mockRequest.addParameter("constraints", SRMProxyControllerConstants.INPUT_CONSTRAINTS);
        mockRequest.addParameter("file_id", "ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1�FAMIPr.cam2.h0.1978-12.nc|esg2-sdnl1.ccs.ornl.gov");
        mockRequest.addParameter("file_url", "url=srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1978-10.nc");
        mockRequest.addParameter("open_id", SRMProxyControllerConstants.INPUT_OPEN_ID);
        mockRequest.addParameter("type", "Dataset");
        mockRequest.addParameter("scriptType", "WGET");
        
        
        SRMProxyController fc = new SRMProxyController();
        
        String response = fc.doPost(mockRequest, null);
        
        /*
        SRMResponse srm_response = new SRMResponse();
        srm_response.fromXML(response);
        
        System.out.println("resp: " + srm_response.getMessage());
        */
    }
    
    
    
    
    @RequestMapping(method=RequestMethod.POST, value="/srmproxy")
    public @ResponseBody String doPost(HttpServletRequest request,final HttpServletResponse response) {
        
        Enumeration<String> paramEnum = request.getParameterNames();
        
        
        
        System.out.println("In ESGF-WEB-FE SRMProxyController. HTTP POST: doPost");
        
        String responseStr = "";
        
        SRMControllerInputObj input = this.request2InputObj(request);
        
        String type = input.getType();
        String scriptType = input.getScriptType();
        String dataset_id = input.getDataset_id();
        String file_id = input.getFile_id();
        String srm_url = input.getFile_url();
        String constraints = input.getConstraints();
        String filtered = input.getFiltered();

        if(srmproxydebugflag) {
            System.out.println("---Input params to SRMProxy---");
            System.out.println("\tSRMController ScriptType: " + scriptType);
            System.out.println("\tSRMController Type: " + type);
            System.out.println("\tSRMController datasetid: " + dataset_id);
            System.out.println("\tSRMController fileid: " + file_id);
            System.out.println("\tSRMController srmurl: " + srm_url);
            System.out.println("\tSRMController constraints: " + constraints);
            System.out.println("\tSRMController filtered: " + filtered);
            System.out.println("---End Input params to SRMProxy---");
        }
        
        
        //get email address here (either from default or the oepnid)
        String openId = "jfharney";
        String emailAddr = EmailUtils.getEmailAddrFromOpenId(openId);
        

        
        String [] file_urls = null;
        String [] file_ids = null;
        String [] checksums = null;
        String [] checksumTypes = null;
        
        //choose flow between the two types
        // - if it is a dataset type, that means the user has selected srm requests for all files in a dataset
                     
        // - if it is a file type, that means the user has selected an individual file in a dataset
        if(type.equals("Dataset")) {

            //System.out.println("In type dataset");
            
            String query = "*";
            dataset_id = input.getDataset_id();
            
            //query solr and get the responses
            SolrResponse solrResponse = querySolr(query,dataset_id);
            
            int numFiles = NUM_FILES_LIMIT;
            
            if(numFiles > solrResponse.getSolrRecords().size()) {
                numFiles = solrResponse.getSolrRecords().size();
            }
            
            //System.out.println("\n\nNumFiles: " + numFiles + "\n\n");
            //System.exit(0);
            
            if(numFiles != 0) {

                file_ids = new String[numFiles];
                file_urls = new String[numFiles];
                checksums = new String[numFiles];
                checksumTypes = new String[numFiles];
            
                numFiles = TEST_NUMFILE_LIMIT;
                
                //grab the file_id, file_url, checksum, checksumType
                for(int i=0;i<numFiles;i++) {
                    SolrRecord solrRecord = solrResponse.getSolrRecords().get(i);
                    String solr_record_url = solrRecord.getArrField("url").get(0);
                    
                    //System.out.println("srm_url: " + solr_record_url);
                    
                    String url = solr_record_url.split("\\|")[0];
                    file_urls[i] = url;
                
                    file_id = solrRecord.getStrField("id");
                    file_ids[i] = file_id;

                    String solr_record_checksum = null;
                    String solr_record_checksum_type = null;
                    if(solrRecord.getArrField("checksum") == null) {
                        solr_record_checksum = "null";
                        solr_record_checksum_type = "null";
                    } else {
                        solr_record_checksum = solrRecord.getArrField("checksum").get(0);
                        solr_record_checksum_type = solrRecord.getArrField("checksum_type").get(0);
                    }
                    
                    checksums[i] = solr_record_checksum;
                    checksumTypes[i] = solr_record_checksum_type;
                    
                }
                //should end the if else here
                
                
                
            
            }
            
            
        } else {
            
            file_urls = new String[1];
            
            file_ids = new String[1];
            
            String file_url = input.getFile_url();
            if(file_url == null) {
                file_url = SRMProxyControllerConstants.INPUT_FILE_FILE_URL;
            }
            
            file_urls[0] = file_url;
            
            file_id = input.getFile_id();
            if(file_id == null) {
                file_id = SRMProxyControllerConstants.INPUT_FILE_FILE_ID;
            }
            
            file_ids[0] = file_id;
            
            checksums = new String[1];
            checksumTypes = new String[1];
        
            //revisit (obviously)
            checksums[0] = "null";
            checksumTypes[0] = "null";
            
        }
        
        if(srmproxydebugflag) {
            System.out.println("DATASET ID: " + dataset_id);
            System.out.println("FILE ID: " + file_ids[0] + "\n");
            System.out.println("FILE URL: " + file_urls[0] + "\n");
        }

        
        //send initial email here
        writeInitialEmail(file_urls,emailAddr);
       

        
        //execute bestman here
        SRMWorkflowFactory srm_workflow_factory = new SRMWorkflowFactory();
        SRMWorkflow srm_workflow_engine = srm_workflow_factory.makeSRMWorkflow("Production");
        SRMResponse srm_response = srm_workflow_engine.runWorkFlow(file_urls);
        String respMessage = srm_response.getMessage();
        String [] outputFiles = srm_response.getResponse_urls();

        System.out.println("Response Message: " + respMessage);
        for(int i=0;i<outputFiles.length;i++) {
            System.out.println("output file: " + i + " " + outputFiles[i]);
        }
        
        
        String timeStamp = Long.toString(System.currentTimeMillis());
        long expirationLong = Long.parseLong(timeStamp) + SRMControls.expiration;
        String expiration = Long.toString(expirationLong);
        
        System.out.println("---SRM Cache Params---");
        System.out.println("\ttimeStamp: " + timeStamp);
        System.out.println("\texpiration: " + expiration);
        System.out.println("---End SRM Cache Params---");
        
        
        //update the expiration times of the files in the srm_cache
        if(type.equals("Dataset")) {
            SRMCacheStoreFactory srmCacheStoreFactory = new SRMCacheStoreFactory();
            
            SRMCacheStore srm_cache = srmCacheStoreFactory.makeSRMCacheStore(DB_TYPE); 

            srm_cache.updateAllSRMEntriesForDatasetId(dataset_id);
            
        } else {
            for(int i=0;i<file_ids.length;i++) {
                SRMCacheStoreFactory srmCacheStoreFactory = new SRMCacheStoreFactory();
                
                SRMCacheStore srm_cache = srmCacheStoreFactory.makeSRMCacheStore(DB_TYPE); 
                
                
                //SRMEntry srm_entry = new SRMEntry(file_ids[i],dataset_id,"N/A",timeStamp,expiration,"openid");
                SRMEntry srm_entry = new SRMEntry(file_ids[i],dataset_id,timeStamp,expiration);
                
                srm_cache.updateSRMEntry(srm_entry);
            }
        }
        
        

        
        //execute script generator here
        org.esgf.srm.scriptgen.ScriptGenerator scriptGenerator = null;
        
        ScriptGeneratorFactory scriptGeneratorFactory = new ScriptGeneratorFactory();

        //scriptType = "wget";
        System.out.println("---ScriptGen Params---");
        System.out.println("\tScriptType: " + scriptType);
        System.out.println("\tScriptComplexity: " + SCRIPT_COMPLEXITY);
        System.out.println("---End ScriptGen Params---");
        
        
        //fix -> scriptType = WGET/GUC, SCRIPTTYPE=basic,complex
        scriptGenerator = scriptGeneratorFactory.makeScriptGenerator(scriptType,SCRIPT_COMPLEXITY);

        String script = null;
        
        
        if(scriptType.equalsIgnoreCase("wget")) {
            
            System.out.println("Generating wget ");
            
            if(SCRIPT_COMPLEXITY.equals("basic")) {
                ((BasicWgetScriptGenerator) scriptGenerator).setFileStr(outputFiles);
                script = scriptGenerator.generateScript();
                
            } else {
                String message = "message";
                String userOpenId = "userOpenId";
                String hostName = "hostName";
                String searchUrl = "searchUrl";
                String date = "date";
                ((ComplexWgetScriptGenerator) scriptGenerator).setFileStr(outputFiles,checksums,checksumTypes);
                ((ComplexWgetScriptGenerator) scriptGenerator).setMessage(message);
                ((ComplexWgetScriptGenerator) scriptGenerator).setUserOpenId(userOpenId);
                ((ComplexWgetScriptGenerator) scriptGenerator).setHostName(hostName);
                ((ComplexWgetScriptGenerator) scriptGenerator).setSearchUrl(searchUrl);
                ((ComplexWgetScriptGenerator) scriptGenerator).setDate(date);
                script = scriptGenerator.generateScript();

            }
        } else {
            if(SCRIPT_COMPLEXITY.equals("basic")) {
                
                ((BasicGlobusUrlCopyScriptGenerator) scriptGenerator).setFileStr(outputFiles);
                script = scriptGenerator.generateScript();
                
            } else {
                
                String message = "message";
                String userOpenId = "userOpenId";
                String hostName = "hostName";
                String searchUrl = "searchUrl";
                String date = "date";
                ((ComplexGlobusUrlCopyScriptGenerator) scriptGenerator).setFileStr(outputFiles,checksums,checksumTypes);
                ((ComplexGlobusUrlCopyScriptGenerator) scriptGenerator).setMessage(message);
                ((ComplexGlobusUrlCopyScriptGenerator) scriptGenerator).setUserOpenId(userOpenId);
                ((ComplexGlobusUrlCopyScriptGenerator) scriptGenerator).setHostName(hostName);
                ((ComplexGlobusUrlCopyScriptGenerator) scriptGenerator).setSearchUrl(searchUrl);
                ((ComplexGlobusUrlCopyScriptGenerator) scriptGenerator).setDate(date);
                script = scriptGenerator.generateScript();
                
            }
        }
        
        System.out.println("Script: " + script);



       
        
        
        
        //send resulting email here
        this.confirmationEmail = new Email();
        this.confirmationEmail.setTo(emailAddr);
        Attachment attachment = new Attachment();
        
        
        
        if(!scriptType.equals("wget")) {
            attachment.setAttachmentName("globus-url-copy.sh");
        } else {
            attachment.setAttachmentName("wget.sh");
        }
        
        attachment.setAttachmentContent(script);
        
        this.confirmationEmail.setAttachment(attachment);
        this.confirmationEmail.setHeaderText("Your request for data has been successfully staged!");
        
        //assemble the body text here
        String bodyStr = "";
        bodyStr += "Your request for data has been successfully staged!\n";
        

        if(type.equals("Dataset")) {
            bodyStr += "\nPlease navigate to the following URL:\n" + 
                    "http://localhost:8080/esgf-web-fe/live?tab=datacart&override=true" +
                    "&datasetid=" + dataset_id +
                    "\n";
        } 
        
        bodyStr += "The data you requested was the following:\n";
        for(int i=0;i<file_urls.length;i++) {
            bodyStr += "\t" + file_urls[i] + "\n";
        }
        
        
        this.confirmationEmail.setBodyText(bodyStr);

        String fileName = "/esg/config/Comfirmation_" + scriptType + ".txt";
        this.confirmationEmail.toFile(fileName);
        
        if(emailTextflag) {
            System.out.println(this.confirmationEmail.toString());
        }

        System.exit(0);
        
        //this.confirmationEmail.sendEmail();
        
        
        
        
        return "";
    }
    
    
    
    
    
    private SRMControllerInputObj request2InputObj(HttpServletRequest request) {
        
        SRMControllerInputObj inputObj = new SRMControllerInputObj();
        
        String type = request.getParameter("type");
        if(type == null) {
            type = "File";
        }
        
        String dataset_id = request.getParameter("dataset_id");
        if(dataset_id == null) {
            dataset_id = "ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1|esg2-sdnl1.ccs.ornl.gov";
        }
        
        String filtered = request.getParameter("filtered");
        if(filtered == null) {
            filtered = SRMProxyControllerConstants.INPUT_FILTERED;
        }
        
        String constraints = request.getParameter("constraints");
        if(constraints == null) {
            constraints = "query=*";
        }

        String file_url = request.getParameter("file_url");
        if(file_url == null) {
            file_url = SRMProxyControllerConstants.INPUT_FILE_FILE_URL;
        }

        String file_id = request.getParameter("file_id");
        if(file_id == null) {
            file_id = SRMProxyControllerConstants.INPUT_FILE_FILE_ID;
        }

        String scriptType = request.getParameter("scriptType");
        if(scriptType == null) {
            scriptType = SRMProxyControllerConstants.INPUT_SCRIPT_TYPE;
        }
        
        
        inputObj.setDataset_id(dataset_id);
        inputObj.setType(type);
        inputObj.setFile_id(file_id);
        inputObj.setFile_url(file_url);
        inputObj.setFiltered(filtered);
        inputObj.setConstraints(constraints);
        inputObj.setScriptType(scriptType);
        
        return inputObj;
    }
    
    
    
    @RequestMapping(method=RequestMethod.POST, value="/srmfilesrequest")
    //public ModelAndView addEmployee(@RequestBody String body) {
    public @ResponseBody List<String> doSRMFilesRequest(HttpServletRequest request,final HttpServletResponse response) {
        
        List<String> files = new ArrayList<String>();
        
        SRMControllerInputObj input = this.request2InputObj(request);
        
        
        
        if(input.getType().equals("Dataset")) {
            
            String dataset_id = input.getDataset_id();
            
            String constraints = input.getConstraints();
            String query = "*";
            if(constraints != null) {
                if(constraints.contains("query=")) {
                    String [] facets = constraints.split(";");
                    for(int i=0;i<facets.length;i++) {
                        if(facets[i].contains("query=")) {
                            String queryValue = facets[i].split("=")[1];
                            query = queryValue;
                        }
                    }
                    
                }
            }
            
            //query solr and get the responses
            SolrResponse solrResponse = querySolr(query,dataset_id);
            
            List<SolrRecord> solrRecords = solrResponse.getSolrRecords();
            
            for(int i=0;i<solrRecords.size();i++) {
                String file = solrRecords.get(i).getStrField("id");
                files.add(file);
            }
            
            
        } else {

            System.out.println("File");
            
            String file_id = request.getParameter("file_id");
            if(file_id == null) {
                file_id = SRMProxyControllerConstants.INPUT_FILE_FILE_ID;
            }

            files.add(file_id);
            
        }
       
        return files;
    }
    
    /*
    private ScriptGenerator ScriptGenerator(String[] response_urls,
            String[] response_urls2, String[] response_urls3,
            String scriptType, boolean basic, String hostName, String message,
            String date, String searchUrl, String userOpenId) {
        // TODO Auto-generated method stub
        return null;
    }
    */
    
    //requires 2 calls to solr
    //1 - Dataset core for the total count (optional)
    //2 - File core for the files
    private SolrResponse querySolr(String query,String dataset_id) {
    
        System.out.println("Querying solr...");
        
        Solr solr = new Solr();
        
        
        //get the total count first
        solr.addConstraint("query", query);
        solr.addConstraint("dataset_id",dataset_id);
        solr.addConstraint("type", "File");
        solr.addConstraint("limit", MAX_LIMIT);
        //to take out later
        solr.addConstraint("distrib", "false");
        
        solr.executeQuery();
        
        SolrResponse solrResponse = solr.getSolrResponse();
        
        System.out.println("Done Querying solr...");
        
        return solrResponse;
        
    }
    
    
    
    
    
    
    public static String [] getSolrParams(DocElement doc, String which) {
        String [] file_ids = new String[doc.getFileElements().size()];
        String [] file_urls = new String[doc.getFileElements().size()];
        String [] checksums = new String[doc.getFileElements().size()];
        String [] checksumTypes = new String[doc.getFileElements().size()];
        
        for(int i=0;i<doc.getFileElements().size();i++) {
            
            file_ids[i] = doc.getFileElements().get(i).getFileId();
            checksums[i] = doc.getFileElements().get(i).getChecksum();
            checksumTypes[i] = doc.getFileElements().get(i).getChecksum_type();
            URLSElement urlsElement = doc.getFileElements().get(i).getURLSElement();
            for(int j=0;j<urlsElement.getUrls().size();j++) {
                if(urlsElement.getUrls().get(j).contains("srm://")) { 
                    file_urls[i] = urlsElement.getUrls().get(j);
                    if(printIDsFlag) {
                       System.out.println("\t" + file_urls[i]);
                    } 
                    
                }
            }
        }
        
        if(which.equals("ids")) {
            return file_ids;
        } else if(which.equals("checksums")) {
            return checksums;
        } else if(which.equals("checksumTypes")) {
            return checksumTypes;
        } else {
            return file_urls;
        }
        
    }
    
    
    
    public void writeInitialEmail(String [] file_urls,String emailAddr) {
        this.initialEmail = new Email();
        this.initialEmail.setTo(emailAddr);
        Attachment attachment1 = new Attachment();
        //attachment1.setAttachmentName("wget.sh");
        //attachment1.setAttachmentContent("New wget content");
        this.initialEmail.setAttachment(attachment1);
        this.initialEmail.setHeaderText("Your request for data has been submitted.");
        
        String bodyStr = "";
        bodyStr += "Your data request for the following files:\n";
        for(int i=0;i<file_urls.length;i++) {
            bodyStr += "\t" + file_urls[i] + "\n";
        }
        bodyStr += "\nHas been submitted.  Please note that it may take some time to extract the data off tertiary storage and onto the local filesystem.  A confirmation email will be sent to this address soon with instructions on how to access this data.";
        this.initialEmail.setBodyText(bodyStr);
    }
    
}


/*
public void writeConfirmationEmail(String returnType,
                                   String openid,
                                   String [] file_urls,
                                   String [] checksums,
                                   String [] checksum,
                                   String emailAddr) {
    
    //writeConfirmationEmail("http",null,file_urls,null,null,emailAddr);
    //System.out.println("returnType: " + returnType);
    
    this.confirmationEmail = new Email();
    this.confirmationEmail.setTo(emailAddr);
    
    //assemble the header text here
    this.confirmationEmail.setHeaderText("Your request for data has been successfully staged!");
    
    //assemble the body text here
    String bodyStr = "";
    bodyStr += "Your request for data has been successfully staged!\n";
    bodyStr += "The data you requested was the following:\n";
    for(int i=0;i<file_urls.length;i++) {
        bodyStr += "\t" + file_urls[i] + "\n";
    }
    
    //assemble the attached script
    Attachment attachment = new Attachment();
    
    returnType = "http";
    
    if(returnType.equals("http")) {
        bodyStr += "\nAttached is a wget get script that may be run on any shell.\n";

        file_urls = SRMUtils.gridftp2httpArr(file_urls);
        
        for(int i=0;i<file_urls.length;i++) {
            System.out.println("file_url: " + i + " " + file_urls[i]);
        }
        
        attachment.setAttachmentName("wgetscript.sh");
        

        //run the script generator
        ScriptGenerator scriptGenerator = new ScriptGenerator();
       
        
    } else if(returnType.equals("gridftp")) {
        bodyStr += "\nAttached is a globus url copy script that may be run on any shell.\n";

        attachment.setAttachmentName("globusurlcopyscript.sh");
        
        for(int i=0;i<srm_response.getResponse_urls().length;i++) {
            System.out.println("srm_response.getResponse_urls(): " + srm_response.getResponse_urls()[i]);
        }
        
        //run the script generator
        ScriptGenerator scriptGenerator = new ScriptGenerator();
        
        
        
        
    }
    
    
    
   
}
*/



/* OLD
//type
String type = request.getParameter("type");
if(type == null) {
    type = SRMProxyControllerConstants.INPUT_TYPE_FILE;
}
        
System.out.println("TYPE: " + type + "\n\n\n") ;
if(type.equals("Dataset")) {
    
    
    String dataset_id = request.getParameter("dataset_id");
    if(dataset_id == null) {
        dataset_id = "ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1|esg2-sdnl1.ccs.ornl.gov";
    }
    
    
    
    //constraints/query
    String constraints = request.getParameter("constraints");
    String query = "*";
    if(constraints != null) {
        System.out.println("Constraints: " + constraints);
        if(constraints.contains("query=")) {
            String [] facets = constraints.split(";");
            for(int i=0;i<facets.length;i++) {
                if(facets[i].contains("query=")) {
                    String queryValue = facets[i].split("=")[1];
                    query = queryValue;
                }
            }
            
        }
    }
    
    
    
    
    
    
    //query solr and get the responses
    SolrResponse solrResponse = querySolr(query,dataset_id);
    
    
    int numFiles = NUM_FILES_LIMIT;
    
    if(numFiles > solrResponse.getSolrRecords().size()) {
        numFiles = solrResponse.getSolrRecords().size();
    }
    
    if(numFiles != 0) {
        
        String [] file_urls = new String[numFiles];
        
        String [] file_ids = new String[numFiles];
        
        
        
        //for(int i=0;i<solrResponse.getSolrRecords().size();i++) {
        for(int i=0;i<numFiles;i++) {
            SolrRecord solrRecord = solrResponse.getSolrRecords().get(i);
            String solr_record_url = solrRecord.getArrField("url").get(0);
            
            //System.out.println("srm_url: " + solr_record_url);
            
            String url = solr_record_url.split("\\|")[0];
            file_urls[i] = url;
         
            String file_id = solrRecord.getStrField("id");
            file_ids[i] = file_id;
        }
        
        
        System.out.println("\nSending initial email...\n");
        String emailAddr = SRMProxyControllerConstants.DEFAULT_EMAIL_ADDR;
        writeInitialEmail(file_urls,emailAddr);
        
        if(isProduction) {
            this.initialEmail.sendEmail();
        } else {
            System.out.println(this.initialEmail.toString());
        }
        
        
        SRMEntryList srm_list = new SRMEntryList();
        
        if(cacheOn) {
            System.out.println("Reading in entry list");
            srm_list.fromFile("/esg/config/srm_entry_list_File.xml");
            System.out.println("Current cache state for ids");
            for(int i=0;i<file_ids.length;i++) {
                System.out.println("file id: " + file_ids[i] + " " + srm_list.isCached(file_ids[i]));
            }
        }
        
        
        
        //issue the request to the srm and get response
        this.srm_response = new SRMResponse();
        if(file_urls != null) {
            responseStr = queryESGSRM(file_urls);
        }
        this.srm_response.fromXML(responseStr);

        
        
        
        
        //cache these file entries
        if(cacheOn) {
            for(int i=0;i<file_ids.length;i++) {
                srm_list.changeCached(file_ids[i], "true");
            }
            //System.out.println(new XmlFormatter().format(srm_list.toXML()));
            srm_list.toFile("/esg/config/srm_entry_list_File.xml");
        }

        System.out.println(new XmlFormatter().format(srm_response.toXML()) + "\n");
        
        writeConfirmationEmail("http",null,file_urls,null,null,emailAddr);
                
        System.out.println("\nSending confirmation email...\n");
        
        if(isProduction) {
            this.confirmationEmail.sendEmail();
        } else {
            System.out.println(this.confirmationEmail.toString());
        }
    } else {
        //there are no files
        
        
    }
    
    
    
    
} else {
    
    System.out.println("In type file");
    
    //files workflow
    
    String [] file_urls = new String[1];
    
    String [] file_ids = new String[1];
    
    String file_url = request.getParameter("file_url");
    if(file_url == null) {
        file_url = SRMProxyControllerConstants.INPUT_FILE_FILE_URL;
    }
    
    file_urls[0] = file_url;
    
    String file_id = request.getParameter("file_id");
    if(file_id == null) {
        file_id = SRMProxyControllerConstants.INPUT_FILE_FILE_ID;
    }
    
    file_ids[0] = file_id;
    
    
    System.out.println("\nSending initial email...\n");
    
    SRMEntryList srm_list = new SRMEntryList();
    
    if(cacheOn) {
        System.out.println("Reading in entry list");
        srm_list.fromFile("/esg/config/srm_entry_list_File.xml");
        System.out.println("Current cache state for ids");
        for(int i=0;i<file_ids.length;i++) {
            System.out.println("file id: " + file_ids[i] + " " + srm_list.isCached(file_ids[i]));
        }
    }
    
    //issue the request to the srm and get response
    this.srm_response = new SRMResponse();
    if(file_urls != null) {
        responseStr = queryESGSRM(file_urls);
    }
    this.srm_response.fromXML(responseStr);

    System.out.println(new XmlFormatter().format(srm_response.toXML()) + "\n");
    
    
    
    //String emailAddr = SRMProxyControllerConstants.DEFAULT_EMAIL_ADDR;
    //writeInitialEmail(file_urls,emailAddr);
    
    //if(isProduction) {
    //    this.initialEmail.sendEma0il();
    //} else {
    //    System.out.println(this.initialEmail.toString());
    //}
    
    
    //SRMEntryList srm_list = new SRMEntryList();
    
    //if(cacheOn) {
    //    System.out.println("Reading in entry list");
    //    srm_list.fromFile("/esg/config/srm_entry_list_File.xml");
    //    System.out.println("Current cache state for ids");
    //    for(int i=0;i<file_ids.length;i++) {
    //        System.out.println("file id: " + file_ids[i] + " " + srm_list.isCached(file_ids[i]));
    //    }
    //}
    
    
    
    //issue the request to the srm and get response
    //this.srm_response = new SRMResponse();
    //if(file_urls != null) {
    //    responseStr = queryESGSRM(file_urls);
    //}
    //this.srm_response.fromXML(responseStr);

    
    
    
    
    //cache these file entries
    //if(cacheOn) {
    //    for(int i=0;i<file_ids.length;i++) {
    //        srm_list.changeCached(file_ids[i], "true");
    //    }
    //    //System.out.println(new XmlFormatter().format(srm_list.toXML()));
    //    srm_list.toFile("/esg/config/srm_entry_list_File.xml");
    //}

    //System.out.println(new XmlFormatter().format(srm_response.toXML()) + "\n");
    
    //writeConfirmationEmail("http",null,file_urls,null,null,emailAddr);
            
    //System.out.println("\nSending confirmation email...\n");
    
    //if(isProduction) {
    //    this.confirmationEmail.sendEmail();
    //} else {
    //    System.out.println(this.confirmationEmail.toString());
    //}
    
} //end if/else dataset or file


*/


