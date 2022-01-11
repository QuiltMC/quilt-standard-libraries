package qsl.internal.extension;

import org.gradle.api.Project;
import qsl.internal.Versions;

public class QslExtension {
	private static final String[] TASKS_TO_DISABLE = {"genSources", "genSourcesWithCfr", "genSourcesWithQuiltflower"};
	protected final Project project;

	public QslExtension(Project project) {
		this.project = project;

		this.project.afterEvaluate(p -> {
			for (var task : TASKS_TO_DISABLE) {
				p.getTasks().findByName(task).setEnabled(false);
			}
		});
	}

	public void setVersion(String version) {
		this.project.setVersion(version + '+' + Versions.getMinecraftVersionFancyString()
				+ (System.getenv("SNAPSHOTS_URL") != null ? "-SNAPSHOT" : ""));
	}
}
