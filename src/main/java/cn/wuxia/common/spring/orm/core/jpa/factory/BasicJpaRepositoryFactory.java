package cn.wuxia.common.spring.orm.core.jpa.factory;

import static org.springframework.data.querydsl.QueryDslUtils.QUERY_DSL_PRESENT;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
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

		return QUERY_DSL_PRESENT && QueryDslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}

	@Override
	protected <T, ID extends Serializable> SimpleJpaRepository<?, ?> getTargetRepository(
			RepositoryInformation information, EntityManager entityManager) {
		Class<?> repositoryInterface = information.getRepositoryInterface();
		JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());

		SimpleJpaRepository<?, ?> repo = null;

		if (isQueryDslExecutor(repositoryInterface)) {
			repo = new QueryDslJpaRepository(entityInformation, entityManager);
		} else {
			repo = new JpaSupportRepository(entityInformation, entityManager);
		}
		return repo;
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslExecutor(metadata.getRepositoryInterface())) {
			return QueryDslJpaRepository.class;
		} else {
			return JpaSupportRepository.class;
		}
	}
}
