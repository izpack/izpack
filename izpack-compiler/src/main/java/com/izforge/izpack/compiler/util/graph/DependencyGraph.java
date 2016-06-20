/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.compiler.util.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Directed graph for dependencies with a <List> ordered for computation
 * @param <Vertex> the generic type of vertexes
 */
public class DependencyGraph<Vertex>
{

    private Map<Vertex, Set<Vertex>> st;
    private DepthUtils<Vertex> depthUtils;

    /**
     * Create an empty directed graph.
     */
    public DependencyGraph()
    {
        st = new HashMap<Vertex, Set<Vertex>>();
        depthUtils = new DepthUtils<Vertex>(st);
    }

    /**
     * Add vertex to to from's list of neighbors; self-loops allowed
     * @param from source vertex
     * @param to target vertex
     */
    public void addEdge(Vertex from, Vertex to)
    {
        if (!st.containsKey(from)) addVertex(from);
        if (!st.containsKey(to)) addVertex(to);
        st.get(from).add(to);
    }

    /**
     * Add a new vertex with no neighbors if vertex does not yet exist
     * @param v vertex to be added
     */
    public void addVertex(Vertex v)
    {
        if (!st.containsKey(v)) 
        {
            st.put(v, new HashSet<Vertex>());
        }
    }

    public List<Vertex> getOrderedList()
    {
        return depthUtils.getOrderedList();
    }

    @Override
    public String toString()
    {
        depthUtils.computeDepths();    // we computate the depth first
        StringBuilder s = new StringBuilder();
        for (Vertex v : st.keySet())
        {
            s.append(String.format("%s: depth=%d [",v,depthUtils.getDepth(v)));
            String sep="";
            for (Vertex w : st.get(v))
            {
                s.append(sep).append(w);
                sep=",";
            }
            s.append("]\n");
        }
        return s.toString();
    }

    /**
     * utility class for DependencyGraph
     * provides
     * - setting and checking the depth of a vertex 
     * - a comparator for sorting vertices in descending order of depth
     *
     * @param <T> type of vertex elements in graph
     */
    private class DepthUtils<T> implements Comparator<T>
    {
        private Map<T, Integer> depths = new HashMap<T, Integer>();
        private Map<T,Set<T>> graph;

        public DepthUtils(Map<T, Set<T>> graph) {
            this.graph = graph;
        }

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(T o1, T o2) {
            return getDepth(o2).compareTo(getDepth(o1));
        }

        private List<T> getOrderedList()
        {
            computeDepths();
            List<T> nodes = new ArrayList<T>(graph.keySet());
            Collections.sort(nodes, this);
            return nodes;
        }

        private void computeDepths()
        {
            HashSet<Vertex> visited = new HashSet<Vertex>();
            for (Vertex vertex : st.keySet()) {
                computeDepths(visited, vertex, 0);
            }
        }

        private void computeDepths(HashSet<Vertex> visited, Vertex vertex, int depth)
        {
            depth++;
            if (! visited.contains(vertex))
            {
                visited.add(vertex);
                depthUtils.ensureDepth(vertex, depth);
                for (Vertex descent : st.get(vertex))
                {
                    computeDepths(visited, descent, depth);
                }
                visited.remove(vertex);
            }
        }

        /**
         * @param o the vertex
         * @return depth of vertex (as far as computed yet)
         */
        private Integer getDepth(T o) {
            return depths.containsKey(o) ? depths.get(o) : 0;
        }

        /**
         * ensure, that the depth of the vertex is at least depth
         * it can only be increased, not lowered
         * 
         * @param o the vertex
         * @param depth of the vertex
         */
        private void ensureDepth(T o, int depth) {
            if (getDepth(o) < depth)
            {
                depths.put(o, new Integer(depth));
            }
        }
    }

}
