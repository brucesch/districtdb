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

public class UnitHelper {

        

    /**
     * @param args
     */
    public static HashMap<String, String> initUnit(Properties prop, Logger logger) {
                
        HashMap<String, String> newunit = new HashMap<String, String>();
        newunit.put("OrgType", prop.getProperty("newunit.OrgType"));
        newunit.put("Location", prop.getProperty("newunit.Location"));
        newunit.put("Website", prop.getProperty("newunit.Website"));
        newunit.put("Phone", prop.getProperty("newunit.Phone"));
        newunit.put("Address", prop.getProperty("newunit.Address"));
        newunit.put("City", prop.getProperty("newunit.City"));
        newunit.put("State", prop.getProperty("newunit.State"));
        newunit.put("Zip", prop.getProperty("newunit.Zip"));
        logger.info("UnitHelper::initUnit processed {}", prop.getProperty("newunit.OrgNumber"));
        return newunit;
            
    }
        

    /**
     * @param args
     */
    public static HashMap<String, String> createUnit(HashMap<String, String> newunitBase, String uniqueStr, Logger logger) {
                
        HashMap<String, String> newunit = new HashMap<String, String>();
                
        for (Map.Entry<String, String> entry : newunitBase.entrySet()) {
            newunit.put(entry.getKey(), entry.getValue().replace("%UNIQUE%", uniqueStr));
        }

        logger.info("UnitHelper::createUnit processed {} for {}", newunitBase.get("unitname"), uniqueStr);
        return newunit;
            
    }
    


