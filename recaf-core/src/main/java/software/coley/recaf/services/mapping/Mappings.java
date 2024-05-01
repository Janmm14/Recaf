package software.coley.recaf.services.mapping;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;

public interface Mappings {
	/**
	 * @param classInfo
	 * 		Class to lookup.
	 *
	 * @return Mapped name of the class, or {@code null} if no mapping exists.
	 */
	@Nullable
	default String getMappedClassName(@Nonnull ClassInfo classInfo) {
		return getMappedClassName(classInfo.getName());
	}

	/**
	 * @param internalName
	 * 		Original class's internal name.
	 *
	 * @return Mapped name of the class, or {@code null} if no mapping exists.
	 */
	@Nullable
	String getMappedClassName(@Nonnull String internalName);

	/**
	 * @param owner
	 * 		Class declaring the field.<br>
	 * 		<b>NOTE</b>: References to class members can point to child sub-types of the class that defines the member.
	 * 		You may need to check the owner's type hierarchy to see if the field is actually defined in a parent class.
	 * @param field
	 * 		Field to lookup.
	 *
	 * @return Mapped name of the field, or {@code null} if no mapping exists.
	 */
	@Nullable
	default String getMappedFieldName(@Nonnull ClassInfo owner, @Nonnull FieldMember field) {
		return getMappedFieldName(owner.getName(), field.getName(), field.getDescriptor());
	}

	/**
	 * @param ownerName
	 * 		Internal name of the class defining the field.<br>
	 * 		<b>NOTE</b>: References to class members can point to child sub-types of the class that defines the member.
	 * 		You may need to check the owner's type hierarchy to see if the field is actually defined in a parent class.
	 * @param fieldName
	 * 		Name of the field.
	 * @param fieldDesc
	 * 		Descriptor of the field.
	 *
	 * @return Mapped name of the field, or {@code null} if no mapping exists.
	 */
	@Nullable
	String getMappedFieldName(@Nonnull String ownerName, @Nonnull String fieldName, @Nonnull String fieldDesc);

	/**
	 * @param owner
	 * 		Class declaring the method.<br>
	 * 		<b>NOTE</b>: References to class members can point to child sub-types of the class that defines the member.
	 * 		You may need to check the owner's type hierarchy to see if the field is actually defined in a parent class.
	 * @param method
	 * 		Method to lookup.
	 *
	 * @return Mapped name of the method, or {@code null} if no mapping exists.
	 */
	@Nullable
	default String getMappedMethodName(@Nonnull ClassInfo owner, @Nonnull MethodMember method) {
		return getMappedMethodName(owner.getName(), method.getName(), method.getDescriptor());
	}

	/**
	 * @param ownerName
	 * 		Internal name of the class defining the method.<br>
	 * 		<b>NOTE</b>: References to class members can point to child sub-types of the class that defines the member.
	 * 		You may need to check the owner's type hierarchy to see if the field is actually defined in a parent class.
	 * @param methodName
	 * 		Name of the method.
	 * @param methodDesc
	 * 		Descriptor of the method.
	 *
	 * @return Mapped name of the method, or {@code null} if no mapping exists.
	 */
	@Nullable
	String getMappedMethodName(@Nonnull String ownerName, @Nonnull String methodName, @Nonnull String methodDesc);

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
	 * @return Mapped name of the variable, or {@code null} if no mapping exists.
	 */
	@Nullable
	String getMappedVariableName(@Nonnull String className, @Nonnull String methodName, @Nonnull String methodDesc,
								 @Nullable String name, @Nullable String desc, int index);
}
