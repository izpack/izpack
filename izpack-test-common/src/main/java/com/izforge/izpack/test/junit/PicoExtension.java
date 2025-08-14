package com.izforge.izpack.test.junit;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.IzPackException;

public class PicoExtension implements TestInstanceFactory, AfterAllCallback {

  private static final Logger logger = Logger.getLogger(PicoExtension.class.getName());

  private Container containerInstance;
  private final ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();

  @Override
  public Object createTestInstance(TestInstanceFactoryContext factoryContext,
                                   ExtensionContext extensionContext) throws TestInstantiationException {
    try {
      Class<?> testClass = factoryContext.getTestClass();
      com.izforge.izpack.test.Container containerAnnotation =
          testClass.getAnnotation(com.izforge.izpack.test.Container.class);
      Class<? extends Container> containerClass = containerAnnotation.value();

      // Create container outside EDT
      containerInstance = createContainer(containerClass, testClass, extensionContext);
      containerInstance.addComponent(testClass);

      // Get test instance on EDT
      final Object[] testInstance = new Object[1];
      SwingUtilities.invokeAndWait(() -> {
        try {
          testInstance[0] = containerInstance.getComponent(testClass);
        } catch (Exception e) {
          logger.log(Level.SEVERE, e.getMessage(), e);
          throw new IzPackException(e);
        }
      });

      return testInstance[0];
    } catch (Exception e) {
      throw new TestInstantiationException("Failed to create test instance", e);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    Thread.currentThread().setContextClassLoader(savedContextClassLoader);
    if (containerInstance != null) {
      try {
        containerInstance.dispose();
      } finally {
        containerInstance = null;
      }
    }
  }

  private Container createContainer(Class<? extends Container> containerClass,
                                    Class<?> testClass,
                                    ExtensionContext context) throws Exception {
    try {
      Constructor<? extends Container> constructor =
          containerClass.getConstructor(Class.class, ExtensionContext.class);
      return constructor.newInstance(testClass, context);
    } catch (NoSuchMethodException e1) {
      try {
        Constructor<? extends Container> constructor =
            containerClass.getConstructor(Class.class);
        return constructor.newInstance(testClass);
      } catch (NoSuchMethodException e2) {
        Constructor<? extends Container> constructor =
            containerClass.getConstructor();
        return constructor.newInstance();
      }
    }
  }
}
