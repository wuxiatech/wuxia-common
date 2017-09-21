package cn.wuxia.common.spring.orm.mongo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.google.common.collect.Lists;
import com.mongodb.WriteResult;

import cn.wuxia.common.entity.ValidationEntity;
import cn.wuxia.common.hibernate.query.Conditions;
import cn.wuxia.common.hibernate.query.Pages;
import cn.wuxia.common.hibernate.query.Sort.Order;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.common.util.reflection.ReflectionUtil;

public abstract class SpringDataMongoDao<T extends ValidationEntity, K extends Serializable> {
    protected static Logger logger = LoggerFactory.getLogger(SpringDataMongoDao.class);

    protected MongoTemplate mongoTemplate;

    /**
     * 如非实体存储的时候需要制定collection name
     *
     * @return
     */
    protected String collectionName;

    public SpringDataMongoDao() {
    }

    public SpringDataMongoDao(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param query
     * @return
     */
    public List<T> find(Query query) {
        if (StringUtil.isBlank(collectionName)) {
            return getMongoTemplate().find(query, this.getEntityClass());
        } else
            return getMongoTemplate().find(query, this.getEntityClass(), collectionName);
    }

    /**
     * 查找唯一
     *
     * @param query
     * @return
     */
    public T findOne(Query query) {
        if (StringUtil.isBlank(collectionName)) {
            return getMongoTemplate().findOne(query, this.getEntityClass());
        } else
            return getMongoTemplate().findOne(query, this.getEntityClass(), collectionName);
    }

    /**
     * 更新
     *
     * @param query
     * @param update
     */
    public void update(Query query, Update update) {
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().findAndModify(query, update, this.getEntityClass());
        } else
            getMongoTemplate().findAndModify(query, update, this.getEntityClass(), collectionName);
    }

    /**
     * 保存
     *
     * @param entity
     * @return
     */
    public void save(T entity) {
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().insert(entity);
        } else {
            getMongoTemplate().insert(entity, collectionName);
        }
    }

    public void batchSave(Collection<T> entitys) {
        if (ListUtil.isEmpty(entitys))
            return;
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().insert(entitys, this.getEntityClass());
        } else {
            getMongoTemplate().insert(entitys, collectionName);
        }
    }

    public void save(Map<String, ?> m) {
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().insert(m);
        } else {
            getMongoTemplate().insert(m, collectionName);
        }
    }

    /**
     * 删除对象
     *
     * @param entity
     * @author songlin
     */
    public void delete(T entity) {
        if (StringUtil.isBlank(collectionName)) {
            WriteResult res = getMongoTemplate().remove(entity);
            logger.info("", res);
        } else {
            WriteResult res = getMongoTemplate().remove(entity, collectionName);
            logger.info("", res);
        }
    }

    public void deleteById(final K id) {
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().findAndRemove(new Query().addCriteria(Criteria.where("id").is(id)), this.getEntityClass());
        } else {
            getMongoTemplate().findAndRemove(new Query().addCriteria(Criteria.where("id").is(id)), this.getEntityClass(), collectionName);
        }
    }

    /**
     * 根据id查找
     *
     * @param id
     * @return
     */
    public T findById(final K id) {
        if (StringUtil.isBlank(collectionName)) {
            return getMongoTemplate().findById(id, this.getEntityClass());
        } else
            return getMongoTemplate().findById(id, this.getEntityClass(), collectionName);
    }

    /**
     * 根据某个熟悉查找
     *
     * @param properties
     * @param value
     * @return
     */
    public List<T> findBy(final String properties, final Object value) {
        Query query = new Query(Criteria.where(properties).is(value));
        return this.find(query);
    }

    /**
     * 分页查找
     *
     * @param query
     * @param page
     * @return
     */
    public Pages<T> findPage(Pages<T> page) {
        Query query = new Query();
        for (Conditions cond : page.getConditions()) {
            query.addCriteria(Criteria.where(cond.getProperty()).is(cond.getValue()));
        }
        long count = this.count(query);
        page.setTotalCount(count);
        int pageNumber = page.getPageNo();
        int pageSize = page.getPageSize();
        query.skip((pageNumber - 1) * pageSize).limit(pageSize);
        if (page.getSort() != null) {
            Iterator<Order> iterator = page.getSort().iterator();
            List<Sort.Order> orders = Lists.newLinkedList();
            while (iterator.hasNext()) {
                Order order = iterator.next();
                if (order.isAscending()) {
                    orders.add(new Sort.Order(Direction.ASC, order.getProperty()));
                } else {
                    orders.add(new Sort.Order(Direction.DESC, order.getProperty()));
                }
            }
            Sort sort = new Sort(orders);
            query.with(sort);
        }
        List<T> rows = this.find(query);
        page.setResult(rows);
        return page;
    }

    /**
     * 统计总数
     *
     * @param query
     * @return
     */
    protected long count(Query query) {
        if (StringUtil.isBlank(collectionName)) {
            return getMongoTemplate().count(query, this.getEntityClass());
        } else
            return getMongoTemplate().count(query, this.getEntityClass(), collectionName);
    }

    /**
     * 获取需要操作的实体类class
     *
     * @return
     */
    protected Class<T> getEntityClass() {
        return ReflectionUtil.getSuperClassGenricType(getClass());
    }

    /**
     * 可以重写注入
     *
     * @param mongoTemplate
     */
    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

}
