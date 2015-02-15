/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyan.wakabayes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import static org.nyan.wakabayes.Test.alphabet;

/**
 *
 * @author sorrge
 */
public class WakabaSimpleRecognition
{
  static public class ScanResult
  {
    public float[][] positionLetterProbabilities;
    public int[] widths;
    public boolean hadDivision = false;
    HashSet<Integer> uncertainDivisions = new HashSet<>();
  }
  
  class Box implements Comparable<Box>
  {
    final int label;
    int minX, maxX, minY, maxY;
    int area = 1;

    Box(int x, int y, int label)
    {
      this.label = label;
      minX = maxX = x;
      minY = maxY = y;
    }
    
    void Extend(int x, int y)
    {
      minX = Math.min(minX, x);
      maxX = Math.max(maxX, x);
      minY = Math.min(minY, y);
      maxY = Math.max(maxY, y);
      ++area;
    }
    
    int Area()
    {
      return area;
    }
    
    int CenterX()
    {
      return (maxX + minX + 1) / 2;
    }
    
    int Height()
    {
      return maxY - minY + 1;
    }
    
    int Width()
    {
      return maxX - minX + 1;
    }    
    
    boolean IntersectsXAndAbove(Box o)
    {
      if(minX > o.maxX + 1 || maxX < o.minX - 1)
        return false;
      
      return maxY < o.minY;
    }
    
    boolean IsIn(int x, int y)
    {
      return minX <= x && x <= maxX && minY <= y && y <= maxY;
    }
    
    void SetY(int[][] labels)
    {
      maxY = -1;
      minY = 10000;
      for(int y = 0; y < labels.length; ++y)
        for(int x = minX; x <= maxX; ++x)
          if(labels[y][x] == label)
            Extend(x, y);
    }

    @Override
    public int compareTo(Box o)
    {
      return Integer.compare(maxX + minX, o.maxX + o.minX);
    }
  }  
  
  LeNet5 net;
  Grammar grammar;

  public WakabaSimpleRecognition() throws IOException
  {
    net = new LeNet5(new File("classifier.b"), alphabet.length());
    grammar = new Grammar(new File("grammar.txt"));
  }

