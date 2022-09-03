package org.quiltmc.qsl.command.test;

import com.mojang.brigadier.StringReader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.command.api.EntitySelectorOptionRegistrationCallback;
import org.quiltmc.qsl.command.api.QuiltEntitySelectorReader;

public class EntitySelectorOptionsTest implements EntitySelectorOptionRegistrationCallback {

	@Override
	public void registerEntitySelectors(EntitySelectorOptionRegistrar registrar) {
		registrar.register(
				new Identifier("quilt_command_testmod", "health"),
				optionReader -> {
					StringReader reader = optionReader.getReader();
					var health = reader.readFloat();
					optionReader.setPredicate(e -> e instanceof LivingEntity l && l.getHealth() >= health);
					((QuiltEntitySelectorReader)optionReader).setFlag("selectsHealth", true);
				},
				optionReader -> !((QuiltEntitySelectorReader)optionReader).getFlag("selectsHealth"),
				Text.literal("With health greater than given value")
		);
	}
}
