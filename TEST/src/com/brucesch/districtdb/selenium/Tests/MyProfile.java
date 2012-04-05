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


public class MyProfile {

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
    private Map<String, String> newuserBase;


    @Before
        public void openBrowser() throws IOException {
                  
        logger= LoggerFactory.getLogger(MyProfile.class);
        prop = new Properties();
        prop.load(new FileInputStream("districtdbtest.properties"));
        baseUrl = prop.getProperty("baseUrl");
        uniqueStr = prop.getProperty("uniqueStr");
        browser = prop.getProperty("browser");
        defsrvc_orgname = prop.getProperty("defsrvc.orgname");
        defsrvc_orgtype = prop.getProperty("defsrvc.orgtype");
        defsrvc_unitnumber = prop.getProperty("defsrvc.unitnumber");
        defsrvc_description = prop.getProperty("defsrvc.description");
        newuserBase = (HashMap<String, String>) PersonHelper.initUser(prop, logger);

        // Two drivers - one for the DistAdmin and one for test
        logger.info("Browser type: {}", browser);
        if (browser.equals("ff")) {
            System.setProperty("webdriver.firefox.bin", "C:\\Documents and Settings\\bruce\\Local Settings\\Application Data\\Mozilla Firefox\\firefox.exe");
            tstdriver = new FirefoxDriver();
            dadriver = new FirefoxDriver();
        } else if (browser.equals("ie")) {
            DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
            ieCapabilities.setCapability(
                                         InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
            tstdriver = new InternetExplorerDriver(ieCapabilities);
            dadriver = new InternetExplorerDriver(ieCapabilities);
        } else {
            fail("Non supported browser selected in properties file.");
        }
        tstdriver.get(baseUrl);
        dadriver.get(baseUrl);
        // Jam the tst window over so that I can watch both - wheee!
        tstdriver.manage().window().setPosition(new Point(840, 4));

        screenshotHelper = new ScreenshotHelper();
    }

    @After
        public void saveScreenshotAndCloseBrowser() throws IOException {
        screenshotHelper.saveScreenshot(tstdriver, "screenshot_tst.png");
        screenshotHelper.saveScreenshot(dadriver, "screenshot_da.png");
        tstdriver.quit();
        dadriver.quit();
    }

    @Test
        public void MyProfileTests() throws IOException {

        logger.info("===============  MyProfileTests being run...");
                
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
                
        logger.info("----------------------------  Test: new user, no profile, no service -----");
                
        // Create test specific user
        Map<String, String> newuser_a = (HashMap<String, String>) PersonHelper.createUser((HashMap<String, String>) newuserBase, uniqueStr + "mpa", logger);
                                
        // Verify that the newuser does not already have a profile
        assertTrue("User already exists!! " + newuser_a.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_a, false, false, false, logger));
                
                        
        // Lets proceed with registering as a new user
        logger.info("Beginning newuser registration.");
                
        // Now fill in the new profile form
        logger.info("Filling in newuser registration form.");
        String[] serviceorgname = {};
        String[] serviceorgtype = {};
        String[] serviceunitnumber = {};
        String[] servicedescription = {};
        PersonHelper.createPerson(tstdriver, (HashMap <String, String>) newuser_a, 
                                  serviceorgname, serviceorgtype, serviceunitnumber, servicedescription, true, logger);
                
        // Now check everything
        assertTrue("Mismatch in newuser profile " + newuser_a.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_a, true, true, true, logger));
        serviceorgname = new String[] {defsrvc_orgname};
        serviceorgtype = new String[] {defsrvc_orgtype};
        serviceunitnumber = new String[] {defsrvc_unitnumber};
        servicedescription = new String[] {defsrvc_description};
        assertTrue("Mismatch in newuser service " + newuser_a.get("username"),
                   PersonHelper.verifyService(dadriver, (HashMap <String, String>) newuser_a, 
                                              serviceorgname, serviceorgtype, serviceunitnumber, servicedescription,
                                              true, logger));
                
        logger.info("----------------------------  Test: new user, no profile, with service -----");
                
        // Create test specific user
        Map<String, String> newuser_b = (HashMap<String, String>) PersonHelper.createUser((HashMap<String, String>) newuserBase, uniqueStr + "mpb", logger);
                                
