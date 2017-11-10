package servercode.TransactionManager;

import java.util.*;

import servercode.LockManager.DeadlockException;
import servercode.ResInterface.MiddlewareServer;
import servercode.LockManager.LockManager;
import servercode.ResInterface.ResourceManager;
import servercode.ResInterface.Transaction;

public class TransactionManager implements Transaction {

    private Map<Integer, ActiveTransaction> activeTransactions; 
    private LockManager lm;
    private MiddlewareServer parent;
    private int xid;

    public TransactionManager(MiddlewareServer parent, Map<Integer, ActiveTransaction> activeTransactions, LockManager lm) {
        setParent(parent);
        setTransactionMap(activeTransactions);
        setLockManager(lm);
        setXID();
    }


    public boolean startTransaction(int xid) throws IllegalArgumentException {


        if (this.activeTransactions.containsKey(new Integer(xid))) {
            throw new IllegalArgumentException("Starting a transaction with an id that already exists.");
        }

        // add the transaction to the list of active transactions
        this.activeTransactions.put(new Integer(xid), new ActiveTransaction(10000));
        return true;

    }

    public Map<Integer, ActiveTransaction> getActiveTransactions() {
        Map<Integer, ActiveTransaction> shallowCopy = new HashMap<Integer, ActiveTransaction>();
        shallowCopy.putAll(this.activeTransactions);
        return shallowCopy;
    }

    public boolean transactionOperation(int xid, int locktype, String strData, ResourceManager rm) throws IllegalArgumentException {

        if (!this.activeTransactions.containsKey(xid)) {
            throw new IllegalArgumentException("Performing operation for nonexisting transaction");
        }

        try {

            boolean acquired = lm.Lock(xid, strData, locktype);
            ActiveTransaction txn = this.activeTransactions.get(xid);
            txn.addActiveManager(rm);
            return true;

        } catch (DeadlockException e) {
            System.out.println(String.format("Deadlock exception for xid %d getting locktype %d for datatype %s", xid, locktype, strData));
            abort(xid);
            return false;
        }

    }

    public void start(int xid)  {

        if (!this.activeTransactions.containsKey(xid)) {

        }

    }

    public void commit(int xid) {

    }

    public void abort(int xid) {

    }



    private void addTransaction(int xid) {
        ActiveTransaction txn = new ActiveTransaction(10000);
    }

    private void setParent(MiddlewareServer parent) {
        this.parent = parent;
    }

    private void setTransactionMap(Map<Integer, ActiveTransaction> activeTransactions) {
        this.activeTransactions = new HashMap<Integer, ActiveTransaction>();
    }

    private void setLockManager(LockManager lm) {
        this.lm = lm;
    }

    private void setXID() {
        this.xid = 0;
    }

    
}
