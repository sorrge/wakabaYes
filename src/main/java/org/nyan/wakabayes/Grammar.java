/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyan.wakabayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sorrge
 */
public class Grammar
{
  static public class PathResult
  {
    String bestPath = null;
    float probability = 0;
  }
  
  final HashMap<Character, String[]> grammar = new HashMap<>();
  final char wordLiteral;

  public Grammar(File grammarFile) throws IOException
  {
    int first = -1;
    try (BufferedReader br = new BufferedReader(new FileReader(grammarFile)))
    {
      for (String line; (line = br.readLine()) != null;)
      {
        String[] fields = line.split("=>");
        char literal = fields[0].trim().charAt(0);
        if(first == -1)
          first = literal;
        
        fields = fields[1].split(",");
        for(int i = 0; i < fields.length; ++i)
          fields[i] = fields[i].replaceAll("[\\[\"%,\\] ]", "");
        
        grammar.put(literal, fields);
      }
    }
    
    wordLiteral = (char)first;
  }
  
  public String Generate(Random rand)
  {
    StringBuilder res = new StringBuilder();
    Generate(rand, res, wordLiteral);
    return res.toString();
  }

  private void Generate(Random rand, StringBuilder res, char literal)
  {
    String[] expansions = grammar.get(literal);
    String expansion = expansions[rand.nextInt(expansions.length)];
    for(int i = 0; i < expansion.length(); ++i)
    {
      char c = expansion.charAt(i);
      if(Character.isUpperCase(c))
        Generate(rand, res, c);
      else
        res.append(c);
    }
  }
  
  public float BeginProbability(String string)
  {
    PushbackReader seq = new PushbackReader(new StringReader(string), string.length());
    try
    {
      ArrayDeque<Character> remainder = new ArrayDeque<>();
      remainder.add(wordLiteral);
      return BeginProbability(seq, remainder);
    }
    catch (IOException ex)
    {
      return -1;
    }
  }

  private float BeginProbability(PushbackReader seq, ArrayDeque<Character> remainder) throws IOException
  {
    float totalProbability = 0;
    if(remainder.isEmpty())
    {
      int next = seq.read();
      if(next == -1)
        totalProbability = 1;
      else
      {
        totalProbability = 0;
        seq.unread(next);
      }
    }
    else
    {
      char c = remainder.removeFirst();
      if(Character.isUpperCase(c))
      {
        String[] expansions = grammar.get(c);
        for(String expansion : expansions)
        {
          for(int i = expansion.length() - 1; i >= 0; --i)
            remainder.addFirst(expansion.charAt(i));

          totalProbability += BeginProbability(seq, remainder) / expansions.length;
          for(int i = 0; i < expansion.length(); ++i)
            remainder.removeFirst();
        }
      }
      else
      {
        int next = seq.read();
        if(next == -1)
          totalProbability = 1;
        else
        {
          if(next != c)
            totalProbability = 0;
          else
            totalProbability = BeginProbability(seq, remainder);

          seq.unread(next);
        }
      }

      remainder.addFirst(c);
    }
    
    return totalProbability;
  }
  
  public PathResult FindBestPath(final float[][] observedProbabilities)
  {
    final ArrayDeque<Character> remainder = new ArrayDeque<>();
    remainder.add(wordLiteral);
    
    class ProbTree
    {
      float terminalProbability = 0;
      HashMap<Character, ProbTree> leaves = null;
      ProbTree prev = null;
      
      ProbTree GetBranch(char letter, boolean add)
      {
        if(leaves == null)
          leaves = new HashMap<>();
        
        
        ProbTree n = leaves.get(letter);
        if(n == null && add)
        {
          n = new ProbTree();
          n.prev = this;
          leaves.put(letter, n);
        }
        
        return n;
      } 
    }
    
    class Finder
    {
      float bestProbability = 0;
      ProbTree bestTerminal = null;
      int numTerminals = 0;
      
      void FillProbTree(ProbTree node, final int pos, float probabilitySoFar)
      {
        if(pos == observedProbabilities.length)
        {
          if(remainder.size() == 1 && remainder.getFirst() == '.')
          {
            if(node.terminalProbability == 0)
              ++numTerminals;
            
            node.terminalProbability += probabilitySoFar;
            if(node.terminalProbability > bestProbability)
            {
              bestProbability = node.terminalProbability;
              bestTerminal = node;
            }
          }
          
          return;
        }
        
        if(remainder.isEmpty() || remainder.size() == 1 && remainder.getFirst() == '.')
          return;
        
        char c = remainder.removeFirst();
        if(Character.isUpperCase(c))
        {
          String[] expansions = grammar.get(c).clone();
          
          Arrays.sort(expansions, new Comparator<String>()
          {
            @Override
            public int compare(String e1, String e2)
            {
              char c1 = e1.charAt(0), c2 = e2.charAt(0);
              boolean u1 = Character.isUpperCase(c1), 
                      u2 = Character.isUpperCase(c2);
              
              if(u1 && !u2)
                return -1;
              
              if(!u1 && u2)
                return 1;
              
              if(u1 && u2)
                return Character.compare(c1, c2);
              
              return Float.compare(observedProbabilities[pos][c2 - 'a'],
                      observedProbabilities[pos][c1 - 'a']);
            }
          });
          
          for(String expansion : expansions)
          {
            if(expansion.length() + remainder.size() > 
                    observedProbabilities.length - pos + 1)
              continue;
            
            for(int i = expansion.length() - 1; i >= 0; --i)
              remainder.addFirst(expansion.charAt(i));
            
            FillProbTree(node, pos, probabilitySoFar / expansions.length);

            for(int i = 0; i < expansion.length(); ++i)
              remainder.removeFirst();
          }
        }
        else
        {
          float nextProb = observedProbabilities[pos][c - 'a'];
          if(nextProb > 1e-8)
          {
            probabilitySoFar *= nextProb;
            if(probabilitySoFar > bestProbability * 0.001)
            {          
              ProbTree branch = node.GetBranch(c, numTerminals < 1000);
              if(branch != null)
                FillProbTree(branch, pos + 1, probabilitySoFar);
            }
          }
        }
        
        remainder.addFirst(c);
      }
    }
    
    PathResult res = new PathResult();
    Finder finder = new Finder();
    finder.FillProbTree(new ProbTree(), 0, 1);
    if(finder.bestTerminal == null)
      return res;
    
    res.probability = finder.bestProbability;    
    StringBuilder path = new StringBuilder();
    ProbTree node = finder.bestTerminal;
    while(true)
    {
      ProbTree prev = node.prev;
      if(prev == null)
        break;
      
      for(Map.Entry<Character, ProbTree> entrySet : prev.leaves.entrySet())
        if(entrySet.getValue() == node)
        {
          path.append(entrySet.getKey());
          break;
        }
      
      node = prev;
    }
    
    path.reverse();
    res.bestPath = path.toString();
    return res;
  }
}
