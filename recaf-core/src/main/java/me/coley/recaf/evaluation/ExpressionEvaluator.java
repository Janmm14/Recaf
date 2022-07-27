package me.coley.recaf.evaluation;

import com.github.javaparser.ast.expr.*;

import java.util.Optional;

public class ExpressionEvaluator {
	private ExpressionEvaluator() {}

	public static Optional<Number> evaluate(BinaryExpr binOp) {
		Optional<Number> leftValue = evaluate(binOp.getLeft());
		Optional<Number> rightValue = evaluate(binOp.getRight());
		if (leftValue.isPresent() && rightValue.isPresent()) {
			return NumberEvaluator.evaluate(leftValue.get(), binOp.getOperator(), rightValue.get());
		}
		return Optional.empty();
	}

	public static Optional<Number> evaluate(UnaryExpr unaryExpr) {
		return evaluate(unaryExpr.getExpression())
			.flatMap(n -> NumberEvaluator.evaluate(unaryExpr.getOperator(), n));
	}

	public static Optional<Number> evaluate(LiteralExpr literalExpr) {
		return literalExpr.toIntegerLiteralExpr().map(IntegerLiteralExpr::asNumber)
			// No byte, short, float
			.or(() -> literalExpr.toDoubleLiteralExpr().map(DoubleLiteralExpr::asDouble))
			.or(() -> literalExpr.toLongLiteralExpr().map(LongLiteralExpr::asNumber));
	}

	public static Optional<Number> evaluate(CastExpr castExpr) {
		return evaluate(castExpr.getExpression()).flatMap(n -> castExpr.getType().toPrimitiveType().flatMap(t -> NumberEvaluator.cast(n, t)));
	}

	public static Optional<Number> evaluate(Expression expr) {
		//un-enclosing parenthesis
		if (expr instanceof EnclosedExpr) expr = ((EnclosedExpr) expr).getInner();
		final Expression unwrappedExpr = expr;
		// only evaluating
		return expr.toLiteralExpr().flatMap(ExpressionEvaluator::evaluate) // the literals themselves
			.or(() -> unwrappedExpr.toBinaryExpr().flatMap(ExpressionEvaluator::evaluate)) // binary operations
			.or(() -> unwrappedExpr.toUnaryExpr().flatMap(ExpressionEvaluator::evaluate)) // unary operations
			.or(() -> unwrappedExpr.toCastExpr().flatMap(ExpressionEvaluator::evaluate)); // cast operations
		// are supported
	}
}
