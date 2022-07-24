package qsl.internal.extension;

import org.gradle.api.Project;
import qsl.internal.Versions;

public class QslExtension {
	private static final String RESOLVE_QUILTFLOWER_TASK = "resolveQuiltflower";
	private static final String GEN_SOURCES_WITH_QUILTFLOWER_TASK = "genSourcesWithQuiltflower";
	private static final String[] TASKS_TO_DISABLE = {RESOLVE_QUILTFLOWER_TASK};
	private static final String[] GEN_TASKS_TO_DISABLE = {"genSources", "genSourcesWithCfr", "resolveQuiltflower", GEN_SOURCES_WITH_QUILTFLOWER_TASK};
	protected final Project project;
	private boolean genTasksAreAllowed = false;

	public QslExtension(Project project) {
		this.project = project;

		this.project.afterEvaluate(p -> {
			for (var task : TASKS_TO_DISABLE) {
				p.getTasks().findByName(task).setEnabled(false);
			}

			if (this.genTasksAreAllowed) {
				p.getTasks().findByName(GEN_SOURCES_WITH_QUILTFLOWER_TASK).dependsOn(p.getRootProject().getTasks().findByName(RESOLVE_QUILTFLOWER_TASK));
			} else {
				for (var task : GEN_TASKS_TO_DISABLE) {
					p.getTasks().findByName(task).setEnabled(false);
				}
			}
		});
	}

	protected void allowGenTasks() {
		this.genTasksAreAllowed = true;
	}

	public void setVersion(String version) {
		this.project.setVersion(version + '+' + Versions.MINECRAFT_VERSION.version()
				+ (System.getenv("SNAPSHOTS_URL") != null ? "-SNAPSHOT" : ""));
	}
}
