/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Daniel Abson
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

package com.izforge.izpack.util.config.base.spi;

import com.izforge.izpack.util.config.base.Config;

public class RegParser extends IniParser {
	
	private static final String COMMENTS = ";";
	private static final String OPERATORS = "=";

	public RegParser() 
	{
		super(OPERATORS, COMMENTS);
	}
	
    public static RegParser newInstance()
    {
        return ServiceFinder.findService(RegParser.class);
    }

    public static RegParser newInstance(Config config)
    {
        RegParser instance = newInstance();

        instance.setConfig(config);

        return instance;
    }
}
