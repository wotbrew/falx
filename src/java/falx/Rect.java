package falx;

import clojure.lang.*;

public final class Rect implements IHashEq, ILookup, Counted, Indexed, Seqable, IReduceInit {
    public final int x;
    public final int y;
    public final int w;
    public final int h;

    private int _hash = -1;

    private final static Keyword xk = Keyword.intern("x");
    private final static Keyword yk = Keyword.intern("y");
    private final static Keyword wk = Keyword.intern("w");
    private final static Keyword hk = Keyword.intern("h");


    public Rect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rect rect = (Rect) o;

        return x == rect.x && y == rect.y && w == rect.w && h == rect.h;
    }

    @Override
    public int hashCode() {
        if(_hash == -1){
            int hash = 1;
            hash = 31 * hash + Murmur3.hashInt(x);
            hash = 31 * hash + Murmur3.hashInt(y);
            hash = 31 * hash + Murmur3.hashInt(w);
            hash = 31 * hash + Murmur3.hashInt(h);

            _hash = hash;
        }

        return _hash;
    }


    @Override
    public int hasheq() {
        return hashCode();
    }

    @Override
    public String toString() {
        return String.format("[%s %s %s %s]", x, y, w, h);
    }

    @Override
    public Object valAt(Object o) {

        if(o == xk){
            return x;
        }
        if(o == yk){
            return y;
        }
        if(o == wk){
            return w;
        }
        if(o == hk){
            return h;
        }

        if(o instanceof Number){
            int l = ((Number)o).intValue();
            switch(l) {
                case 0: return x;
                case 1: return y;
                case 2: return w;
                case 3: return h;
            }
        }

        return null;
    }

    @Override
    public Object valAt(Object o, Object o1) {
        if(o == xk){
            return x;
        }
        if(o == yk){
            return y;
        }
        if(o == wk){
            return w;
        }
        if(o == hk){
            return h;
        }

        if(o instanceof Number){
            int i = ((Number)o).intValue();
            switch(i) {
                case 0: return x;
                case 1: return y;
                case 2: return w;
                case 3: return h;
            }
        }

        return o1;
    }

    @Override
    public int count() {
        return 4;
    }

    @Override
    public ISeq seq() {
        return new Cons(x, new Cons(y, new Cons(w, new Cons(h, null))));
    }

    @Override
    public Object nth(int i) {
        switch(i) {
            case 0: return x;
            case 1: return y;
            case 2: return w;
            case 3: return h;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Object nth(int i, Object notFound) {
        switch(i) {
            case 0: return x;
            case 1: return y;
            case 2: return w;
            case 3: return h;
        }
        return notFound;
    }

    @Override
    public Object reduce(IFn f, Object start) {
        Object ret = f.invoke(start, x);
        if(ret instanceof Reduced){
            return ((Reduced)ret).deref();
        }
        ret = f.invoke(ret, y);
        if(ret instanceof Reduced){
            return ((Reduced)ret).deref();
        }
        ret = f.invoke(ret, w);
        if(ret instanceof Reduced){
            return ((Reduced)ret).deref();
        }
        ret = f.invoke(ret, h);
        if(ret instanceof Reduced){
            return ((Reduced)ret).deref();
        }
        return ret;
    }
}
