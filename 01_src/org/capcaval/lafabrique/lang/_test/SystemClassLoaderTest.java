package org.capcaval.lafabrique.lang._test;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.capcaval.lafabrique.lang.SystemClassLoader;

public class SystemClassLoaderTest {
	@org.junit.Test
	public void testLoadingCLassFileFromFile(){
		File f = new File("/home/mik/temp/C3/test");
		SystemClassLoader scm = null;
		
		try {
			scm =  new SystemClassLoader(f.toURI().toURL());
		
			Class<?> c = scm.loadClass("Test14ValueInteger");
			Object o = c.newInstance();
			String strValue = o.toString();
			System.out.println(strValue);
			
			Assert.assertEquals("14", strValue);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				scm.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

//	@org.junit.Test
//	public void testLoadingCLassFileFromString(){
//		SystemClassLoader scm = null;
//		try {
//			scm = new SystemClassLoader("/home/mik/temp/C3/test");
//		
//			Class<?> c = scm.loadClass("Test14ValueInteger");
//			Object o = c.newInstance();
//			String strValue = o.toString();
//			System.out.println(strValue);
//			
//			Assert.assertEquals("14", strValue);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail();
//		}finally{
//			try {
//				scm.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	@org.junit.Test
	public void testLoadingCLassFileFromStringError(){
		try {
			SystemClassLoader scm = new SystemClassLoader("/home/mik/temp/C3/test");
		
			Class<?> c = scm.loadClass("ClassThatDoesNotExist");
			// shall not be executed
			Assert.fail();
			
		} catch (Exception e) {
			System.out.println("The following error are normal as far as the exception generation is tested.");
			e.printStackTrace();
			// normal behavior
			Assert.assertTrue(true);
		}
	}
	
	
}
