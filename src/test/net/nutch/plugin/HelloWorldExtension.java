/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   
 * Use subject to the conditions in http://www.nutch.org/LICENSE.txt. 
 */

package net.nutch.plugin;

/**
 * Simple Test-extensions
 * 
 * @author joa23
 */
public class HelloWorldExtension implements ITestExtension {

  /* (non-Javadoc)
   * @see net.nutch.plugin.ITestExtension#testGetExtension(java.lang.String)
   */
  public String testGetExtension(String hello) {
    return hello + " World";
  }
}
