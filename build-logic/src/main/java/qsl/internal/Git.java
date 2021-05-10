package qsl.internal;

import java.util.Iterator;

import net.kyori.indra.git.IndraGitExtension;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public final class Git {
	/**
	 * Gets the latest commit hash for a project.
	 * This will do nothing outside of the `library` folder.
	 */
	public static String getLatestCommitHash(Project project) {
		IndraGitExtension indraGit = project.getExtensions().getByType(IndraGitExtension.class);

		if (!indraGit.isPresent()) {
			return null;
		}

		try {
			Iterator<RevCommit> iterator = indraGit.git()
					.log()
					.add(indraGit.commit()) // Start traversal from current commit
					.addPath("library" + project.getPath().replace(':', '/')) // Resolve commits from project dir
					.setMaxCount(1) // Only one commit
					.call()
					.iterator();

			// No commits exist - you will need to create a commit to have a hash
			if (!iterator.hasNext()) {
				return "uncommited";
			}

			// We only care about the last commit
			return ObjectId.toString(iterator.next());
		} catch (GitAPIException | MissingObjectException | IncorrectObjectTypeException e) {
			throw new GradleException(String.format("Failed to get commit hash of last commit from project %s", project.getName()), e);
		}
	}

	private Git() {
	}
}
