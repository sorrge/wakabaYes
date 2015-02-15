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
public class Transform
{
  float[][] W;
  float[] b;
  
  public Transform(int in, int out, FloatBuffer fb)
  {
    W = new float[in][out];
    for(int i = 0; i < in; ++i)
      for(int o = 0; o < out; ++o)
        W[i][o] = fb.get();
    
    b = new float[out];
    for(int o = 0; o < out; ++o)
      b[o] = fb.get();
  }
  
  public void Apply(float[] in, float[] out)
  {
    for(int o = 0; o < out.length; ++o)
    {
      out[o] = b[o];
      for(int i = 0; i < W.length; ++i)
        out[o] += in[i] * W[i][o];
    }
  }
}
