/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyan.wakabayes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sorrge
 */
public class Run1
{
  public static void main(String[] args)
  {
    if(args.length == 0)
    {
      System.err.println("Usage: wakabaYes captchaImageFile");
      return;
    }
    
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
    
    try
    {
      String detected = recognizer.Recognize(args[0]);
      if(detected != null)
        System.out.println(detected);
    }
    catch (IOException ex)
    {
      System.err.println("Error reading image: " + ex);
    }
    catch (Exception ex)
    {
      System.err.println("Recognition error: " + ex);
    }
  }
}
