package gov.nasa.jpf.symbc.numeric;

/*
 * This class stores the Symbolic Expression and the Shadow (Symbolic) Expression of a variable
 */
public class DiffExpression {

    /* Symbolic Expressions */
    Expression oldSymbolicExpr; // also called shadow expression
    Expression newSymbolicExpr; // also called symbc expression

    /*
     * Concrete Expressions (might be null in full symbolic mode, but might have values in symcrete mode).
     */
    Expression oldConcreteExpr;
    Expression newConcreteExpr;

    public DiffExpression(Expression old_sym_expr, Expression new_sym_expr, Expression old_con_expr,
            Expression new_con_expr) {
        this.oldSymbolicExpr = old_sym_expr;
        this.newSymbolicExpr = new_sym_expr;
        this.oldConcreteExpr = old_con_expr;
        this.newConcreteExpr = new_con_expr;
    }

    public Expression getOldSymbolicExpr() {
        return this.oldSymbolicExpr;
    }

    public Expression getNewSymbolicExpr() {
        return this.newSymbolicExpr;
    }

    public Expression getOldConcreteExpr() {
        return this.oldConcreteExpr;
    }

    public Expression getNewConcreteExpr() {
        return this.newConcreteExpr;
    }

    public String toString() {
        return "oldSym=" + oldSymbolicExpr + "\noldCon=" + oldConcreteExpr + "\nnewSym=" + newSymbolicExpr + "\nnewCon="
                + newConcreteExpr;
    }

}
