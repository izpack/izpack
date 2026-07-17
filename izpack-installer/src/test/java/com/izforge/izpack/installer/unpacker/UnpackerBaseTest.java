package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class UnpackerBaseTest {

    @Test
    public void shouldAllowRegularPathInsideInstallRoot() throws Exception {
        UnpackerBase unpacker = newUnpacker("/tmp/install");
        invokeValidateTargetPath(unpacker, "/tmp/install/subdir/file.txt");
    }

    @Test(expected = InstallerException.class)
    public void shouldRejectTraversalPath() throws Exception {
        UnpackerBase unpacker = newUnpacker("/tmp/install");
        invokeValidateTargetPath(unpacker, "/tmp/install/../escaped/file.txt");
    }

    @Test
    public void shouldAllowAbsolutePathOutsideInstallRootWithoutTraversal() throws Exception {
        UnpackerBase unpacker = newUnpacker("/tmp/install");
        invokeValidateTargetPath(unpacker, "/etc/myapp/config.properties");
    }

    private UnpackerBase newUnpacker(String installPath) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("getInstallPath".equals(method.getName())) {
                return installPath;
            }
            if ("getVariables".equals(method.getName()) || "getMessages".equals(method.getName())) {
                return null;
            }
            Class<?> returnType = method.getReturnType();
            if (returnType.equals(Boolean.TYPE)) {
                return false;
            }
            if (returnType.equals(Integer.TYPE) || returnType.equals(Short.TYPE) || returnType.equals(Byte.TYPE)
                    || returnType.equals(Long.TYPE) || returnType.equals(Float.TYPE) || returnType.equals(Double.TYPE)) {
                return 0;
            }
            return null;
        };
        InstallData installData = (InstallData) Proxy.newProxyInstance(
                InstallData.class.getClassLoader(),
                new Class[]{InstallData.class},
                handler
        );

        return new UnpackerBase(
                installData,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ) { };
    }

    private void invokeValidateTargetPath(UnpackerBase unpacker, String path) throws Exception {
        Method method = UnpackerBase.class.getDeclaredMethod("validateTargetPath", String.class);
        method.setAccessible(true);
        try {
            method.invoke(unpacker, path);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw e;
        }
    }
}
