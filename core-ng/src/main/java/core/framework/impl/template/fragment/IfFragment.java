package core.framework.impl.template.fragment;

import core.framework.api.util.Exceptions;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.template.CallStack;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class IfFragment extends CompositeFragment {
    private static final Pattern STATEMENT_PATTERN = Pattern.compile("if ((not )?)([#a-zA-Z1-9\\.\\(\\)]+)");
    final ExpressionHolder expression;
    final boolean reverse;

    public IfFragment(String statement, CallTypeStack stack, String location) {
        Matcher matcher = STATEMENT_PATTERN.matcher(statement);
        if (!matcher.matches())
            throw Exceptions.error("statement must match \"if (not) condition\", statement={}, location={}", statement, location);

        reverse = "not ".equals(matcher.group(2));
        String condition = matcher.group(3);

        expression = new ExpressionBuilder(condition, stack, location).build();
        if (!Boolean.class.equals(GenericTypes.rawClass(expression.returnType)))
            throw Exceptions.error("if condition must return Boolean, condition={}, returnType={}, location={}",
                condition, expression.returnType.getTypeName(), location);
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Object result = expression.eval(stack);
        Boolean expected = reverse ? Boolean.FALSE : Boolean.TRUE;
        if (expected.equals(result)) {
            for (Fragment handler : handlers) {
                handler.process(builder, stack);
            }
        }
    }
}
