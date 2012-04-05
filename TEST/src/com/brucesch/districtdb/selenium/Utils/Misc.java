package com.brucesch.districtdb.selenium.Utils;

import java.io.File; 
import java.io.IOException;
import org.apache.commons.io.FileUtils; 
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType; 
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;

import com.brucesch.districtdb.selenium.Utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Misc {

	/**
	 * @param args
	 */
	public static void selectValue(WebDriver driver, String elemid, String valToBeSelected, Logger logger){
	    logger.info("Misc::selectValue {} {}", elemid, valToBeSelected);
	    WebElement dropdown = driver.findElement(By.id(elemid));
        List <WebElement> options = dropdown.findElements(By.tagName("option"));
	    for (WebElement option : options) {
		    if (valToBeSelected.equalsIgnoreCase(option.getText())){
		        option.click();
                logger.info("Misc::selectValue {} {} found", elemid, valToBeSelected);
		    }
        }
	}

	/**
	 * @param args
	 */
	public static boolean verifyMsg(WebDriver driver, String msgtype, String msg, Logger logger){

		logger.info("Misc::verifyMsg {} {}", msgtype, msg);
		
        boolean retval = false;
        
        ExpectedCondition<WebElement> hdrmsg = new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("fabrikMainError"));
            }
        };
        
        ExpectedCondition<WebElement> detmsg = new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver d) {
                return d.findElement(By.className("fabrikErrorMessage"));
            }
        };
        
        ExpectedCondition<WebElement> usemsg;
        
        if (msgtype.equals("hdrmsg")) {
        	usemsg = hdrmsg;
        } else {
        	usemsg = detmsg;
        }
        
		// Start off by waiting to make sure that the msg is there
		WebDriverWait wait = new WebDriverWait(driver, 30);
		WebElement latestElem = wait.until(usemsg);
		

		// Check for message
        if (msgtype.equals("hdrmsg")) {
        	if (latestElem.getText().contains(msg)) {
        		retval = true;
        	} else {	
        		logger.info("msg mismatch  type: {}  exp: {}  fnd: {}", new Object[] {msgtype, msg, latestElem.getText()});
			}
        } else {
        	// Need to walk all the element err elems looking for specific err msg
            // Grab all the msg elems
            List <WebElement> msgelems = (ArrayList <WebElement>) driver.findElements(By.className("fabrikErrorMessage"));
            // Walk and look for msg
            for ( int i = 0; i < msgelems.size(); i++ ) {
            	// 'floatingtitle' is a non-standard attribute so this is more brute force
            	WebElement melem = msgelems.get(i);
            	String contents = (String)((JavascriptExecutor)driver).executeScript("return arguments[0].innerHTML;", melem);
                // logger.info("Misc::verifyMsg found txt {} {}", i, contents);
	            if (contents.contains(msg)) {
		            retval = true;
                }
            }
            if (!retval) {
        		logger.info("msg mismatch  type: {}  exp: {}", msgtype, msg);
            }
        }
		
		return retval;
		
	}
	
}

