/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.testing.impl.game;

import java.util.function.Consumer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.Holder;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestData;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.test.TestInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Represents Quilt's extended {@link TestInstance}.
 */
@ApiStatus.Internal
public final class QuiltTestInstance extends TestInstance {
	public static final MapCodec<QuiltTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
			.group(
				Identifier.CODEC.fieldOf("id").forGetter(QuiltTestInstance::id)
			)
			.apply(instance, QuiltGameTestImpl::getQuiltTest)
	);

	private final Class<?> sourceClass;
	private final Identifier id;
	private final Consumer<TestContext> testInvoker;

	public QuiltTestInstance(
			TestData<Holder<TestEnvironmentDefinition>> data, Consumer<TestContext> testInvoker, Identifier id,
			Class<?> sourceClass
	) {
		super(data);
		this.testInvoker = testInvoker;
		this.id = id;
		this.sourceClass = sourceClass;
	}

	public Class<?> getSourceClass() {
		return this.sourceClass;
	}

	public Identifier id() {
		return this.id;
	}

	@Override
	public void start(TestContext context) {
		context.succeedWhen(() -> this.testInvoker.accept(context));
	}

	@Override
	public MapCodec<? extends TestInstance> getCodec() {
		return CODEC;
	}

	@Override
	protected MutableText getDescription() {
		return Text.literal("Quilt Test for Class `" + this.sourceClass.getName() + "`");
	}
}
