// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Maintains instances used to communicate with the corresponding objects in Dart.
 *
 * <p>Objects stored in this container are represented by an object in Dart that is also stored in
 * an InstanceManager with the same identifier.
 *
 * <p>When an instance is added with an identifier, either can be used to retrieve the other.
 *
 * <p>Added instances are added as a weak reference and a strong reference. When the strong
 * reference is removed with `{@link #remove(long)}` and the weak reference is deallocated, the
 * `finalizationListener` is made with the instance's identifier. However, if the strong reference
 * is removed and then the identifier is retrieved with the intention to pass the identifier to Dart
 * (e.g. calling {@link #getIdentifierForStrongReference(Object)}), the strong reference to the
 * instance is recreated. The strong reference will then need to be removed manually again.
 */
@SuppressWarnings("unchecked")
public class InstanceManager {
  /// Constant returned from #addHostCreatedInstance() if the manager is closed.
  public static final int INSTANCE_CLOSED = -1;

  // Identifiers are locked to a specific range to avoid collisions with objects
  // created simultaneously from Dart.
  // Host uses identifiers >= 2^16 and Dart is expected to use values n where,
  // 0 <= n < 2^16.
  private static final long MIN_HOST_CREATED_IDENTIFIER = 65536;
  private static final long CLEAR_FINALIZED_WEAK_REFERENCES_INTERVAL = 30000;
  private static final String TAG = "InstanceManager";
  private static final String CLOSED_WARNING = "Method was called while the manager was closed.";

  /** Interface for listening when a weak reference of an instance is removed from the manager. */
  public interface FinalizationListener {
    void onFinalize(long identifier);
  }

  private final WeakHashMap<Object, Long> identifiers = new WeakHashMap<>();
  private final HashMap<Long, WeakReference<Object>> weakInstances = new HashMap<>();
  private final HashMap<Long, Object> strongInstances = new HashMap<>();

  private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
  private final HashMap<WeakReference<Object>, Long> weakReferencesToIdentifiers = new HashMap<>();

  private final Handler handler = new Handler(Looper.getMainLooper());

  private final FinalizationListener finalizationListener;

  private long nextIdentifier = MIN_HOST_CREATED_IDENTIFIER;
  private boolean isClosed = false;

  /**
   * Instantiate a new manager.
   *
   * <p>When the manager is no longer needed, {@link #close()} must be called.
   *
   * @param finalizationListener the listener for garbage collected weak references.
   * @return a new `InstanceManager`.
   */
  public static InstanceManager open(FinalizationListener finalizationListener) {
    return new InstanceManager(finalizationListener);
  }

  private InstanceManager(FinalizationListener finalizationListener) {
    this.finalizationListener = finalizationListener;
    handler.postDelayed(
        this::releaseAllFinalizedInstances, CLEAR_FINALIZED_WEAK_REFERENCES_INTERVAL);
  }

  /**
   * Removes `identifier` and its associated strongly referenced instance, if present, from the
   * manager.
   *
   * @param identifier the identifier paired to an instance.
   * @param <T> the expected return type.
   * @return the removed instance if the manager contains the given identifier, otherwise null if
   *     the manager doesn't contain the value or the manager is closed.
   */
  @Nullable
  public <T> T remove(long identifier) {
    if (assertNotClosed()) {
      return null;
    }
    return (T) strongInstances.remove(identifier);
  }

  /**
   * Retrieves the identifier paired with an instance.
   *
   * <p>If the manager contains a strong reference to `instance`, it will return the identifier
   * associated with `instance`. If the manager contains only a weak reference to `instance`, a new
   * strong reference to `instance` will be added and will need to be removed again with {@link
   * #remove(long)}.
   *
   * <p>If this method returns a nonnull identifier, this method also expects the Dart
   * `InstanceManager` to have, or recreate, a weak reference to the Dart instance the identifier is
   * associated with.
   *
   * @param instance an instance that may be stored in the manager.
   * @return the identifier associated with `instance` if the manager contains the value, otherwise
   *     null if the manager doesn't contain the value or the manager is closed.
   */
  @Nullable
  public Long getIdentifierForStrongReference(Object instance) {
    if (assertNotClosed()) {
      return null;
    }
    final Long identifier = identifiers.get(instance);
    if (identifier != null) {
      strongInstances.put(identifier, instance);
    }
    return identifier;
  }

  /**
   * Adds a new instance that was instantiated from Dart.
   *
   * <p>The same instance can be added multiple times, but each identifier must be unique. This
   * allows two objects that are equivalent (e.g. the `equals` method returns true and their
   * hashcodes are equal) to both be added.
   *
   * <p>If the manager is closed, the addition is ignored and a warning is logged.
   *
   * @param instance the instance to be stored.
   * @param identifier the identifier to be paired with instance. This value must be >= 0 and
   *     unique.
   */
  public void addDartCreatedInstance(Object instance, long identifier) {
    if (assertNotClosed()) {
      return;
    }
    addInstance(instance, identifier);
  }

  /**
   * Adds a new instance that was instantiated from the host platform.
   *
   * @param instance the instance to be stored. This must be unique to all other added instances.
   * @return the unique identifier stored with instance. If the manager is closed, returns -1.
   *     Otherwise, returns a value >= 0.
   */
  public long addHostCreatedInstance(Object instance) {
    if (assertNotClosed()) {
      return INSTANCE_CLOSED;
    }

    if (containsInstance(instance)) {
      throw new IllegalArgumentException(
          String.format("Instance of `%s` has already been added.", instance.getClass()));
    }
    final long identifier = nextIdentifier++;
    addInstance(instance, identifier);
    return identifier;
  }

  /**
   * Retrieves the instance associated with identifier.
   *
   * @param identifier the identifier associated with an instance.
   * @param <T> the expected return type.
   * @return the instance associated with `identifier` if the manager contains the value, otherwise
   *     null if the manager doesn't contain the value or the manager is closed.
   */
  @Nullable
  public <T> T getInstance(long identifier) {
    if (assertNotClosed()) {
      return null;
    }

    final WeakReference<T> instance = (WeakReference<T>) weakInstances.get(identifier);
    if (instance != null) {
      return instance.get();
    }
    return null;
  }

  /**
   * Returns whether this manager contains the given `instance`.
   *
   * @param instance the instance whose presence in this manager is to be tested.
   * @return whether this manager contains the given `instance`. If the manager is closed, returns
   *     `false`.
   */
  public boolean containsInstance(Object instance) {
    if (assertNotClosed()) {
      return false;
    }
    return identifiers.containsKey(instance);
  }

  /**
   * Closes the manager and releases resources.
   *
   * <p>Methods called after this one will be ignored and log a warning.
   */
  public void close() {
    handler.removeCallbacks(this::releaseAllFinalizedInstances);
    isClosed = true;
    clear();
  }

  /**
   * Removes all of the instances from this manager.
   *
   * <p>The manager will be empty after this call returns.
   */
  public void clear() {
    identifiers.clear();
    weakInstances.clear();
    strongInstances.clear();
    weakReferencesToIdentifiers.clear();
  }

  /**
   * Whether the manager has released resources and is no longer usable.
   *
   * <p>See {@link #close()}.
   */
  public boolean isClosed() {
    return isClosed;
  }

  private void releaseAllFinalizedInstances() {
    WeakReference<Object> reference;
    while ((reference = (WeakReference<Object>) referenceQueue.poll()) != null) {
      final Long identifier = weakReferencesToIdentifiers.remove(reference);
      if (identifier != null) {
        weakInstances.remove(identifier);
        strongInstances.remove(identifier);
        finalizationListener.onFinalize(identifier);
      }
    }
    handler.postDelayed(
        this::releaseAllFinalizedInstances, CLEAR_FINALIZED_WEAK_REFERENCES_INTERVAL);
  }

  private void addInstance(Object instance, long identifier) {
    if (identifier < 0) {
      throw new IllegalArgumentException(String.format("Identifier must be >= 0: %d", identifier));
    }
    if (weakInstances.containsKey(identifier)) {
      throw new IllegalArgumentException(
          String.format("Identifier has already been added: %d", identifier));
    }
    final WeakReference<Object> weakReference = new WeakReference<>(instance, referenceQueue);
    identifiers.put(instance, identifier);
    weakInstances.put(identifier, weakReference);
    weakReferencesToIdentifiers.put(weakReference, identifier);
    strongInstances.put(identifier, instance);
  }

  private boolean assertNotClosed() {
    if (isClosed()) {
      Log.w(TAG, CLOSED_WARNING);
      return true;
    }
    return false;
  }
}
