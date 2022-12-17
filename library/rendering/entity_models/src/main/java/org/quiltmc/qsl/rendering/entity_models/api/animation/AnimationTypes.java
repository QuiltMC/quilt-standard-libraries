package org.quiltmc.qsl.rendering.entity_models.api.animation;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.client.render.animation.Animation;
import net.minecraft.util.Identifier;

public class AnimationTypes {
	private static final BiMap<Identifier, AnimationType> TYPES = HashBiMap.create();

	public static final AnimationType QUILT_ANIMATION = register("quilt:animation", AnimationCodecs.ANIMATION);

	public static final Codec<AnimationType> TYPE_CODEC = Identifier.CODEC.flatXmap(identifier -> {
		AnimationType type = TYPES.get(identifier);
		return type != null ? DataResult.success(type) : DataResult.error("Unknown animation type: " + identifier);
	}, animationType -> {
		Identifier id = TYPES.inverse().get(animationType);
		return id != null ? DataResult.success(id) : DataResult.error("Unknown animation type.");
	});
	public static Codec<Animation> CODEC = TYPE_CODEC.dispatch(animation -> ((TypedAnimation) (Object) animation).getType(), AnimationType::codec);

	public static AnimationType register(String name, Codec<Animation> codec) {
		return register(new Identifier(name), codec);
	}
	public static AnimationType register(Identifier id, Codec<Animation> codec) {
		AnimationType type = new AnimationType(codec);
		AnimationType old = TYPES.putIfAbsent(id, type);
		if (old != null) {
			throw new IllegalStateException("Duplicate registration for " + id);
		}

		return type;
	}
}
