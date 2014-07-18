package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BoolOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BooleanOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NotOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.*;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.*;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.output.Dumper;

public class IfStatement extends GotoStatement {

    private static final int JUMP_NOT_TAKEN = 0;
    private static final int JUMP_TAKEN = 1;

    private ConditionalExpression condition;
    private BlockIdentifier knownIfBlock = null;
    private BlockIdentifier knownElseBlock = null;


    public IfStatement(ConditionalExpression conditionalExpression) {
        this.condition = conditionalExpression;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("if (").dump(condition).print(") ");
        return super.dump(dumper);
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        Expression replacementCondition = condition.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, getContainer());
        if (replacementCondition != condition) {
            this.condition = (ConditionalExpression) replacementCondition;
        }
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        condition = expressionRewriter.rewriteExpression(condition, ssaIdentifiers, getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        condition.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean condenseWithNextConditional() {
        // Get the next (fall through) statement.  If that's not a conditional, ignore.
        // Since the next statement is ALWAYS fall through, we don't need to test that.
        Statement nextStatement = getTargetStatement(JUMP_NOT_TAKEN);
        return nextStatement.condenseWithPriorIfStatement(this);
    }

    public ConditionalExpression getCondition() {
        return condition;
    }

    public void setCondition(ConditionalExpression condition) {
        this.condition = condition;
    }

    public void simplifyCondition() {
        condition = ConditionalUtils.simplify(condition);
    }

    public void negateCondition() {
        condition = ConditionalUtils.simplify(condition.getNegated());
    }

    @Override
    public boolean condenseWithPriorIfStatement(IfStatement prior) {
        Statement fallThrough2 = getTargetStatement(JUMP_NOT_TAKEN);
        Statement target1 = prior.getTargetStatement(JUMP_TAKEN);

        // if (c1) goto a
        // if (c2) goto b
        // a
        // ->
        // if (!c1 && c2) goto b
        if (fallThrough2 == target1) {
            this.condition = new BooleanOperation(new NotOperation(prior.getCondition()), getCondition(), BoolOp.AND).simplify();
            prior.getContainer().nopOutConditional();
            return true;
        }
        // if (c1) goto a
        // if (c2) goto a
        // b
        // ->
        // if (c1 || c2) goto a
        Statement target2 = getTargetStatement(JUMP_TAKEN);
        if (target1 == target2) {
            this.condition = new BooleanOperation(prior.getCondition(), getCondition(), BoolOp.OR).simplify();
            prior.getContainer().nopOutConditional();
            return true;
        }
        return false;
    }


    public void replaceWithWhileLoopStart(BlockIdentifier blockIdentifier) {
        WhileStatement replacement = new WhileStatement(ConditionalUtils.simplify(condition.getNegated()), blockIdentifier);
        getContainer().replaceStatement(replacement);
    }

    public void replaceWithWhileLoopEnd(BlockIdentifier blockIdentifier) {
        WhileStatement replacement = new WhileStatement(ConditionalUtils.simplify(condition), blockIdentifier);
        getContainer().replaceStatement(replacement);
    }

    @Override
    public Statement getJumpTarget() {
        return getTargetStatement(JUMP_TAKEN);
    }

    @Override
    public boolean isConditional() {
        return true;
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return condition.canThrow(caught);
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        switch (getJumpType()) {
            case GOTO:
            case GOTO_OUT_OF_IF:
            case GOTO_OUT_OF_TRY:
                return new UnstructuredIf(condition, knownIfBlock, knownElseBlock);
            case CONTINUE:
                return new StructuredIf(condition, new Op04StructuredStatement(new UnstructuredContinue(getTargetStartBlock())));
            case BREAK:
                return new StructuredIf(condition, new Op04StructuredStatement(new UnstructuredBreak(getJumpTarget().getContainer().getBlocksEnded())));
            case BREAK_ANONYMOUS: {
                Statement target = getJumpTarget();
                if (!(target instanceof AnonBreakTarget)) {
                    throw new IllegalStateException("Target of anonymous break unexpected.");
                }
                AnonBreakTarget anonBreakTarget = (AnonBreakTarget) target;
                BlockIdentifier breakFrom = anonBreakTarget.getBlockIdentifier();
                Op04StructuredStatement unstructuredBreak = new Op04StructuredStatement(new UnstructuredAnonymousBreak(breakFrom));
                return new StructuredIf(condition, unstructuredBreak);
            }
        }
        throw new UnsupportedOperationException("Unexpected jump type in if block - " + getJumpType());
    }

    public void setKnownBlocks(BlockIdentifier ifBlock, BlockIdentifier elseBlock) {
        this.knownIfBlock = ifBlock;
        this.knownElseBlock = elseBlock;
    }

    public Pair<BlockIdentifier, BlockIdentifier> getBlocks() {
        return Pair.make(knownIfBlock, knownElseBlock);
    }

    public void optimiseForTypes() {
        condition = condition.optimiseForType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IfStatement that = (IfStatement) o;

        if (condition != null ? !condition.equals(that.condition) : that.condition != null) return false;

        return true;
    }


}
