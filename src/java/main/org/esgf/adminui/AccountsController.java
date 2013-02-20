/*****************************************************************************
 * Copyright � 2011 , UT-Battelle, LLC All rights reserved
 *
 * OPEN SOURCE LICENSE
 *
 * Subject to the conditions of this License, UT-Battelle, LLC (the
 * �Licensor�) hereby grants to any person (the �Licensee�) obtaining a copy
 * of this software and associated documentation files (the "Software"), a
 * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
 * copy, modify, merge, publish, distribute, and/or sublicense copies of the
 * Software.
 *
 * 1. Redistributions of Software must retain the above open source license
 * grant, copyright and license notices, this list of conditions, and the
 * disclaimer listed below.  Changes or modifications to, or derivative works
 * of the Software must be noted with comments and the contributor and
 * organization�s name.  If the Software is protected by a proprietary
 * trademark owned by Licensor or the Department of Energy, then derivative
 * works of the Software may not be distributed using the trademark without
 * the prior written approval of the trademark owner.
 *
 * 2. Neither the names of Licensor nor the Department of Energy may be used
 * to endorse or promote products derived from this Software without their
 * specific prior written permission.
 *
 * 3. The Software, with or without modification, must include the following
 * acknowledgment:
 *
 *    "This product includes software produced by UT-Battelle, LLC under
 *    Contract No. DE-AC05-00OR22725 with the Department of Energy.�
 *
 * 4. Licensee is authorized to commercialize its derivative works of the
 * Software.  All derivative works of the Software must include paragraphs 1,
 * 2, and 3 above, and the DISCLAIMER below.
 *
 *
 * DISCLAIMER
 *
 * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
 * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
 * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
 * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
 * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
 * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
 * liabilities, penalties, fines, claims, causes of action, and costs and
 * expenses, caused by, resulting from or arising out of, in whole or in part
 * the use, storage or disposal of the SOFTWARE.
 *
 *
 ******************************************************************************/


/**
 * On request mapping:
 *
 *     url rewrite filter will take over first, then we do regular Spring mapping.
 *     RedirectView is discouraged here as it will mess up the current rewrite
 *     rule, use "redirect:" prefix instead, and it is regarded as a better alternative
 *     anyway.
 *
 * For any redirect trouble, please refers to ROOT/urlrewrite.xml
 *
 * @author Feiyi Wang (fwang2@ornl.gov)
 *
 */
package org.esgf.adminui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.DataInputStream;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import org.esgf.commonui.UserOperationsESGFDBImpl;
import org.esgf.commonui.UserOperationsInterface;
import org.esgf.commonui.UserOperationsXMLImpl;
import org.esgf.commonui.Utils;

import esg.security.attr.service.api.FederatedAttributeService;

@Controller
@RequestMapping(value="/accountsview")

public class AccountsController {

    private final static String ACCOUNTS_INPUT = "accounts_input";
    private final static String ACCOUNTS_USERINFO = "accounts_userinfo";
    private final static String ACCOUNTS_GROUPINFO = "accounts_groupinfo";
    private final static String ACCOUNTS_ROLEINFO = "accounts_roleinfo";
    private final static String ACCOUNTS_MODEL = "accounts_model";
    private final static String ACCOUNTS_ERROR = "accounts_error";

    private final static Logger LOG = Logger.getLogger(AccountsController.class);
    private UserOperationsInterface uoi;
    private FederatedAttributeService fas;
    private String openId;

    //private final static String USERS_FILE = "C:\\Users\\8xo\\esgProjects\\esgf-6-29\\esgf-web-fe\\esgf-web-fe\\src\\java\\main\\users.file";

