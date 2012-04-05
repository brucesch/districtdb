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

public class OtherOrgHelper {

        

    /**
     * @param args
     */
    public static HashMap<String, String> initOtherOrg(Properties prop, Logger logger) {
                
        HashMap<String, String> newotherorg = new HashMap<String, String>();
        newotherorg.put("OrgName", prop.getProperty("newotherorg.OrgName"));
        newotherorg.put("Website", prop.getProperty("newotherorg.Website"));
        newotherorg.put("Phone", prop.getProperty("newotherorg.Phone"));
        newotherorg.put("Address", prop.getProperty("newotherorg.Address"));
        newotherorg.put("City", prop.getProperty("newotherorg.City"));
        newotherorg.put("State", prop.getProperty("newotherorg.State"));
        newotherorg.put("Zip", prop.getProperty("newotherorg.Zip"));
        logger.info("OtherOrgHelper::initOtherOrg processed {}", prop.getProperty("newotherorg.OrgName"));
        return newotherorg;
            
    }
        

    /**
     * @param args
     */
    public static HashMap<String, String> createOtherOrg(HashMap<String, String> newotherorgBase, String uniqueStr, Logger logger) {
                
        HashMap<String, String> newotherorg = new HashMap<String, String>();
                
        for (Map.Entry<String, String> entry : newotherorgBase.entrySet()) {
            newotherorg.put(entry.getKey(), entry.getValue().replace("%UNIQUE%", uniqueStr));
        }

        logger.info("OtherOrgHelper::createOtherOrg processed {} for {}", newotherorgBase.get("otherorgname"), uniqueStr);
        return newotherorg;
            
    }
    


    /**
     * @param args
     */
    public static boolean verifyOtherOrg(WebDriver dadriver, HashMap<String, String> othinfo, 
                                           boolean hasprofile, Logger logger) throws IOException {

        logger.info("OtherOrgHelper::verifyOtherOrg for {}", othinfo.get("OrgName"));
            
        List <String> elemlist = new ArrayList <String>(Arrays.asList("OrgName", "OrgType", "Website", "Phone", "Address", "City", "State", "Zip"));
        boolean retval = false;
            
        // Get the Other Orgs link.
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.linkText("Other Orgs"));
            }
        });

                
        // Bring up the Other Orgs table and then search for the specific CO.
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
                return d.findElement(By.name("fabrik_list_filter_all_5_com_fabrik_5"));
            }
        });
                
        latestElem.clear();
        latestElem.sendKeys(othinfo.get("OrgName"));
        latestElem.submit();
                
        // Wait on the Other Org Search results
        // This is the Other Org table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("listform_5_com_fabrik_5"));
            }
        });
                
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("OtherOrgHelper::verifyOtherOrg: {} does not exist at start.", othinfo.get("OrgName"));
            if (hasprofile) {
                retval = false;
            } else {
                retval = true;
            }
        } else if (!hasprofile) {
            logger.info("OtherOrgHelper::verifyOtherOrg: {} exists and should not.", othinfo.get("OrgName"));
        	retval = false;
        } else {
            logger.info("OtherOrgHelper::verifyOtherOrg: {} does exist at start.", othinfo.get("OrgName"));
            // Now the profile must match to be OK
            retval = true;
            // Need to avoid looking at the table header row
            WebElement tabledata = dadriver.findElement(By.className("fabrik_groupdata"));
            for (Map.Entry<String, String> entry : othinfo.entrySet()) {
                if (elemlist.contains(entry.getKey())) {
                    String elemval = tabledata.findElement(By.className("districtdb_orgs___"+entry.getKey())).getText().trim();
                    if (!elemval.equals(entry.getValue())) {
                        logger.info("OtherOrgHelper::verifyOtherOrg elem mismatch {} {} {}", new Object[] {entry.getKey(), entry.getValue(), elemval});
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
    public static void addOtherOrg(WebDriver dadriver, Map<String, String> othinfo, boolean active, Logger logger) throws IOException {

        logger.info("OtherOrgHelper::addOtherOrg for {}", othinfo.get("OrgName"));

        // Bring up the Other Orgs table and then search for the otherorg.
        dadriver.findElement(By.linkText("Other Orgs")).click();

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
        
        // Wait for the Add OtherOrg form
        dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("form_5"));
            }
        });
        
        // Now fill in the add Other Org form
        logger.info("OtherOrgHelper::addOtherOrg filling in org form.");
        for (Map.Entry<String, String> entry : othinfo.entrySet()) {
            // Special handling for the OtherOrgType which is a dropdown
            if (!entry.getKey().equals("OtherOrgType")) {
                dadriver.findElement(By.id("districtdb_orgs___" + entry.getKey())).sendKeys(entry.getValue());
            } else {
                Misc.selectValue(dadriver, "districtdb_orgs___OtherOrgType", entry.getValue(), logger);
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
    public static boolean editOtherOrg(WebDriver dadriver, Map<String, String> othinfo, boolean active, Logger logger) throws IOException {

        logger.info("OtherOrgHelper::editOtherOrg for {}", othinfo.get("OrgName"));

        boolean retval = true;

        // Get the Other Orgs link.
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.linkText("Other Orgs"));
            }
        });

                
        // Bring up the Other Orgs table and then search for the specific CO.
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
                return d.findElement(By.name("fabrik_list_filter_all_5_com_fabrik_5"));
            }
        });
                
        latestElem.clear();
        latestElem.sendKeys(othinfo.get("OrgName"));
        latestElem.submit();
                
        // Wait on the CO Search results
        // This is the CO table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("listform_5_com_fabrik_5"));
            }
        });
                
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("OtherOrgHelper::verifyOtherOrg: {} does not exist at start.", othinfo.get("OrgName"));
            retval = false;
        } else {
            // Now click on the 'edit other org' icon in the search results table.
            // The Edit link should be the first one encountered. The other is the "Details" link.
            dadriver.findElement(By.className("fabrik__rowlink")).click();
            // Now wait for the edit Other Org form to show
            latestElem = dawait.until(new ExpectedCondition<WebElement>() {
                @Override public WebElement apply(WebDriver d) {
                    return d.findElement(By.id("form_5"));
                }
            });
        
            // Now fill in the edit Other Org form
            logger.info("OtherOrgHelper::editOtherOrg filling in org form.");
            for (Map.Entry<String, String> entry : othinfo.entrySet()) {
                // Special handling for the OrgType which is a dropdown
                if (!entry.getKey().equals("OrgType")) {
                    dadriver.findElement(By.id("districtdb_orgs___" + entry.getKey())).clear();
                    dadriver.findElement(By.id("districtdb_orgs___" + entry.getKey())).sendKeys(entry.getValue());
                } else {
                    Misc.selectValue(dadriver, "districtdb_orgs___OrgType", entry.getValue(), logger);
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

    
