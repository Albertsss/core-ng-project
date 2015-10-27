package core.framework.impl.template.fragment;

import core.framework.api.util.Exceptions;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;

/**
 * @author neo
 */
public class EmptyAttributeFragment implements Fragment {
    private final String name;
    private final ExpressionHolder expression;

    public EmptyAttributeFragment(String name, String expression, TemplateMetaContext context, String location) {
        this.name = name;
        this.expression = new ExpressionBuilder(expression, context, location).build();
        if (!Boolean.class.equals(GenericTypes.rawClass(this.expression.returnType)))
            throw Exceptions.error("expression must return Boolean, condition={}, returnType={}, location={}",
                expression, this.expression.returnType.getTypeName(), location);
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        Object result = expression.eval(context);
        if (Boolean.TRUE.equals(result)) {
            builder.append(' ').append(name);
        }
    }
}
