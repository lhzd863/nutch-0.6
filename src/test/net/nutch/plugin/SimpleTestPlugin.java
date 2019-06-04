/*
 * Copyright (c) 2003 The Nutch Organization. All rights reserved. Use subject
 * to the conditions in http://www.nutch.org/LICENSE.txt.
 */

package net.nutch.plugin;

/**
 * Simple Test plugin
 * 
 * @author joa23
 */
public class SimpleTestPlugin extends Plugin {

  /**
   * @param pDescriptor 
   */
  public SimpleTestPlugin(PluginDescriptor pDescriptor) {

    super(pDescriptor);
  }

  /*
   * @see net.nutch.plugin.Plugin#startUp()
   */
  public void startUp() throws PluginRuntimeException {
    System.err.println("start up Plugin: " + getDescriptor().getPluginId());

  }

  /*
   * (non-Javadoc)
   * 
   * @see net.nutch.plugin.Plugin#shutDown()
   */
  public void shutDown() throws PluginRuntimeException {
    System.err.println("shutdown Plugin: " + getDescriptor().getPluginId());

  }

}

