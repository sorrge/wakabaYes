/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyan.wakabayes;

/**
 *
 * @author sorrge
 */
import java.awt.*;


public class ConnectedComponent
{
  public static void Label8(float[][] image, int[][] labels)
  {
    int h = image.length, w = image[0].length;    
    DisjointSet ds = new DisjointSet();
    for (int y = 0; y < h; ++y)
      for (int x = 0; x < w; ++x)
      {
        if (image[y][x] == 0)
        {
          labels[y][x] = -1;
          continue;
        }
        
        int minLabel = -1;
        for(int yy = Math.max(0, y - 1); yy <= y; ++yy)
          for(int xx = Math.max(0, x - 1); xx <= Math.min(w - 1, x + 1); ++xx)
            if((yy < y || xx == x - 1) && labels[yy][xx] != -1 && 
                    (minLabel == -1 || labels[yy][xx] < minLabel))
              minLabel = labels[yy][xx];
        
        if(minLabel == -1)
          labels[y][x] = ds.MakeSet();
        else
        {
          labels[y][x] = minLabel;
          for(int yy = Math.max(0, y - 1); yy <= y; ++yy)
            for(int xx = Math.max(0, x - 1); xx <= Math.min(w - 1, x + 1); ++xx)
              if((yy < y || xx == x - 1) && labels[yy][xx] != -1 && 
                      labels[yy][xx] != minLabel)
                ds.Union(minLabel, labels[yy][xx]);
        }
      }
    
    for (int y = 0; y < h; ++y)
      for (int x = 0; x < w; ++x)
        if(labels[y][x] != -1)
          labels[y][x] = ds.Find(labels[y][x]);
  }
  
  public static void Label4(float[][] image, int[][] labels)
  {
    int h = image.length, w = image[0].length;    
    DisjointSet ds = new DisjointSet();
    for (int y = 0; y < h; ++y)
      for (int x = 0; x < w; ++x)
      {
        if (image[y][x] == 0)
        {
          labels[y][x] = -1;
          continue;
        }
        
        int minLabel = -1;
        if(y > 0 && labels[y - 1][x] != -1)
          minLabel = labels[y - 1][x];
        
        if(x > 0 && labels[y][x - 1] != -1 && 
                (minLabel == -1 || minLabel > labels[y][x - 1]))
          minLabel = labels[y][x - 1];
        
        if(minLabel == -1)
          labels[y][x] = ds.MakeSet();
        else
        {
          labels[y][x] = minLabel;
          
          if(y > 0 && labels[y - 1][x] != -1 && labels[y - 1][x] != minLabel)
            ds.Union(minLabel, labels[y - 1][x]);

          if(x > 0 && labels[y][x - 1] != -1 && labels[y][x - 1] != minLabel)
            ds.Union(minLabel, labels[y][x - 1]);
        }
      }
    
    for (int y = 0; y < h; ++y)
      for (int x = 0; x < w; ++x)
        if(labels[y][x] != -1)
          labels[y][x] = ds.Find(labels[y][x]);
  }  
}
