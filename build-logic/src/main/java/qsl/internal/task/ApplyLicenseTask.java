package qsl.internal.task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;
import qsl.internal.license.LicenseHeader;

public class ApplyLicenseTask extends JavaSourceBasedTask {
	private final LicenseHeader licenseHeader;

	@Inject
	public ApplyLicenseTask(LicenseHeader licenseHeader) {
		this.licenseHeader = licenseHeader;
		this.setDescription("Apply the correct license headers to source files.");
		this.setGroup("generation");
	}

	@TaskAction
	public void execute() {
		this.execute(new Consumer(this.licenseHeader));
	}

	public static class Consumer implements JavaSourceConsumer {
		private final LicenseHeader licenseHeader;
		private final List<Path> updatedFiles = new ArrayList<>();
		private int total = 0;

		public Consumer(LicenseHeader licenseHeader) {
			this.licenseHeader = licenseHeader;
		}

		@Override
		public void consume(Project project, Path rootPath, Path path) {
			if (this.licenseHeader.format(project, rootPath, path)) {
				this.updatedFiles.add(path);
			}

			this.total++;
		}

		@Override
		public void end(Logger logger) {
			for (var path : this.updatedFiles) {
				logger.lifecycle(" - Updated file {}", path);
			}

			logger.lifecycle("Updated {} out of {} files.", this.updatedFiles.size(), this.total);
		}
	}
}
