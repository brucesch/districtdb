package com.brucesch.districtdb.selenium.Tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Point;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
//import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.brucesch.districtdb.selenium.Utils.*;


public class Service {

    private String baseUrl;
    private String siteStyle;
    private String uniqueStr;
    private String browser;
    private String defsrvc_orgname;
    private String defsrvc_orgtype;
    private String defsrvc_unitnumber;
    private String defsrvc_description;
    private WebDriver tstdriver;
    private WebDriver dadriver;
    private ScreenshotHelper screenshotHelper;
    private Properties prop;
    private Logger logger;
    private Map<String, String> newjobBase;


    @Before
        public void openBrowser() throws IOException {
                  
        logger= LoggerFactory.getLogger(Service.class);
        prop = new Properties();
        prop.load(new FileInputStream("districtdbtest.properties"));
        baseUrl = prop.getProperty("baseUrl");
        siteStyle = prop.getProperty("siteStyle");
        uniqueStr = prop.getProperty("uniqueStr");
        browser = prop.getProperty("browser");
        newjobBase = (HashMap<String, String>) ServiceHelper.initJob(prop, logger);

        // Two drivers - one for the DistAdmin and one for test
        logger.info("Browser type: {}", browser);
        if (browser.equals("ff")) {
            System.setProperty("webdriver.firefox.bin", "C:\\Documents and Settings\\bruce\\Local Settings\\Application Data\\Mozilla Firefox\\firefox.exe");
            dadriver = new FirefoxDriver();
        } else if (browser.equals("ie")) {
            DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
            ieCapabilities.setCapability(
                                         InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
            dadriver = new InternetExplorerDriver(ieCapabilities);
        } else {
            fail("Non supported browser selected in properties file.");
        }
        dadriver.get(baseUrl);

        screenshotHelper = new ScreenshotHelper();
    }

    @After
        public void saveScreenshotAndCloseBrowser() throws IOException {
        screenshotHelper.saveScreenshot(dadriver, "screenshot_da.png");
        dadriver.quit();
    }

    @Test
        public void ServiceTests() throws IOException {

        logger.info("===============  ServiceTests being run...");
                
        // Before we start with new user stuff lets check that he does not exist.
        logger.info("Init the DistAdmin helper panel.");
        WebElement nameField = Misc.getLoginElem(dadriver, "username", siteStyle, logger);
        WebElement pwField   = Misc.getLoginElem(dadriver, "passwd", siteStyle, logger);
        nameField.clear();
        nameField.sendKeys(prop.getProperty("dauser.name"));
        pwField.clear();
        pwField.sendKeys(prop.getProperty("dauser.pw"));
        pwField.submit();
                
        // Wait till results page
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("login-greeting"));
            }
        });
                
        logger.info("Successful login to the DistAdmin panel.");
                
        logger.info("----------------------------  Test: create Job -----");

        // Create test specific Job
        Map<String, String> newjob_a = (HashMap<String, String>) ServiceHelper.createJob((HashMap<String, String>) newjobBase, uniqueStr + "svja", logger);
                                
        // First verify that the Job does not exist
        assertTrue("Job already exists!! " + newjob_a.get("Description"),
                   ServiceHelper.verifyJob(dadriver, (HashMap <String, String>) newjob_a, false, siteStyle, logger));
                
        // Lets proceed with creating new Job
        logger.info("Beginning new Job creation.");
                
        // Now fill in the new Job form
        logger.info("Filling in Job registration form.");

        ServiceHelper.addJob(dadriver, (HashMap <String, String>) newjob_a, siteStyle, logger);
                
        // Now check everything
        assertTrue("Mismatch in new job profile " + newjob_a.get("Description"),
                   ServiceHelper.verifyJob(dadriver, (HashMap <String, String>) newjob_a, true, siteStyle, logger));



        logger.info("----------------------------  Test: create My Service -----");

        // Create test specific Service
        Map<String, String> newservice_a = new HashMap<String, String> ();
        newservice_a.put("orgid", "Pack 58");
        newservice_a.put("jobid", newjob_a.get("Description"));
                                
        // First verify that the Service does not exist
        assertTrue("Service already exists!! " + newservice_a.get("Description"),
                   ServiceHelper.verifyMyService(dadriver, "", "Pack", "58", newservice_a.get("jobid"), false, siteStyle, logger) == -1);
                
        // Lets proceed with creating new Service
        logger.info("Beginning new Service creation.");
                
        // Now fill in the new Service form
        logger.info("Filling in Service registration form.");

        ServiceHelper.addMyService(dadriver, (HashMap <String, String>) newservice_a, siteStyle, logger);
                
        // Now check everything
        int mysvcpos = ServiceHelper.verifyMyService(dadriver, "", "Pack", "58", newservice_a.get("jobid"), true, siteStyle, logger);
        assertTrue("Mismatch in new service profile " + newservice_a.get("Description"),
                   mysvcpos > -1);


        logger.info("----------------------------  Test: deactivate My Service -----");

        assertTrue("Problem with deactivate My Service " + newservice_a.get("Description"),
                   ServiceHelper.deactivateMyService(dadriver, mysvcpos, true, siteStyle, logger));

        // Verify that the Service is gone
        assertTrue("Service not deactivated " + newservice_a.get("Description"),
                   ServiceHelper.verifyMyService(dadriver, "", "Pack", "58", newservice_a.get("jobid"), false, siteStyle, logger) == -1);
                
          
    }

                
                        
}
