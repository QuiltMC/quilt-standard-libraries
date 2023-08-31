package qsl.internal.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import qsl.internal.analysis.ClassAnalysisUtils;

public abstract class GenerateAccessWidenerTask extends DefaultTask {
	private BiConsumer<List<String>, FileSystem> generator;

	public void setGenerator(BiConsumer<List<String>, FileSystem> generator) {
		this.generator = generator;
	}

	@InputFile
	public abstract RegularFileProperty getTemplatePath();

	@OutputFile
	public abstract RegularFileProperty getOutputPath();

	@Inject
	public GenerateAccessWidenerTask() {
		this.setGroup("generation");
	}

	@TaskAction
	public void generateAccessWidener() throws IOException {
		Provider<Path> inputPath = this.getTemplatePath().getAsFile().map(File::toPath);
		List<String> lines;

		if (inputPath.isPresent()) {
			lines = Files.readAllLines(inputPath.get());
		} else {
			lines = new ArrayList<>();
		}

		var fs = ClassAnalysisUtils.loadMinecraftJar(this.getProject());
		this.generator.accept(lines, fs);

		Path path = this.getOutputPath().getAsFile().get().toPath();
		Files.writeString(path, String.join("\n", lines) + '\n');
	}
}