        // Verify that the newuser does not already have a profile
        assertTrue("User already exists!! " + newuser_b.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_b, false, false, false, logger));
                
                        
        // Lets proceed with registering as a new user
        logger.info("Beginning newuser registration.");
                
        // Now fill in the new profile form
        logger.info("Filling in newuser registration form.");
        serviceorgname = new String[] {"", "", "Bee Cave District"};
        serviceorgtype = new String[] {"Pack", "Troop", "District"};
        serviceunitnumber = new String[] {"37", "2020", ""};
        servicedescription = new String[] {"Parent", "Assistant Scoutmaster", "Advancement Committee Chair"};
        PersonHelper.createPerson(tstdriver, (HashMap <String, String>) newuser_b, 
                                  serviceorgname, serviceorgtype, serviceunitnumber, servicedescription, true, logger);
                
        // Now check everything
        assertTrue("Mismatch in newuser profile " + newuser_b.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_b, true, true, true, logger));
        assertTrue("Mismatch in newuser service " + newuser_b.get("username"),
                   PersonHelper.verifyService(dadriver, (HashMap <String, String>) newuser_b, 
                                              serviceorgname, serviceorgtype, serviceunitnumber, servicedescription,
                                              true, logger));
                
        logger.info("----------------------------  Test: new user, with profile, with service -----");
                
        // Create test specific user
        Map<String, String> newuser_c = (HashMap<String, String>) PersonHelper.createUser((HashMap<String, String>) newuserBase, uniqueStr + "mpc", logger);
                                
        // Verify that the newuser does not already have a profile
        assertTrue("User already exists!! " + newuser_c.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_c, false, false, false, logger));
                
        // Create a user profile before the user registers at the site
        PersonHelper.addPerson(dadriver, (HashMap <String, String>) newuser_c, 
                               true, true, logger);
                
        // Now verify that the person is there and has the default service
        assertTrue("User should exist with no JUser, yes NL " + newuser_c.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_c, false, true, true, logger));
        serviceorgname = new String[] {defsrvc_orgname};
        serviceorgtype = new String[] {defsrvc_orgtype};
        serviceunitnumber = new String[] {defsrvc_unitnumber};
        servicedescription = new String[] {defsrvc_description};
        assertTrue("Mismatch in newuser service - initial " + newuser_c.get("username"),
                   PersonHelper.verifyService(dadriver, (HashMap <String, String>) newuser_c, 
                                              serviceorgname, serviceorgtype, serviceunitnumber, servicedescription,
                                              true, logger));
                
        // Now register with one service but NO newsletter.
        serviceorgname = new String[] {""};
        serviceorgtype = new String[] {"Pack"};
        serviceunitnumber = new String[] {"42"};
        servicedescription = new String[] {"Parent"};
        PersonHelper.createPerson(tstdriver, (HashMap <String, String>) newuser_c, 
                                  serviceorgname, serviceorgtype, serviceunitnumber, servicedescription, false, logger);
                
        // Finally verify the results
        assertTrue("User should exist with yes JUser, no NL " + newuser_c.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_c, true, false, true, logger));
        serviceorgname = new String[] {"", defsrvc_orgname};
        serviceorgtype = new String[] {"Pack", defsrvc_orgtype};
        serviceunitnumber = new String[] {"42", defsrvc_unitnumber};
        servicedescription = new String[] {"Parent", defsrvc_description};
        assertTrue("Mismatch in newuser service - final " + newuser_c.get("username"),
                   PersonHelper.verifyService(dadriver, (HashMap <String, String>) newuser_c, 
                                              serviceorgname, serviceorgtype, serviceunitnumber, servicedescription,
                                              true, logger));
                
        logger.info("----------------------------  Test: NEG new user reg, dupe email -----");
                
        // Re-use the newuser_c existing user.
        String origusername = newuser_c.get("username");
        String origemail = newuser_c.get("email");
        serviceorgname = new String[] {};
        serviceorgtype = new String[] {};
        serviceunitnumber = new String[] {};
        servicedescription = new String[] {};
        // Twiddle the username to something different
        newuser_c.put("username", "notaduplicationusername");
        PersonHelper.createPerson(tstdriver, (HashMap <String, String>) newuser_c, 
                                  serviceorgname, serviceorgtype, serviceunitnumber, servicedescription, false, logger);
        // Now check for the dupe email error
        assertTrue("Missing dupe email reg header.", Misc.verifyMsg(tstdriver, "hdrmsg", 
                                                                    "Some parts of your form have not been correctly filled in", logger));
        // This is the JUser/joomla sys test
        assertTrue("Missing dupe email reg details.", Misc.verifyMsg(tstdriver, "detmsg", 
                                                                     "This email is already registered.", logger));
        newuser_c.put("username", origusername);
                

        logger.info("----------------------------  Test: NEG new user reg, dupe username -----");

        // Twiddle the email to something different
        newuser_c.put("email", "nondupemail@schurmann.org");
        PersonHelper.createPerson(tstdriver, (HashMap <String, String>) newuser_c, 
                                  serviceorgname, serviceorgtype, serviceunitnumber, servicedescription, false, logger);
        // Now check for the dupe username error
        assertTrue("Missing dupe username reg header.", Misc.verifyMsg(tstdriver, "hdrmsg", 
                                                                       "Some parts of your form have not been correctly filled in", logger));
        // This is the JUser/joomla sys test
        assertTrue("Missing dupe username reg details.", Misc.verifyMsg(tstdriver, "detmsg", 
                                                                        "User name in use", logger));
        newuser_c.put("email", origemail);
                

        logger.info("----------------------------  Test: NEG new user add, dupe email -----");
          
        // Twiddle the username to something different
        newuser_c.put("username", "notaduplicationusername");
        PersonHelper.addPerson(dadriver, (HashMap <String, String>) newuser_c, 
                               true, true, logger);
        // Now check for the dupe email error
        assertTrue("Missing dupe email reg header.", Misc.verifyMsg(dadriver, "hdrmsg", 
                                                                    "Some parts of your form have not been correctly filled in", logger));
        // This is the Fabrik element 'email' isunique plugin validation test
        assertTrue("Missing dupe email reg details.", Misc.verifyMsg(dadriver, "detmsg", 
                                                                     "This email address is already in use.", logger));
        newuser_c.put("username", origusername);
                

        logger.info("----------------------------  Test: Edit My Profile - just change a simple field and verify -----");

        // Create a simpler user profile for the DistAdmin person
        Map<String, String> newuser_distadmin = new HashMap<String, String> ();
        newuser_distadmin.put("username", prop.getProperty("dauser.name"));
        newuser_distadmin.put("password", prop.getProperty("dauser.pw"));
        newuser_distadmin.put("phone", "1-888-" + uniqueStr + " ext 000");
        PersonHelper.myProfile(dadriver, newuser_distadmin, true, logger);
        assertTrue("My Profile edit appears to have failed",
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_distadmin, true, true, true, logger));
        
                  
        logger.info("----------------------------  Test: Edit Person Profile not JUser -----");
                
        // Create test specific user
        Map<String, String> newuser_d = (HashMap<String, String>) PersonHelper.createUser((HashMap<String, String>) newuserBase, uniqueStr + "mpd", logger);
                                
        // Verify that the newuser does not already have a profile
        assertTrue("User already exists!! " + newuser_d.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_d, false, false, false, logger));
                
        // Create a user profile before the user registers at the site
        PersonHelper.addPerson(dadriver, (HashMap <String, String>) newuser_d, 
                               true, true, logger);
                
        // Now verify that the person is there
        assertTrue("User should exist with no JUser, yes NL " + newuser_d.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_d, false, true, true, logger));

        // Twiddle the phone number
        Map<String, String> newuser_dalt = new HashMap<String, String> ();
        newuser_dalt.put("username", newuser_d.get("username"));
        newuser_dalt.put("phone", "1-888-" + uniqueStr + " ext 000");
        assertTrue("Something wrong with the edit Person Profile form for " + newuser_dalt.get("username"), 
                   PersonHelper.editPerson(dadriver, (HashMap <String, String>) newuser_dalt, true, true, false, logger));
        assertTrue("User should exist with no JUser, yes NL, altered phone " + newuser_dalt.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_dalt, false, true, true, logger));
        // Just to keep things consistent
        newuser_d.put("phone", newuser_dalt.get("phone"));
                   
                
        logger.info("----------------------------  Test: Edit Person Profile is JUser -----");
                
        // Verify that the person is there
        assertTrue("User should exist with yes JUser, yes NL " + newuser_b.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_b, true, true, true, logger));

        // Twiddle the phone number
        Map<String, String> newuser_balt = new HashMap<String, String> ();
        newuser_balt.put("username", newuser_b.get("username"));
        newuser_balt.put("phone", "1-888-" + uniqueStr + " ext 000");
        assertTrue("Something wrong with the edit Person Profile form for " + newuser_balt.get("username"), 
                   PersonHelper.editPerson(dadriver, (HashMap <String, String>) newuser_balt, true, true, true, logger));
        assertTrue("User should exist with no JUser, yes NL, altered phone " + newuser_balt.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_balt, true, true, true, logger));
        // Just to keep things consistent
        newuser_b.put("phone", newuser_balt.get("phone"));
                   
                
        logger.info("----------------------------  Test: De-activate Person -----");
                
        // Verify that the person is there
        assertTrue("User should exist with yes JUser, yes NL " + newuser_b.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_b, true, true, true, logger));
        assertTrue("Something wrong with the edit Person Profile form for " + newuser_b.get("username"), 
                   PersonHelper.editPerson(dadriver, (HashMap <String, String>) newuser_b, false, false, true, logger));
        assertTrue("User should be de-activated" + newuser_b.get("username"),
                   PersonHelper.verifyProfile(dadriver, (HashMap <String, String>) newuser_b, false, false, false, logger));
                
                  
          
    }

                
                        
}
