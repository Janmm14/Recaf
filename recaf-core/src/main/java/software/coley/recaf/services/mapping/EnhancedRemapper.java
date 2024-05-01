package software.coley.recaf.services.mapping;

import jakarta.annotation.Nonnull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.commons.Remapper;

public abstract class EnhancedRemapper extends Remapper {
	/**
	 * @param name
	 * 		The name of the method.
	 * @param descriptor
	 * 		The descriptor of the method.
	 * @param bsm
	 * 		The bootstrap method handle.
	 * @param bsmArguments
	 * 		The arguments to the bsm.
	 *
	 * @return New name of the method.
	 */
	@Nonnull
	public abstract String mapInvokeDynamicMethodName(@Nonnull String name, @Nonnull String descriptor, @Nonnull Handle bsm,
											 @Nonnull Object[] bsmArguments);

	/**
	 * @param className
	 * 		Internal name of the class defining the method the variable resides in.
	 * @param methodName
	 * 		Name of the method.
	 * @param methodDesc
	 * 		Descriptor of the method.
	 * @param name
	 * 		Name of the variable.
	 * @param desc
	 * 		Descriptor of the variable.
	 * @param index
	 * 		Index of the variable.
	 *
	 * @return Mapped name of the variable, or the existing name if no mapping exists.
	 */
	@Nonnull
	public abstract String mapVariableName(@Nonnull String className, @Nonnull String methodName, @Nonnull String methodDesc,
										   String name, String desc, int index);
}
