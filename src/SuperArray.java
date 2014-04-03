import sun.misc.Unsafe;

import java.lang.reflect.Field;

class SuperArray {
    private final static int BYTE = 1;
    private final static int INT = 4;
    private final long size;
    private final long address;
    private long tail = 0;
    private final Unsafe unsafe;
    public SuperArray(long sizeOf) throws Exception {
        Field f =  Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        this.unsafe = (Unsafe) f.get(null);
        this.size = sizeOf;
        this.address = unsafe.allocateMemory(size * INT);
    }
    public final void set(long i, byte value) {
        unsafe.putByte(address + i * BYTE, value);
    }
    public final void putInt(long i, int value){
        unsafe.putInt(address + i * INT,value);
        tail = ++i;
    }
    public final void putInt(int value){
        unsafe.putInt(address + tail++ * INT,value);
    }
    public final int get(long idx) {
        return unsafe.getByte(address + idx * BYTE);
    }
    public final int getInt(long idx){ return unsafe.getInt(address + idx * INT);}
    public final int getInt(){ return unsafe.getInt(address + --tail * INT);}
    public final void finalize(){discard();}
    public final void discard(){unsafe.freeMemory(address);}
    public final long size() {
        return size;
    }
}