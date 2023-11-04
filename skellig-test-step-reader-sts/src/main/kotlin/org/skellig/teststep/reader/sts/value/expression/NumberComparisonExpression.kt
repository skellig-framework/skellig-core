package org.skellig.teststep.reader.sts.value.expression

class NumberComparisonExpression(private val operator: String,
                                 private val leftValueExpression: ValueExpression,
                                 private val rightValueExpression: ValueExpression) : ValueExpression {

    override fun evaluate(): Any {
//        BigDecimal left = (BigDecimal) leftExpression.evaluate();
//        BigDecimal right = (BigDecimal) rightExpression.evaluate();
//
//        switch (operator) {
//            case ">": left > right; break;
//            case ">=": left >= right; break;
//            case "<": left < right; break;
//            case "<=": left <= right; break;
//            case "==": left == right; break;
//            case "!=": left != right; break;
//            default: throw new IllegalArgumentException("Invalid operator: $operator");
//        }
//
//        return result;
        return false
    }

    override fun toString(): String {
        return "$leftValueExpression $operator $rightValueExpression"
    }
}