package servercode.TransactionManager;

import java.util.*;
import servercode.ResInterface.ResourceManager;

public class ActiveTransaction {

    // list of managers active in this transaction
    private ArrayList<ResourceManager> activeManagers;

    // how long this transaction has to live 
    private int timeToLive;

    public ActiveTransaction(int timeToLive) {
        setTimeToLive(timeToLive);
        setActiveManagers();
    }

    public void addActiveManager(ResourceManager manager) {

        // if the manager isn't already recorded as an active manager, add them to the list 
        if (!this.activeManagers.contains(manager)) {
            this.activeManagers.add(manager);
        }

    }

    private void setTimeToLive(int i) {
        this.timeToLive = i;
    }

    private void setActiveManagers() {
        this.activeManagers = new ArrayList<ResourceManager>();
    }
}