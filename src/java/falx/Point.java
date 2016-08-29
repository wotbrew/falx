package falx;

import clojure.lang.*;

public final class Point implements IHashEq, ILookup, Counted, Indexed, Seqable, IReduceInit {
    public final int x;
    public final int y;

    private int _hash = -1;

    private final static Keyword xk = Keyword.intern("x");
    private final static Keyword yk = Keyword.intern("y");

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        if(_hash == -1){
            int hash = 1;
            hash = 31 * hash + Murmur3.hashInt(x);
            hash = 31 * hash + Murmur3.hashInt(y);
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
        return String.format("[%s %s]", x, y);
    }

    @Override
    public Object valAt(Object o) {

        if(o == xk){
            return x;
        }
        if(o == yk){
            return y;
        }

        if(o instanceof Number){
            int i = ((Number)o).intValue();
            switch(i) {
                case 0:
                    return x;
                case 1:
                    return y;
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

        if(o instanceof Number){
            int i = ((Number)o).intValue();
            switch(i) {
                case 0:
                    return x;
                case 1:
                    return y;
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
        return new Cons(x, new Cons(y, null));
    }

    @Override
    public Object nth(int i) {
        switch(i) {
            case 0:
                return x;
            case 1:
                return y;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Object nth(int i, Object notFound) {
        switch(i) {
            case 0:
                return x;
            case 1:
                return y;
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
        return ret;
    }
}
