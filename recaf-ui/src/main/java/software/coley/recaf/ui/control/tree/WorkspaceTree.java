package software.coley.recaf.ui.control.tree;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import software.coley.recaf.info.*;
import software.coley.recaf.path.*;
import software.coley.recaf.services.cell.CellConfigurationService;
import software.coley.recaf.services.navigation.Actions;
import software.coley.recaf.services.workspace.WorkspaceCloseListener;
import software.coley.recaf.ui.config.WorkspaceExplorerConfig;
import software.coley.recaf.ui.control.PathNodeTree;
import software.coley.recaf.util.FxThreadUtil;
import software.coley.recaf.util.StringUtil;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.WorkspaceModificationListener;
import software.coley.recaf.workspace.model.bundle.AndroidClassBundle;
import software.coley.recaf.workspace.model.bundle.FileBundle;
import software.coley.recaf.workspace.model.bundle.JvmClassBundle;
import software.coley.recaf.workspace.model.resource.ResourceAndroidClassListener;
import software.coley.recaf.workspace.model.resource.ResourceFileListener;
import software.coley.recaf.workspace.model.resource.ResourceJvmClassListener;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Tree view for navigating a {@link Workspace}.
 *
 * @author Matt Coley
 */
@Dependent
public class WorkspaceTree extends PathNodeTree implements
		WorkspaceModificationListener, WorkspaceCloseListener,
		ResourceJvmClassListener, ResourceAndroidClassListener, ResourceFileListener {
	private final WorkspaceExplorerConfig explorerConfig;
	private WorkspaceTreeNode root;
	private WorkspacePathNode rootPath;
	private Workspace workspace;

	/**
	 * Initialize empty tree.
	 *
	 * @param configurationService
	 * 		Service to configure cell content.
	 */
	@Inject
	public WorkspaceTree(@Nonnull CellConfigurationService configurationService, @Nonnull Actions actions,
						 @Nonnull WorkspaceExplorerConfig explorerConfig) {
		super(configurationService, actions);

		this.explorerConfig = explorerConfig;
	}

	/**
	 * Sets the workspace, and creates a complete model for it.
	 *
	 * @param workspace
	 * 		Workspace to represent.
	 */
	public void createWorkspaceRoot(@Nullable Workspace workspace) {
		Workspace oldWorkspace = this.workspace;
		if (oldWorkspace != null) {
			// Remove listeners on old workspace
			oldWorkspace.removeWorkspaceModificationListener(this);
			for (WorkspaceResource resource : oldWorkspace.getAllResources(false))
				resource.removeListener(this);
		}

		// Update workspace reference & populate root.
		this.workspace = workspace;
		if (workspace == null) {
			root = null;
		} else {
			// Create root
			rootPath = PathNodes.workspacePath(workspace);
			root = new WorkspaceTreeNode(rootPath);
			List<WorkspaceResource> resources = workspace.getAllResources(false);
			for (WorkspaceResource resource : resources)
				createResourceSubTree(resource);

			// Add listeners
			workspace.addWorkspaceModificationListener(this);
			for (WorkspaceResource resource : resources)
				resource.addListener(this);
		}
		FxThreadUtil.run(() -> setRoot(root));
	}

	/**
	 * Adds the given resource to the tree.
	 * All paths to items contained by the resource are generated <i>(classes, files, etc)</i>.
	 *
	 * @param resource
	 * 		Resource to add to the tree.
	 */
	private void createResourceSubTree(WorkspaceResource resource) {
		ResourcePathNode resourcePath = rootPath.child(resource);
		resource.classBundleStream().forEach(bundle -> {
			Map<String, DirectoryPathNode> directories = new HashMap<>();
			BundlePathNode bundlePath = resourcePath.child(bundle);

			// Pre-sort classes to skip tree-building comparisons/synchronizations.
			TreeSet<ClassInfo> sortedClasses = new TreeSet<>(Named.NAME_PATH_COMPARATOR);
			sortedClasses.addAll(bundle.values());

			// Add each class in sorted order.
			for (ClassInfo classInfo : sortedClasses) {
				String packageName = interceptDirectoryName(classInfo.getPackageName());
				DirectoryPathNode packagePath = directories.computeIfAbsent(packageName, bundlePath::child);
				ClassPathNode classPath = packagePath.child(classInfo);
				WorkspaceTreeNode.getOrInsertIntoTree(root, classPath, true);
			}
		});
		resource.fileBundleStream().forEach(bundle -> {
			Map<String, DirectoryPathNode> directories = new HashMap<>();
			BundlePathNode bundlePath = resourcePath.child(bundle);

			// Pre-sort classes to skip tree-building comparisons/synchronizations.
			TreeSet<FileInfo> sortedFiles = new TreeSet<>(Named.NAME_PATH_COMPARATOR);
			sortedFiles.addAll(bundle.values());

			// Add each class in sorted order.
			for (FileInfo fileInfo : sortedFiles) {
				String directoryName = interceptDirectoryName(fileInfo.getDirectoryName());
				DirectoryPathNode directoryPath = directories.computeIfAbsent(directoryName, bundlePath::child);
				FilePathNode filePath = directoryPath.child(fileInfo);
				WorkspaceTreeNode.getOrInsertIntoTree(root, filePath, true);
			}
		});
	}

	/**
	 * @param workspace
	 * 		Workspace to check.
	 *
	 * @return {@code true} when it matches our current {@link #workspace}.
	 */
	private boolean isTargetWorkspace(Workspace workspace) {
		return this.workspace == workspace;
	}

	/**
	 * @param resource
	 * 		Resource to check.
	 *
	 * @return {@code true} when it belongs to the target workspace.
	 */
	private boolean isTargetResource(WorkspaceResource resource) {
		if (workspace.getPrimaryResource() == resource)
			return true;
		for (WorkspaceResource supportingResource : workspace.getSupportingResources()) {
			if (supportingResource == resource)
				return true;
		}
		for (WorkspaceResource internalSupportingResource : workspace.getInternalSupportingResources()) {
			if (internalSupportingResource == resource)
				return true;
		}
		return false;
	}

	@Override
	public void onWorkspaceClosed(@Nonnull Workspace workspace) {
		// Workspace closed, disable tree.
		if (isTargetWorkspace(workspace))
			setDisable(true);
	}

	@Override
	public void onAddLibrary(@Nonnull Workspace workspace, @Nonnull WorkspaceResource library) {
		if (isTargetWorkspace(workspace))
			createResourceSubTree(library);
	}

	@Override
	public void onRemoveLibrary(@Nonnull Workspace workspace, @Nonnull WorkspaceResource library) {
		if (isTargetWorkspace(workspace))
			root.removeNodeByPath(rootPath.child(library));
	}

	@Override
	public void onNewClass(@Nonnull WorkspaceResource resource, @Nonnull JvmClassBundle bundle, @Nonnull JvmClassInfo cls) {
		if (isTargetResource(resource))
			root.getOrCreateNodeByPath(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(cls.getPackageName()))
					.child(cls));
	}

	@Override
	public void onUpdateClass(@Nonnull WorkspaceResource resource, @Nonnull JvmClassBundle bundle, @Nonnull JvmClassInfo oldCls, @Nonnull JvmClassInfo newCls) {
		if (isTargetResource(resource)) {
			WorkspaceTreeNode node = root.getOrCreateNodeByPath(rootPath
					.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(oldCls.getPackageName()))
					.child(oldCls));
			node.setValue(rootPath.child(resource).child(bundle).child(newCls.getPackageName()).child(newCls));
		}
	}

	@Override
	public void onRemoveClass(@Nonnull WorkspaceResource resource, @Nonnull JvmClassBundle bundle, @Nonnull JvmClassInfo cls) {
		if (isTargetResource(resource))
			root.removeNodeByPath(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(cls.getPackageName()))
					.child(cls));
	}

	@Override
	public void onNewClass(@Nonnull WorkspaceResource resource, @Nonnull AndroidClassBundle bundle, @Nonnull AndroidClassInfo cls) {
		if (isTargetResource(resource))
			root.getOrCreateNodeByPath(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(cls.getPackageName()))
					.child(cls));
	}

	@Override
	public void onUpdateClass(@Nonnull WorkspaceResource resource, @Nonnull AndroidClassBundle bundle, @Nonnull AndroidClassInfo oldCls, @Nonnull AndroidClassInfo newCls) {
		if (isTargetResource(resource)) {
			WorkspaceTreeNode node = root.getOrCreateNodeByPath(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(oldCls.getPackageName()))
					.child(oldCls));
			node.setValue(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(newCls.getPackageName()))
					.child(newCls));
		}
	}

	@Override
	public void onRemoveClass(@Nonnull WorkspaceResource resource, @Nonnull AndroidClassBundle bundle, @Nonnull AndroidClassInfo cls) {
		if (isTargetResource(resource))
			root.removeNodeByPath(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(cls.getPackageName()))
					.child(cls));
	}

	@Override
	public void onNewFile(@Nonnull WorkspaceResource resource, @Nonnull FileBundle bundle, @Nonnull FileInfo file) {
		if (isTargetResource(resource))
			root.getOrCreateNodeByPath(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(file.getDirectoryName()))
					.child(file));
	}

	@Override
	public void onUpdateFile(@Nonnull WorkspaceResource resource, @Nonnull FileBundle bundle, @Nonnull FileInfo oldFile, @Nonnull FileInfo newFile) {
		if (isTargetResource(resource)) {
			WorkspaceTreeNode node = root.getOrCreateNodeByPath(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(oldFile.getDirectoryName()))
					.child(oldFile));
			node.setValue(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(newFile.getDirectoryName()))
					.child(newFile));
		}
	}

	@Override
	public void onRemoveFile(@Nonnull WorkspaceResource resource, @Nonnull FileBundle bundle, @Nonnull FileInfo file) {
		if (isTargetResource(resource))
			root.removeNodeByPath(rootPath.child(resource)
					.child(bundle)
					.child(interceptDirectoryName(file.getDirectoryName()))
					.child(file));
	}

	/**
	 * @param directory
	 * 		Input package or directory name.
	 *
	 * @return Filtered name to prevent bogus paths with thousands of embedded directories.
	 */
	@Nullable
	private String interceptDirectoryName(@Nullable String directory) {
		if (directory == null) return null;

		String[] split = directory.split("/");
		int max = explorerConfig.getMaxTreeDirectoryDepth();
		if (split.length > max) {
			return StringUtil.cutOffAtNth(directory, '/', max) + "...";
		}
		return directory;
	}
}
