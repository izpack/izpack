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
package com.izforge.izpack.api.data;

import com.izforge.izpack.api.handler.DefaultConfigurationHandler;
import com.izforge.izpack.api.installer.DataValidator;

/**
 * Enhancement of {@code DataValidator}, allows to implement a custom panel
 * validator which can be configured by a <code>&lt;configuration&gt;</code>
 * section.<br>
 * Implement your panel validators by inheriting from this class.
 */
public abstract class PanelValidator extends DefaultConfigurationHandler implements DataValidator
{

    private static final long serialVersionUID = -6662478927015059872L;

    @Override
    public abstract Status validateData(InstallData installData);

    @Override
    public abstract String getErrorMessageId();

    @Override
    public abstract String getWarningMessageId();

    @Override
    public abstract boolean getDefaultAnswer();
}
