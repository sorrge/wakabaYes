/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyan.wakabayes;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 *
 * @author sorrge
 */
public class Test
{
  static final String alphabet = "abcdefghijklmnopqrstuvwxyz";
  
  public static void main(String[] args)
  {
    WakabaSimpleRecognition recognizer;
    try
    {
      recognizer = new WakabaSimpleRecognition();
    }
    catch (IOException ex)
    {
      System.err.println("Error reading network: " + ex);
      return;
    }

    Pattern answerPattern = Pattern.compile("^([a-z]+).*\\.gif");
    
    File captchaFolder = new File("../iichan/");
    int tested = 0, correct = 0;
    long start = System.nanoTime();
    for (final File fileEntry : captchaFolder.listFiles()) 
    {
      if(!fileEntry.getName().endsWith(".gif"))
        continue;
      
      String fileName = fileEntry.getName();
      Matcher m = answerPattern.matcher(fileName);
      m.find();
      String answer = m.group(1);

      String detected;
      try
      {
        detected = recognizer.Recognize(captchaFolder.getPath() + "/" + fileName);
      }
      catch (IOException ex)
      {
        System.err.println("Error reading image: " + ex);
        continue;
      }
      catch (Exception ex)
      {
        System.err.println("Error reading image: " + ex);
        continue;
      }
      
      if(answer.equals(detected))
        ++correct;
      else
        System.out.println("Wrong [" + detected + "]" + ": " + fileName);
      
      ++tested;
    }
    long elapsed = System.nanoTime() - start;
    
    System.out.println("Tested: " + tested + ", correct: " + correct + " (" + (float)correct / tested * 100 + "%)");
    float elapsedSeconds = elapsed / 1e9f;
    System.out.println("Time: " + elapsedSeconds + "s., per captcha: " + elapsedSeconds / tested + "s.");
  }

}
