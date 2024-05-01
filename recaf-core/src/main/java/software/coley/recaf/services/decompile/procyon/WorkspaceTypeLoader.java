package software.coley.recaf.services.decompile.procyon;

import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.ITypeLoader;
import jakarta.annotation.Nonnull;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.path.ClassPathNode;
import software.coley.recaf.services.decompile.filter.WorkspaceJvmBytecodeFilter;
import software.coley.recaf.workspace.model.Workspace;

import java.util.List;

/**
 * Type loader that pulls classes from a {@link Workspace}.
 *
 * @author xDark
 */
public final class WorkspaceTypeLoader implements ITypeLoader {
	private final Workspace workspace;
	private final List<WorkspaceJvmBytecodeFilter> workspaceFilters;

	/**
	 * @param workspace
	 * 		Active workspace.
	 * @param workspaceFilters
	 * 		Workspace filters.
	 */
	public WorkspaceTypeLoader(@Nonnull Workspace workspace, @Nonnull List<WorkspaceJvmBytecodeFilter> workspaceFilters) {
		this.workspace = workspace;
		this.workspaceFilters = workspaceFilters;
	}

	@Override
	public boolean tryLoadType(String internalName, Buffer buffer) {
		internalName = WorkspaceJvmBytecodeFilter.unapplyClassNameFilters(workspace, workspaceFilters, internalName);
		ClassPathNode node = workspace.findClass(internalName);
		if (node == null)
			return false;
		JvmClassInfo jvmClass = node.getValue().asJvmClass();
		byte[] data = WorkspaceJvmBytecodeFilter.applyFilters(workspace, workspaceFilters, jvmClass, jvmClass.getBytecode());
		buffer.position(0);
		buffer.putByteArray(data, 0, data.length);
		buffer.position(0);
		return true;
	}
}
