package org.quiltmc.qsl.base.api.event.data.predicate;

import java.util.function.Predicate;

import org.quiltmc.qsl.base.api.event.data.CodecAware;

public interface CodecAwarePredicate<T> extends CodecAware, Predicate<T> {

}
