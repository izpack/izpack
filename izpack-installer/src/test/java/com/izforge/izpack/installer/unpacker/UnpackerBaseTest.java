package com.izforge.izpack.installer.unpacker;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

public class UnpackerBaseTest {

    @Test
    public void shouldAllowRegularPathInsideInstallRoot() throws Exception {
        UnpackerBase unpacker = newUnpacker("/tmp/install");
        invokeValidateTargetPath(unpacker, "/tmp/install/subdir/file.txt");
    }

    @Test
    public void shouldRejectTraversalPath() throws Exception {
        assertThatThrownBy(() -> {
            UnpackerBase unpacker = newUnpacker("/tmp/install");
            invokeValidateTargetPath(unpacker, "/tmp/install/../escaped/file.txt");
        }).isInstanceOf(InstallerException.class);
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
        InstallData installData = (InstallData) newProxyInstance(
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
