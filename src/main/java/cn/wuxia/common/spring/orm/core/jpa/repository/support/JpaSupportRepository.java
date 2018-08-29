package cn.wuxia.common.spring.orm.core.jpa.repository.support;

import cn.wuxia.common.hibernate.query.Conditions;
import cn.wuxia.common.hibernate.query.Pages;
import cn.wuxia.common.hibernate.query.PropertyType;
import cn.wuxia.common.spring.orm.annotation.StateDelete;
import cn.wuxia.common.spring.orm.core.PropertyFilter;
import cn.wuxia.common.spring.orm.core.RestrictionNames;
import cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository;
import cn.wuxia.common.spring.orm.core.jpa.specification.Specifications;
import cn.wuxia.common.spring.orm.enumeration.ExecuteMehtod;
import cn.wuxia.common.spring.orm.strategy.utils.ConvertCodeUtils;
import cn.wuxia.common.util.NumberUtil;
import cn.wuxia.common.util.reflection.ConvertUtil;
import cn.wuxia.common.util.reflection.ReflectionUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * {@link BasicJpaRepository}接口实现类，并在{@link SimpleJpaRepository}基础上扩展,包含对
 * {@link PropertyFilter}的支持。或其他查询的支持, 重写了
 * {@link SimpleJpaRepository#save(Object)}和
 * {@link SimpleJpaRepository#delete(Object)}
 * 方法，支持@StateDelete注解和@ConvertProperty注解
 *
 * @param <T>  ORM对象
 * @param <ID> 主键Id类型
 * @author songlin.li
 */
@SuppressWarnings("unchecked")
public class JpaSupportRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BasicJpaRepository<T, ID> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private EntityManager entityManager;

    private JpaEntityInformation<T, ?> entityInformation;

    protected Class<T> entityClass;

    public JpaSupportRepository(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
        // this.entityClass =
        // ReflectionUtil.getSuperClassGenricType(getClass());
        this.entityClass = (Class<T>) ReflectionUtil.getTargetClass(domainClass);
    }

    public JpaSupportRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        // this.entityClass =
        // ReflectionUtil.getSuperClassGenricType(getClass());
        this.entityClass = (Class<T>) ReflectionUtil.getTargetClass(entityInformation.getJavaType());
    }

    /**
     * return org.hibernate.Session;
     *
     * @return
     */
    public Session getSession() {
        return (Session) entityManager.getDelegate();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public JpaEntityInformation<T, ?> getJpaEntityInformation() {
        return entityInformation;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.data.jpa.repository.support.SimpleJpaRepository#save
     * (S)
     */
    @Override
    @Transactional
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "entity Can not be null");

        logger.debug("save entity {}", entityClass.getName());
        if (entityInformation.isNew(entity)) {
            ConvertCodeUtils.convertObject(entity, ExecuteMehtod.Save, ExecuteMehtod.Insert);
            entityManager.persist(entity);
            return entity;
        } else {
            ConvertCodeUtils.convertObject(entity, ExecuteMehtod.Save, ExecuteMehtod.Update);
            return entityManager.merge(entity);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.data.jpa.repository.support.SimpleJpaRepository#delete
     * (java.lang.Object)
     */
    @Override
    @Transactional
    public void delete(T entity) {
        Assert.notNull(entity, "entity Can not be null");
        Class<?> entityClass = ReflectionUtil.getTargetClass(entity);
        logger.debug("delete entity {}", entityClass.getName());
        StateDelete stateDelete = ReflectionUtil.getAnnotation(entityClass, StateDelete.class);
        if (stateDelete != null) {
            Object value = ConvertUtil.convertToObject(stateDelete.value(), stateDelete.type().getValue());
            ReflectionUtil.invokeSetterMethod(entity, stateDelete.propertyName(), value);
            save(entity);
        } else {
            super.delete(entity);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findBy
     * (java.util.List)
     */
    @Override
    public List<T> findBy(List<PropertyFilter> filters) {
        return findBy(filters, (Sort) null);
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findBy
     * (java.util.List, org.springframework.data.domain.Sort)
     */
    @Override
    public List<T> findBy(List<PropertyFilter> filters, Sort sort) {
        return findAll(Specifications.get(filters), sort);
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findPage
     * (org.springframework.data.domain.Pageable, java.util.List)
     */
    @Override
    public Page<T> findPage(Pageable pageable, List<PropertyFilter> filters) {
        if (CollectionUtils.isEmpty(filters))
            return findAll(pageable);
        Specification s = Specifications.get(filters);
        return findAll(Specifications.get(filters), pageable);
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findBy
     * (java.lang.String, java.lang.Object)
     */
    @Override
    public List<T> findBy(String propertyName, Object value) {
        return findBy(propertyName, value, (Sort) null);
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findBy
     * (java.lang.String, java.lang.Object,
     * org.springframework.data.domain.Sort)
     */
    @Override
    public List<T> findBy(String propertyName, Object value, Sort sort) {
        return findBy(propertyName, value, sort, RestrictionNames.EQ);
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findBy
     * (java.lang.String, java.lang.Object, java.lang.String)
     */
    @Override
    public List<T> findBy(String propertyName, Object value, String restrictionName) {
        return findBy(propertyName, value, (Sort) null, restrictionName);
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findBy
     * (java.lang.String, java.lang.Object,
     * org.springframework.data.domain.Sort, java.lang.String)
     */
    @Override
    public List<T> findBy(String propertyName, Object value, Sort sort, String restrictionName) {
        return findAll(Specifications.get(propertyName, value, restrictionName), sort);
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findOneBy
     * (java.util.List)
     */
    @Override
    public T findOneBy(List<PropertyFilter> filters) {
        return (T) findOne(Specifications.get(filters));
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findOneBy
     * (java.lang.String, java.lang.Object)
     */
    @Override
    public T findOneBy(String propertyName, Object value) {
        return findOneBy(propertyName, value, RestrictionNames.EQ);
    }

    /*
     * (non-Javadoc)
     * @see
     * cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository#findOne
     * (java.lang.String, java.lang.Object, java.lang.String)
     */
    @Override
    public T findOneBy(String propertyName, Object value, String restrictionName) {
        return (T) findOne(Specifications.get(propertyName, value, restrictionName));
    }

    @Override
    public Pages<T> findPage(Pages<T> pages) {

        List<Order> orders = Lists.newArrayList();
        if (null != pages.getSort()) {
            Iterator<cn.wuxia.common.hibernate.query.Sort.Order> it = pages.getSort().iterator();
            while (it.hasNext()) {
                cn.wuxia.common.hibernate.query.Sort.Order order = it.next();
                if (order.isAscending())
                    orders.add(new Order(Direction.ASC, order.getProperty()));
                else
                    orders.add(new Order(Direction.DESC, order.getProperty()));
            }
        }
        List<PropertyFilter> filters = Lists.newArrayList();
        for (Conditions condition : pages.getConditions()) {
            PropertyFilter filter = new PropertyFilter();

            switch (condition.getMatchType()) {
                case LL:
                    filter.setRestrictionName(RestrictionNames.LLIKE);
                    break;
                case RL:
                    filter.setRestrictionName(RestrictionNames.RLIKE);
                    break;
                case EQ:
                    filter.setRestrictionName(RestrictionNames.EQ);
                    break;
                case NE:
                    filter.setRestrictionName(RestrictionNames.NE);
                    break;
                case BW:
                    break;
                case FL:
                    filter.setRestrictionName(RestrictionNames.LIKE);
                    break;
                case ISN:
                    filter.setRestrictionName(RestrictionNames.ISN);
                    break;
                case INN:
                    filter.setRestrictionName(RestrictionNames.INN);
                    break;
            }
            filter.setPropertyType(PropertyType.S.getValue());
            filter.setPropertyNames(new String[]{condition.getProperty()});
            filter.setMatchValue((String) condition.getValue());
            filters.add(filter);
        }

        if (pages.getPageSize() == -1) {
            List<T> result = findBy(filters, new Sort(orders));
            pages.setResult(result);
            pages.setTotalCount(result.size());
        } else {
            PageRequest pageRequest = new PageRequest(pages.getPageNo() - 1, pages.getPageSize(), new Sort(orders));
            Page<T> page = findPage(pageRequest, filters);
            pages.setResult(page.getContent());
            pages.setPageNo(page.getNumber() + 1);
            pages.setPageSize(page.getSize());
            pages.setTotalCount(NumberUtil.toInteger(page.getTotalElements()));
        }
        return pages;
    }
}
