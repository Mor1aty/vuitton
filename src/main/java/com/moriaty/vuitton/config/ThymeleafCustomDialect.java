package com.moriaty.vuitton.config;

import com.moriaty.vuitton.util.ThymeleafUtil;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Collections;
import java.util.Set;

/**
 * <p>
 * 自定义 Thymeleaf 方法
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/28 上午4:25
 */
public class ThymeleafCustomDialect extends AbstractDialect implements IExpressionObjectDialect {

    public static final IExpressionObjectFactory FACTORY = new IExpressionObjectFactory() {
        @Override
        public Set<String> getAllExpressionObjectNames() {
            return Collections.singleton("customUtil");
        }

        @Override
        public Object buildObject(IExpressionContext context, String expressionObjectName) {
            return new ThymeleafUtil();
        }

        @Override
        public boolean isCacheable(String expressionObjectName) {
            return true;
        }
    };

    public ThymeleafCustomDialect(String name) {
        super(name);
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return FACTORY;
    }
}
