package qsl.internal.task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;
import qsl.internal.license.LicenseHeader;

public class CheckLicenseTask extends JavaSourceBasedTask {
	private final LicenseHeader licenseHeader;

	@Inject
	public CheckLicenseTask(LicenseHeader licenseHeader) {
		this.licenseHeader = licenseHeader;
		this.setDescription("Check whether source files contain a valid license header.");
		this.setGroup("verification");
	}

	@TaskAction
	public void execute() {
		this.execute(new Consumer(this.licenseHeader));
	}

	public static class Consumer implements JavaSourceConsumer {
		private final LicenseHeader licenseHeader;
		private final List<Path> failedChecks = new ArrayList<>();
		private int total = 0;

		public Consumer(LicenseHeader licenseHeader) {
			this.licenseHeader = licenseHeader;
		}

		@Override
		public void consume(Project project, Path sourceSetPath, Path path) {
			if (!this.licenseHeader.validate(path)) {
				this.failedChecks.add(path);
			}

			this.total++;
		}

		@Override
		public void end(Logger logger) {
			if (this.failedChecks.isEmpty()) {
				logger.lifecycle("All license header checks passed ({} files).", this.total);
			} else {
				for (var failedPath : this.failedChecks) {
					logger.error(" - {} - license checks have failed.", failedPath);
				}

				throw new GradleException(
						String.format("License header checks have failed on %s out of %d files.",
								this.failedChecks.size(), this.total
						)
				);
			}
		}
	}
}
