package io.javaoperatorsdk.operator.api.reconciler.dependent.managed;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javaoperatorsdk.operator.processing.dependent.workflow.WorkflowCleanupResult;
import io.javaoperatorsdk.operator.processing.dependent.workflow.WorkflowReconcileResult;

@SuppressWarnings("rawtypes")
public class DefaultManagedDependentResourceContext implements ManagedDependentResourceContext {
  private static final Logger log =
      LoggerFactory.getLogger(DefaultManagedDependentResourceContext.class);
  public static final Object RECONCILE_RESULT_KEY = new Object();
  public static final Object CLEANUP_RESULT_KEY = new Object();
  private final ConcurrentHashMap attributes = new ConcurrentHashMap();

  @Override
  public <T> Optional<T> get(Object key, Class<T> expectedType) {
    return Optional.ofNullable(attributes.get(key))
        .filter(expectedType::isInstance)
        .map(expectedType::cast);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T put(Object key, T value) {
    Object previous;
    if (value == null) {
      return (T) attributes.remove(key);
    } else {
      previous = attributes.put(key, value);
    }

    if (previous != null && !previous.getClass().isAssignableFrom(value.getClass())) {
      logWarning("Previous value (" + previous +
          ") for key (" + key +
          ") was not of type " + value.getClass() +
          ". This might indicate an issue in your code. If not, use put(" + key +
          ", null) first to remove the previous value.");
    }
    return (T) previous;
  }

  // only for testing purposes
  void logWarning(String message) {
    log.warn(message);
  }

  @Override
  @SuppressWarnings("unused")
  public <T> T getMandatory(Object key, Class<T> expectedType) {
    return get(key, expectedType).orElseThrow(() -> new IllegalStateException(
        "Mandatory attribute (key: " + key + ", type: " + expectedType.getName()
            + ") is missing or not of the expected type"));
  }

  @Override
  public Optional<WorkflowReconcileResult> getWorkflowReconcileResult() {
    return get(RECONCILE_RESULT_KEY, WorkflowReconcileResult.class);
  }

  @Override
  public Optional<WorkflowCleanupResult> getWorkflowCleanupResult() {
    return get(CLEANUP_RESULT_KEY, WorkflowCleanupResult.class);
  }
}
