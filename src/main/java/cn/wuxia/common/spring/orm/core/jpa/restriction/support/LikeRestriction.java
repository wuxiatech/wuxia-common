package cn.wuxia.common.spring.orm.core.jpa.restriction.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import cn.wuxia.common.spring.orm.core.RestrictionNames;
import cn.wuxia.common.spring.orm.core.jpa.restriction.PredicateSingleValueSupport;

/**
 * 模糊约束 ( from object o where o.value like '%?%') RestrictionName:LIKE
 * <p>
 * 表达式:LIKE属性类型_属性名称[_OR_属性名称...]
 * </p>
 * 
 * @author songlin.li
 */
public class LikeRestriction extends PredicateSingleValueSupport {

    /*
     * (non-Javadoc)
     * @see
     * org.exitsoft.orm.core.spring.data.jpa.PredicateBuilder#getRestrictionName
     * ()
     */
    public String getRestrictionName() {
        return RestrictionNames.LIKE;
    }

    /*
     * (non-Javadoc)
     * @see org.exitsoft.orm.core.spring.data.jpa.PredicateBuilder#build(javax.
     * persistence.criteria.Path, java.lang.Object,
     * javax.persistence.criteria.CriteriaBuilder)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Predicate build(Path expression, Object value, CriteriaBuilder builder) {

        return builder.like(expression, "%" + value + "%");
    }

}
