/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyan.wakabayes;

import java.nio.FloatBuffer;

/**
 *
 * @author sorrge
 */
public class Convolution
{
  float[][][][] W;
  float[] b;
  
  public Convolution(int numFilters, int numInputs, int height, int width, 
          FloatBuffer fb)
  {
    W = new float[numFilters][numInputs][height][width];
    for(int f = 0; f < numFilters; ++f)
      for(int i = 0; i < numInputs; ++i)
        for(int y = 0; y < height; ++y)
          for(int x = 0; x < width; ++x)
            W[f][i][y][x] = fb.get();
    
    b = new float[numFilters];
    for(int f = 0; f < numFilters; ++f)
      b[f] = fb.get();
  }
  
  public void Apply(float[][][] in, float[][][] out)
  {
    int numFilters = W.length, numInputs = W[0].length, height = W[0][0].length,
            width = W[0][0][0].length;
    
    int oHeight = out[0].length, oWidth = out[0][0].length;
    
    for(int f = 0; f < numFilters; ++f)
      for(int y = 0; y < oHeight; ++y)
        for(int x = 0; x < oWidth; ++x)
        {
          out[f][y][x] = b[f];
          for(int i = 0; i < numInputs; ++i)
            for(int sy = 0; sy < height; ++sy)
              for(int sx = 0; sx < width; ++sx)
                out[f][y][x] += 
                        in[i][y + sy][x + sx] * 
                        W[f][i][height - sy - 1][width - sx - 1];
                  // the convolution operator is transposed
        }
  }
}
