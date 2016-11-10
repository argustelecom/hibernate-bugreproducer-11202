package ru.argustelecom.system.inf.hibernate.cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

/**
 * 
 * kostd, TASK-74078: freely copy-pasted from {@link DefaultCacheKeysFactory}
 * <p>
 * (absolutely like`em but ignores tenant stuff)
 * 
 * @see WFLY-6596 WrongClassException when using infinispan as remote-store for hibernate entity cache
 * @see HHH-10287 Cache keys no longer include the entity type
 * @see HHH-11083 WrongClassException using Infinispan and sharing cache regions
 * 
 * 
 * @author of copy-past is kostd
 *
 */
public class AffectedClassicalCacheKeysFactory implements CacheKeysFactory {

	@Override
	public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory,
			String tenantIdentifier) {
		return new ClassicalCacheKeyImplementation(id, persister.getKeyType(), persister.getRole(), factory);
	}

	@Override
	public Object createEntityKey(Object id, EntityPersister persister, SessionFactoryImplementor factory,
			String tenantIdentifier) {
		return new ClassicalCacheKeyImplementation(id, persister.getIdentifierType(), persister.getRootEntityName(),
				factory);
	}

	@Override
	public Object createNaturalIdKey(Object[] naturalIdValues, EntityPersister persister, SessionImplementor session) {
		return org.hibernate.cache.internal.DefaultCacheKeysFactory.createNaturalIdKey(naturalIdValues, persister,
				session);
	}

	@Override
	public Object getEntityId(Object cacheKey) {
		return ((ClassicalCacheKeyImplementation) cacheKey).getId();
	}

	@Override
	public Object getCollectionId(Object cacheKey) {
		return ((ClassicalCacheKeyImplementation) cacheKey).getId();
	}

	@Override
	public Object[] getNaturalIdValues(Object cacheKey) {
		return org.hibernate.cache.internal.DefaultCacheKeysFactory.getNaturalIdValues(cacheKey);
	}

	/**
	 * kostd, TASK-74078: freely copy-pasted from {@link OldCacheKeyImplementation}
	 * 
	 *
	 */
	static class ClassicalCacheKeyImplementation implements Serializable {

		private static final long serialVersionUID = 1L;

		private final Object id;
		private final Type type;
		private final String entityOrRoleName;
		private final int hashCode;

		ClassicalCacheKeyImplementation(final Object id, final Type type, final String entityOrRoleName,
				final SessionFactoryImplementor factory) {
			this.id = id;
			this.type = type;
			this.entityOrRoleName = entityOrRoleName;
			this.hashCode = calculateHashCode(type, factory);
		}

		private int calculateHashCode(Type type, SessionFactoryImplementor factory) {
			return type.getHashCode(id, factory);
		}

		public Object getId() {
			return id;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (this == other) {
				return true;
			}
			if (hashCode != other.hashCode() || !(other instanceof ClassicalCacheKeyImplementation)) {
				// hashCode is part of this check since it is pre-calculated and hash must match for equals to be true
				return false;
			}
			final ClassicalCacheKeyImplementation that = (ClassicalCacheKeyImplementation) other;
			return EqualsHelper.equals(entityOrRoleName, that.entityOrRoleName) && type.isEqual(id, that.id);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public String toString() {
			// Used to be required for OSCache
			return entityOrRoleName + '#' + id.toString();
		}

	}

	/**
	 * kostd, TASK-74078: freely copy-pasted from {@link OldCacheKeyImplementation}
	 * 
	 */
	static class ClassicalNaturalIdCacheKey implements Serializable {

		private static final long serialVersionUID = 1L;

		private final Serializable[] naturalIdValues;
		private final String entityName;
		private final int hashCode;
		// "transient" is important here -- NaturalIdCacheKey needs to be Serializable
		private transient ValueHolder<String> toString;

		public ClassicalNaturalIdCacheKey(final Object[] naturalIdValues, Type[] propertyTypes,
				int[] naturalIdPropertyIndexes, final String entityName, final SessionImplementor session) {

			this.entityName = entityName;

			this.naturalIdValues = new Serializable[naturalIdValues.length];

			final SessionFactoryImplementor factory = session.getFactory();

			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.entityName == null) ? 0 : this.entityName.hashCode());
			for (int i = 0; i < naturalIdValues.length; i++) {
				final int naturalIdPropertyIndex = naturalIdPropertyIndexes[i];
				final Type type = propertyTypes[naturalIdPropertyIndex];
				final Object value = naturalIdValues[i];

				result = prime * result + (value != null ? type.getHashCode(value, factory) : 0);

				// The natural id may not be fully resolved in some situations. See HHH-7513 for one of them
				// (re-attaching a mutable natural id uses a database snapshot and hydration does not resolve
				// associations).
				// TODO: The snapshot should probably be revisited at some point. Consider semi-resolving, hydrating,
				// etc.
				if (type instanceof EntityType
						&& type.getSemiResolvedType(factory).getReturnedClass().isInstance(value)) {
					this.naturalIdValues[i] = (Serializable) value;
				} else {
					this.naturalIdValues[i] = type.disassemble(value, session, null);
				}
			}

			this.hashCode = result;
			initTransients();
		}

		private void initTransients() {
			this.toString = new ValueHolder<String>(new ValueHolder.DeferredInitializer<String>() {
				@Override
				public String initialize() {
					// Complex toString is needed as naturalIds for entities are not simply based on a single value like
					// primary keys
					// the only same way to differentiate the keys is to included the disassembled values in the string.
					final StringBuilder toStringBuilder = new StringBuilder().append(entityName).append("##NaturalId[");
					for (int i = 0; i < naturalIdValues.length; i++) {
						toStringBuilder.append(naturalIdValues[i]);
						if (i + 1 < naturalIdValues.length) {
							toStringBuilder.append(", ");
						}
					}
					toStringBuilder.append("]");

					return toStringBuilder.toString();
				}
			});
		}

		public String getEntityName() {
			return entityName;
		}

		public Serializable[] getNaturalIdValues() {
			return naturalIdValues;
		}

		@Override
		public String toString() {
			return toString.getValue();
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			if (this == o) {
				return true;
			}

			if (hashCode != o.hashCode() || !(o instanceof ClassicalNaturalIdCacheKey)) {
				// hashCode is part of this check since it is pre-calculated and hash must match for equals to be true
				return false;
			}

			final ClassicalNaturalIdCacheKey other = (ClassicalNaturalIdCacheKey) o;
			return EqualsHelper.equals(entityName, other.entityName)
					&& Arrays.deepEquals(this.naturalIdValues, other.naturalIdValues);
		}

		private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
			ois.defaultReadObject();
			initTransients();
		}
	}

}
