/*
* Created on :2015年10月30日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.spring.el;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.StringUtils;

public class OperationContext {
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    private final OperationBean operation;

    private final Method method;

    private final Object[] args;

    private final Object target;

    private final Class<?> targetClass;

    public OperationContext(OperationBean operation, Method method, Object[] args, Object target, Class<?> targetClass) {
        this.operation = operation;
        this.method = method;
        this.args = args;
        this.target = target;
        this.targetClass = targetClass;
    }

    protected boolean isConditionPassing() {
        return isConditionPassing(ExpressionEvaluator.NO_RESULT);
    }

    protected boolean isConditionPassing(Object result) {
        if (StringUtils.hasText(this.operation.getCondition())) {
            EvaluationContext evaluationContext = createEvaluationContext(result);
            return evaluator.condition(this.operation.getCondition(), this.method, evaluationContext);
        }
        return true;
    }

    protected boolean canRecodeOperationLog(Object value) {
        String unless = "";
        if (this.operation instanceof OperationBean) {
            unless = ((OperationBean) this.operation).getUnless();
        }
        if (StringUtils.hasText(unless)) {
            EvaluationContext evaluationContext = createEvaluationContext(value);
            return !evaluator.unless(unless, this.method, evaluationContext);
        }
        return true;
    }

    /**
     * Computes the key for the given caching operation.
     * @return generated key (null if none can be generated)
     */
    public Object getValue() {
        if (StringUtils.hasText(this.operation.getValue())) {
            EvaluationContext evaluationContext = createEvaluationContext(ExpressionEvaluator.NO_RESULT);
            return evaluator.key(this.operation.getValue(), this.method, evaluationContext);
        }
        return new SimpleKeyGenerator().generate(this.target, this.method, this.args);
    }

    private EvaluationContext createEvaluationContext(Object result) {
        return evaluator.createEvaluationContext(this.method, this.args, this.target, this.targetClass, result);
    }

}
