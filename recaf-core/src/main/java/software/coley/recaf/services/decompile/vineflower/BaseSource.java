package software.coley.recaf.services.decompile.vineflower;

import jakarta.annotation.Nonnull;
import org.jetbrains.java.decompiler.main.extern.IContextSource;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.services.decompile.filter.WorkspaceJvmBytecodeFilter;
import software.coley.recaf.workspace.model.Workspace;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Base Vineflower class/library source.
 *
 * @author therathatter
 */
public abstract class BaseSource implements IContextSource {
	protected final Workspace workspace;
	protected final List<WorkspaceJvmBytecodeFilter> workspaceFilters;

	/**
	 * @param workspace
	 * 		Workspace to pull class files from.
	 */
	protected BaseSource(@Nonnull Workspace workspace, @Nonnull List<WorkspaceJvmBytecodeFilter> workspaceFilters) {
		this.workspace = workspace;
		this.workspaceFilters = workspaceFilters;
	}

	@Override
	public String getName() {
		return "Recaf";
	}

	@Override
	public InputStream getInputStream(String resource) {
		String name = resource.substring(0, resource.length() - IContextSource.CLASS_SUFFIX.length());
		name = WorkspaceJvmBytecodeFilter.unapplyClassNameFilters(workspace, workspaceFilters, name);
		ClassPathNode node = workspace.findClass(name);
		if (node == null) return InputStream.nullInputStream();
		JvmClassInfo jvmClass = node.getValue().asJvmClass();
		byte[] bytes = WorkspaceJvmBytecodeFilter.applyFilters(workspace, workspaceFilters, jvmClass, jvmClass.getBytecode());
		return new ByteArrayInputStream(bytes);
	}
}