    private final static boolean debugFlag = true;
    ///**
    // * List of invalid text characters -
    // * anything that is not within square brackets.
    // */
    //private static Pattern pattern =
    //    Pattern.compile(".*[^a-zA-Z0-9_\\-\\.\\@\\'\\:\\;\\,\\s/()].*");
    @Autowired
    public AccountsController(FederatedAttributeService fas) throws FileNotFoundException, IOException {
        //Thanks Spring for doing basic programming for me ...
        this.fas = fas;

        //System.out.println("In accounts controller");
        LOG.debug("IN AccountsController Constructor");
        if(Utils.environmentSwitch) {
            //System.out.println("In accounts controller db");
            uoi = new UserOperationsESGFDBImpl();
        }
        else {
            //System.out.println("In accounts controller xml");
            uoi = new UserOperationsXMLImpl();
        }
        //System.out.println("End accounts controller");
    }

    /**
     * Method invoked in response to a GET request:
     * -) if invoked directly, a new set of facets is retrieved (but no results)
     * -) if invoked in response to a POST-REDIRECT,
     * @param request
     * @param input
     * @param result
     * @return
     * @throws IOException 
     * @throws JDOMException 
     * @throws Exception
     */
    //@SuppressWarnings("unchecked")
    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView doGet(final HttpServletRequest request,
            final @ModelAttribute(ACCOUNTS_INPUT) String accountsInput) throws JDOMException, IOException {

        LOG.debug("In do get");

        //get the userId from the cookie
        openId = Utils.getIdFromHeaderCookie(request);
        if(debugFlag) {
            LOG.debug("UserId Retrieved: " + openId);
        }
        
        //debug
        //if(userId.equals("https://pcmdi3.llnl.gov/esgcet/myopenid/jfharney")) {
        //    userId = "user1_userName";
        //}
        
        //initialize the model sent to the view
        Map<String,Object> model = getModel(request,accountsInput);

//        //make sure this is a "fresh" model
//        if (request.getParameter(ACCOUNTS_INPUT)!=null) {
//            LOG.debug("model not null");
//            // retrieve model from session
//            model = (Map<String,Object>)request.getSession().getAttribute(ACCOUNTS_MODEL);
//
//        } 
//        else {
//            LOG.debug("model is null");
//            
//            //User user = Utils.populateUserObjectFromIdXML(userId,new File(USERS_FILE));
//            
//            // populate model with the UserInfo
//            //model.put( ACCOUNTS_USERINFO, user);
//            LOG.debug("AccountsView Input: " + accountsInput);
//            model.put(ACCOUNTS_INPUT, accountsInput);
//            
//            //put the model in the session
//            request.getSession().setAttribute(ACCOUNTS_INPUT, model);
//            LOG.debug("GotHere2");
//            
//        }

        return new ModelAndView("accountsview", model);
    }
    
