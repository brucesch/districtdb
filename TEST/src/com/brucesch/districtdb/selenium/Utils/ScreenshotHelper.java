package com.brucesch.districtdb.selenium.Utils;

import java.io.File; 
import java.io.IOException;
import org.apache.commons.io.FileUtils; 

import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.OutputType; 

public class ScreenshotHelper {

	/**
	 * @param args
	 */
	public void saveScreenshot(WebDriver driver, String screenshotFileName) throws IOException {
		 File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
	     FileUtils.copyFile(screenshot, new File(screenshotFileName)); 

	}

}
