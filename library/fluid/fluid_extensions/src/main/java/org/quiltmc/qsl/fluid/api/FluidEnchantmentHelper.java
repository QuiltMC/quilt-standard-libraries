package org.quiltmc.qsl.fluid.api;

public class FluidEnchantmentHelper {

	private float horizontalViscosity, speed;

	public FluidEnchantmentHelper(float horizontalViscosity, float speed) {
		this.horizontalViscosity = horizontalViscosity;
		this.speed = speed;
	}

	public float getHorizontalViscosity() {
		return horizontalViscosity;
	}

	public float getSpeed() {
		return speed;
	}
}
