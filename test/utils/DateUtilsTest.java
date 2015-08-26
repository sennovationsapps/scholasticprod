package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Test;

import base.utils.DateUtils;


public class DateUtilsTest {
	
	@Test
	public void testFormatExpDate() {
		try {
			Date date = new Date(114, 0, 1);
			String str = DateUtils.formatExpDate(date);
			assertEquals("The date should reformat to a MMYY", str, "0114");
		} catch (Exception e) {
			fail("An exception should not have occurred - " + e.getMessage());
		}
	}
	
	@Test
	public void testFormatReconcileDate() {
		try {
			Date date = new Date(114, 0, 1);
			String str = DateUtils.formatReconcileDate(date);
			assertEquals("The date should reformat to a MM/DD/YYYY", str, "01/01/2014");
		} catch (Exception e) {
			fail("An exception should not have occurred - " + e.getMessage());
		}
	}
	
}
