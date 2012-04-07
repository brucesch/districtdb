package com.brucesch.districtdb.selenium.Utils;

import java.io.File; 
import java.io.IOException;
import org.apache.commons.io.FileUtils; 
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.OutputType; 
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.brucesch.districtdb.selenium.Utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonHelper {

        
    /**
     * @param args
     */
    public static HashMap<String, String> initUser(Properties prop, Logger logger) {
                
        HashMap<String, String> newuser = new HashMap<String, String>();
        newuser.put("username", prop.getProperty("newuser.username"));
        newuser.put("name", prop.getProperty("newuser.name"));
        newuser.put("password", prop.getProperty("newuser.password"));
        newuser.put("email", prop.getProperty("newuser.email"));
        newuser.put("address1", prop.getProperty("newuser.address1"));
        newuser.put("city", prop.getProperty("newuser.city"));
        newuser.put("state", prop.getProperty("newuser.state"));
        newuser.put("zip", prop.getProperty("newuser.zip"));
        newuser.put("phone", prop.getProperty("newuser.phone"));
        logger.info("PersonHelper::initUser processed {}", prop.getProperty("newuser.username"));
        return newuser;
            
    }
        
    /**
     * @param args
     */
    public static HashMap<String, String> createUser(HashMap<String, String> newuserBase, String uniqueStr, Logger logger) {
                
        HashMap<String, String> newuser = new HashMap<String, String>();
                
        for (Map.Entry<String, String> entry : newuserBase.entrySet()) {
            newuser.put(entry.getKey(), entry.getValue().replace("%UNIQUE%", uniqueStr));
        }

        logger.info("PersonHelper::createUser processed {} for {}", newuserBase.get("username"), uniqueStr);
        return newuser;
            
    }
    
    /**
     * @param args
     */
    public static void createPerson(WebDriver driver, HashMap<String, String> userinfo, 
                                    String[] serviceorgname, String[] serviceorgtype, String[] serviceunitnumber, String[] servicedescription,
                                    boolean newsletter, String siteStyle, Logger logger) throws IOException {

        logger.info("PersonHelper::createPerson for {}", userinfo.get("username"));

        WebDriverWait wait = new WebDriverWait(driver, 30);
        WebElement latestElem = null;

        // Some sites need us to naviagate back to Home to get the create new user link
        if (siteStyle.equals("B")) {
            // This is the waterloo style
            // Menus only on the Home page, need to keep going to Home for the menu links
            latestElem = wait.until(Misc.visibilityOfElementLocated(By.linkText("HOME")));
            latestElem.click();
        }

        latestElem = wait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.linkText("Create an account"));
            }
        });
        latestElem.click();
            
        wait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("form_16"));
            }
        });
            
        // Now fill in the new profile form
        logger.info("PersonHelper::createPerson filling in newuser registration form.");
        for (Map.Entry<String, String> entry : userinfo.entrySet()) {
            driver.findElement(By.id("districtdb_persons___" + entry.getKey())).sendKeys(entry.getValue());
            if (entry.getKey().equals("password")) {
                // Password has a dupe field used for checking.
                driver.findElement(By.id("districtdb_persons___password_check")).sendKeys(entry.getValue());
            }
        }

        // Service processing
        for( int i = 0; i < serviceorgname.length; i++) {
            String orgelemid = "join___30___districtdb_service___orgid_" + i;
            String servicestr = serviceorgtype[i]+" "+serviceorgname[i]+" "+serviceunitnumber[i];
            servicestr = servicestr.replaceAll(" {2,}", " ").trim();
            Misc.selectValue(driver, orgelemid, servicestr, logger);
            String jobelemid = "join___30___districtdb_service___jobid_" + i;
            Misc.selectValue(driver, jobelemid, servicedescription[i], logger);
            driver.findElement(By.className("addGroup")).click();
        }

        // Now process the newsletter radio button
        WebElement newsletterelem = driver.findElement(By.id("districtdb_persons___newsletter")); 
        if (newsletter) {
            // In all of these clicking the link was giving:
            // Element is not currently visible and so may not be interacted with
            //newsletterelem.findElement(By.xpath("//input[@value='Yes']")).click();
            newsletterelem.findElement(By.className("fabrikgrid_Yes")).click();
        } else {
            //newsletterelem.findElement(By.xpath("//input[@value='No']")).click();
            newsletterelem.findElement(By.className("fabrikgrid_No")).click();
        }

        // If form has a 'captcha' then we need to do something special
        if (driver.findElements(By.id("districtdb_persons___captcha")).size() != 0) {
            logger.info("PersonHelper::createPerson captcha processed.");
            //String captchaText = JOptionPane.showInputDialog("Please enter the captcha text"); 
            //driver.findElement(By.id("districtdb_persons___captcha")).sendKeys(captchaText);
            driver.findElement(By.id("districtdb_persons___captcha")).sendKeys("xb9rjw");
        }

        // Click the Save button to finish up
        driver.findElement(By.name("submit")).click();

    }

    /**
     * @param args
     */
    public static boolean verifyProfile(WebDriver dadriver, HashMap<String, String> userinfo, 
                                        boolean confirmed, boolean nomail, 
                                        boolean hasprofile, String siteStyle, Logger logger) throws IOException {

        logger.info("PersonHelper::verifyProfile for {}", userinfo.get("username"));
            
        String fullname = "";
        String firstname = "";
        String lastname = "";
        if (userinfo.containsKey("name")) {
            fullname = userinfo.get("name");
            firstname = fullname.split(" ")[0];
            lastname = fullname.split(" ")[1];
        }
            
        List <String> elemlist = new ArrayList <String>(Arrays.asList("email", "phone", "address1", "city", "state", "zip"));
        boolean retval = false;
            
        // Get the main list
        Misc.getListPage(dadriver, "All People", siteStyle, logger);

        // Get the clear filters button
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("clearFilters"));
            }
        });
                
        // First - lets clear the filters - just in case
        latestElem.click();

        // Get the search field
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.name("fabrik_list_filter_all_2_com_fabrik_2"));
            }
        });
                
        latestElem.clear();
        latestElem.sendKeys(userinfo.get("username"));
        latestElem.submit();
                
        // Wait on the Person Search results
        // This is the Person table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("list_2_com_fabrik_2"));
            }
        });
                
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("PersonHelper::verifyProfile: {} does not exist at start.", userinfo.get("username"));
            if (hasprofile) {
                retval = false;
            } else {
                retval = true;
            }
        } else if (!hasprofile) {
            logger.info("PersonHelper::verifyProfile: {} exists and should not.", userinfo.get("username"));
            retval = false;
        } else {
            logger.info("PersonHelper::verifyProfile: {} does exist at start.", userinfo.get("username"));
            // Now the profile must match to be OK
            retval = true;
            // Need to avoid looking at the table header row
            WebElement tabledata = dadriver.findElement(By.className("fabrik_groupdata"));
            for (Map.Entry<String, String> entry : userinfo.entrySet()) {
                if (elemlist.contains(entry.getKey())) {
                    String elemval = tabledata.findElement(By.className("districtdb_persons___"+entry.getKey())).getText().trim();
                    if (!elemval.equals(entry.getValue())) {
                        logger.info("PersonHelper::verifyProfile elem mismatch {} {} {}", new Object[] {entry.getKey(), entry.getValue(), elemval});
                        retval = false;
                    }
                }
            }
            // Special case tests
            if (firstname.length() > 0) {
                if (!tabledata.findElement(By.className("districtdb_persons___firstname")).getText().trim().equals(firstname)) {
                    logger.info("PersonHelper::verifyProfile firstname mismatch {}", firstname);
                    retval = false;
                }                       
            }
            if (lastname.length() > 0) {
                if (!tabledata.findElement(By.className("districtdb_persons___lastname")).getText().trim().equals(lastname)) {
                    logger.info("PersonHelper::verifyProfile lastname mismatch {}", lastname);
                    retval = false;
                }                       
            }
            if (tabledata.findElement(By.className("districtdb_persons___confirmed")).findElements(By.tagName("img")).size() != 0) {
                if (!confirmed) {
                    logger.info("PersonHelper::verifyProfile confirmed has mismatch");
                    retval = false;
                }
            } else {
                if (confirmed) {
                    logger.info("PersonHelper::verifyProfile confirmed missing mismatch");
                    retval = false;
                }                               
            }
            if (tabledata.findElement(By.className("districtdb_persons___nomail")).findElements(By.tagName("img")).size() != 0) {
                if (!nomail) {
                    logger.info("PersonHelper::verifyProfile nomail has mismatch");
                    retval = false;
                }
            } else {
                if (nomail) {
                    logger.info("PersonHelper::verifyProfile nomail missing mismatch");
                    retval = false;
                }                               
            }
                        
        }
        return retval;   
    }


    /**
     * NOTE!!! The four service arrays must be alpha sorted in the order listed below.
     *         name = '' comes first.
     * This test verifies ALL the service for a user.
     */
    public static boolean verifyService(WebDriver dadriver, HashMap<String, String> userinfo, 
                                        String[] serviceorgname, String[] serviceorgtype, String[] serviceunitnumber, String[] servicedescription,
                                        boolean hasservice, String siteStyle, Logger logger) throws IOException {

        logger.info("PersonHelper::verifyService for {}", userinfo.get("username"));
        
        // Temporarily create firstname/lastname in a simple way
        String fullname = userinfo.get("name");
        String firstname = fullname.split(" ")[0];
        String lastname = fullname.split(" ")[1];
        
        // Create the comparison service descriptor
        StringBuffer servicedescexp = new StringBuffer();
        for (int i = 0; i < serviceorgname.length; i++) {
            servicedescexp.append(serviceorgname[i] + ":");
            servicedescexp.append(serviceorgtype[i] + ":");
            servicedescexp.append(serviceunitnumber[i] + ":");
            servicedescexp.append(servicedescription[i] + "::");
        }
        
        List <String> elemlist = new ArrayList <String>(Arrays.asList("email", "phone", "address1", "city", "state", "zip"));
        boolean retval = false;
        
        // Get the main list
        Misc.getListPage(dadriver, "All Service", siteStyle, logger);

        // Get the clear filters button
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("clearFilters"));
            }
        });
            
        // First - lets clear the filters - just in case
        latestElem.click();
        
        // Get the search field
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.name("fabrik_list_filter_all_7_com_fabrik_7"));
            }
        });
            
        latestElem.clear();
        latestElem.sendKeys(userinfo.get("username"));
        latestElem.submit();
        
        // Wait on the Service Search results
        // This is the Service table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("list_7_com_fabrik_7"));
            }
        });
        
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("PersonHelper::verifyService: {} has no service records.", userinfo.get("username"));
            if (hasservice) {
                retval = false;
            } else {
                retval = true;
            }
        } else if (!hasservice) {
            retval = false;
        } else {
            logger.info("PersonHelper::verifyService: {} has service record(s).", userinfo.get("username"));
            // Now the service must match to be OK
            retval = true;
            // Need to avoid looking a the table header row
            WebElement tabledata = dadriver.findElement(By.className("fabrik_groupdata"));
            // Grab all the service bits
            List <WebElement> description = (ArrayList <WebElement>) tabledata.findElements(By.className("districtdb_jobs___Description"));
            List <WebElement> orgtype = (ArrayList <WebElement>) tabledata.findElements(By.className("districtdb_orgs___OrgType"));
            List <WebElement> orgnumber = (ArrayList <WebElement>) tabledata.findElements(By.className("districtdb_orgs___OrgNumber"));
            List <WebElement> orgname = (ArrayList <WebElement>) tabledata.findElements(By.className("districtdb_orgs___OrgName"));
            // Mash it all together into one big service descriptor
            StringBuffer servicedescfnd = new StringBuffer();
            for ( int i = 0; i < description.size(); i++ ) {
                servicedescfnd.append(orgname.get(i).getText().trim() + ":");
                servicedescfnd.append(orgtype.get(i).getText().trim() + ":");
                servicedescfnd.append(orgnumber.get(i).getText().trim() + ":");
                servicedescfnd.append(description.get(i).getText().trim() + "::");
            }
            // logger.info("PersonHelper::verifyService: servicedescfnd {}", servicedescfnd);
            if (!servicedescfnd.toString().equals(servicedescexp.toString())) {
                logger.info("PersonHelper::verifyService: service mismatch {} : {}", servicedescfnd, servicedescexp);
                retval = false;
            }
        }
        return retval;
    }


    /**
     * @param args
     */
    public static void addPerson(WebDriver dadriver, Map<String, String> userinfo, boolean newsletter, boolean active, String siteStyle, Logger logger) throws IOException {

        logger.info("PersonHelper::addPerson for {}", userinfo.get("username"));

        // Get the main list
        Misc.getListPage(dadriver, "All People", siteStyle, logger);

        // Get the clear filters button
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("clearFilters"));
            }
        });
                
        // First - lets clear the filters - just in case
        latestElem.click();

        // Get the add button
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("addRecord"));
            }
        });

        // Click on add
        latestElem.click();
        
        // Wait for the Add Person form
        dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("form_2"));
            }
        });
        
        // Now fill in the add person form
        logger.info("PersonHelper::addPerson filling in newuser registration form.");
        for (Map.Entry<String, String> entry : userinfo.entrySet()) {
            // Skip username and password - those are only for the New Profile form
            if (!(entry.getKey().equals("username") || entry.getKey().equals("password"))) {
                dadriver.findElement(By.id("districtdb_persons___" + entry.getKey())).sendKeys(entry.getValue());
                if (entry.getKey().equals("password")) {
                    // Password has a dupe field used for checking.
                    dadriver.findElement(By.id("districtdb_persons___password_check")).sendKeys(entry.getValue());
                }
            }
        }

        // Now process the newsletter and active radio buttons
        WebElement newsletterelem = dadriver.findElement(By.id("districtdb_persons___newsletter")); 
        if (newsletter) {
            // In all of these clicking the link was giving:
            // Element is not currently visible and so may not be interacted with
            //newsletterelem.findElement(By.xpath("//input[@value='Yes']")).click();
            newsletterelem.findElement(By.className("fabrikgrid_Yes")).click();
        } else {
            //newsletterelem.findElement(By.xpath("//input[@value='No']")).click();
            newsletterelem.findElement(By.className("fabrikgrid_No")).click();
        }
        WebElement activeelem = dadriver.findElement(By.id("districtdb_persons___active")); 
        if (active) {
            //activeelem.findElement(By.xpath("//input[@value='Yes']")).click();
            activeelem.findElement(By.className("fabrikgrid_Yes")).click();
        } else {
            //activeelem.findElement(By.xpath("//input[@value='No']")).click();
            activeelem.findElement(By.className("fabrikgrid_No")).click();
        }


        // Click the Save button to finish up
        dadriver.findElement(By.name("submit")).click();

    }

    /**
     * @param args
     */
    public static void myProfile(WebDriver dadriver, Map<String, String> userinfo, boolean newsletter, String siteStyle, Logger logger) throws IOException {

        logger.info("PersonHelper::myProfile for {}", userinfo.get("username"));

        // Get the main list
        Misc.getListPage(dadriver, "My Profile", siteStyle, logger);

        // Wait for the form
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("form_1"));
            }
        });
                
        // Now fill in the My Profile form
        logger.info("PersonHelper::myProfile filling in My Profile form.");
        for (Map.Entry<String, String> entry : userinfo.entrySet()) {
            WebElement slot = dadriver.findElement(By.id("districtdb_persons___" + entry.getKey()));
            slot.clear();
            slot.sendKeys(entry.getValue());
            if (entry.getKey().equals("password")) {
                // Password has a dupe field used for checking.
                slot = dadriver.findElement(By.id("districtdb_persons___password_check"));
                slot.clear();
                slot.sendKeys(entry.getValue());
            }
        }

        // Now process the newsletter button
        WebElement newsletterelem = dadriver.findElement(By.id("districtdb_persons___newsletter")); 
        if (newsletter) {
            //newsletterelem.findElement(By.xpath("//input[@value='Yes']")).click();
            newsletterelem.findElement(By.className("fabrikgrid_Yes")).click();
        } else {
            //newsletterelem.findElement(By.xpath("//input[@value='No']")).click();
            newsletterelem.findElement(By.className("fabrikgrid_No")).click();
        }
        // Click the Save button to finish up
        dadriver.findElement(By.name("submit")).click();

    }

    /**
     * @param args
     */
    public static boolean editPerson(WebDriver dadriver, Map<String, String> userinfo, boolean newsletter, boolean active, boolean isjuser, String siteStyle, Logger logger) throws IOException {

        logger.info("PersonHelper::editPerson for {}", userinfo.get("username"));

        List <String> elemlist = new ArrayList <String>(Arrays.asList("phone", "address1", "city", "state", "zip"));
        boolean retval = true;
            
        // Get the main list
        Misc.getListPage(dadriver, "All People", siteStyle, logger);

        // Get the clear filters button
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("clearFilters"));
            }
        });
                
        // First - lets clear the filters - just in case
        latestElem.click();

        // Get the search field
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.name("fabrik_list_filter_all_2_com_fabrik_2"));
            }
        });
                
        latestElem.clear();
        latestElem.sendKeys(userinfo.get("username"));
        latestElem.submit();
                
        // Wait on the Person Search results
        // This is the Person table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("list_2_com_fabrik_2"));
            }
        });
                
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("PersonHelper::editPerson: {} does not exist.", userinfo.get("username"));
            retval = false;
        } else {
            // Now click on the 'edit person' icon in the search results table.
            dadriver.findElement(By.className("fabrik__rowlink")).click();
            // Now wait for the edit Person Profile form to show
            latestElem = dawait.until(new ExpectedCondition<WebElement>() {
                @Override public WebElement apply(WebDriver d) {
                    return d.findElement(By.id("form_2"));
                }
            });
            
            // Now check if the form is properly handling the JUser status.
            String juserstatus = dadriver.findElement(By.id("districtdb_persons___userid")).getText().trim();
            if ((isjuser && !juserstatus.contains("This person has a Joomla acct.")) ||
                (!isjuser && !juserstatus.contains("This person does not have a Joomla acct."))) {
                logger.info("PersonHelper::editPerson: {} has a mismatch in JUser status on form.", userinfo.get("username"));
                retval = false;
            } else {
                // JUser status is OK. Proceed with the form.
                for (Map.Entry<String, String> entry : userinfo.entrySet()) {
                    if (elemlist.contains(entry.getKey())) {
                        WebElement elemval = dadriver.findElement(By.id("districtdb_persons___"+entry.getKey()));
                        elemval.clear();
                        elemval.sendKeys(entry.getValue());
                    }
                }

                // Now process the newsletter and active radio buttons
                WebElement newsletterelem = dadriver.findElement(By.id("districtdb_persons___newsletter")); 
                if (newsletter) {
                    // In all of these clicking the link was giving:
                    // Element is not currently visible and so may not be interacted with
                    //newsletterelem.findElement(By.xpath("//input[@value='Yes']")).click();
                    newsletterelem.findElement(By.className("fabrikgrid_Yes")).click();
                } else {
                    //newsletterelem.findElement(By.xpath("//input[@value='No']")).click();
                    newsletterelem.findElement(By.className("fabrikgrid_No")).click();
                }
                WebElement activeelem = dadriver.findElement(By.id("districtdb_persons___active")); 
                if (active) {
                    //activeelem.findElement(By.xpath("//input[@value='Yes']")).click();
                    activeelem.findElement(By.className("fabrikgrid_Yes")).click();
                } else {
                    //activeelem.findElement(By.xpath("//input[@value='No']")).click();
                    activeelem.findElement(By.className("fabrikgrid_No")).click();
                }

                // Click the Save button to finish up
                dadriver.findElement(By.name("submit")).click();
            }
        }
        return retval;
    }




        
}

