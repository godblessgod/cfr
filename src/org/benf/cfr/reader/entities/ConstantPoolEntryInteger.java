package org.benf.cfr.reader.entities;

import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

/**
 * Created by IntelliJ IDEA.
 * User: lee
 * Date: 15/04/2011
 * Time: 20:35
 * To change this template use File | Settings | File Templates.
 */
public class ConstantPoolEntryInteger implements ConstantPoolEntry {
    private final long OFFSET_OF_BYTES = 1;

    private final long value;

    public ConstantPoolEntryInteger(ByteData data)
    {
        this.value = data.getU4At(OFFSET_OF_BYTES);
    }

    @Override
    public long getRawByteLength()
    {
        return 5;
    }

    @Override
    public void dump(Dumper d, ConstantPool cp)
    {
        d.print("CONSTANT_Integer value=" + value);
    }

    public long getValue() {
        return value;
    }
}