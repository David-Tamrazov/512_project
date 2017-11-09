package servercode.ResInterface;

public interface Transaction {

    public void start(int xid);
    public void commit(int xid);
    public void abort(int xid);
}