    /**
     * @param args
     */
    public static boolean verifyUnit(WebDriver dadriver, HashMap<String, String> unitinfo, 
                                     boolean hasprofile, Logger logger) throws IOException {

        logger.info("UnitHelper::verifyUnit for {}", unitinfo.get("OrgNumber"));
            
        List <String> elemlist = new ArrayList <String>(Arrays.asList("OrgType", "OrgNumber", "Location", "Website", "CharterOrgType", "OrgName", "Phone", "Address", "City", "State", "Zip"));
        boolean retval = false;
            
        // Get the Units link.
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.linkText("Units"));
            }
        });

                
        // Bring up the Units table and then search for the specific Unit.
        latestElem.click();

        // Get the clear filters button
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("clearFilters"));
            }
        });
                
        // First - lets clear the filters - just in case
        latestElem.click();

        // Get the search field
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.name("fabrik_list_filter_all_4_com_fabrik_4"));
            }
        });
                
        latestElem.clear();
        latestElem.sendKeys(unitinfo.get("OrgNumber"));
        latestElem.submit();
                
        // Wait on the Unit Search results
        // This is the Unit table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("listform_4_com_fabrik_4"));
            }
        });
                
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("UnitHelper::verifyUnit: {} does not exist at start.", unitinfo.get("OrgNumber"));
            if (hasprofile) {
                retval = false;
            } else {
                retval = true;
            }
        } else if (!hasprofile) {
            logger.info("UnitHelper::verifyUnit: {} exists and should not.", unitinfo.get("OrgNumber"));
            retval = false;
        } else {
            logger.info("UnitHelper::verifyUnit: {} does exist at start.", unitinfo.get("OrgNumber"));
            // Now the profile must match to be OK
            retval = true;
            // Need to avoid looking at the table header row
            WebElement tabledata = dadriver.findElement(By.className("fabrik_groupdata"));
            for (Map.Entry<String, String> entry : unitinfo.entrySet()) {
                if (elemlist.contains(entry.getKey())) {
                    // CharterOrgType and OrgName need special treatment because they are in the view
                	String elemval;
                    if (!(entry.getKey().equals("CharterOrgType") || entry.getKey().equals("OrgName"))) {
                        elemval = tabledata.findElement(By.className("districtdb_orgs___"+entry.getKey())).getText().trim();
                    } else {
                        elemval = tabledata.findElement(By.className("districtdb_orgs_view___"+entry.getKey())).getText().trim();
                    }
                    if (!elemval.equals(entry.getValue())) {
                        logger.info("UnitHelper::verifyUnit elem mismatch {} {} {}", new Object[] {entry.getKey(), entry.getValue(), elemval});
                        retval = false;
                    }
                }
            }
                        
        }
        return retval;   
    }


    /**
     * @param args
     */
    public static void addUnit(WebDriver dadriver, Map<String, String> unitinfo, boolean active, Logger logger) throws IOException {

        logger.info("UnitHelper::addUnit for {}", unitinfo.get("OrgNumber"));

        List <String> elemlistwoco = new ArrayList <String>(Arrays.asList("OrgType", "OrgNumber", "Location", "Website", "CharterOrgID", "Phone", "Address", "City", "State", "Zip"));
        List <String> elemlistwco  = new ArrayList <String>(Arrays.asList("OrgType", "OrgNumber", "Location", "Website", "CharterOrgID"));
        List <String> elemlist;

        // Bring up the Units table.
        dadriver.findElement(By.linkText("Units")).click();

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
        
        // Wait for the Add Unit form
        dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("form_14"));
            }
        });
        
        // First need to process the GetInfoFromCharterOrg radio to get the form in the proper format
        WebElement GetInfoFromCharterOrgelem = dadriver.findElement(By.id("districtdb_orgs___GetInfoFromCharterOrg")); 
        if (unitinfo.get("GetInfoFromCharterOrg").equals("Yes")) {
            GetInfoFromCharterOrgelem.findElement(By.className("fabrikgrid_Yes")).click();
            elemlist = elemlistwco;
        } else {
            GetInfoFromCharterOrgelem.findElement(By.className("fabrikgrid_No")).click();
            elemlist = elemlistwoco;
        }
                
        // Now fill in the add Unit form
        logger.info("UnitHelper::addUnit filling in org form.");
        for (Map.Entry<String, String> entry : unitinfo.entrySet()) {
            if (elemlist.contains(entry.getKey())) {
                // Special handling for OrgType and CharterOrgID which are dropdowns
                if (!(entry.getKey().equals("OrgType") || entry.getKey().equals("CharterOrgID"))) {
                    dadriver.findElement(By.id("districtdb_orgs___" + entry.getKey())).sendKeys(entry.getValue());
                } else {
                    Misc.selectValue(dadriver, "districtdb_orgs___" + entry.getKey(), entry.getValue(), logger);
                }
            }
        }

        // Now process the active radio buttons
        WebElement activeelem = dadriver.findElement(By.id("districtdb_orgs___active")); 
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
    public static boolean editUnit(WebDriver dadriver, Map<String, String> unitinfo, boolean active, Logger logger) throws IOException {

        logger.info("UnitHelper::editUnit for {}", unitinfo.get("OrgNumber"));

        List <String> elemlistwoco = new ArrayList <String>(Arrays.asList("OrgType", "OrgNumber", "Location", "Website", "CharterOrgID", "Phone", "Address", "City", "State", "Zip"));
        List <String> elemlistwco = new ArrayList <String>(Arrays.asList("OrgType", "OrgNumber", "Location", "Website", "CharterOrgID"));
        List <String> elemlist;

        boolean retval = true;

        // Get the Units link.
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.linkText("Units"));
            }
        });

                
        // Bring up the Units table and then search for the specific Unit.
        latestElem.click();

        // Get the clear filters button
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("clearFilters"));
            }
        });
                
        // First - lets clear the filters - just in case
        latestElem.click();

        // Get the search field
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.name("fabrik_list_filter_all_4_com_fabrik_4"));
            }
        });
                
        latestElem.clear();
        latestElem.sendKeys(unitinfo.get("OrgNumber"));
        latestElem.submit();
                
        // Wait on the Unit Search results
        // This is the Unit table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("listform_4_com_fabrik_4"));
            }
        });
                
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("UnitHelper::verifyUnit: {} does not exist at start.", unitinfo.get("OrgNumber"));
            retval = false;
        } else {
            // Now click on the 'edit unit org' icon in the search results table.
            // The Edit link should be the first one encountered. The other is the "Details" link.
            dadriver.findElement(By.className("fabrik__rowlink")).click();
            // Now wait for the edit Unit form to show
            latestElem = dawait.until(new ExpectedCondition<WebElement>() {
                @Override public WebElement apply(WebDriver d) {
                    return d.findElement(By.id("form_14"));
                }
            });
        
            // First need to process the GetInfoFromCharterOrg radio to get the form in the proper format
            WebElement GetInfoFromCharterOrgelem = dadriver.findElement(By.id("districtdb_orgs___GetInfoFromCharterOrg")); 
            if (unitinfo.get("GetInfoFromCharterOrg").equals("Yes")) {
                GetInfoFromCharterOrgelem.findElement(By.className("fabrikgrid_Yes")).click();
                elemlist = elemlistwco;
            } else {
                GetInfoFromCharterOrgelem.findElement(By.className("fabrikgrid_No")).click();
                elemlist = elemlistwoco;
            }
                
            // Now fill in the add Unit form
            logger.info("UnitHelper::addUnit filling in org form.");
            for (Map.Entry<String, String> entry : unitinfo.entrySet()) {
                if (elemlist.contains(entry.getKey())) {
                    // Special handling for OrgType and CharterOrgID which are dropdowns
                    if (!(entry.getKey().equals("OrgType") || entry.getKey().equals("CharterOrgID"))) {
                        dadriver.findElement(By.id("districtdb_orgs___" + entry.getKey())).clear();
                        dadriver.findElement(By.id("districtdb_orgs___" + entry.getKey())).sendKeys(entry.getValue());
                    } else {
                        Misc.selectValue(dadriver, "districtdb_orgs___" + entry.getKey(), entry.getValue(), logger);
                    }
                }
            }

            // Now process the active radio buttons
            WebElement activeelem = dadriver.findElement(By.id("districtdb_orgs___active")); 
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

        return retval;

    }


}

    
