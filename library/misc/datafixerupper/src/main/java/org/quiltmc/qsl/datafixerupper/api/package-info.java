/**
 * <h2>Custom DataFixerUpper API</h2>
 * <p>
 * This API lets you register a {@code DataFixer} for your own mod, letting you use Minecraft's built-in
 * "old save compatibility" system without bodges!
 * <p>
 * Here is an example simple use of this API:
 * <pre><code>
 * // the latest version of the mod's data
 * // this should match the version of the last schema added!
 * // note that the default data version is 0, meaning that you can upgrade
 * //  from a version that did not have a fixer
 * //  (by registering a schema for upgrading from version 0 to version 1)
 * public static final int CURRENT_DATA_VERSION = 1;
 *
 * public static void initialize(ModContainer mod) {
 *     // create a builder
 *     var builder = new QuiltDataFixerBuilder(CURRENT_DATA_VERSION);
 *     // add the "base" version 0 schema
 *     builder.addSchema(0, QuiltDataFixes.BASE_SCHEMA);
 *     // add a schema for upgrading from version 0 to version 1
 *     Schema schemaV1 = builder.addSchema(1, IdentifierNormalizingSchema::new)
 *     // add fixes to the schema - for example, an item rename (identifier change)
 *     SimpleFixes.addItemRenameFix(builder, "Rename cool_item to awesome_item",
 *         new Identifier("mymod", "cool_item"),
 *         new Identifier("mymod", "awesome_item"),
 *         schemaV1);
 *
 *     // register the fixer!
 *     // this will create either an unoptimized fixer or an optimized fixer,
 *     //  depending on the game configuration
 *     QuiltDataFixes.buildAndRegisterFixer(mod, builder);
 * }
 * </code></pre>
 *
 * @see org.quiltmc.qsl.datafixerupper.api.QuiltDataFixes
 * @see org.quiltmc.qsl.datafixerupper.api.SimpleFixes
 * @see org.quiltmc.qsl.datafixerupper.api.QuiltDataFixerBuilder
 */

package org.quiltmc.qsl.datafixerupper.api;
