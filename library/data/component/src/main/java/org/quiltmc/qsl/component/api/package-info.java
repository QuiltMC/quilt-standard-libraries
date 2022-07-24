/**
 * <h1>Quilt Component API</h1>
 * An QSL library module, allowing the attachment/querying of arbitrary data and/or behaviour to various game objects.
 * <p/>
 *
 * <h2>Getting Started</h2>
 * The simplest thing you can do using this module is create a {@link org.quiltmc.qsl.component.api.ComponentType}.
 * It works as an identifier for components of a specific type.<br/>
 * To create one simply follow this:
 * <pre>{@code
 * public class MainClass implements ModInitializer {
 *    // ...
 *    public static final ComponentType<AComponent> A_COMPONENT = Components.register(
 *    	new Identifier("modid", "a_component"), // The identifier used for registration
 *    	DefaultAComponent::new // the default factory of this type
 *    );
 * }
 * }</pre>
 * <p/>
 * Java is now gonna complain since we need 2 more things: the AComponent interface as well as the ADefaultComponent default class.
 * Creating them is as simple as this:
 * <pre>{@code
 * public interface AComponent extends Component {
 *     void doSomething();
 *
 *     String getStringData();
 * }
 *
 * public class ADefaultComponent implements AComponent {
 * 	   private final String stringData;
 *
 *     public ADefaultComponent(Component.Operations ignored) {
 *         this.stringData = "Hello there";
 *     }
 *
 *     public void doSomething() {
 *         System.out.println(this.getStringData());
 *     }
 *
 *     public String getStringData() {
 *         return this.stringData;
 *     }
 * }
 * }</pre>
 * <p/>
 * The {@code AComponent} interface provides the API for {@code AComponent} instances.<br/>
 * The {@code ADefaultComponent} class provides a default implementation of the {@code AComponent} interface.
 * The data contained by {@code ADefaultComponent} is not saved or synced. If you want to learn how to save or sync data, <br/>
 * visit the {@linkplain  org.quiltmc.qsl.component.api.component.NbtSerializable NbtComponent} and {@linkplain org.quiltmc.qsl.component.api.component.Syncable SyncedComponent} documentation.
 * <p/>
 * <h2>Component Injection</h2>
 * The {@link org.quiltmc.qsl.component.api.ComponentType} we have created does exist, but nothing actually exposes it's interface.<br/>
 * For that to happen we need to inject our type into game objects.
 * <p/>
 * Game Objects that can have {@linkplain org.quiltmc.qsl.component.api.Component components} injected to them are by convention called {@link org.quiltmc.qsl.component.api.provider.ComponentProvider}s.<br/>
 * The {@linkplain org.quiltmc.qsl.component.api.provider.ComponentProvider providers} implemented by default are the following:
 * <ul>
 *     <li>BlockEntity</li>
 *     <li>Entity</li>
 *     <li>Chunk</li>
 *     <li>World</li>
 *     <li>Level(meaning a game save)</li>
 * </ul>
 * For more info on {@linkplain org.quiltmc.qsl.component.api.provider.ComponentProvider providers} check the {@linkplain org.quiltmc.qsl.component.api.provider.ComponentProvider relevant documentation}.
 * <p/>
 *
 * <p>
 * Choose one of those and then you are 1 step away from getting a functioning component injected to it.<br/>
 * On the below example, I have chosen BlockEntity as the target of my injection.
 * <pre>{@code
 * public class MainClass implements ModInitializer {
 *     // ...
 *       @Override
 *       public void onInitialize(ModContainer mod) {
 * 	   	   // ...
 * 	       Components.inject(ChestBlockEntity.class, A_COMPONENT);
 * 	       // ...
 *       }
 * }
 * }</pre>
 *
 * <p>
 * Now all chests should have {@code AComponent} injected into them, when they are created.
 * <p/>
 * <h2>Accessing Components</h2>
 * For any {@link org.quiltmc.qsl.component.api.provider.ComponentProvider} that has components injected into it, accessing them follows pretty much the same pattern.<br/>
 * You need an instance of the {@link org.quiltmc.qsl.component.api.provider.ComponentProvider} that your component is injected into.<br/>
 * After that you can do this:
 * <pre>{@code
 * //...
 * // consider pos and world to be arguments
 * // passed into the current method.
 * var blockEntity = world.getBlockEntity(pos);
 * // The expose method returns a Maybe instance.
 * // ifJust just unwraps that instance if it exists and gives it to us.
 * blockEntity.expose(MainClass.A_COMPONENT).ifJust(AComponent::doSomething);
 * // notice how we can basically avoid casting the block entity into a chest block entity.
 * //...
 * }</pre>
 * <p/>
 *
 * <p>
 * Keep in mind that when exposing component instances:
 * <ol>
 *     <li>You always get back the type that matches your {@link org.quiltmc.qsl.component.api.ComponentType}. In this case that would be an AComponent instance.</li>
 *     <li>
 *         You always get back a {@link org.quiltmc.qsl.base.api.util.Maybe} instance. You need to use its methods to perform actions on the component.
 *         Furthermore it's not recommended to attempt to {@linkplain org.quiltmc.qsl.base.api.util.Maybe#unwrap() unwrap} the instance, because in the
 *         case the injection failed(for whatever reason), this will crash the game instance.
 *     </li>
 * </ol>
 *
 * @author 0xJoeMama
 */

package org.quiltmc.qsl.component.api;
