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

public abstract class GenerateQmjTask extends DefaultTask {
	@OutputDirectory
	abstract DirectoryProperty getOutputDir();

	@Nested
	abstract Property<QslModuleExtensionImpl> getQslModule();
	@TaskAction
	void generateQmj() throws IOException {
		QmjBuilder.buildQmj(getProject().getVersion().toString(), "*", "*", getQslModule().get(),
				getOutputDir().get().getAsFile().toPath().resolve("quilt.mod.json"));
	}
}
