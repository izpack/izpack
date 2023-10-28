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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.Test;

public class DependencyGraphTest {

    /**
     * Test method for {@link DependencyGraph#getOrderedList()}.
     */
    @Test
    public void testGetOrderedList() {
        DependencyGraph<String> graph = new DependencyGraph<String>();
        String var1 = "var1";
        String var2 = "var2";
        String var3 = "var3";
        String var4 = "var4";
        String var5 = "var5";
        String var6 = "var6";
        graph.addVertex(var1);      // some elements added explicit
        graph.addVertex(var6);
        graph.addEdge(var1, var2);  // other elements implicit
        graph.addEdge(var3, var2);
        graph.addEdge(var2, var3);
        graph.addEdge(var1, var4);
        graph.addEdge(var5, var1);
        List<String> list = graph.getOrderedList();
        testOrder(list, var1, var2);
        testOrder(list, var1, var4);
        testOrder(list, var5, var1);
        testContained(list, var6);
        testUnique(list);
    }

    private void testContained(List<? extends Object> list, Object var)
    {
        assertTrue(list.contains(var), String.format("'%s' must be contained in list '%s'", var, list));
    }

    private void testOrder(List<? extends Object> list, Object var1, Object var2)
    {
        testContained(list, var1);
        testContained(list, var2);
        assertTrue(list.indexOf(var1) > list.indexOf(var2), String.format("'%s' must come after '%s' in list '%s'", var1, var2, list));
    }

    private void testUnique(List<? extends Object> list)
    {
        ListIterator<? extends Object> it = list.listIterator();
        while (it.hasNext())
        {
            Object object = it.next();
            ListIterator<? extends Object> it2 = list.listIterator(it.nextIndex());
            while (it2.hasNext())
            {
              assertNotEquals(object, it2.next(), String.format("'%s' must occur only once in list '%s'", object, list));
            }
        }
    }

}
