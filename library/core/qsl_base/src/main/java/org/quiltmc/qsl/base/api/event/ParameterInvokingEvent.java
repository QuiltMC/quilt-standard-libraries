/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.base.api.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker interface that indicates an event stored in a field or returned by a method may invoke the event callback on
 * one or more context objects passed in some method parameters.
 *
 * <p>The primary use of this annotation is for static analysis and documentation. When a field or method return value
 * is annotated as a parameter invoking, you are encouraged to note in the documentation which context parameters may be
 * invoked.
 *
 * <p>Event implementations may have an undefined order in which the parameters may be invoked, see the documentation of
 * the event for specific invocation order.
 *
 * <p>For the examples below, we will be using an event where the callback type is the interface
 * {@code ScaryEvents.Spooked}. {@code Spooked} is a functional interface where the first parameter is a ghost and the
 * second parameter is the entity being spooked. The entity being spooked is the invoking parameter.
 *
 * <h2>Why make an event parameter invoking?</h2>
 * <p>
 * A good reason to make an event parameter invoking is when the event directly relates to an action performed on a
 * specific object. For example, an entity being unloaded from a world. With a traditional event, you would register a
 * callback and check for your specific entity in the callback, typically via an {@code instanceof} check for modded
 * entities. With a parameter invoking event, if you directly control the object the action is being performed on such
 * as the object being one of your own modded entities, you can implement the callback interface, {@code T}, and have
 * the event execute the callback on your own object, removing the need to find your specific entity through further
 * checks.
 *
 * <p>Assuming {@code ScaryEvents.SPOOKED}'s callback interface is {@code Spooked} and the event is parameter invoking,
 * you may implement {@code ScaryEvents.Spooked} directly on to your Entity to be notified that when your entity is
 * spooked.
 *
 * <pre>{@code
 * class MyEntity extends MobEntity implements ScaryEvents.Spooked {
 *     ...
 *
 *     @Override
 *     public void spooked(Ghost ghost, Entity ignored) {
 *         // This method will be invoked when the event, `ScaryEvents.SPOOKED` is executed and the invokable parameter
 *         // is instanceof MyEntity
 *
 *         // My entity is very scared of ghosts, hence it should die when spooked.
 *         this.setHealth(0);
 *     }
 * }
 * }</pre>
 *
 * <h2>How to use this annotation</h2>
 * <p>
 * An event which may invoke a parameter should first place the annotation on the field of the event instance or the
 * method which returns an event instance.
 *
 * <pre>{@code
 * @ParameterInvokingEvent
 * public static final Event<ScaryEvents.Spooked> SPOOKED = Event.create(ScaryEvents.Spooked.class, callbacks -> (ghost, spookedEntity) -> {
 *     // Since this event is parameter invoking, does the entity being spooked implement `Spooked`?
 *     if (spookedEntity instanceof ScaryEvents.Spooked callback) {
 *         // Since the entity implements the callback, invoke the callback on the entity.
 *         // For this event we invoke on the parameter first, other events may do this differently.
 *         callback.spooked(ghost, spookedEntity);
 *     }
 *
 *     for (var callback : callbacks) {
 *         // Invoke the callbacks implemented by this event
 *         callback.spooked(ghost, spookedEntity);
 *     }
 * });
 * }</pre>
 */
@Documented // Documentation
@Retention(RetentionPolicy.CLASS) // For static analysis
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ParameterInvokingEvent {}
