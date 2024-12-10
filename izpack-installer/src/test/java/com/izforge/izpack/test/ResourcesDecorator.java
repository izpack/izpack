/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Marcus Stursberg
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.test;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.ImageIcon;

import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallerRequirement;
import com.izforge.izpack.api.data.PackInfo;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Resources;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

@Priority(value = 0)
@Decorator
public class ResourcesDecorator implements Resources
{
    protected final Resources delegate;

    @Inject
    public ResourcesDecorator(@Delegate @Any Resources delegate)
    {
      this.delegate = delegate;
    }

    @Override
    public InputStream getInputStream(String name)
    {
      return delegate.getInputStream(name);
    }

    @Override
    public URL getURL(String name)
    {
      return delegate.getURL(name);
    }

    @Override
    public String getString(String name)
    {
      return delegate.getString(name);
    }

    @Override
    public String getString(String name, String defaultValue)
    {
      return delegate.getString(name, defaultValue);
    }

    @Override
    public String getString(String name, String encoding, String defaultValue)
    {
      return delegate.getString(name, encoding, defaultValue);
    }

    @Override
    public Object getObject(String name) throws ResourceException
    {
        switch (name)
        {
            case "GUIPrefs":
                return new GUIPrefs();
            case "info":
                return new Info();
            case "panelsOrder":
                return new ArrayList<Panel>();
            case "packs.info":
                return new ArrayList<PackInfo>();
            case "vars":
                return new Properties();
            case "installerrequirements":
                return new ArrayList<InstallerRequirement>();
            case "langpacks.info":
                return new ArrayList<String>();
            default:
                return delegate.getObject(name);
         }
    }

    @Override
    public ImageIcon getImageIcon(String name, String... alternatives)
    {
      return delegate.getImageIcon(name, alternatives);
    }
}
