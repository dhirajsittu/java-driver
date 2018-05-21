/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.core.util.collection;

import com.datastax.oss.driver.shaded.guava.common.collect.Iterables;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/** An immutable list that allows null elements. */
@Immutable
public class NullAllowingImmutableList<E> extends AbstractList<E> implements Serializable {

  @SafeVarargs
  public static <E> NullAllowingImmutableList<E> of(E... elements) {
    return new NullAllowingImmutableList<>(Arrays.copyOf(elements, elements.length));
  }

  public static <E> NullAllowingImmutableList<E> of() {
    return new NullAllowingImmutableList<>(new Object[] {});
  }

  public static <E> NullAllowingImmutableList<E> of(E element) {
    return new NullAllowingImmutableList<>(new Object[] {element});
  }

  public static <E> NullAllowingImmutableList<E> of(E element1, E element2) {
    return new NullAllowingImmutableList<>(new Object[] {element1, element2});
  }

  public static <E> NullAllowingImmutableList<E> of(E element1, E element2, E element3) {
    return new NullAllowingImmutableList<>(new Object[] {element1, element2, element3});
  }

  public static <E> NullAllowingImmutableList<E> copyOf(Iterable<E> iterable) {
    return iterable instanceof NullAllowingImmutableList
        ? (NullAllowingImmutableList<E>) iterable
        : new NullAllowingImmutableList<>(Iterables.toArray(iterable, Object.class));
  }

  public static <E> Builder<E> builder() {
    return builder(16);
  }

  public static <E> Builder<E> builder(int expectedSize) {
    return new Builder<>(expectedSize);
  }

  /** @serial an array containing the list's elements in order */
  private final Object[] elements;

  private NullAllowingImmutableList(Object[] elements) {
    this.elements = elements;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E get(int index) {
    return (E) elements[index];
  }

  @Override
  public int size() {
    return elements.length;
  }

  @NotThreadSafe
  public static class Builder<E> {

    private Object[] elements;
    private int size;

    public Builder(int expectedSize) {
      elements = new Object[expectedSize];
    }

    public Builder<E> add(E newElement) {
      maybeResize(1);
      elements[size++] = newElement;
      return this;
    }

    @SuppressWarnings("unchecked")
    public Builder<E> addAll(Iterable<? extends E> newElements) {
      Collection<E> collection;
      if (newElements instanceof Collection) {
        collection = (Collection<E>) newElements;
      } else {
        collection = new ArrayList<>();
        for (E newElement : newElements) {
          collection.add(newElement);
        }
      }
      maybeResize(collection.size());
      for (Object newElement : collection) {
        elements[size++] = newElement;
      }
      return this;
    }

    public NullAllowingImmutableList<E> build() {
      return new NullAllowingImmutableList<>(
          (size == elements.length) ? elements : Arrays.copyOfRange(elements, 0, size));
    }

    private void maybeResize(int toAdd) {
      int neededSize = size + toAdd;
      if (neededSize < 0) { // overflow
        throw new OutOfMemoryError();
      }
      if (neededSize > elements.length) {
        int newLength = elements.length * 2;
        if (newLength < 0) { // overflow
          newLength = neededSize;
        }
        elements = Arrays.copyOf(elements, newLength);
      }
    }
  }
}
