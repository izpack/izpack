package com.izforge.izpack.installer.unpacker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.factory.ObjectFactory;

public class UnpackerProvider implements Provider<IUnpacker> {

    private final InstallData installData;
    private final ObjectFactory objectFactory;

    @Inject
    public UnpackerProvider(InstallData installData, ObjectFactory objectFactory) {
        this.installData = installData;
        this.objectFactory = objectFactory;
    }

    @Override
    public IUnpacker get() {
        String className = installData.getInfo().getUnpackerClassName();
        return objectFactory.create(className, IUnpacker.class);
    }
}
