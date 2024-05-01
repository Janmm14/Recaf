package software.coley.recaf.services.decompile.filter;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.workspace.model.Workspace;

import java.util.List;

/**
 * Filter for bytecode that is applied to all classes in the workspace.
 * 
 * @author Janmm14
 */
public interface WorkspaceJvmBytecodeFilter extends JvmBytecodeFilter {

	/**
	 * Filter the class name. This needs to be reversible with {@link #unfilterClassName(Workspace, String)}.
	 * 
	 * @param className
	 * 		Class name.
	 *
	 * @return Filtered class name.
	 */
	@Nonnull
	String filterClassName(@Nonnull Workspace workspace, @Nonnull String className);

	/**
	 * Undo the effects of {@link #filterClassName(Workspace, String)}.
	 * 
	 * @param className
	 * 		Class name.
	 * @return Unfiltered class name.
	 */
	@Nonnull
	String unfilterClassName(@Nonnull Workspace workspace, @Nonnull String className);

	@Nonnull
	static byte[] applyFilters(@Nonnull Workspace workspace, @Nonnull List<WorkspaceJvmBytecodeFilter> workspaceFilters,
							   @Nonnull JvmClassInfo jvmClass, @Nonnull byte[] code) {
		for (WorkspaceJvmBytecodeFilter filter : workspaceFilters) {
			code = filter.filter(workspace, jvmClass, code);
		}
		return code;
	}
	
	@Nonnull
	static String applyClassNameFilters(@Nonnull Workspace workspace, @Nonnull List<WorkspaceJvmBytecodeFilter> workspaceFilters,
									  @Nonnull String className) {
		for (WorkspaceJvmBytecodeFilter filter : workspaceFilters) {
			className = filter.filterClassName(workspace, className);
		}
		return className;
	}

	@Nonnull
	static String unapplyClassNameFilters(@Nonnull Workspace workspace, @Nonnull List<WorkspaceJvmBytecodeFilter> workspaceFilters,
										@Nonnull String className) {
		for (WorkspaceJvmBytecodeFilter filter : workspaceFilters.reversed()) {
			className = filter.unfilterClassName(workspace, className);
		}
		return className;
	}
}
