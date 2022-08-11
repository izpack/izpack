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

package com.izforge.izpack.api.installer;

/**
 * Created by IntelliJ IDEA.
 * User: sora
 * Date: Dec 6, 2009
 * Time: 5:33:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ISummarisable
{
    /**
     * This method will be called to get the summary of this class which should be placed
     * in the SummaryPanel. The returned text should not contain a caption of this
     * item. The caption will be requested from the method getCaption. If <code>null</code>
     * returns, no summary for this panel will be generated. Default behaviour is to return
     * <code>null</code>.
     *
     * @return the summary for this class
     */
    String getSummaryBody();

    /**
     * This method will be called to get the caption for this class which should be placed in
     * the SummaryPanel. If <code>null</code> returns, no summary for this panel will be
     * generated. Default behaviour is to return the string given by langpack for the
     * key <code>&lt;current class name>.summaryCaption&gt;</code> if exist, else the string
     * &quot;summaryCaption.&lt;ClassName&gt;&quot;.
     *
     * @return the caption for this class
     */
    String getSummaryCaption();

    /**
     * Checks if the panel has been visited during the input process or not. This method allows
     * the implementer to conditionally show infos within the summary panel.
     *
     * @return <code>true</code> if the panel was displayed, <code>false</code> otherwise
     */
    boolean isVisited();
}
