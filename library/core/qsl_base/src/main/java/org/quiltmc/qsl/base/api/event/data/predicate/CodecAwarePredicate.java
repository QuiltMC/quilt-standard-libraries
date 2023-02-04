package org.quiltmc.qsl.base.api.event.data.predicate;

import java.util.function.Predicate;

import org.quiltmc.qsl.base.api.event.data.CodecAware;

/**
 * A predicate which may be aware of a codec that can be used to encode it.
 * @param <T> the type of the input to the test
 */
public interface CodecAwarePredicate<T> extends CodecAware, Predicate<T> {

}
