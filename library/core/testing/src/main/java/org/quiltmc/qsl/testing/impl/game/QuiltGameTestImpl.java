/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 The Quilt Project
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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFailureLogger;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestFunctions;
import net.minecraft.test.TestServer;
import net.minecraft.test.TestUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSaveStorage;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.testing.api.game.QuiltGameTest;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;
import org.quiltmc.qsl.testing.api.game.TestMethod;
import org.quiltmc.qsl.testing.api.game.TestRegistrationContext;
import org.quiltmc.qsl.testing.api.game.TestStructureNamePrefix;
import org.quiltmc.qsl.testing.mixin.TestContextAccessor;

@ApiStatus.Internal
public final class QuiltGameTestImpl implements ModInitializer {
	public static final boolean ENABLED = TriState.fromProperty("quilt.game_test").toBooleanOrElse(false);
	public static final boolean COMMAND_ENABLED = TriState.fromProperty("quilt.game_test.command").toBooleanOrElse(ENABLED);
	private static final Map<Class<?>, GameTestData> GAME_TESTS = new Reference2ObjectOpenHashMap<>();
	public static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * Starts a game-test headless server.
	 *
	 * @param storageSession      the storage session
	 * @param resourcePackManager the resource pack manager
	 */
	public static void runHeadlessServer(WorldSaveStorage.Session storageSession, ResourcePackManager resourcePackManager) {
		LOGGER.info("Starting test server...");
		LOGGER.info("By starting a Minecraft server you agree to its EULA.");

		try (var server = TestServer.startServer(
				thread -> TestServer.create(thread, storageSession, resourcePackManager, getBatches(), BlockPos.ORIGIN)
		)) {
			// Server runs.
			server.getThread().join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static Collection<GameTestBatch> getBatches() {
		return TestUtil.createBatches(getTestFunctions());
	}

	private static Collection<TestFunction> getTestFunctions() {
		return TestFunctions.getTestFunctions();
	}

	public static GameTestData getDataForTestClass(Class<?> declaringClass) {
		return GAME_TESTS.get(declaringClass);
	}

	/**
	 * Gets the test function from the given method.
	 *
	 * @param method the method that executes the test
	 * @return the test function
	 */
	public static @NotNull TestFunction getTestFunction(@NotNull Method method) {
		var data = QuiltGameTestImpl.getDataForTestClass(method.getDeclaringClass());

		var gameTest = method.getAnnotation(GameTest.class);
		String testSuiteName = method.getDeclaringClass().getSimpleName().toLowerCase(Locale.ROOT);
		var testCaseName = data.namespace() + ':' + testSuiteName + '/' + method.getName().toLowerCase(Locale.ROOT);

		var structureName = testCaseName;

		if (!gameTest.structureName().isEmpty()) {
			structureName = gameTest.structureName();

			var structurePrefix = method.getDeclaringClass().getAnnotation(TestStructureNamePrefix.class);
			if (structurePrefix != null) {
				structureName = structurePrefix.value() + structureName;
			}
		}

		return new QuiltTestFunction(
				gameTest.batchId(),
				testCaseName,
				structureName,
				StructureTestUtil.getRotation(gameTest.rotation()),
				gameTest.timeout(),
				gameTest.startDelay(),
				gameTest.required(),
				gameTest.requiredSuccesses(),
				gameTest.maxAttempts(),
				QuiltGameTestImpl.getTestMethodInvoker(data, method),
				method.getDeclaringClass()
		);
	}

	/**
	 * Returns the test method invoker from the given method.
	 *
	 * @param method the method
	 * @return the test method invoker
	 */
	private static Consumer<TestContext> getTestMethodInvoker(GameTestData data, Method method) {
		final var testMethod = new TestMethod(method);

		final Class<?> testClass = testMethod.getDeclaringClass();
		final boolean isQuilted = testClass.isAssignableFrom(QuiltGameTest.class);

		return testContext -> {
			var quiltTestContext = new QuiltTestContext(((TestContextAccessor) testContext).getTest());

			if (testMethod.isStatic() && !isQuilted) {
				runTest(testMethod, quiltTestContext, null);
			} else {
				QuiltGameTest instance = data.instance();

				if (instance == null) {
					Constructor<?> constructor;

					try {
						constructor = testClass.getConstructor();
					} catch (NoSuchMethodException e) {
						throw new RuntimeException("Test class (%s) provided by (%s) must have a public default or no args constructor"
								.formatted(testClass.getSimpleName(), data.namespace())
						);
					}

					Object testObject;

					try {
						testObject = constructor.newInstance();
					} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
						throw new RuntimeException("Failed to create instance of test class (%s)".formatted(testClass.getCanonicalName()), e);
					}

					runTest(testMethod, quiltTestContext, testObject);
				} else {
					instance.invokeTestMethod(quiltTestContext, testMethod);
				}
			}
		};
	}

	private static void runTest(TestMethod testMethod, TestContext context, Object instance) {
		testMethod.invoke(instance, context);
	}

	/**
	 * Registers a test class from a mod.
	 *
	 * @param mod       the mod associated with the test class
	 * @param testClass the test class
	 * @param instance  the quilt game test instance if it exists, or {@code null} otherwise
	 */
	public static void registerTestClass(ModContainer mod, Class<?> testClass, @Nullable QuiltGameTest instance) {
		String modId = mod.metadata().id();

		if (GAME_TESTS.containsKey(testClass)) {
			throw new UnsupportedOperationException("Test class (%s) has already been registered with mod (%s)"
					.formatted(testClass.getCanonicalName(), modId)
			);
		}

		GAME_TESTS.put(testClass, new GameTestData(modId, instance));
		TestFunctions.register(testClass);

		LOGGER.debug("Registered test class {} for mod {}", testClass.getCanonicalName(), modId);

		if (instance != null) {
			instance.registerTests(new TestRegistrationContext(mod));
		}
	}

	@Override
	public void onInitialize(ModContainer mod) {
		String reportPath = System.getProperty("quilt.game_test.report_file");

		if (reportPath != null) {
			try {
				TestFailureLogger.setCompletionListener(new SavingXmlReportingTestCompletionListener(new File(reportPath)));
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		var entrypointContainers = QuiltLoader.getEntrypointContainers(
				QuiltGameTest.ENTRYPOINT_KEY, Object.class
		);

		for (var container : entrypointContainers) {
			var entrypoint = container.getEntrypoint();
			Class<?> testClass = entrypoint.getClass();

			registerTestClass(container.getProvider(), testClass, entrypoint instanceof QuiltGameTest gameTest ? gameTest : null);
		}
	}
}
