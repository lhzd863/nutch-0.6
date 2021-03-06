/* Copyright (c) 2004 The Nutch Organization. All rights reserved. */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */
package net.nutch.analysis.lang;
import net.nutch.parse.Parse;
import net.nutch.parse.HtmlParseFilter;
import net.nutch.parse.ParseException;
import net.nutch.protocol.Content;
import org.w3c.dom.*;

import java.util.logging.Logger;
import net.nutch.util.LogFormatter;

/** Adds metadata identifying language of document if found
 * We could also run statistical analysis here but we'd miss all other formats
 */
public class HTMLLanguageParser implements HtmlParseFilter {
  public static final String META_LANG_NAME="X-meta-lang";
  public static final Logger LOG = LogFormatter
    .getLogger(HTMLLanguageParser.class.getName());

  /**
   * Scan the HTML document looking at possible indications of content language<br>
   * <li>1. html lang attribute (http://www.w3.org/TR/REC-html40/struct/dirlang.html#h-8.1)
   * <li>2. meta dc.language (http://dublincore.org/documents/2000/07/16/usageguide/qualified-html.shtml#language)
   * <li>3. meta http-equiv (content-language) (http://www.w3.org/TR/REC-html40/struct/global.html#h-7.4.4.2)
   * <br>Only the first occurence of language is stored.
   */
  public Parse filter(Content content, Parse parse, DocumentFragment doc)
    throws ParseException {
    String lang = findLanguage(doc);

    if (lang != null) {
      parse.getData().getMetadata().put(META_LANG_NAME, lang);
    }
                
    return parse;
  }
        
  private String findLanguage(Node node) {
    String lang = null;

    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        
      //lang attribute
      lang = ((Element) node).getAttribute("lang");
      if (lang != null && lang.length()>1) {
        return lang;
      }
      if ("meta".equalsIgnoreCase(node.getNodeName())) {

        NamedNodeMap attrs=node.getAttributes();

        //dc.language
        for(int i=0;i<attrs.getLength();i++){
          Node attrnode=attrs.item(i);
          if("name".equalsIgnoreCase(attrnode.getNodeName())){
            if("dc.language".equalsIgnoreCase(attrnode.getNodeValue())){
              Node valueattr=attrs.getNamedItem("content");
              lang = (valueattr!=null)?valueattr.getNodeValue():null;
            }
          }
        }
        
        //http-equiv content-language
        for(int i=0;i<attrs.getLength();i++){
          Node attrnode=attrs.item(i);
          if("http-equiv".equalsIgnoreCase(attrnode.getNodeName())){
            if("content-language".equals(attrnode.getNodeValue().toLowerCase())){
              Node valueattr=attrs.getNamedItem("content");
              lang = (valueattr!=null)?valueattr.getNodeValue():null;
            }
          }
        }
      }
    }
                
    //recurse
    NodeList children = node.getChildNodes();
    for (int i = 0; children != null && i < children.getLength(); i++) {
      lang = findLanguage(children.item(i));
      if(lang != null && lang.length()>1) return lang;
    }

    return lang;
  }
}
