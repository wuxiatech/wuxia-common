package cn.wuxia.common.spring.orm.core.jpa.factory;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class BasicRepositoryFactoryBean<T extends JpaRepository<Object, Serializable>> extends JpaRepositoryFactoryBean<T, Object, Serializable> {

    public BasicRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {

        return new BasicJpaRepositoryFactory(entityManager);
    }
}
