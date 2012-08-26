/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.compiler.merge.resolve;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.core.IsNot;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.compiler.container.TestResolveContainer;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.merge.PanelMerge;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * @author Anthonin Bonnefoy
 */

@RunWith(PicoRunner.class)
@Container(TestResolveContainer.class)
public class PathResolverRealPanelTest
{
    private CompilerPathResolver pathResolver;

    public PathResolverRealPanelTest(CompilerPathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
    }


    @Test
    public void testAddProcessPanel() throws Exception
    {
        PanelMerge panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.process.ProcessPanel");
        assertThat(panelMerge, IsNot.not(
                MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/process/VariableCondition.class")));
        assertThat(panelMerge,
                   MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/panels/process/ProcessPanel.class"));
    }
}
