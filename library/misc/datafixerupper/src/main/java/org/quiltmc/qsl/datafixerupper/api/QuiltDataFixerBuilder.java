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

import net.minecraft.SharedConstants;

/**
 * An extended variant of the {@link DataFixerBuilder} class, which provides an extra method.
 */
public class QuiltDataFixerBuilder extends DataFixerBuilder {
	public QuiltDataFixerBuilder(@Range(from = 0, to = Integer.MAX_VALUE) int dataVersion) {
		super(dataVersion);
	}

	/**
	 * Builds the final {@code DataFixer}.
	 * <p>
	 * This will build either an {@linkplain #buildUnoptimized() unoptimized fixer} or an
	 * {@linkplain #buildOptimized(Executor) optimized fixer}, depending on the vanilla game's settings.
	 *
	 * @param executorGetter the executor supplier, only invoked if the game is using optimized data fixers
	 * @return the newly built data fixer
	 */
	@Contract(value = "_ -> new")
	public @NotNull DataFixer build(@NotNull Supplier<Executor> executorGetter) {
		return switch (SharedConstants.DATA_FIXER_MODE) {
			case UNINITIALIZED_UNOPTIMIZED, INITIALIZED_UNOPTIMIZED -> buildUnoptimized();
			case UNINITIALIZED_OPTIMIZED, INITIALIZED_OPTIMIZED -> buildOptimized(executorGetter.get());
		};
	}
}
