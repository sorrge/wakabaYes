/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyan.wakabayes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author sorrge
 */
public class LeNet5
{
  static final int filterSize = 3;
  static final int[] numFilters = { 5, 10 };

  public final int imageHeight, imageWidth;
  int numHidden;
  float imageMean, imageStd;
  Convolution[] convolutionLayers = new Convolution[numFilters.length];
  Transform hidden, output;
  float[][][][] convIOs = new float[numFilters.length + 1][][][];
  float[] hiddenInput, hiddenOutput, finalOutput;
  
  public LeNet5(File netFile, int numOutputs)
          throws FileNotFoundException, IOException
  {
    try (FileChannel fc = new RandomAccessFile(netFile, "r").getChannel())
    {
      FloatBuffer fb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size())
              .order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
      
      imageMean = fb.get();
      imageStd = fb.get();
      imageHeight = (int)fb.get();
      imageWidth = (int)fb.get();
      numHidden = (int)fb.get();
      
      convIOs[0] = new float[1][imageHeight][imageWidth];
      for(int i = 0; i < convolutionLayers.length; ++i)
      {
        int dim = i == 0 ? 1 : numFilters[i - 1];
        convIOs[i + 1] = new float[numFilters[i]]
                [convIOs[i][0].length - filterSize + 1]
                [convIOs[i][0][0].length - filterSize + 1];
        
        convolutionLayers[i] = new Convolution(numFilters[i],
                dim, filterSize, filterSize, fb);
      }
      
      int lastW = convIOs[convIOs.length - 1][0].length;
      int lastH = convIOs[convIOs.length - 1][0][0].length;
      int numConvOutputs = lastW * lastH * numFilters[numFilters.length - 1];
      hiddenInput = new float[numConvOutputs];
      hiddenOutput = new float[numHidden];
      hidden = new Transform(numConvOutputs, numHidden, fb);
      output = new Transform(numHidden, numOutputs, fb);
      finalOutput = new float[numOutputs];
      
      if(fb.hasRemaining())
        throw new IOException("File too long");
    }
  }
  
  public float[][] GetInput() { return convIOs[0][0]; }
  public float[] GetOutput() { return finalOutput; }
  
  public void Apply()
  {
    for(int y = 0; y < convIOs[0][0].length; ++y)
      for(int x = 0; x < convIOs[0][0][y].length; ++x)
        convIOs[0][0][y][x] = (convIOs[0][0][y][x] - imageMean) / imageStd;
    
    for(int i = 0; i < convolutionLayers.length; ++i)
    {
      convolutionLayers[i].Apply(convIOs[i], convIOs[i + 1]);
      Tanh(convIOs[i + 1]);
    }
    
    Flatten(convIOs[convIOs.length - 1], hiddenInput);
    hidden.Apply(hiddenInput, hiddenOutput);
    Tanh(hiddenOutput);
    output.Apply(hiddenOutput, finalOutput);
    Softmax(finalOutput);
  }

  private static void Tanh(float[][][] x)
  {
    for(int i = 0; i < x.length; ++i)
      for(int j = 0; j < x[i].length; ++j)
        for(int k = 0; k < x[i][j].length; ++k)
          x[i][j][k] = (float)Math.tanh(x[i][j][k]);
  }
  
  private static void Tanh(float[] x)
  {
    for(int i = 0; i < x.length; ++i)
      x[i] = (float)Math.tanh(x[i]);
  }  
  
  private static void Softmax(float[] x)
  {
    float sum = 0;
    for(int i = 0; i < x.length; ++i)
      sum += x[i] = (float)Math.exp(x[i]);
    
    for(int i = 0; i < x.length; ++i)
      x[i] /= sum;
  }    
  
  private static void Flatten(float[][][] x, float[] y)
  {
    int l = 0;
    for(int i = 0; i < x.length; ++i)
      for(int j = 0; j < x[i].length; ++j)
        for(int k = 0; k < x[i][j].length; ++k)
          y[l++] = x[i][j][k];
  }  
}
