package org.esgfLegacy.datacart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.esgf.metadata.JSONException;
import org.esgf.metadata.JSONObject;
import org.esgf.metadata.XML;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TechnotesElement {

    /** Urls given by the results of the search api */
    private List<TechnoteElement> technotes;
    

    private final static String testInitializationFile = "C:\\Users\\8xo\\esgf-web-fe\\technoteselement.xml";
    
    /**
     * 
     */
    public TechnotesElement() {
        
    }

    /**
     * 
     * @param urls
     */
    public void setTechnotes(List<TechnoteElement> technotes) {
        if(technotes != null) {
            this.technotes = technotes;
        }
    }

    /**
     * 
     * @return
     */
    public List<TechnoteElement> getTechnotes() {
        return technotes;
    }
    
    
    public void addTechnote(TechnoteElement technote) {
        if(this.technotes == null) {
            this.technotes = new ArrayList<TechnoteElement>();
        }
        if(technote != null) {
            this.technotes.add(technote);
        }
    }
    
    public void removeTechnote(TechnoteElement technote) {
        if(technote != null) {
            this.technotes.remove(technote);
        }
    }
    
    /** Description of toElement()
     * 
     * @return serialized XML element equivalent of the class
     */
    public Element toElement() {
        Element technotesEl = new Element("technotes");
        
        if(this.technotes != null) {
            for(int i=0;i<technotes.size();i++) {
                technotesEl.addContent(technotes.get(i).toElement());
            }
        }
        
        return technotesEl;
    }
    

    /** Description of toXML()
     * 
     * @return
     */
    public String toXML() {
        String xml = "";
        
        Element technotesEl = this.toElement();

        XMLOutputter outputter = new XMLOutputter();
        xml = outputter.outputString(technotesEl);
        
        return xml;
    }

    /** Description of toJSONObject()
     * 
     * @return
     */
    public JSONObject toJSONObject() {
        
        JSONObject json = null;
        
        try {
            json = XML.toJSONObject(this.toXML());
        } catch (JSONException e) {
            System.out.println("Problem in toJSONObject");
            e.printStackTrace();
        }
        
        return json;
    }
    
    
    public String toJSON() {
        String json = null;
        
        try {
            json = this.toJSONObject().toString(3);
        } catch (JSONException e) {
            System.out.println("Problem in toJSON");
            e.printStackTrace();
        }
        
        return json;
    }
    
    
    
    /** Description of toFile()
     * 
     * @param file Filename of the output
     */
    public void toFile(String file) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(new XmlFormatter().format(this.toXML()));
            out.close();
        } 
        catch (IOException e) { 
            e.printStackTrace();
            System.out.println("Exception ");

        }
    }
    
    /**Description of fromFile()
     * 
     * @param file
     */
    public void fromFile(String file) {
        
        //overwrite whatever was in the data structure
        this.technotes = null;
        
        
        File fXmlFile = new File(file);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            
            doc.getDocumentElement().normalize();
            
            
            if(doc.getDocumentElement().getNodeName().equals("technotes")) {
              System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                NodeList technoteNodeList = doc.getDocumentElement().getElementsByTagName("technote");
         
                
                //parse over technote elements
                for (int i = 0; i < technoteNodeList.getLength(); i++) {
                    String name = null;
                    String location = null;
                    
                    Node technoteNode = technoteNodeList.item(i);
                    if (technoteNode.getNodeType() == Node.ELEMENT_NODE) {
                        org.w3c.dom.Element technoteElement = (org.w3c.dom.Element) technoteNode;
                        NodeList technoteChildren = technoteElement.getChildNodes();
                        for(int j=0;j<technoteChildren.getLength();j++) {
                            Node node = technoteChildren.item(j);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                org.w3c.dom.Element technoteChild = (org.w3c.dom.Element) node;
                                if(technoteChild.getTagName().equals("name")) {
                                    name = technoteChild.getTextContent();
                                } else if(technoteChild.getTagName().equals("location")) {
                                    location = technoteChild.getTextContent();
                                }
                            }
                        }
                        //eElement.getElementsByTagName(name)
                    }
                    TechnoteElement technote = new TechnoteElement();
                    technote.setLocation(location);
                    technote.setName(name);
                    this.addTechnote(technote);
                }
                
                
                
            }
            
            
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public static void main(String [] args) {
        TechnotesElement u = new TechnotesElement();
        
        u.fromFile(testInitializationFile);
        
        System.out.println(new XmlFormatter().format(u.toXML()));
        
        //u.toFile(testInitializationFile);
        
        //u = new URLSElement2();
        
        //u.fromFile(testInitializationFile);

        //System.out.println(u.toXML());
        
        
    }
    
    
}
