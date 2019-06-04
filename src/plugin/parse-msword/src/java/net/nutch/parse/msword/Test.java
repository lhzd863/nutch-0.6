package net.nutch.parse.msword;

import java.io.*;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:
 * @author
 * @version 1.0
 */

class Test
{

  public Test()
  {
  }
  public static void main(String[] args)
  {
    try
    {
      WordExtractor extractor = new WordExtractor();
      String s = extractor.extractText(new FileInputStream(args[0]));
      System.out.println(s);
      //OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("C:\\test.txt"), "UTF-16LE");
      OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("./test.txt"));
      out.write(s);
      out.flush();
      out.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
