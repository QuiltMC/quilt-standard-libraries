package org.quiltmc.qsl.worldgen.material_rule.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.class_7196;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import org.apache.commons.logging.Log;
import org.quiltmc.qsl.worldgen.material_rule.api.MaterialRuleRegistrationEvents;
import org.quiltmc.qsl.worldgen.material_rule.impl.RuleHolder;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_7196.class)
public class Class7196Mixin {
	/**
	 * Called right before datapacks get loaded, in order to inject into the vanilla surface rules with our mutable reference.
	 */
	@Inject(
			method = "method_41892(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;)V",
			at = @At("HEAD")
	)
	private static void quilt$injectMaterialRules(MinecraftClient minecraftClient, CreateWorldScreen createWorldScreen, Lifecycle lifecycle, Runnable runnable, CallbackInfo ci) {
		MaterialRuleRegistrationEvents.OVERWORLD_RULE_INIT.invoker().registerRules(RuleHolder.overworldRules);
		MaterialRuleRegistrationEvents.NETHER_RULE_INIT.invoker().registerRules(RuleHolder.netherRules);
		MaterialRuleRegistrationEvents.THE_END_RULE_INIT.invoker().registerRules(RuleHolder.theEndRules);
	}
}
