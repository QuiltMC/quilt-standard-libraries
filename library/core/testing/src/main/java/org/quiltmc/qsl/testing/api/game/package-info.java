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

/**
 * <h2>The Game Test API</h2>
 *
 * <p>
 * <h3>What is the Game Test API?</h3>
 * The Game Test API is the custom automated test framework used by Minecraft.
 * Minecraft uses a custom framework because most pre-existing ones do not adapt well to such a dynamic environment as Minecraft.
 * Thus Mojang made their own testing framework. Watching the <a href="https://www.youtube.com/watch?v=TNkPE6NTNHQ">keynote by Henrik Kniberg</a>
 * is highly recommended to grasp how it works and why it was designed that way.
 * <p>
 * The Game Test API exposed by this module is a bit different to what Vanilla provides. The framework has been adapted to work better with mods, with some extensions.
 *
 * <p>
 * <h3>Get Started</h3>
 * Writing tests for your mod is relatively simple: create a new class that extends {@link org.quiltmc.qsl.testing.api.game.QuiltGameTest}, and add it
 * as an entrypoint of key {@value org.quiltmc.qsl.testing.api.game.QuiltGameTest#ENTRYPOINT_KEY}.
 * <p>
 * Now you can add public methods (may or may not be {@code static}) to the class,
 * they take as argument {@link org.quiltmc.qsl.testing.api.game.QuiltTestContext} which is an extension of {@link net.minecraft.test.TestContext},
 * and annotating them with {@link net.minecraft.test.GameTest} will make them a test to run.
 * <p>
 * One of the special parts of game tests is that they have a structure associated to them, as the goal is to test inside a game world with running
 * game logic. Structures are located in the mod resources as follows: {@code data/<id namespace>/game_test/structures/<id path>.snbt}.
 * For example, the provided empty structure {@link org.quiltmc.qsl.testing.api.game.QuiltGameTest#EMPTY_STRUCTURE {@code quilt:empty}} is located at
 * {@code data/quilt/game_test/structures/empty.snbt}.
 * <p>
 * When running the game with this module a new command will be available: {@code /test}. It allows you to create new test structures,
 * export test structures, run a specific test, or run every test.
 * <p>
 * To run the dedicated server in game test mode (which runs the server, executes the tests, then shuts down), you can pass the argument
 * {@code -Dquilt.game_test=true} to the JVM. You also can pass the argument {@code -Dquilt.game_test.report_file=<path>} to store the test results
 * into the file at the given path.
 * <p>
 * <h4>Example</h4>
 * <pre><code>
 * public class ExampleTestSuite implements QuiltGameTest {
 * 	&#64;GameTest(structureName = QuiltGameTest.EMPTY_STRUCTURE)
 * 	public void noStructure(QuiltTestContext context) {
 * 		context.setBlockState(0, 2, 0, Blocks.DIAMOND_BLOCK);
 *
 * 		context.addInstantFinalTask(() ->
 * 				context.checkBlock(new BlockPos(0, 2, 0), (block) -> block == Blocks.DIAMOND_BLOCK, "Expected block to be diamond")
 * 		);
 * 	}
 * }
 * </code></pre>
 * <p>
 * <h4>Gradle Setup Example</h4>
 * Here's an example of a Gradle setup to handle tests in a separate sourceSet, with a special task to run the tests:
 * <pre><code>
 * sourceSets {
 * 	testmod
 * }
 *
 * dependencies {
 * 	// testmod sourceSet should depend on everything in the main source set.
 * 	testmodImplementation sourceSets.main.output
 * }
 *
 * loom {
 * 	runs {
 * 		testmodClient {
 * 			client()
 * 			source(sourceSets.testmod)
 * 		}
 *
 * 		testmodServer {
 * 			server()
 * 			source(sourceSets.testmod)
 * 		}
 *
 * 		gameTestServer {
 * 			inherit testmodServer
 * 			configName = "Game test server"
 *
 * 			// Enable the game test runner.
 * 			property("quilt.game_test", "true")
 * 			property("quilt.game_test.report_file", "${project.buildDir}/game_test/report.xml")
 * 			runDir("build/game_test")
 * 		}
 * 	}
 * }
 *
 * afterEvaluate {
 * 	tasks.test.dependsOn tasks.runGameTestServer
 * }
 * </code></pre>
 *
 * <p>
 * <h3>Test registration</h3>
 * While methods annotated with {@link net.minecraft.test.GameTest} in a {@link org.quiltmc.qsl.testing.api.game.QuiltGameTest} provided as entrypoint
 * may be sufficient for some, if you have lots of tests, or need conditional testing, you might want to look into
 * {@link org.quiltmc.qsl.testing.api.game.QuiltGameTest#registerTests(TestRegistrationContext)}, which is a method that
 * you can override in the entrypoint to provide more tests through the given context.
 *
 * <p>
 * <h3>Structure Handling</h3>
 * <p>
 * If {@link net.minecraft.test.GameTest#structureName()} isn't specified, the default structure identifier will be
 * {@code <mod namespace>:<test class name (lower case)/<test method name (lower case)}.
 * <p>
 * If you prefer custom structure paths, but have a common prefix in the class, you can annotate the class with
 * {@link org.quiltmc.qsl.testing.api.game.TestStructureNamePrefix} which accept as value the common prefix,
 * then in every {@link net.minecraft.test.GameTest} of the class you only need what's following after the prefix for the structure name.
 *
 * <p>
 * <h3>Custom Test Method Invocation</h3>
 * Test methods are gathered using the {@link net.minecraft.test.GameTest} annotation, but if the class implements
 * {@link org.quiltmc.qsl.testing.api.game.QuiltGameTest} you can override the method
 * {@link org.quiltmc.qsl.testing.api.game.QuiltGameTest#invokeTestMethod(QuiltTestContext, TestMethod)}
 * which entirely overrides how the test is invoked; the default invocation is done using
 * {@link org.quiltmc.qsl.testing.api.game.TestMethod#invoke(java.lang.Object, java.lang.Object...)} with the instance of the class (if the method isn't
 * {@code static}), and the test context as the first parameter.
 */

package org.quiltmc.qsl.testing.api.game;
