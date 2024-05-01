package software.coley.recaf.services.decompile.vineflower;

import jakarta.annotation.Nonnull;
import org.jetbrains.java.decompiler.main.extern.IContextSource;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.services.decompile.filter.WorkspaceJvmBytecodeFilter;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Full library source for Vineflower.
 *
 * @author Matt Coley
 * @author therathatter
 */
public class LibrarySource extends BaseSource {
	/**
	 * @param workspace
	 * 		Workspace to pull class files from.
	 */
	protected LibrarySource(@Nonnull Workspace workspace, List<WorkspaceJvmBytecodeFilter> workspaceFilters) {
		super(workspace, workspaceFilters);
	}

	@Override
	public Entries getEntries() {
		List<Entry> entries = workspace.getAllResources(false).stream()
				.map(WorkspaceResource::getJvmClassBundle)
				.flatMap(c -> c.keySet().stream())
				.map(className -> WorkspaceJvmBytecodeFilter.applyClassNameFilters(workspace, workspaceFilters, className))
				.map(className -> new Entry(className, Entry.BASE_VERSION))
				.collect(Collectors.toList());
		return new Entries(entries, Collections.emptyList(), Collections.emptyList());
	}
}
