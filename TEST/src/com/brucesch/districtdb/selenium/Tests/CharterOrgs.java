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


public class CharterOrgs {

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
    private Map<String, String> newcharterorgBase;


    @Before
        public void openBrowser() throws IOException {
                  
        logger= LoggerFactory.getLogger(CharterOrgs.class);
        prop = new Properties();
        prop.load(new FileInputStream("districtdbtest.properties"));
        baseUrl = prop.getProperty("baseUrl");
        uniqueStr = prop.getProperty("uniqueStr");
        browser = prop.getProperty("browser");
        newcharterorgBase = (HashMap<String, String>) CharterOrgHelper.initCharterOrg(prop, logger);

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
        public void CharterOrgsTests() throws IOException {

        logger.info("===============  CharterOrgsTests being run...");
                
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
                
        logger.info("----------------------------  Test: create Charter Org -----");

        // Create test specific Charter Org
        Map<String, String> newcharterorg_a = (HashMap<String, String>) CharterOrgHelper.createCharterOrg((HashMap<String, String>) newcharterorgBase, uniqueStr + "coa", logger);
                                
        // First verify that the Charter Org does not exist
        assertTrue("Charter Org already exists!! " + newcharterorg_a.get("OrgName"),
                   CharterOrgHelper.verifyCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_a, false, logger));
                
        // Lets proceed with creating new CO
        logger.info("Beginning new CharterOrg creation.");
                
        // Now fill in the new CO form
        logger.info("Filling in CharterOrg registration form.");

        CharterOrgHelper.addCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_a, true, logger);
                
        // Now check everything
        assertTrue("Mismatch in new charterorg profile " + newcharterorg_a.get("OrgName"),
                   CharterOrgHelper.verifyCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_a, true, logger));


        logger.info("----------------------------  Test: edit Charter Org -----");

        // Twiddle the phone number
        Map<String, String> newcharterorg_aalt = new HashMap<String, String> ();
        newcharterorg_aalt.put("OrgName", newcharterorg_a.get("OrgName"));
        newcharterorg_aalt.put("Phone", "1-888-" + uniqueStr + " ext 000");
        CharterOrgHelper.editCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_aalt, true, logger);
        assertTrue("Mismatch in new charterorg profile " + newcharterorg_aalt.get("OrgName"),
                   CharterOrgHelper.verifyCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_aalt, true, logger));
                  
        
        logger.info("----------------------------  Test: de-activate Charter Org -----");

        assertTrue("Error during edit Charter Org for " + newcharterorg_aalt.get("OrgName"),
                   CharterOrgHelper.editCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_aalt, false, logger));
        assertTrue("Mismatch in new charterorg profile " + newcharterorg_aalt.get("OrgName"),
                   CharterOrgHelper.verifyCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_aalt, false, logger));
                  
        
                  
          
    }

                
                        
}
