package qsl.internal.json;

import org.gradle.api.Project;
import org.quiltmc.json5.JsonWriter;
import qsl.internal.dependency.QslLibraryDependency;
import qsl.internal.extension.QslModuleExtension;
import qsl.internal.extension.QslModuleExtensionImpl;

import java.io.IOException;
import java.nio.file.Path;

public final class QmjBuilder {
	public static void buildQmj(Project project, String version, String loaderVersion, String minecraftVersion, QslModuleExtensionImpl ext, Path path) throws IOException {
		JsonWriter writer = JsonWriter.json(path);
		// write everything that is always present
		writer.beginObject()
				.name("schema_version").value(1)
				.name("quilt_loader").beginObject() // root object -> quilt_loader
				.name("group").value("org.quiltmc.qsl." + ext.getLibrary().get())
				.name("id").value(ext.getId().get())
				.name("version").value(version)
				.name("metadata").beginObject() // quilt_loader -> metadata
				.name("name").value(ext.getName().get())
				.name("description").value(ext.getDescription().get())
				.name("contributors").beginObject() // metadata -> contributors
				.name("Owner").value("QuiltMC: QSL Team")
				.endObject() // contributors -> metadata
				.name("contact").beginObject() // contributors -> contact
				.name("homepage").value("https://quiltmc.org")
				.name("issues").value("https://github.com/quiltmc/quilt-standard-libaries/issues")
				.name("sources").value("https://github.com/quiltmc/quilt-standard-libraries")
				.endObject() // contact -> metadata
				.name("license").value("Apache-2.0")
				.name("icon").value("assets/qsl_" + ext.getLibrary().get() + "/icon.png")
				.endObject(); // metadata -> quilt_loader
		writer.name("intermediate_mappings").value("net.fabricmc:intermediary");
		writer.name("depends").beginArray();
		writer.beginObject()
				.name("id").value("quilt_loader")
				.name("versions").value(">=" + loaderVersion)
				.endObject()
				.beginObject()
				.name("id").value("minecraft")
					.name("versions").value("=" + minecraftVersion)
					.endObject();
		for (QslLibraryDependency depend : ext.getModuleDependencyDefinitions()) {
			for (QslLibraryDependency.ModuleDependencyInfo moduleDependencyInfo : depend.getDependencyInfo().get()) {
				Project depProject = project.getRootProject().project(depend.getName()).project(moduleDependencyInfo.module());
				QslModuleExtension depExt = depProject.getExtensions().getByType(QslModuleExtension.class);
				writer.beginObject()
						.name("id").value(depExt.getId().get())
						.name("versions").value(depProject.getVersion().toString())
						.endObject();
			}
		}
		writer.endArray(); // depends -> quilt_loader

		if (!ext.getEntrypoints().isEmpty()) {
			writer.name("entrypoints").beginObject(); // quilt_loader -> entrypoints
			for (QslModuleExtensionImpl.NamedWriteOnlyList entrypoint : ext.getEntrypoints()) {
				writer.name(entrypoint.getName());
				writer.beginArray();
				for (String clazz : entrypoint.getValues().get()) {
					writer.value(clazz);
				}
				writer.endArray();
			}
			writer.endObject(); // entrypoints -> quilt_loader
		}
		writer.endObject(); // quilt_loader -> root object

		if (ext.getHasMixins().get()) {
			writer.name("mixin").value(ext.getId().get() + ".mixins.json");
		}

		if (ext.getHasAccessWidener().get()) {
			writer.name("access_widener").value(ext.getId().get() + ".accesswidener");
		}

		// TODO: environment
		if (ext.getEnvironment().get() != Environment.ANY) {
			writer.name("minecraft").beginObject() // root object -> minecraft
					.name("environment").value(ext.getEnvironment().get().qmj)
					.endObject(); // minecraft -> root object
		}
		writer.name("modmenu").beginObject() // root object -> modmenu
				.name("badges").beginArray().value("library").endArray()
				.name("parent").beginObject() // modmenu -> parent
				.name("id").value("qsl")
				.name("name").value("Quilt Standard Libraries")
				.name("description").value("A set of libraries to assist in making Quilt mods.")
				.name("id").value("assets/qsl/icon.png") // Located at Quilt Base API
				.name("badges").beginArray().value("library").endArray()
				.endObject() // parent -> modmenu
				.endObject(); // modmenu -> root

		if (!ext.getInjectedInterfaces().isEmpty()) {
			writer.name("quilt_loom").beginObject() // root object -> quilt_loom
					.name("injected_interfaces").beginObject(); // quilt_loom -> injected_interfaces

			for (QslModuleExtensionImpl.NamedWriteOnlyList injectedInterface : ext.getInjectedInterfaces()) {
				writer.name(injectedInterface.getName());
				writer.beginArray();

				for (String clazz : injectedInterface.getValues().get()) {
					writer.value(clazz);
				}

				writer.endArray();
			}

			writer.endObject() // injected_interfaces -> quilt_loom
					.endObject(); // quilt_loom -> root object
		}

		writer.endObject(); // end root object
		writer.flush();
		writer.close();
	}
}
