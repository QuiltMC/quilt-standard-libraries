package org.quiltmc.qsl.component.impl.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Lazy<T> {

	@NotNull
	private final Supplier<T> sup;
	@Nullable
	private T value;

	@NotNull
	public static <T> Lazy<T> of(@NotNull Supplier<T> sup) {
		return new Lazy<>(sup);
	}

	private Lazy(@NotNull Supplier<T> sup) {
		this.sup = sup;
		this.value = null;
	}

	@NotNull
	public T get() {
		if (value == null) {
			this.value = sup.get();
		}

		return value;
	}

	@NotNull
	public Supplier<T> getSupplier() {
		return sup;
	}

	public boolean isEmpty() {
		return this.value == null;
	}

	@NotNull
	public <U> Lazy<U> map(@NotNull Function<T, U> func) {
		return Lazy.of(() -> func.apply(this.get()));
	}

	public void computeIfPresent(@NotNull Consumer<T> action) {
		if (!this.isEmpty()) {
			action.accept(this.get());
		}
	}

	public void compute(@NotNull Consumer<T> action) {
		action.accept(this.get());
	}
}
