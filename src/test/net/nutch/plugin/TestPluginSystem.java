/*
 * Copyright (c) 2003 The Nutch Organization. All rights reserved. Use subject
 * to the conditions in http://www.nutch.org/LICENSE.txt.
 */
package net.nutch.plugin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.net.URL;
import junit.framework.TestCase;
import net.nutch.util.NutchConf;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
/**
 * Unit tests for the plugin system
 * 
 * @author joa23
 */
public class TestPluginSystem extends TestCase {
  private int fPluginCount;
  private LinkedList fFolders = new LinkedList();
  
  protected void setUp() throws Exception {
    fPluginCount = 5;
    createDummyPlugins(fPluginCount);
  }
  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
      for (int i = 0; i < fFolders.size(); i++) {
        File folder = (File) fFolders.get(i);
        delete(folder);
	folder.delete();
      }
    
  }
  /**
   * @throws IOException
   */
  public void testPluginConfiguration() throws IOException {
    String string = getPluginFolder();
    File file = new File(string);
    if (!file.exists()) {
      file.mkdir();
    }
    assertTrue(file.exists());
  }
  /**
   * @throws IOException
   */
  public void testLoadPlugins() throws IOException {
    PluginDescriptor[] descriptors = PluginRepository.getInstance()
      .getPluginDescriptors();
    int k = descriptors.length;
    assertTrue(fPluginCount <= k);
    for (int i = 0; i < descriptors.length; i++) {
      PluginDescriptor descriptor = descriptors[i];
      if (!descriptor.getPluginId().startsWith("getPluginFolder()")) {
        continue;
      }
      assertEquals(1, descriptor.getExportedLibUrls().length);
      assertEquals(1, descriptor.getNotExportedLibUrls().length);
    }
  }
  /**
   *  
   */
  public void testGetExtensionAndAttributes() {
    String xpId = " sdsdsd";
    ExtensionPoint extensionPoint = PluginRepository.getInstance()
      .getExtensionPoint(xpId);
    assertEquals(extensionPoint, null);
    Extension[] extension1 = PluginRepository.getInstance()
      .getExtensionPoint(getGetExtensionId()).getExtentens();
    assertEquals(extension1.length, fPluginCount);
    for (int i = 0; i < extension1.length; i++) {
      Extension extension2 = extension1[i];
      String string = extension2.getAttribute(getGetConfigElementName());
      assertEquals(string, getAttributeValue());
    }
  }
  /**
   * @throws PluginRuntimeException
   */
  public void testGetExtensionInstances() throws PluginRuntimeException {
    Extension[] extensions = PluginRepository.getInstance()
      .getExtensionPoint(getGetExtensionId()).getExtentens();
    assertEquals(extensions.length, fPluginCount);
    for (int i = 0; i < extensions.length; i++) {
      Extension extension = extensions[i];
      Object object = extension.getExtensionInstance();
      if (!(object instanceof HelloWorldExtension))
        fail(" object is not a instance of HelloWorldExtension");
      ((ITestExtension) object).testGetExtension("Bla ");
      String string = ((ITestExtension) object).testGetExtension("Hello");
      assertEquals("Hello World", string);
    }
  }
  /**
   * 
   *  
   */
  public void testGetClassLoader() {
    PluginDescriptor[] descriptors = PluginRepository.getInstance()
      .getPluginDescriptors();
    for (int i = 0; i < descriptors.length; i++) {
      PluginDescriptor descriptor = descriptors[i];
      assertNotNull(descriptor.getClassLoader());
    }
  }
  /**
   * @throws PluginRuntimeException
   * @throws IOException
   */
  public void testGetResources() throws PluginRuntimeException, IOException {
    PluginDescriptor[] descriptors = PluginRepository.getInstance()
      .getPluginDescriptors();
    for (int i = 0; i < descriptors.length; i++) {
      PluginDescriptor descriptor = descriptors[i];
      if (!descriptor.getPluginId().startsWith("getPluginFolder()")) {
        continue;
      }
      String value = descriptor.getResourceString("key", Locale.UK);
      assertEquals("value", value);
      value = descriptor.getResourceString("key",
                                           Locale.TRADITIONAL_CHINESE);
      assertEquals("value", value);
     
    }
  }
  /**
   * @return a PluginFolderPath
   */
  private String getPluginFolder() {
    String[] strings = NutchConf.getStrings("plugin.folders");
    if (strings == null || strings.length == 0)
      fail("no plugin directory setuped..");

    String name = strings[0];
    return PluginManifestParser.getPluginFolder(name).toString();
  }

  /**
   * Creates some Dummy Plugins
   * 
   * @param pCount
   */
  private void createDummyPlugins(int pCount) {
    String string = getPluginFolder();
    try {
      File folder = new File(string);
      folder.mkdir();
      for (int i = 0; i < pCount; i++) {
        String pluginFolder = string + File.separator + "DummyPlugin"
          + i;
        File file = new File(pluginFolder);
        file.mkdir();
        fFolders.add(file);
        createPluginManifest(i, file.getAbsolutePath());
        createResourceFile(i, file.getAbsolutePath());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  /**
   * Creates an ResourceFile
   * 
   * @param i
   * @param pFolderPath
   * @throws FileNotFoundException
   * @throws IOException
   */
  private void createResourceFile(int i, String pFolderPath)
    throws FileNotFoundException, IOException {
    Properties properties = new Properties();
    properties.setProperty("key", "value");
    properties.store(new FileOutputStream(pFolderPath + File.separator
                                          + "messages" + ".properties"), "");
  }
  /**
   * Deletes files in path
   * 
   * @param path
   * @throws IOException
   */
  private void delete(File path) throws IOException {
    File[] files = path.listFiles();
    for (int i = 0; i < files.length; ++i) {
      if (files[i].isDirectory())
        delete(files[i]);
      files[i].delete();
    }
  }
  /**
   * Creates an Plugin Manifest File
   * 
   * @param i
   * @param pFolderPath
   * @throws IOException
   */
  private void createPluginManifest(int i, String pFolderPath)
    throws IOException {
    FileWriter out = new FileWriter(pFolderPath + File.separator
                                    + "plugin.xml");
    Document document = DocumentFactory.getInstance().createDocument();
    document.addComment("this is just a simple plugin for testing issues.");
    Element pluginElement = document.addElement("nutch-plugin")
      .addAttribute("id", "net.nutch.plugin." + i).addAttribute(
                                                                getGetConfigElementName(), "" + i).addAttribute(
                                                                                                                "version", "1.0")
      .addAttribute("provider-name", "joa23").addAttribute("class",
                                                           "net.nutch.plugin.SimpleTestPlugin");
    pluginElement.addElement("extension-point").addAttribute("id",
                                                             getGetExtensionId()).addAttribute(getGetConfigElementName(),
                                                                                               getAttributeValue()).addAttribute("schema",
                                                                                                                                 "schema/testExtensionPoint.exsd");
    Element runtime = pluginElement.addElement("runtime");
    Element element = runtime.addElement("library").addAttribute("name",
                                                                 "libs/exported.jar");
    element.addElement("extport");
    Element element1 = runtime.addElement("library").addAttribute("name",
                                                                  "libs/not_exported.jar");
    Element extension = pluginElement.addElement("extension").addAttribute(
                                                                           "point", getGetExtensionId());
    extension.addElement("extemsion-item").addAttribute("objectClass",
                                                        "IInterfaceForExtensionPoint").addAttribute(
                                                                                                    getGetConfigElementName(), getAttributeValue()).addAttribute(
                                                                                                                                                                 "id", "aExtensionId.").addAttribute("class",
                                                                                                                                                                                                     "net.nutch.plugin.HelloWorldExtension");
    document.write(out);
    out.flush();
    out.close();
  }
  private String getAttributeValue() {
    return "simple Parser Extension";
  }
  private static String getGetExtensionId() {
    return "aExtensioID";
  }
  private static String getGetConfigElementName() {
    return "name";
  }
}
