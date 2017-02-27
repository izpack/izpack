/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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
package com.izforge.izpack.data;

import com.izforge.izpack.api.handler.DefaultConfigurationHandler;

/**
 * Implements an adapter for the abstract {@code DefaultConfigurationHandler}
 * class to be able to make it a utility for handling configuration options
 * temporarily while using the functionality of the parent class.<br>
 * This is especially necessary because the abstract parent class cannot be used
 * as a inner class, because Java can't serialize them to the installer jar.
 */
public class DefaultConfigurationHandlerAdapter extends DefaultConfigurationHandler
{

    private static final long serialVersionUID = -4178638617090133890L;
}
