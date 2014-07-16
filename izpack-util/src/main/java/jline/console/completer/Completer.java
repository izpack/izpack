/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright (c) 2002-2012, the original authors of the JLine project
 * Copyright 2014 René Krell
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

package jline.console.completer;

import java.util.List;

/**
 * A completer is the mechanism by which tab-completion candidates will be resolved.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.3
 */
public interface Completer
{
    //
    // FIXME: Check if we can use CharSequece for buffer?
    //

    /**
     * Populates <i>candidates</i> with a list of possible completions for the <i>buffer</i>.
     *
     * The <i>candidates</i> list will not be sorted before being displayed to the user: thus, the
     * complete method should sort the {@link List} before returning.
     *
     * @param buffer        The buffer
     * @param cursor        The current position of the cursor in the <i>buffer</i>
     * @param candidates    The {@link List} of candidates to populate
     * @return              The index of the <i>buffer</i> for which the completion will be relative
     */
    int complete(String buffer, int cursor, List<CharSequence> candidates);
}
