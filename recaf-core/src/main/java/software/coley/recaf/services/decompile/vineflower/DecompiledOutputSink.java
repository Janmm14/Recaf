package software.coley.recaf.services.decompile.vineflower;

import jakarta.annotation.Nonnull;
import org.jetbrains.java.decompiler.main.extern.IContextSource;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.services.decompile.filter.WorkspaceJvmBytecodeFilter;
import software.coley.recaf.workspace.model.Workspace;

import java.io.IOException;
import java.util.List;

/**
 * Output sink for Vineflower decompiler.
 *
 * @author therathatter
 */
public class DecompiledOutputSink implements IContextSource.IOutputSink {
	protected final JvmClassInfo target;
	private final String filteredTarget;
	protected final ThreadLocal<String> out = new ThreadLocal<>();
	private final List<WorkspaceJvmBytecodeFilter> workspaceFilters;

	/**
	 * @param target
	 * 		Target class to get output of.
	 */
	protected DecompiledOutputSink(@Nonnull Workspace workspace, @Nonnull JvmClassInfo target, @Nonnull List<WorkspaceJvmBytecodeFilter> workspaceFilters) {
		this.target = target;
		String filteredTarget = target.getName();
		for (WorkspaceJvmBytecodeFilter filter : workspaceFilters) {
			filteredTarget = filter.filterClassName(workspace, filteredTarget);
		}
		this.filteredTarget = filteredTarget;
		this.workspaceFilters = workspaceFilters;
	}

	/**
	 * @return Local wrapper of decompilation output.
	 */
	@Nonnull
	protected ThreadLocal<String> getDecompiledOutput() {
		return out;
	}

	@Override
	public void begin() {
		// no-op
	}

	@Override
	public void acceptClass(String qualifiedName, String fileName, String content, int[] mapping) {
		if (filteredTarget.equals(qualifiedName))
			out.set(content);
	}

	@Override
	public void acceptDirectory(String directory) {
		// no-op
	}

	@Override
	public void acceptOther(String path) {
		// no-op
	}

	@Override
	public void close() throws IOException {
		// no-op
	}
}
