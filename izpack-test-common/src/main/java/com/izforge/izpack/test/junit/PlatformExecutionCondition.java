package com.izforge.izpack.test.junit;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

public class PlatformExecutionCondition implements ExecutionCondition {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    RunOn runOn = AnnotationUtils.findAnnotation(context.getElement(), RunOn.class)
        .orElse(null);

    if (runOn == null) {
      return ConditionEvaluationResult.enabled("No @RunOn restriction");
    }

    Platform platform = new Platforms().getCurrentPlatform();
    for (Platform.Name name : runOn.value()) {
      if (platform.isA(name)) {
        return ConditionEvaluationResult.enabled("Platform matches");
      }
    }

    return ConditionEvaluationResult.disabled("Test not supported on this platform");
  }
}
