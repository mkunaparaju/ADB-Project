/**
 * 
 */
package Transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import Storage.AccessedSites;

/**
 * @author mkunaparaju
 *
 */
public class Transaction {


    public enum Status 
    {
        RUNNING, WAITING, ABORTED, COMMITED
    }
	private String id;
	private int timeStamp;
	private boolean isReadOnly;
	private HashMap<String, Integer> presentStateRO;
	private Set<AccessedSites> sitesAccessed;
	private HashMap<String, Integer> uncommittedindexes;
	private Status transactionStatus;
	
	Transaction(String id, int timeStamp, boolean isReadOnly)
	{
		this.id = id;
		this.timeStamp = timeStamp;
		this.isReadOnly = isReadOnly;
		
		 sitesAccessed = new HashSet<AccessedSites>();
		 uncommittedindexes = new HashMap<String, Integer>();
	}
	
	void setPresentStateRO(HashMap<String, Integer> map)
	{
		presentStateRO = map;
	}
	
	HashMap<String,Integer> getPresentStateRO()
	{
		return presentStateRO;
	}

	 public String getID() {
	        return id;
	    }

	    public int getTimeStamp() {
	        return timeStamp;
	    }

	    public Status getTransactionStatus() {
	        return transactionStatus;
	    }

	    public void setTransactionStatus(Status transactionStatus) {
	        this.transactionStatus = transactionStatus;
	    }

	    public Boolean getIsReadOnly() {
	        return isReadOnly;
	    }
	    
	    public Set<AccessedSites> getSitesAccessed() {
	        return sitesAccessed;
	    }

	    /**
	     * Used to keep track of the sites accessed by a transaction.
	     * 
	     * @param siteAccessed
	     *            - the site that was accessed by the Transaction
	     */
	    public void addToSitesAccessed(AccessedSites siteAccessed) {
	        this.sitesAccessed.add(siteAccessed);
	    }

	    /**
	     * Used to keep track of the indexes that are changed but uncommitted.
	     * 
	     * @param var - the variable that was changed by the transaction
	     * @param value  - the value that we are writing to the variable before the commit
	     * 
	     */
	    public void addToUncommitedindexes(String var, int value) {
	        this.uncommittedindexes.put(var, value);
	    }

	    public HashMap<String, Integer> getUncommitedindexes() {
	        return this.uncommittedindexes;
	    }

	    /**
	     * status chage on commit
	     * 
	     * @param timestamp - the timestamp of the transaction commit
	     * 
	    */
	    public void commit(int timestamp) {
	        this.transactionStatus = Status.COMMITED;
	    }

	    /**
	     * status changes on abort
	     * 
	     * @param timestamp - the timestamp of the transaction abort
	     */
	    public void abort(int timestamp) {
	        this.transactionStatus = Status.ABORTED;
	    }
}
