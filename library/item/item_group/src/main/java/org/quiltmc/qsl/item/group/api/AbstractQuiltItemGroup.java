package org.quiltmc.qsl.item.group.api;

import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public abstract class AbstractQuiltItemGroup extends ItemGroup {
	public final Identifier identifier;

	public AbstractQuiltItemGroup(int i, Identifier identifier) {
		super(i, String.format("%s.%s", identifier.getNamespace(), identifier.getPath()));

		this.identifier = identifier;
	}
}
