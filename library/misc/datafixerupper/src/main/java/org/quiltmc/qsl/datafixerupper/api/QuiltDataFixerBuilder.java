/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.datafixerupper.api;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * An extended variant of the {@link DataFixerBuilder} class, which provides an extra method.
 */
public class QuiltDataFixerBuilder extends DataFixerBuilder {
	protected final int dataVersion;

	/**
	 * Creates a new {@code QuiltDataFixerBuilder}.
	 *
	 * @param dataVersion the current data version
	 */
	public QuiltDataFixerBuilder(@Range(from = 0, to = Integer.MAX_VALUE) int dataVersion) {
		super(dataVersion);
		this.dataVersion = dataVersion;
	}

	/**
	 * {@return the current data version}
	 */
	@Range(from = 0, to = Integer.MAX_VALUE)
	public int getDataVersion() {
		return this.dataVersion;
	}

	/**
	 * Builds the final {@code DataFixer}.
	 *
	 * @param executorGetter the executor supplier, always invoked
	 * @return the newly built data fixer
	 * @see #build(Executor)
	 */
	@Contract(value = "_ -> new")
	public @NotNull DataFixer build(@NotNull Supplier<Executor> executorGetter) {
		return this.build(executorGetter.get());
	}
}
