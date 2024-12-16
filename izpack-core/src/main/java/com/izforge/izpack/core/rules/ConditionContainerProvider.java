package com.izforge.izpack.core.rules;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.izforge.izpack.api.container.Container;

public class ConditionContainerProvider implements Provider<ConditionContainer> {

    private final Container parent;

    @Inject
    public ConditionContainerProvider(Container parent) {
        this.parent = parent;
    }

    @Override
    public ConditionContainer get() {
        return new ConditionContainer(parent);
    }
}
