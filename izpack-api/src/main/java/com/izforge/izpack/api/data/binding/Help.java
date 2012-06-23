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

package com.izforge.izpack.api.data.binding;

import java.io.Serializable;

/**
 * Help element for panel.
 *
 * @author Anthonin Bonnefoy
 */
public class Help implements Serializable
{
    /**
     * auto-generated version number
     */
    private static final long serialVersionUID = -2560125306490380153L;
    /**
     * language of the help
     */
    private String iso3;
    /**
     * html source of the help
     */
    private String src;

    public Help(String iso3, String src)
    {
        this.iso3 = iso3;
        this.src = src;
    }

    public String getIso3()
    {
        return iso3;
    }

    public void setIso3(String iso3)
    {
        this.iso3 = iso3;
    }

    public String getSrc()
    {
        return src;
    }

    public void setSrc(String src)
    {
        this.src = src;
    }
}
