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

public class ServiceHelper {


    /**
     * @param args
     */
    public static HashMap<String, String> initJob(Properties prop, Logger logger) {
                
        HashMap<String, String> newjob = new HashMap<String, String>();
        newjob.put("jobtype", prop.getProperty("newjob.jobtype"));
        newjob.put("Title", prop.getProperty("newjob.Title"));
        newjob.put("Description", prop.getProperty("newjob.Description"));
        logger.info("JobHelper::initJob processed  {}", prop.getProperty("newjob.Description"));
        return newjob;
            
    }
        

    /**
     * @param args
     */
    public static HashMap<String, String> createJob(HashMap<String, String> newjobBase, String uniqueStr, Logger logger) {
                
        HashMap<String, String> newjob = new HashMap<String, String>();
                
        for (Map.Entry<String, String> entry : newjobBase.entrySet()) {
            newjob.put(entry.getKey(), entry.getValue().replace("%UNIQUE%", uniqueStr));
        }

        logger.info("JobHelper::createJob processed {} for {}", newjobBase.get("Description"), uniqueStr);
        return newjob;
            
    }
    
    /**
     * @param args
     */
    public static boolean verifyJob(WebDriver dadriver, HashMap<String, String> jobinfo, 
                                           boolean hasprofile, Logger logger) throws IOException {

        logger.info("JobHelper::verifyJob for {}", jobinfo.get("Description"));
            
        boolean retval = false;
            
        // Get the Jobs link.
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.linkText("Jobs"));
            }
        });

                
        // Bring up the Jobs table and then search for the specific user.
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
                return d.findElement(By.name("fabrik_list_filter_all_6_com_fabrik_6"));
            }
        });
                
        latestElem.clear();
        latestElem.sendKeys(jobinfo.get("Description"));
        latestElem.submit();
                
        // Wait on the Job Search results
        // This is the Job table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("listform_6_com_fabrik_6"));
            }
        });
                
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("JobHelper::verifyJob: {} does not exist at start.", jobinfo.get("Description"));
            if (hasprofile) {
                retval = false;
            } else {
                retval = true;
            }
        } else if (!hasprofile) {
            logger.info("JobHelper::verifyJob: {} exists and should not.", jobinfo.get("Description"));
                retval = false;
        } else {
            logger.info("JobHelper::verifyJob: {} does exist at start.", jobinfo.get("Description"));
            // Now the profile must match to be OK
            retval = true;
            // Need to avoid looking at the table header row
            WebElement tabledata = dadriver.findElement(By.className("fabrik_groupdata"));
            for (Map.Entry<String, String> entry : jobinfo.entrySet()) {
                String elemval = tabledata.findElement(By.className("districtdb_jobs___"+entry.getKey())).getText().trim();
                if (!elemval.equals(entry.getValue())) {
                    logger.info("JobHelper::verifyJob elem mismatch {} {} {}", new Object[] {entry.getKey(), entry.getValue(), elemval});
                    retval = false;
                }
            }
                        
        }
        return retval;   
    }


    /**
     * @param args
     */
    public static void addJob(WebDriver dadriver, Map<String, String> jobinfo, Logger logger) throws IOException {

        logger.info("JobHelper::addJob");

        // Bring up the Jobs table and then search for the job.
        dadriver.findElement(By.linkText("Jobs")).click();

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
        
        // Wait for the Add Job form
        dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("form_6"));
            }
        });
        
        // Now fill in the add Job form
        logger.info("JobHelper::addJob filling in job form.");
        for (Map.Entry<String, String> entry : jobinfo.entrySet()) {
            // Special handling for the jobtype which is a dropdown
            if (!entry.getKey().equals("jobtype")) {
                dadriver.findElement(By.id("districtdb_jobs___" + entry.getKey())).sendKeys(entry.getValue());
            } else {
                Misc.selectValue(dadriver, "districtdb_jobs___jobtype", entry.getValue(), logger);
            }
        }

        // Click the Save button to finish up
        dadriver.findElement(By.name("submit")).click();

    }

    /**
     * This test verifies that the user has this one service out of a possible list of service.
     */
    public static int verifyMyService(WebDriver dadriver,
                                        String serviceorgname, String serviceorgtype, String serviceunitnumber, String servicedescription,
                                        boolean hasservice, Logger logger) throws IOException {

        logger.info("ServiceHelper::verifyMyService single");
        
        // Create the comparison service descriptor
        StringBuffer servicedescexp = new StringBuffer();
        servicedescexp.append(serviceorgname + ":");
        servicedescexp.append(serviceorgtype + ":");
        servicedescexp.append(serviceunitnumber + ":");
        servicedescexp.append(servicedescription + "::");

        ArrayList <String> foundsvc = new ArrayList <String> ();
        
        int retval = -1;
        
        // Bring up the All Service form and then search for the user.
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.linkText("My Service"));
            }
        });
        latestElem.click();

        // No filters on My Service
        // No search on My Service
        // This is the My Service table
        latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("listform_8_com_fabrik_8"));
            }
        });
        
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("ServiceHelper::verifyMyService: has no service records.");
            if (hasservice) {
                retval = -1;
            } else {
                retval = 0;
            }
        } else if (!hasservice) {
            retval = -1;
        } else {
            logger.info("ServiceHelper::verifyMyService: has service record(s).");
            // Now the service must match to be OK
            // Need to avoid looking a the table header row
            WebElement tabledata = dadriver.findElement(By.className("fabrik_groupdata"));
            // Grab all the service bits
            List <WebElement> description = (ArrayList <WebElement>) tabledata.findElements(By.className("districtdb_jobs___Description"));
            List <WebElement> orgtype = (ArrayList <WebElement>) tabledata.findElements(By.className("districtdb_orgs___OrgType"));
            List <WebElement> orgnumber = (ArrayList <WebElement>) tabledata.findElements(By.className("districtdb_orgs___OrgNumber"));
            List <WebElement> orgname = (ArrayList <WebElement>) tabledata.findElements(By.className("districtdb_orgs___OrgName"));
            // Create found service array
            StringBuffer servicedescfnd = new StringBuffer();
            for ( int i = 0; i < description.size(); i++ ) {
                servicedescfnd.append(orgname.get(i).getText().trim() + ":");
                servicedescfnd.append(orgtype.get(i).getText().trim() + ":");
                servicedescfnd.append(orgnumber.get(i).getText().trim() + ":");
                servicedescfnd.append(description.get(i).getText().trim() + "::");
                foundsvc.add(servicedescfnd.toString());
                servicedescfnd.setLength(0);
            }
            // logger.info("ServiceHelper::verifyMyService: servicedescfnd {}", servicedescfnd);
            retval = foundsvc.indexOf(servicedescexp.toString());
        }
        return retval;
    }


    /**
     * @param args
     */
    public static void addMyService(WebDriver dadriver, Map<String, String> svcinfo, Logger logger) throws IOException {

        logger.info("ServiceHelper::addMyService for {}", svcinfo.get("Description"));

        // Bring up the Services table and then search for the service.
        dadriver.findElement(By.linkText("My Service")).click();

        // No filters on My Service

        // Get the add button
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("addRecord"));
            }
        });

        // Click on add
        latestElem.click();
        
        // Wait for the Add Service form
        dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("form_12"));
            }
        });
        
        // Now fill in the add Service form
        logger.info("ServiceHelper::addMyService filling in svc form.");
        // Cascading dropdowns so need to start with the orgid
        Misc.selectValue(dadriver, "districtdb_service___orgid", svcinfo.get("orgid"), logger);

        // Now jobid
        Misc.selectValue(dadriver, "districtdb_service___jobid", svcinfo.get("jobid"), logger);

        // Click the Save button to finish up
        dadriver.findElement(By.name("submit")).click();

    }

    /**
     * Deactivate a My Service entry by position
     */
    public static boolean deactivateMyService(WebDriver dadriver, int myservice,
                                        boolean hasservice, Logger logger) throws IOException {

        logger.info("ServiceHelper::deactivateMyService single");
        
        boolean retval = false;
        
        // Bring up the All Service form and then search for the user.
        dadriver.findElement(By.linkText("My Service")).click();

        // No filters on My Service
        // No search on My Service
        // This is the My Service table
        WebDriverWait dawait = new WebDriverWait(dadriver, 30);
        WebElement latestElem = dawait.until(new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.id("listform_8_com_fabrik_8"));
            }
        });
        
        // Empty results?
        if (latestElem.getText().indexOf("No records") >= 0) {
            logger.info("ServiceHelper::deactivateMyService: has no service records.");
            if (hasservice) {
                retval = false;
            } else {
                retval = true;
            }
        } else if (!hasservice) {
            retval = false;
        } else {
            logger.info("ServiceHelper::deactivateMyService: has service record(s).");
            // Look for and then press the nth edit button
            // Need to avoid looking a the table header row
            WebElement tabledata = dadriver.findElement(By.className("fabrik_groupdata"));
            List <WebElement> editbuttons = tabledata.findElements(By.className("fabrik__rowlink"));
            editbuttons.get(myservice).click();

            // Now wait for the edit my service panel
            latestElem = dawait.until(new ExpectedCondition<WebElement>() {
                @Override public WebElement apply(WebDriver d) {
                    return d.findElement(By.id("form_13"));
                }
            });

            // Click on active No
            WebElement activeelem = dadriver.findElement(By.id("districtdb_service___active")); 
            activeelem.findElement(By.className("fabrikgrid_No")).click();

            // Click the Save button to finish up
            dadriver.findElement(By.name("submit")).click();

            retval = true;

        }
        return retval;
    }



}

    
