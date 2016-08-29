package falx;

import clojure.lang.*;

public final class Size implements IHashEq, ILookup, Counted, Indexed, Seqable, IReduceInit {
    public final int w;
    public final int h;

    private int _hash = -1;

    private final static Keyword wk = Keyword.intern("w");
    private final static Keyword hk = Keyword.intern("h");

    public Size(int w, int h) {
        this.w = w;
        this.h = h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Size size = (Size) o;

        return w == size.w && h == size.h;
    }

    @Override
    public int hashCode() {
        if(_hash == -1){
            int hash = 1;
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
        return String.format("[%s %s]", w, h);
    }

    @Override
    public Object valAt(Object o) {

        if(o == wk){
            return w;
        }
        if(o == hk){
            return h;
        }

        if(o instanceof Number){
            int i = ((Number)o).intValue();
            switch(i) {
                case 0:
                    return w;
                case 1:
                    return h;
            }
        }

        return null;
    }

    @Override
    public Object valAt(Object o, Object o1) {
        if(o == wk){
            return w;
        }
        if(o == hk){
            return h;
        }

        if(o instanceof Number){
            int i = ((Number)o).intValue();
            switch(i) {
                case 0:
                    return w;
                case 1:
                    return h;
            }
        }

        return o1;
    }

    @Override
    public int count() {
        return 2;
    }

    @Override
    public ISeq seq() {
        return new Cons(w, new Cons(h, null));
    }

    @Override
    public Object nth(int i) {
        switch(i) {
            case 0:
                return w;
            case 1:
                return h;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Object nth(int i, Object notFound) {
        switch(i) {
            case 0:
                return w;
            case 1:
                return h;
        }
        return notFound;
    }

    @Override
    public Object reduce(IFn f, Object start) {
        Object ret = f.invoke(start, w);
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
