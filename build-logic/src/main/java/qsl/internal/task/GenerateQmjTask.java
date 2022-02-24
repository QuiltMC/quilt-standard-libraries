package qsl.internal.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import qsl.internal.extension.QslModuleExtensionImpl;
import qsl.internal.json.QmjBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class GenerateQmjTask extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getOutputDir();

	@Nested
	public abstract Property<QslModuleExtensionImpl> getQslModule();
	@TaskAction
	public void generateQmj() throws IOException {
		Path output = getOutputDir().getAsFile().get().toPath().resolve("quilt.mod.json");
		if (Files.exists(output)) {
			Files.delete(output);
		}

		QmjBuilder.buildQmj(getProject(), getProject().getVersion().toString(), "*", "*", getQslModule().get(), output);
	}
}
