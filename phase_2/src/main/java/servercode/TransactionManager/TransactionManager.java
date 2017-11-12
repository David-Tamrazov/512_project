package servercode.TransactionManager;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;

import org.omg.CORBA.DynAnyPackage.Invalid;
import org.omg.PortableInterceptor.ACTIVE;
import servercode.LockManager.DeadlockException;
import servercode.ResInterface.MiddlewareServer;
import servercode.LockManager.LockManager;
import servercode.ResInterface.ResourceManager;
import servercode.ResInterface.Transaction;

public class TransactionManager implements Transaction {

    private Map<Integer, ActiveTransaction> activeTransactions;
    private MiddlewareServer parent;
    private int xid;

    public TransactionManager(MiddlewareServer parent, Map<Integer, ActiveTransaction> activeTransactions, LockManager lm) {
        setParent(parent);
        setTransactionMap(activeTransactions);
        setXID();

        (new Thread() {
            public void run() {
                while(true) {
                    for(Map.Entry<Integer, ActiveTransaction> activeTransaction : getActiveTransactions().entrySet()) {
                        if(activeTransaction.getValue().getLastTransationTime().getTime() - new Date().getTime() > activeTransaction.getValue().getTimeToLive()) {
                            System.out.println("Abort this damn transation");
//                            abort/commit? activeTransaction
                        }
                    }
                }
            }
        }).start();
    }

    public boolean transactionOperation(int xid, int locktype, String strData, ResourceManager rm) throws InvalidTransactionException, RemoteException {

        if (!addActiveManager(xid, rm)) {
            throw new InvalidTransactionException(xid, "Invalid transaction id passed for txn operation");
        }

        return true;

    }

    public int start()  {

        // increment the transaction counter
        xid += 1;

        // add the transaction to the active transactions list
        addActiveTransaction(xid);

        // return the transaction id
        return xid;

    }

    public boolean commit(int xid) throws InvalidTransactionException, TransactionAbortedException, RemoteException {

        // if the transaction doesn't exist in the list of active transactions, throw an exception
        if (!this.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid, "Invalid transaction passed for commit.");
        }

        // remove the transaction from the list of transactions a
        ActiveTransaction t = this.activeTransactions.get(xid);

        try {

            // remove the transaction from the list of active transactions
            this.activeTransactions.remove(xid);

            // return the commit result from t
            return t.commit(xid);

        } catch(InvalidTransactionException | TransactionAbortedException | RemoteException e) {

            throw e;

        }


    }

    public void abort(int xid) throws InvalidTransactionException, RemoteException {

    }


    public Map<Integer, ActiveTransaction> getActiveTransactions() {
        Map<Integer, ActiveTransaction> shallowCopy = new HashMap<Integer, ActiveTransaction>();
        shallowCopy.putAll(this.activeTransactions);
        return shallowCopy;
    }

    private boolean addActiveManager(int xid, ResourceManager rm) {

        if (!this.activeTransactions.containsKey(xid)) {
            return false;
        }

        ActiveTransaction txn = this.activeTransactions.get(xid);

        txn.updateLastTransaction();

        txn.addActiveManager(rm);

        return true;
    }

    private void addActiveTransaction(int xid) {
        ActiveTransaction txn = new ActiveTransaction(xid, 10000, new ArrayList<ResourceManager>());
        this.activeTransactions.put(xid, txn);
    }

    private void setParent(MiddlewareServer parent) {
        this.parent = parent;
    }

    private void setTransactionMap(Map<Integer, ActiveTransaction> activeTransactions) {
        this.activeTransactions = activeTransactions;
    }


    private void setXID() {
        this.xid = 0;
    }

    
}
