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

package org.quiltmc.qsl.resource.loader.impl.cache;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.qsl.resource.loader.impl.ModIoOps;

/**
 * Contains the definition of a tree structure for caching, each node represents either a directory, a file, or a missing entry.
 * <p>
 * This may be dangerous if someone does a lot of one-time access.
 *
 * @author LambdAurora
 */
@ApiStatus.Internal
public final class CacheTree {
	/**
	 * {@return a new tree, represented as a root branch node}
	 */
	static Branch newTree() {
		return new Branch(null, null);
	}

	/**
	 * Represents a node of the tree.
	 */
	public abstract static class Node {
		private final Branch parent;
		private final String path;
		private final EntryType type;

		protected Node(Branch parent, String path, EntryType type) {
			this.parent = parent;
			this.path = path;
			this.type = type;
		}

		public Branch getParent() {
			return this.parent;
		}

		public boolean isRoot() {
			return this.getParent() == null;
		}

		public String getPathPart() {
			return this.path;
		}

		public String getFullPath() {
			var list = new ArrayList<String>();
			var elem = this;

			while (!elem.isRoot()) {
				list.add(0, elem.getPathPart());

				elem = elem.getParent();
			}

			return String.join("/", list);
		}

		public EntryType getType() {
			return this.type;
		}

		public @Nullable ResourceAccess.Entry toEntry(ModIoOps ops) {
			if (this.type == EntryType.EMPTY) return null;

			return new ResourceAccess.Entry(ops.getNormalizedPath(this.getFullPath()), this.type);
		}
	}

	/**
	 * Represents a branch of the tree.
	 */
	public static final class Branch extends Node {
		private final Map<String, Node> nodes = new ConcurrentHashMap<>();

		public Branch(Branch parent, String path) {
			super(parent, path, EntryType.DIRECTORY);
		}

		public Branch putBranch(String name) {
			var child = new Branch(this, name);
			this.nodes.put(name, child);
			return child;
		}

		public void putEmpty(String name) {
			var child = new Leaf(this, name, EntryType.EMPTY);
			this.nodes.put(name, child);
		}

		public Leaf putFile(String name) {
			var child = new Leaf(this, name, EntryType.FILE);
			this.nodes.put(name, child);
			return child;
		}

		public @Nullable Node resolveOrCompute(ModIoOps io, String path) {
			int firstSeparator = path.indexOf('/');
			String childName = firstSeparator == -1 ? path : path.substring(0, firstSeparator);

			Node node = this.nodes.get(childName);

			if (node == null) {
				String absolutePath = childName;

				if (!this.isRoot()) {
					absolutePath = this.getFullPath() + '/' + absolutePath;
				}

				var type = io.getEntryType(absolutePath);

				switch (type) {
					case EMPTY -> {
						this.putEmpty(childName);
						return null;
					}
					case DIRECTORY -> {
						Branch branch = this.putBranch(childName);

						if (firstSeparator != -1) {
							return branch.resolveOrCompute(io, path.substring(firstSeparator + 1));
						} else {
							return branch;
						}
					}
					case FILE -> {
						Leaf leaf = this.putFile(childName);

						if (firstSeparator == -1) {
							return leaf;
						}
					}
				}
			} else {
				if (node.getType() == EntryType.EMPTY) {
					return null;
				}

				if (firstSeparator == -1) {
					return node;
				} else if (node instanceof Branch branch) {
					return branch.resolveOrCompute(io, path.substring(firstSeparator + 1));
				}
			}

			return null;
		}
	}

	public static final class Leaf extends Node {
		public Leaf(Branch parent, String path, EntryType type) {
			super(parent, path, type);
		}
	}
}