  public ScanResult PositionLetterProbabilities(float[][] pic, int[][] labels, 
          int divisionBias, HashSet<Integer> ignoreDivisions)
  {
    int h = pic.length, w = pic[0].length;
    HashMap<Integer, Box> componentBoxes = new HashMap<>();
    for(int y = 0; y < h; ++y)
      for(int x = 0; x < w; ++x)
        if(labels[y][x] != -1)
        {
          Box box = componentBoxes.get(labels[y][x]);
          if(box == null)
            componentBoxes.put(labels[y][x], new Box(x, y, labels[y][x]));
          else
            box.Extend(x, y);
        }
    
    HashSet<Box> dots = new HashSet<>();
    for (Box box : componentBoxes.values())
      for (Box box2 : componentBoxes.values())
        if(box != box2 && box.IntersectsXAndAbove(box2))
          dots.add(box);      
    
    ScanResult res = new ScanResult();    
    SortedSet<Box> letters = new TreeSet<>();
    for (Box box : componentBoxes.values())
      if(!dots.contains(box))
      {
        if(box.Width() < net.imageWidth - 3 || 
                ignoreDivisions.contains(box.CenterX()))
          letters.add(box);
        else
        {
          Box left = new Box(box.minX, box.minY, box.label);
          Box right = new Box(box.maxX, box.minY, box.label);
          left.Extend(box.CenterX() + divisionBias, box.maxY);
          right.Extend(box.CenterX() + 1 + divisionBias, box.maxY);
          left.SetY(labels);
          right.SetY(labels);
          letters.add(left);
          letters.add(right);
          res.hadDivision = true;
          if(box.Width() <= net.imageWidth)
            res.uncertainDivisions.add(box.CenterX());
        }
      }
    
    HashMap<Box, Box> dotsMap = new HashMap<>();
    for (Box dot : dots)
    {
      SortedSet<Box> prev = letters.headSet(dot), next = letters.tailSet(dot);
      if(prev.isEmpty() || !next.isEmpty() && 
              Math.abs(prev.last().CenterX() - dot.CenterX()) > 
              Math.abs(next.first().CenterX() - dot.CenterX()))
        dotsMap.put(next.first(), dot);
      else
        dotsMap.put(prev.last(), dot);          
    }
    
    for (Map.Entry<Box, Box> entrySet : dotsMap.entrySet())
    {
      Box letter = entrySet.getKey();
      Box dot = entrySet.getValue();
      letter.Extend(dot.minX, dot.minY);
      letter.Extend(dot.maxX, dot.maxY);
    }
    
    float[][] input = net.GetInput();
    float[] output = net.GetOutput();
    res.positionLetterProbabilities = new float[letters.size()][alphabet.length()];
    res.widths = new int[letters.size()];
    int i = 0;
    for (Box box : letters)
    {
      Box dot = dotsMap.get(box);
      for(int x = box.CenterX() - net.imageWidth / 2; x < box.CenterX() + net.imageWidth / 2 + 1; ++x)
        for(int y = box.maxY - net.imageHeight + 1; y < box.maxY + 1; ++y)
        {
          int xx = x - box.CenterX() + net.imageWidth / 2;
          int yy = y - box.maxY + net.imageHeight - 1;
          
          if(x < 0 || x >= w || y < 0 || labels[y][x] == -1 ||
                  labels[y][x] != box.label && (dot == null || 
                  labels[y][x] != dot.label) || !box.IsIn(x, y))
            input[yy][xx] = 0;
          else
            input[yy][xx] = 255;
        }
      
      net.Apply();
      for(int j = 0; j < alphabet.length(); ++j)
        if(Float.isNaN(output[j]))
          throw new ArithmeticException();
        else
          res.positionLetterProbabilities[i][j] = output[j];
      
      res.widths[i] = box.Width();
      
      ++i;
    }

    return res;
  }
  
  public int MaxWidth() { return net.imageWidth; }
  
  public String Recognize(BufferedImage img)
  {
    float[][] pic;
    int[][] labels;
    img = Images.ConvertToGray(img);
    pic = Images.ConvertToArray(img);
    int h = pic.length, w = pic[0].length;
    labels = new int[h][w];
    ConnectedComponent.Label4(pic, labels);        

    WakabaSimpleRecognition.ScanResult scan = 
            PositionLetterProbabilities(pic, labels, 0, new HashSet<Integer>());

    HashSet<Integer> uncertainDivisions = scan.uncertainDivisions;

    Grammar.PathResult best = grammar.FindBestPath(scan.positionLetterProbabilities);
    if(scan.hadDivision)
      for(int bias = -3; bias <= 3; ++bias)
        if(bias != 0 || !uncertainDivisions.isEmpty())
        {
          scan = PositionLetterProbabilities(pic, labels, bias, new HashSet<Integer>());
          Grammar.PathResult biased = grammar.FindBestPath(scan.positionLetterProbabilities);
          if(biased.probability > best.probability)
            best = biased;

          if(uncertainDivisions.isEmpty())
            continue;

          scan = PositionLetterProbabilities(pic, labels, bias, uncertainDivisions);
          biased = grammar.FindBestPath(scan.positionLetterProbabilities);
          if(biased.probability > best.probability)
            best = biased;

          if(uncertainDivisions.size() == 1)
            continue;

          HashSet<Integer> toIgnore = new HashSet<>();
          for(int div : uncertainDivisions)
          {
            toIgnore.clear();
            toIgnore.add(div);
            scan = PositionLetterProbabilities(pic, labels, bias, toIgnore);
            biased = grammar.FindBestPath(scan.positionLetterProbabilities);
            if(biased.probability > best.probability)
              best = biased;              
          }
        }

    return best.bestPath;
  }
  
  public String Recognize(String fileName) throws IOException
  {
    return Recognize(ImageIO.read(new File(fileName)));
  }
}
