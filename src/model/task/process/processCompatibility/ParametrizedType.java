package model.task.process.processCompatibility;

public class ParametrizedType {
	private final Class<?> classT;
	private final Class<?> principalClass;
	private final Class<?> associatedInterface;

	public ParametrizedType(Class<?> parameterClass, Class<?> principalClass, Class<?> associatedInterface) {
		super();
		this.classT = parameterClass;
		this.principalClass = principalClass;
		this.associatedInterface = associatedInterface;
	}

	public Class<?> getParameterClass() {
		return classT;
	}

	public Class<?> getPrincipalClass() {
		return principalClass;
	}
	
	public Class<?> getAssociatedInterface() {
		return associatedInterface;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((associatedInterface == null) ? 0 : associatedInterface.hashCode());
		result = prime * result + ((classT == null) ? 0 : classT.hashCode());
		result = prime * result + ((principalClass == null) ? 0 : principalClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParametrizedType other = (ParametrizedType) obj;
		if (associatedInterface == null) {
			if (other.associatedInterface != null)
				return false;
		} else if (!associatedInterface.equals(other.associatedInterface))
			return false;
		if (classT == null) {
			if (other.classT != null)
				return false;
		} else if (!classT.equals(other.classT))
			return false;
		if (principalClass == null) {
			if (other.principalClass != null)
				return false;
		} else if (!principalClass.equals(other.principalClass))
			return false;
		return true;
	}
}
