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


public class OtherOrgs {

    private String baseUrl;
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
    private Map<String, String> newotherorgBase;


    @Before
        public void openBrowser() throws IOException {
                  
        logger= LoggerFactory.getLogger(OtherOrgs.class);
        prop = new Properties();
        prop.load(new FileInputStream("districtdbtest.properties"));
        baseUrl = prop.getProperty("baseUrl");
        uniqueStr = prop.getProperty("uniqueStr");
        browser = prop.getProperty("browser");
        newotherorgBase = (HashMap<String, String>) OtherOrgHelper.initOtherOrg(prop, logger);

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
        public void OtherOrgsTests() throws IOException {

        logger.info("===============  OtherOrgsTests being run...");
                
        // Before we start with new user stuff lets check that he does not exist.
        logger.info("Init the DistAdmin helper panel.");
        WebElement nameField = dadriver.findElement(By.id("modlgn-username"));
        WebElement pwField   = dadriver.findElement(By.id("modlgn-passwd"));
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
                
        logger.info("----------------------------  Test: create Other Org -----");

        // Create test specific Other Org
        Map<String, String> newotherorg_a = (HashMap<String, String>) OtherOrgHelper.createOtherOrg((HashMap<String, String>) newotherorgBase, uniqueStr + "ota", logger);
                                
        // First verify that the Other Org does not exist
        assertTrue("Other Org already exists!! " + newotherorg_a.get("OrgName"),
                   OtherOrgHelper.verifyOtherOrg(dadriver, (HashMap <String, String>) newotherorg_a, false, logger));
                
        // Lets proceed with creating new Other Org
        logger.info("Beginning new OtherOrg creation.");
                
        // Now fill in the new Other Org form
        logger.info("Filling in OtherOrg registration form.");

        OtherOrgHelper.addOtherOrg(dadriver, (HashMap <String, String>) newotherorg_a, true, logger);
                
        // Now check everything
        assertTrue("Mismatch in new otherorg profile " + newotherorg_a.get("OrgName"),
                   OtherOrgHelper.verifyOtherOrg(dadriver, (HashMap <String, String>) newotherorg_a, true, logger));


        logger.info("----------------------------  Test: edit Other Org -----");

        // Twiddle the phone number
        Map<String, String> newotherorg_aalt = new HashMap<String, String> ();
        newotherorg_aalt.put("OrgName", newotherorg_a.get("OrgName"));
        newotherorg_aalt.put("Phone", "1-888-" + uniqueStr + " ext 000");
        OtherOrgHelper.editOtherOrg(dadriver, (HashMap <String, String>) newotherorg_aalt, true, logger);
        assertTrue("Mismatch in new otherorg profile " + newotherorg_aalt.get("OrgName"),
                   OtherOrgHelper.verifyOtherOrg(dadriver, (HashMap <String, String>) newotherorg_aalt, true, logger));
                  
        
        logger.info("----------------------------  Test: de-activate Other Org -----");

        assertTrue("Error during edit Other Org for " + newotherorg_aalt.get("OrgName"),
                   OtherOrgHelper.editOtherOrg(dadriver, (HashMap <String, String>) newotherorg_aalt, false, logger));
        assertTrue("Mismatch in new otherorg profile " + newotherorg_aalt.get("OrgName"),
                   OtherOrgHelper.verifyOtherOrg(dadriver, (HashMap <String, String>) newotherorg_aalt, false, logger));
                  
        
                  
          
    }

                
                        
}
