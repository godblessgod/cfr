package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.opgraph.GraphConversionHelper;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op02WithProcessedDataAndRefs;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueCollector;
import org.benf.cfr.reader.util.output.Dumper;

/**
 * Created by IntelliJ IDEA.
 * User: lee
 * Date: 16/03/2012
 * Time: 17:40
 * To change this template use File | Settings | File Templates.
 */
public class ReturnValueStatement extends ReturnStatement {
    private Expression rvalue;

    public ReturnValueStatement(Expression rvalue) {
        this.rvalue = rvalue;
    }

    @Override
    public void dump(Dumper dumper) {
        dumper.print("return " + rvalue.toString() + ";\n");
    }

    @Override
    public void replaceSingleUsageLValues(LValueCollector lValueCollector) {
        this.rvalue = rvalue.replaceSingleUsageLValues(lValueCollector);
    }

}