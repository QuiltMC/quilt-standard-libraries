/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC, 2021 QuiltMC
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

package org.quiltmc.qsl.tag.api;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;

import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

/**
 * Interface implemented by {@link net.minecraft.tag.Tag.Builder} instances when QSL is present.
 * <p>
 * May be used standalone since it proxies all methods of the original tag builder.
 *
 * @see #create()
 * @see #cast(Tag.Builder)
 */
public interface QuiltTagBuilder {
	/**
	 * Adds a tracked entry to this builder.
	 *
	 * @param trackedEntry the entry
	 * @return this builder
	 * @see Tag.Builder#add(Tag.TrackedEntry)
	 */
	QuiltTagBuilder add(Tag.TrackedEntry trackedEntry);

	/**
	 * Adds a tag entry to this builder.
	 *
	 * @param entry  the entry
	 * @param source the source of the entry
	 * @return this builder
	 * @see Tag.Builder#add(Tag.Entry, String)
	 */
	QuiltTagBuilder add(Tag.Entry entry, String source);

	/**
	 * Adds the given object entry to this builder.
	 *
	 * @param id     the identifier of the object
	 * @param source the source of the entry
	 * @return this builder
	 * @see Tag.Builder#add(Identifier, String)
	 */
	QuiltTagBuilder add(Identifier id, String source);

	/**
	 * Adds the given object entry as optional to this builder.
	 *
	 * @param id     the identifier of the object, may not exist in the associated registry
	 * @param source the source of the entry
	 * @return this builder
	 * @see Tag.Builder#addOptional(Identifier, String)
	 */
	QuiltTagBuilder addOptional(Identifier id, String source);

	/**
	 * Adds the given tag entry to this builder.
	 *
	 * @param id     the identifier of the tag
	 * @param source the source of the entry
	 * @return this builder
	 * @see Tag.Builder#addTag(Identifier, String)
	 */
	QuiltTagBuilder addTag(Identifier id, String source);

	/**
	 * Adds the given tag entry as optional to this builder.
	 *
	 * @param id     the identifier of the tag, may not exist
	 * @param source the source of the entry
	 * @return this builder
	 * @see Tag.Builder#addOptionalTag(Identifier, String)
	 */
	QuiltTagBuilder addOptionalTag(Identifier id, String source);

	/**
	 * Clears the contained entries and mark the tag as replaced.
	 *
	 * @return this builder
	 */
	QuiltTagBuilder clearEntries();

	/**
	 * {@return the stream of the entries which are currently in this builder}
	 *
	 * @see Tag.Builder#streamEntries()
	 */
	Stream<Tag.TrackedEntry> streamEntries();

	/**
	 * Visits the required dependencies.
	 *
	 * @param consumer the visit action
	 * @see Tag.Builder#forEachTagId(Consumer)
	 */
	void visitRequiredDependencies(Consumer<Identifier> consumer);

	/**
	 * Visits the optional dependencies.
	 *
	 * @param consumer the visit action
	 * @see Tag.Builder#forEachGroupId(Consumer)
	 */
	void visitOptionalDependencies(Consumer<Identifier> consumer);

	/**
	 * Reads the entry from the JSON-serialized tag.
	 *
	 * @param json   the JSON representation of the tag
	 * @param source the source of the JSON representation
	 * @return this builder
	 * @see Tag.Builder#read(JsonObject, String)
	 */
	QuiltTagBuilder read(JsonObject json, String source);

	/**
	 * Builds a tag from this tag builder.
	 * <p>
	 * May fail and return a list of missing entries instead.
	 *
	 * @param tagGetter    the tag getter
	 * @param objectGetter the object getter
	 * @param <T>          the type of the content of the tag
	 * @return either a list of missing entries, or the built tag
	 * @see Tag.Builder#build(Function, Function)
	 */
	<T> Either<Collection<Tag.TrackedEntry>, Tag<T>> build(Function<Identifier, Tag<T>> tagGetter,
	                                                       Function<Identifier, T> objectGetter);

	/**
	 * {@return the JSON representation of the tag that is being built}
	 *
	 * @see Tag.Builder#toJson()
	 */
	JsonObject toJson();

	static QuiltTagBuilder create() {
		return cast(Tag.Builder.create());
	}

	static QuiltTagBuilder cast(Tag.Builder builder) {
		return (QuiltTagBuilder) builder;
	}
}
