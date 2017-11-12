package servercode.TransactionManager;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;
import servercode.ResInterface.ResourceManager;
import servercode.ResInterface.Transaction;

public class ActiveTransaction implements Transaction {

    // list of managers active in this transaction
    private ArrayList<ResourceManager> activeManagers;

    // how long this transaction has to live 
    private int timeToLive;
    private int xid;
    private Date lastTransationTime;

    public ActiveTransaction(int xid, int timeToLive, ArrayList<ResourceManager> resourceManagers) {
        setXID(xid);
        setTimeToLive(timeToLive);
        setActiveManagers(resourceManagers);
    }

    public void addActiveManager(ResourceManager manager) {

        // if the manager isn't already recorded as an active manager, add them to the list 
        if (!this.activeManagers.contains(manager)) {
            this.activeManagers.add(manager);
        }

    }

    // nothing for the transaction to do at start
    public int start()  {

        return 0;

    }

    public void updateLastTransaction() {
        this.lastTransationTime =  new Date();
    }

    public Date getLastTransationTime() {
        return this.lastTransationTime;
    }

    public boolean commit(int xid) throws InvalidTransactionException, TransactionAbortedException, RemoteException {

        if (xid != this.xid) {
            throw new InvalidTransactionException(xid, "Invalid transaction exception passed to transaction commit.");
        }

        boolean success = false;

        // send the commit command to every resource manager involved in this transaction
        for (ResourceManager rm: this.activeManagers) {

            try {

                success = rm.commit(xid);

            } catch(InvalidTransactionException | TransactionAbortedException | RemoteException e) {

                throw e;

            }

        }

        return success;

    }

    public void abort(int xid) throws InvalidTransactionException, RemoteException {

    }

    private void setXID(int i) {
        this.xid = xid;
    }
    private void setTimeToLive(int i) {
        this.timeToLive = i;
    }

    public int getTimeToLive() {
        return this.timeToLive;
    }

    private void setActiveManagers(ArrayList<ResourceManager> resourceManagers) {
        this.activeManagers = resourceManagers;
    }
}