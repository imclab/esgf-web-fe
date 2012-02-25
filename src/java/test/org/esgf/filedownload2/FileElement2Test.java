package org.esgf.filedownload2;


import static org.junit.Assert.*;

import java.io.IOException;

import org.dom4j.DocumentException;
import org.jdom.JDOMException;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class FileElement2Test {
    //test runner
    public static void main(String [] args) {
        
        
        System.out.println("Running test for URLSElement2Test");
        Result result = JUnitCore.runClasses(FileElement2Test.class);
        
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println("Total Tests: " + result.getRunCount());
        System.out.println("Failure count: " + result.getFailureCount());
         
    }
    
    /*
     * Test Cases to be conducted (grouped by method name)
     * 
     * toElement()
     * 
     * toXML()
     * 
     * toJSONObject()
     * 
     * toJSON()
     * 
     * toFile()
     * 
     * fromFile()
     * 
     * readHelper()
     * 
     * addUrl()
     * 
     * removeUrl()
     * 
     * addMime()
     * 
     * removeMime()
     * 
     * addService()
     * 
     * removeService()
     * 
     */
    
    
    
    @Test
    public void testInitialization() {
        int a = 0;
        
        assertEquals(a,0);
        
    }

}
