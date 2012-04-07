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


public class Units {

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
    private Map<String, String> newcharterorgBase;
    private Map<String, String> newunitBase;


    @Before
        public void openBrowser() throws IOException {
                  
        logger= LoggerFactory.getLogger(Units.class);
        prop = new Properties();
        prop.load(new FileInputStream("districtdbtest.properties"));
        baseUrl = prop.getProperty("baseUrl");
        siteStyle = prop.getProperty("siteStyle");
        uniqueStr = prop.getProperty("uniqueStr");
        browser = prop.getProperty("browser");
        newcharterorgBase = (HashMap<String, String>) CharterOrgHelper.initCharterOrg(prop, logger);
        newunitBase = (HashMap<String, String>) UnitHelper.initUnit(prop, logger);

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
        public void UnitsTests() throws IOException {

        logger.info("===============  UnitsTests being run...");
                
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
                
        logger.info("----------------------------  Test: create Unit with Charter Org address info -----");

        // Start off by creating a Charter Org
        // Create test specific Charter Org
        Map<String, String> newcharterorg_a = (HashMap<String, String>) CharterOrgHelper.createCharterOrg((HashMap<String, String>) newcharterorgBase, uniqueStr + "una", logger);
                                
        // First verify that the Charter Org does not exist
        assertTrue("Charter Org already exists!! " + newcharterorg_a.get("OrgName"),
                   CharterOrgHelper.verifyCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_a, false, siteStyle, logger));
                
        // Lets proceed with creating new CO
        logger.info("Beginning new CharterOrg creation.");
                
        // Now fill in the new CO form
        logger.info("Filling in CharterOrg registration form.");

        CharterOrgHelper.addCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_a, true, siteStyle, logger);
                
        // Now check everything
        assertTrue("Mismatch in new charterorg profile " + newcharterorg_a.get("OrgName"),
                   CharterOrgHelper.verifyCharterOrg(dadriver, (HashMap <String, String>) newcharterorg_a, true, siteStyle, logger));

        // Now we can proceeed with creating the Unit and pointing at the Charter Org
        // Create test specific Unit
        Map<String, String> newunit_a = (HashMap<String, String>) UnitHelper.createUnit((HashMap<String, String>) newunitBase, uniqueStr + "una", logger);
        newunit_a.put("OrgNumber", "1" + uniqueStr);
        newunit_a.put("GetInfoFromCharterOrg", "Yes");
        newunit_a.put("CharterOrgID", newcharterorg_a.get("OrgName"));
                                
        // First verify that the Unit does not exist
        assertTrue("Unit already exists!! " + newunit_a.get("OrgNumber"),
                   UnitHelper.verifyUnit(dadriver, (HashMap <String, String>) newunit_a, false, siteStyle, logger));
                
        // Lets proceed with creating new Unit
        logger.info("Beginning new Unit creation.");
                
        // Now fill in the new Unit form
        logger.info("Filling in Unit registration form.");

        UnitHelper.addUnit(dadriver, (HashMap <String, String>) newunit_a, true, siteStyle, logger);
                
        // Now check everything
        // We need to verify using the Charter Orgs address info
        Map<String, String> newunit_aorig = new HashMap<String, String> (newunit_a);
        newunit_a.put("Phone", newcharterorg_a.get("Phone"));
        newunit_a.put("Address", newcharterorg_a.get("Address"));
        newunit_a.put("City", newcharterorg_a.get("City"));
        newunit_a.put("State", newcharterorg_a.get("State"));
        newunit_a.put("Zip", newcharterorg_a.get("Zip"));
        assertTrue("Mismatch in new unit profile " + newunit_a.get("OrgNumber"),
                   UnitHelper.verifyUnit(dadriver, (HashMap <String, String>) newunit_a, true, siteStyle, logger));


        logger.info("----------------------------  Test: edit Unit simple -----");

        // Twiddle the Location
        Map<String, String> newunit_aalt = new HashMap<String, String> ();
        newunit_aalt.put("OrgNumber", newunit_a.get("OrgNumber"));
        newunit_aalt.put("GetInfoFromCharterOrg", "Yes");
        newunit_aalt.put("Location", "new loc " + uniqueStr);
        UnitHelper.editUnit(dadriver, (HashMap <String, String>) newunit_aalt, true, siteStyle, logger);
        assertTrue("Mismatch in new unit profile " + newunit_aalt.get("OrgNumber"),
                   UnitHelper.verifyUnit(dadriver, (HashMap <String, String>) newunit_aalt, true, siteStyle, logger));
                  
        
        logger.info("----------------------------  Test: edit Unit switch to unit address  -----");

        newunit_aorig.put("GetInfoFromCharterOrg", "No");
        UnitHelper.editUnit(dadriver, (HashMap <String, String>) newunit_aorig, true, siteStyle, logger);
        assertTrue("Mismatch in new unit profile " + newunit_aorig.get("OrgNumber"),
                   UnitHelper.verifyUnit(dadriver, (HashMap <String, String>) newunit_aorig, true, siteStyle, logger));
        

        logger.info("----------------------------  Test: de-activate Unit -----");

        assertTrue("Error during edit Unit for " + newunit_aorig.get("OrgNumber"),
                   UnitHelper.editUnit(dadriver, (HashMap <String, String>) newunit_aorig, false, siteStyle, logger));
        assertTrue("Mismatch in new unit profile " + newunit_aorig.get("OrgNumber"),
                   UnitHelper.verifyUnit(dadriver, (HashMap <String, String>) newunit_aorig, false, siteStyle, logger));
                  
        
                  
          
    }

                
                        
}
