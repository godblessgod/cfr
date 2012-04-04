package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueCollector;

/**
 * Created by IntelliJ IDEA.
 * User: lee
 * Date: 16/03/2012
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class Literal implements Expression {
    private final TypedLiteral value;

    public Literal(TypedLiteral value) {
        this.value = value;
    }

    
    @Override 
    public String toString() {
        return "" + value;
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueCollector lValueCollector) {
        return this;
    }

}