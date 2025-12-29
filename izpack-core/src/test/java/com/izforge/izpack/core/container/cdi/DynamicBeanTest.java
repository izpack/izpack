/*
 * IzPack - Copyright 2001-20212 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2010 Anthonin Bonnefoy Copyright 2012 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.core.container.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Set;

import org.junit.Test;

import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.resource.AbstractResources;
import com.izforge.izpack.core.resource.ResourceManager;

public class DynamicBeanTest {

  @Test
  public void testCollectTypesClassOfQ() {
    Set<Type> types = DynamicBean.collectTypes(ResourceManager.class);
    assertEquals(4, types.size());
    assertTrue(types.contains(ResourceManager.class));
    assertTrue(types.contains(AbstractResources.class));
    assertTrue(types.contains(Resources.class));
    assertTrue(types.contains(Object.class));
  }

}
