/*
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

package org.quiltmc.qsl.base.test.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import org.quiltmc.qsl.base.api.event.Event;

public final class EventListenAllTests implements Runnable, TestCallback, GenericTestCallback<Item> {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventListenAllTests.class);

	@Override
	public void run() {
		var testEvent = Event.create(TestCallback.class, listeners -> () -> {
			for (var test : listeners) {
				test.onTest();
			}
		});
		var testGenericEventBlock = Event.<GenericTestCallback<Block>>create(GenericTestCallback.class, listeners -> msg -> {
			for (var test : listeners) {
				test.onGenericTest(msg);
			}
		});
		var testGenericEventItem = Event.<GenericTestCallback<Item>>create(GenericTestCallback.class, listeners -> msg -> {
			for (var test : listeners) {
				test.onGenericTest(msg);
			}
		});

		try {
			Event.listenAll(this, testEvent, testGenericEventBlock);

			throw new IllegalStateException("Event#listenAll failed to refuse registration of listener for event testGenericEventBlock");
		} catch (IllegalArgumentException e) {
			// Expected behavior
		}

		testEvent.invoker().onTest();
		try {
			testGenericEventBlock.invoker().onGenericTest(Blocks.SPRUCE_LOG);
			LOGGER.info("Event#listenAll successfully refused registration of listener for event testGenericEventBlock");
		} catch (ClassCastException e) {
			LOGGER.error("Event#listenAll failed to refuse registration of listener for event testGenericEventBlock");
			throw e;
		}

		Event.listenAll(this, testEvent);
		testGenericEventItem.register(this);

		testEvent.invoker().onTest();
		testGenericEventItem.invoker().onGenericTest(Items.ROSE_BUSH);
	}

	@Override
	public void onTest() {
		LOGGER.info("Tested test event.");
	}

	@Override
	public void onGenericTest(Item obj) {
		LOGGER.info("Hello object {}.", obj);
	}
}