    /* Helper function for extracting the model */
    @SuppressWarnings({ "unchecked", "deprecation" })
    private Map<String,Object> getModel(final HttpServletRequest request,
                                       final @ModelAttribute(ACCOUNTS_INPUT)  String accountsInput) throws IOException {
        LOG.debug("------AccountsController getModel------");
        Map<String,Object> model = new HashMap<String,Object>();
        
        if (request.getParameter(ACCOUNTS_MODEL)!=null) {
            // retrieve model from session
            model = (Map<String,Object>)request.getSession().getAttribute(ACCOUNTS_MODEL);

        } 
        else {
            /* * Getting List of all group names to extract group desc * */
              String fileNameStatic = System.getenv().get("ESGF_HOME")+"/config/esgf_ats_static.xml"; //File is messy, created by humans.
              String fileNameDynamic = System.getenv().get("ESGF_HOME")+"/config/esgf_ats.xml";       //File is clean, created by machine.
              ArrayList<String> fileStatic = new ArrayList<String>();
              ArrayList<String> fileDynamic = new ArrayList<String>();
              String strLine = "";
              String tmp = "";
              List<Group> mygroups = new ArrayList<Group>();
              List<String> myroles = new ArrayList<String>();
              
              try{
                /* * get all groups and info out of dynamic file * */
                FileInputStream dstream = new FileInputStream(fileNameDynamic);
                DataInputStream get = new DataInputStream(dstream);
                while ((strLine = get.readLine()) != null)   {
                  strLine = strLine.trim();
                  String finder[] = strLine.split(" ");
                  if(finder[0].toString().equals("<attribute")){  
                    fileDynamic.add(strLine);
                  }
                }
                get.close();
                /* * get all grops and info out of static file * */

                FileInputStream fstream = new FileInputStream(fileNameStatic);
                DataInputStream in = new DataInputStream(fstream);
                while ((strLine = in.readLine()) != null ){
                  strLine = strLine.trim();
                  if(strLine.length() == 0){
                    continue;
                  }
                  else if(strLine.charAt(0) == '<' && strLine.charAt(strLine.length() - 1) == '>'){
                    String finder[] = strLine.split(" ");
                    if(finder[0].toString().equals("<attribute")){
                      fileStatic.add(strLine);  
                    }
                  }
                  else if(strLine.charAt(0) == '<'){
                    tmp = strLine;
                  }
                  else if(strLine.charAt(strLine.length() - 1) == '>'){
                    tmp = tmp + strLine;
                    tmp = tmp.replace("\n", " ");
                    String finder[] = tmp.split(" ");
                    if(finder[0].toString().equals("<attribute")){
                      fileStatic.add(tmp);
                    }
                    tmp = "";
                  }
                  else{
                    tmp = tmp + strLine;
                  }
                }
                in.close();
                /* * merge both sets of groups and info * */
                for(int e = 0; e < fileDynamic.size(); e++){
                  String dynamicLength[] = fileDynamic.get(e).split("\"");
                  if(dynamicLength.length != 9){
                    fileDynamic.remove(e);
                  }
                  else{
                    for(int r = 0; r < fileStatic.size(); r++){
                      String staticLength[] = fileStatic.get(r).split("\"");
                      if(fileDynamic.get(e) == fileStatic.get(r)){
                        fileStatic.remove(r);
                      }
                      else if(staticLength.length != 9){
                        fileStatic.remove(r);
                      }
                    }
                  }
                }
                fileDynamic.addAll(fileStatic);
              }
              catch (Exception e){
                System.err.println("Error: " + e.getMessage());
              }

            /* * get map of all groups and info this.user is in * */
            try {
              openId = Utils.getIdFromHeaderCookie(request);
              Map<String,Set<String>> userGroupsAndRoles = fas.getAttributes(openId);
                
              for (Object key : userGroupsAndRoles.keySet()) {
                String gn = key.toString();

		            System.out.println("Key : " + key.toString() + " Value : " + userGroupsAndRoles.get(key));
	            }
              /* * Putting the two together for delivery to the view * */
              for (Object key : userGroupsAndRoles.keySet()) {
                String name = key.toString();
                for(int count = 0; count < fileDynamic.size(); count++){
                  String spliter[] = fileDynamic.get(count).split("\"");
                  if(spliter[1].equals(name)){ //yes user is in this group
                    Group temp = new Group(key.toString(), spliter[1], spliter[5]);
                    mygroups.add(temp);
                    
                    for(int parts = 0; parts < userGroupsAndRoles.get(key).size(); parts++){
                        String tmproles = "";
                        Iterator<String> myit = userGroupsAndRoles.get(key).iterator();
                        while(myit.hasNext()){
                            tmproles += myit.next();
                        }
                        myroles.add(tmproles);
                    }
                  }
                }
              } 
            } catch (Exception e) {
              e.printStackTrace();
            }
            
            
            /* * * */


            // get user info from DAO
            User userInfo = uoi.getUserObjectFromUserOpenID(openId);
            
            if(userInfo == null){
              //LOG.debug("userInfo:" + userInfo);
              String error = "true";
              model.put(ACCOUNTS_ERROR, error);
            }
            else {
              /*
              // get group info from DAO
              List<Group> groups = uoi.getGroupsFromUser(userInfo.getUserName());
            
              // Get roles for each group as concatenated strings
              List<String> roles = new ArrayList<String>();
              Map<String,Set<String>> userperms = uoi.getUserPermissionsFromOpenID(openId);
              LOG.debug("userperms = " + userperms);
            
              // TODO: this should be added to GroupOperationsESGFDBImpl or better yet, create a combined GroupRole (or Permissions) OperationsESGFDBImpl
              for (Group g : groups) {
                // iterate through role set
                String roleNames = "";
                Set<String> roleSet = userperms.get(g.getname());
                
                if (roleSet != null) {
                    Iterator<String> it = userperms.get(g.getname()).iterator();
                    while (it.hasNext()) {
                        roleNames += it.next();
                        if (it.hasNext()) roleNames += ", ";
                    }
                }
                roles.add(roleNames);
              }
                */
              // populate model
              model.put(ACCOUNTS_INPUT, accountsInput);
              model.put(ACCOUNTS_USERINFO, userInfo);
              Group [] groupArray = mygroups.toArray(new Group[mygroups.size()]);
              model.put(ACCOUNTS_GROUPINFO, groupArray);
              String [] roleArray = myroles.toArray(new String[myroles.size()]);
              model.put(ACCOUNTS_ROLEINFO, roleArray);
              request.getSession().setAttribute(ACCOUNTS_MODEL, model);
              model.put(ACCOUNTS_ERROR, "false");
            }
        }

        LOG.debug("------End AccountsController getModel------");
        return model;
    }
    
    
    /**
     * Method invoked in response to a POST request:
     * both results and facets are retrieved.
     * @param request
     * @param input
     * @param result
     * @return
     * @throws Exception
     */
    @RequestMapping(method=RequestMethod.POST)
    @SuppressWarnings("unchecked")
    protected ModelAndView doPost(final HttpServletRequest request,
            final @ModelAttribute(ACCOUNTS_INPUT) String accountsInput) throws Exception {
        LOG.debug("In do post");

        //get the userId from the cookie
        String userId = Utils.getIdFromHeaderCookie(request);
        if(debugFlag) {
            LOG.debug("UserId Retrieved: " + userId);
        }
        
        if(userId.equals("https://pcmdi3.llnl.gov/esgcet/myopenid/jfharney")) {
            userId = "user1_userName";
        }
        
        //initialize the model sent to the view
        Map<String,Object> model = new HashMap<String,Object>();

        //make sure this is a "fresh" model
        if (request.getParameter(ACCOUNTS_MODEL)!=null) {
            LOG.debug("Not null");
            // retrieve model from session
            model = (Map<String,Object>)request.getSession().getAttribute(ACCOUNTS_MODEL);

        } 
        else {
            
            //User user = Utils.populateUserObjectFromIdXML(userId,new File(USERS_FILE));
            
            // populate model with the UserInfo
            //model.put( ACCOUNTS_USERINFO, user);
            model.put(ACCOUNTS_INPUT, accountsInput);
            
            //put the model in the session
            request.getSession().setAttribute(ACCOUNTS_MODEL, model);
            
        }
        return new ModelAndView("accountsview", model);
    }
    
    
    
    /**
     *
     * @param request
     * @return
     * @throws Exception
     */
    /*
    @ModelAttribute(ACCOUNTS_MISC)
    public String formAccountsMiscObject(final HttpServletRequest request) throws Exception {
        LOG.debug("formAccountsMiscObject");
        
        
        
        return "ACCOUNTS_MISC here";
    }
    */
    
    /**
     *
     * @param request
     * @return
     * @throws Exception
     */
    @ModelAttribute(ACCOUNTS_INPUT)
    public String formAccountsInputObject(final HttpServletRequest request) throws Exception {

        LOG.debug("formAccountsInputObject called");

        return "ACCOUNTS_INPUT here";

    }
    
    

}

