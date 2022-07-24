/**
 * <h2>Item Rendering Extensions</h2>
 *
 * Provides APIs for adding to and modifying the rendering of items in inventories.
 *
 * <p><h3>Item Overlay Hooks</h3>
 *
 * This API adds the {@code Item.preRenderOverlay} method, which allows you to add to the item's overlay <em>before</em>
 * any other components of it are drawn.<br>
 * Additionally, you can return {@code false} from this method to cancel the rest of the overlay rendering.
 * <p>
 * This API also adds the {@code Item.postRenderOverlay} method, which allows you to add to the item's overlay <em>after</em>
 * all other components of it are drawn.
 *
 * <p><h3>Item Overlay Component Customization</h3>
 *
 * This API adds the {@code Item.getCountLabelRenderer}, {@code Item.getItemBarRenderers},
 * and {@code Item.getCooldownOverlayRenderer} methods to allow for customizing specific parts (components)
 * of the item overlay.
 *
 * <p><h4>Compatibility</h4>
 *
 * The component customization API is compatible with mods that inject into {@code renderGuiItemOverlay} directly,
 * <em>as long as</em> the mod doesn't also try to use this API.
 * <p>
 * In other words, using the API and injecting into {@code renderGuiItemOverlay} are <em>mutually exclusive</em>
 * (the API will win).
 * <p>
 * This is due to how the API injects its rendering methods - it adds a completely separate "customized" rendering path,
 * which is taken <em>only</em> if the item does not customize any of its component renderers.
 *
 * @see org.quiltmc.qsl.rendering.item.api.client.QuiltItemRenderingExtensions
 * @see org.quiltmc.qsl.rendering.item.api.client.CountLabelRenderer
 * @see org.quiltmc.qsl.rendering.item.api.client.ItemBarRenderer
 * @see org.quiltmc.qsl.rendering.item.api.client.CooldownOverlayRenderer
 */

package org.quiltmc.qsl.rendering.item.api.client;
