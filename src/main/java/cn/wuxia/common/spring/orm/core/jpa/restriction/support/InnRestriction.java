package cn.wuxia.common.spring.orm.core.jpa.restriction.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import cn.wuxia.common.spring.orm.core.RestrictionNames;
import cn.wuxia.common.spring.orm.core.jpa.restriction.PredicateMultipleValueSupport;
import cn.wuxia.common.spring.orm.core.jpa.restriction.PredicateSingleValueSupport;

/**
 * 包含约束 (from object o where o.value is not null)RestrictionName:INN
 * <p>
 * 表达式:IS NOT NULL属性类型_属性名称[属性名称...]
 * </p>
 * 
 * @author songlin.li
 */
public class InnRestriction extends PredicateSingleValueSupport {

    /*
     * (non-Javadoc)
     * @see
     * org.exitsoft.orm.core.spring.data.jpa.PredicateBuilder#getRestrictionName
     * ()
     */
    public String getRestrictionName() {
        return RestrictionNames.INN;
    }

    /*
     * (non-Javadoc)
     * @see org.exitsoft.orm.core.spring.data.jpa.restriction.
     * PredicateSingleValueSupport
     * #buildRestriction(javax.persistence.criteria.Path, java.lang.Object,
     * javax.persistence.criteria.CriteriaBuilder)
     */
    @SuppressWarnings("rawtypes")
    public Predicate build(Path<?> expression, Object value, CriteriaBuilder builder) {
        return expression.isNotNull();
    }

}
