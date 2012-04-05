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

public class CharterOrgHelper {

        

    /**
     * @param args
     */
    public static HashMap<String, String> initCharterOrg(Properties prop, Logger logger) {
                
        HashMap<String, String> newcharterorg = new HashMap<String, String>();
        newcharterorg.put("OrgName", prop.getProperty("newcharterorg.OrgName"));
        newcharterorg.put("Website", prop.getProperty("newcharterorg.Website"));
        newcharterorg.put("Phone", prop.getProperty("newcharterorg.Phone"));
        newcharterorg.put("Address", prop.getProperty("newcharterorg.Address"));
        newcharterorg.put("City", prop.getProperty("newcharterorg.City"));
        newcharterorg.put("State", prop.getProperty("newcharterorg.State"));
        newcharterorg.put("Zip", prop.getProperty("newcharterorg.Zip"));
        logger.info("CharterOrgHelper::initCharterOrg processed {}", prop.getProperty("newcharterorg.OrgName"));
        return newcharterorg;
            
    }
        

    /**
     * @param args
     */
    public static HashMap<String, String> createCharterOrg(HashMap<String, String> newcharterorgBase, String uniqueStr, Logger logger) {
                
        HashMap<String, String> newcharterorg = new HashMap<String, String>();
                
        for (Map.Entry<String, String> entry : newcharterorgBase.entrySet()) {
            newcharterorg.put(entry.getKey(), entry.getValue().replace("%UNIQUE%", uniqueStr));
        }

        logger.info("CharterOrgHelper::createCharterOrg processed {} for {}", newcharterorgBase.get("charterorgname"), uniqueStr);
        return newcharterorg;
            
    }
    


    /**
     * @param args
     */
    public static boolean verifyCharterOrg(WebDriver dadriver, HashMap<String, String> coinfo, 
                                           boolean hasprofile, Logger logger) throws IOException {

        logger.info("CharterOrgHelper::verifyCharterOrg for {}", coinfo.get("OrgName"));
            
        List <String> elemlist = new ArrayList <String>(Arrays.asList("OrgName", "CharterOrgType", "Website", "Phone", "Address", "City", "State", "Zip"));
        boolean retval = false;
            
        // Get the Charter Orgs link.
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.linkText("Charter Orgs"));
            }
        });

                
        // Bring up the Charter Orgs table and then search for the specific CO.
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
                return d.findElement(By.name("fabrik_list_filter_all_3_com_fabrik_3"));
            }
        });
                
        latestElem.clear();
        latestElem.sendKeys(coinfo.get("OrgName"));
        latestElem.submit();
                
        // Wait on the CO Search results
        // This is the CO table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("listform_3_com_fabrik_3"));
            }
        });
                
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("CharterOrgHelper::verifyCharterOrg: {} does not exist at start.", coinfo.get("OrgName"));
            if (hasprofile) {
                retval = false;
            } else {
                retval = true;
            }
        } else if (!hasprofile) {
            logger.info("CharterOrgHelper::verifyCharterOrg: {} exists and should not.", coinfo.get("OrgName"));
        	retval = false;
        } else {
            logger.info("CharterOrgHelper::verifyCharterOrg: {} does exist at start.", coinfo.get("OrgName"));
            // Now the profile must match to be OK
            retval = true;
            // Need to avoid looking at the table header row
            WebElement tabledata = dadriver.findElement(By.className("fabrik_groupdata"));
            for (Map.Entry<String, String> entry : coinfo.entrySet()) {
                if (elemlist.contains(entry.getKey())) {
                    String elemval = tabledata.findElement(By.className("districtdb_orgs___"+entry.getKey())).getText().trim();
                    if (!elemval.equals(entry.getValue())) {
                        logger.info("CharterOrgHelper::verifyCharterOrg elem mismatch {} {} {}", new Object[] {entry.getKey(), entry.getValue(), elemval});
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
    public static void addCharterOrg(WebDriver dadriver, Map<String, String> coinfo, boolean active, Logger logger) throws IOException {

        logger.info("CharterOrgHelper::addCharterOrg for {}", coinfo.get("OrgName"));

        // Bring up the Charter Orgs table and then search for the charterorg.
        dadriver.findElement(By.linkText("Charter Orgs")).click();

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
        
        // Wait for the Add CharterOrg form
        dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("form_3"));
            }
        });
        
        // Now fill in the add Charter Org form
        logger.info("CharterOrgHelper::addCharterOrg filling in org form.");
        for (Map.Entry<String, String> entry : coinfo.entrySet()) {
            // Special handling for the CharterOrgType which is a dropdown
            if (!entry.getKey().equals("CharterOrgType")) {
                dadriver.findElement(By.id("districtdb_orgs___" + entry.getKey())).sendKeys(entry.getValue());
            } else {
                Misc.selectValue(dadriver, "districtdb_orgs___CharterOrgType", entry.getValue(), logger);
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
    public static boolean editCharterOrg(WebDriver dadriver, Map<String, String> coinfo, boolean active, Logger logger) throws IOException {

        logger.info("CharterOrgHelper::editCharterOrg for {}", coinfo.get("OrgName"));

        boolean retval = true;

        // Get the Charter Orgs link.
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.linkText("Charter Orgs"));
            }
        });

                
        // Bring up the Charter Orgs table and then search for the specific CO.
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
                return d.findElement(By.name("fabrik_list_filter_all_3_com_fabrik_3"));
            }
        });
                
        latestElem.clear();
        latestElem.sendKeys(coinfo.get("OrgName"));
        latestElem.submit();
                
        // Wait on the CO Search results
        // This is the CO table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("listform_3_com_fabrik_3"));
            }
        });
                
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("CharterOrgHelper::verifyCharterOrg: {} does not exist at start.", coinfo.get("OrgName"));
            retval = false;
        } else {
            // Now click on the 'edit charter org' icon in the search results table.
            // The Edit link should be the first one encountered. The other is the "Details" link.
            dadriver.findElement(By.className("fabrik__rowlink")).click();
            // Now wait for the edit Charter Org form to show
            latestElem = dawait.until(new ExpectedCondition<WebElement>() {
                @Override public WebElement apply(WebDriver d) {
                    return d.findElement(By.id("form_3"));
                }
            });
        
            // Now fill in the edit Charter Org form
            logger.info("CharterOrgHelper::editCharterOrg filling in org form.");
            for (Map.Entry<String, String> entry : coinfo.entrySet()) {
                // Special handling for the CharterOrgType which is a dropdown
                if (!entry.getKey().equals("CharterOrgType")) {
                    dadriver.findElement(By.id("districtdb_orgs___" + entry.getKey())).clear();
                    dadriver.findElement(By.id("districtdb_orgs___" + entry.getKey())).sendKeys(entry.getValue());
                } else {
                    Misc.selectValue(dadriver, "districtdb_orgs___CharterOrgType", entry.getValue(), logger);
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

    
