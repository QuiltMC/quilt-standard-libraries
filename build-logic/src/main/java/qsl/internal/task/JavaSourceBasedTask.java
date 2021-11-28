package qsl.internal.task;

import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginExtension;

public abstract class JavaSourceBasedTask extends DefaultTask {
	protected void execute(JavaSourceConsumer consumer) {
		var javaConvention = this.getProject().getExtensions()
				.getByType(JavaPluginExtension.class);

		for (var sourceSet : javaConvention.getSourceSets()) {
			for (var javaDir : sourceSet.getAllJava()) {
				Path sourcePath = javaDir.toPath();

				if (sourcePath.endsWith("package-info.java") || sourcePath.endsWith("module-info.java")) {
					continue;
				}

				consumer.consume(this.getProject(), this.getProject().getProjectDir().toPath(), sourcePath);
			}
		}

		consumer.end(this.getLogger());
	}

	public interface JavaSourceConsumer {
		void consume(Project project, Path sourceSetPath, Path path);

		void end(Logger logger);
	}
}
