/**
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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
package com.izforge.izpack.panels.imgpacks;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.packs.PacksPanelConsole;

/**
 * A Console implementation for ImgPacksPanel. Behaves exactly like the
 * {@link PacksPanelConsole}
 * 
 * @author Polane Ramothea
 * 
 */
public final class ImgPacksPanelConsole extends PacksPanelConsole {

	/**
	 * Constructor for {@link ImgPacksPanelConsole}
	 * 
	 * @param prompt
	 *            prompt
	 */
	public ImgPacksPanelConsole(Prompt prompt) {
		super(prompt);
	}

}
