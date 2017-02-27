/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Laurent Bovet, Alex Mathey
 * Copyright 2010 Rene Krell
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
package com.izforge.izpack.util.xmlmerge.action;

/**
 * Constants for built-in actions. The constant names are also used in the
 * configuration.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public final class StandardActions
{

    /**
     * {@link FullMergeAction}.
     */
    public static final FullMergeAction FULLMERGE = new FullMergeAction();

    /**
     * {@link OrderedMergeAction}.
     */
    public static final OrderedMergeAction ORDEREDMERGE = new OrderedMergeAction();

    /**
     * {@link ReplaceAction}
     */
    public static final ReplaceAction REPLACE = new ReplaceAction();

    /**
     * {@link OverrideAction}
     */
    public static final OverrideAction OVERRIDE = new OverrideAction();

    /**
     * {@link KeepAction}
     */
    public static final KeepAction KEEP = new KeepAction();

    /**
     * {@link CompleteAction}
     */
    public static final CompleteAction COMPLETE = new CompleteAction();

    /**
     * {@link DeleteAction}
     */
    public static final DeleteAction DELETE = new DeleteAction();

    /**
     * {@link PreserveAction}
     */
    public static final PreserveAction PRESERVE = new PreserveAction();

    /**
     * {@link InsertAction}
     */
    public static final InsertAction INSERT = new InsertAction();

    /**
     * {@link DtdInsertAction}
     */
    public static final DtdInsertAction DTD = new DtdInsertAction();

}
