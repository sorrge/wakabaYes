/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyan.wakabayes;

import java.util.ArrayList;

/**
 *
 * @author sorrge
 */
public class DisjointSet
{
  ArrayList<Integer> parent = new ArrayList<>(), rank = new ArrayList<>();

  public int MakeSet()
  {
    int x = parent.size();
    parent.add(x);
    rank.add(0);
    return x;
  }

  public int Find(int x)
  {
    if (parent.get(x) != x)
      parent.set(x, Find(parent.get(x)));

    return parent.get(x);
  }

  public void Union(int x, int y)
  {
    int xRoot = Find(x), yRoot = Find(y);
    if (xRoot == yRoot)
      return;

    if (rank.get(xRoot) < rank.get(yRoot))
      parent.set(xRoot, yRoot);
    else if (rank.get(xRoot) > rank.get(yRoot))
      parent.set(yRoot, xRoot);
    else
    {
      parent.set(yRoot, xRoot);
      rank.set(xRoot, rank.get(xRoot) + 1);
    }
  }
}
