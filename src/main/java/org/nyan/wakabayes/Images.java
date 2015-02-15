/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyan.wakabayes;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

/**
 *
 * @author sorrge
 */
public class Images
{
  static BufferedImage ConvertToGray(BufferedImage image)
  {
    BufferedImage res = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics g = res.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return res;
  }

  static float[][] ConvertToArray(BufferedImage image)
  {
    final Raster raster = image.getRaster();
    final byte[] pixels = ((DataBufferByte) raster.getDataBuffer()).getData();
    final int width = image.getWidth();
    final int height = image.getHeight();
    int dataElems = raster.getNumDataElements();
    float[][] result = new float[height][width];
    if (dataElems == 4)
      for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += dataElems)
      {
        float argb = 0;
        argb += (int) pixels[pixel + 1] & 255;
        argb += (int) pixels[pixel + 2] & 255;
        argb += (int) pixels[pixel + 3] & 255;
        result[row][col] = argb / dataElems / 255.0f;
        col++;
        if (col == width)
        {
          col = 0;
          row++;
        }
      }
    else if (dataElems == 3)
      for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += dataElems)
      {
        float argb = 0;
        argb += (int) pixels[pixel] & 255;
        argb += (int) pixels[pixel + 1] & 255;
        argb += (int) pixels[pixel + 2] & 255;
        result[row][col] = argb / dataElems / 255.0f;
        col++;
        if (col == width)
        {
          col = 0;
          row++;
        }
      }
    else if (dataElems == 1)
      for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += dataElems)
      {
        float argb = 0;
        argb += ((int) pixels[pixel] & 255);
        result[row][col] = argb / 255.0f;
        col++;
        if (col == width)
        {
          col = 0;
          row++;
        }
      }
    else
      throw new IllegalArgumentException("Unexpected image type");
    return result;
  }
  
  
}
