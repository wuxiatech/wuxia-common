package cn.wuxia.common.spring.orm.core.jpa.factory;


import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

import cn.wuxia.common.spring.orm.core.jpa.repository.support.JpaSupportRepository;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BasicJpaRepositoryFactory extends JpaRepositoryFactory {

	private EntityManager entityManager;

	public BasicJpaRepositoryFactory(EntityManager entityManager) {
		super(entityManager);
		this.entityManager = entityManager;

	}

	public void init() {
	}

	private boolean isQueryDslExecutor(Class<?> repositoryInterface) {

		return QuerydslUtils.QUERY_DSL_PRESENT && QuerydslJpaRepository.class.isAssignableFrom(repositoryInterface);
	}

	@Override
	protected <T, ID extends Serializable> SimpleJpaRepository<T, ?> getTargetRepository(
			RepositoryInformation information, EntityManager entityManager) {
		Class<?> repositoryInterface = information.getRepositoryInterface();
		JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());

		SimpleJpaRepository<T, ID> repo = null;

		if (isQueryDslExecutor(repositoryInterface)) {
			repo = new QuerydslJpaRepository(entityInformation, entityManager);
		} else {
			repo = new JpaSupportRepository(entityInformation, entityManager);
		}
		return repo;
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslExecutor(metadata.getRepositoryInterface())) {
			return QuerydslJpaRepository.class;
		} else {
			return JpaSupportRepository.class;
		}
	}
}
