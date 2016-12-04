package Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import Storage.Lock;
import Storage.Lock.LockType;
import Transaction.TransactionManager;
import Transaction.Transaction;
import Transaction.WaitOperation;

public class Site {
	 public enum ServerStatus 
	 {
	        UP, DOWN, RECOVERING;
	 }
	 private int siteID;
	 private ServerStatus status;
	 private HashMap<Integer, Index> indexes;
	 private HashMap<String, ArrayList<Lock>> lockTable;
	 private HashSet<String> IndexRecovered;
	 private int previousFailtime;
	 
	 public Site(int siteID)
	 {
		 this.siteID = siteID;
	        indexes = new HashMap<Integer, Index>();
	        lockTable = new HashMap<String, ArrayList<Lock>>();
	        IndexRecovered = new HashSet<String>();
	        if (siteID % 2 == 0) {
	            IndexRecovered.add("x" + (siteID - 1));
	            IndexRecovered.add("x" + (siteID - 1 + 10));
	        }
	        status = ServerStatus.UP;
	 }
	 
	 public ServerStatus getStatus() 
	 {
		return status;
	 }
	 public void setStatus(ServerStatus status) 
	 {
	     this.status = status;
	 }
	 
	 /**
	  * adds the given index to site
	  * 
	 * @param index - add index to site
	 * 
	 */
	 
	public void addIndexToSite(Index index) 
	 {
	     indexes.put(index.getId(), index);
	 }
	
	 /**
	  * creates a new index and adds it to site
	  * 
	 * @param id - index id
	 * 
	 * @param value - index value
	 */
	
	 public void addIndexToSite(int id, int value) 
	 {
	    indexes.put(id, new Index(id, value));
	 }
	 
	 /**
	     * returns true if the index with the given id is present on the site
	     * 
	     * @param id - id of the index
	     * @return true or false
	     * 
	     */
	 
	 public boolean doesIndexExistOnSite(String id) 
	 {
	    return indexes.containsKey(getWithoutStartingX(id));
	 }
	 
	 public HashMap<String, ArrayList<Lock>> getLockTable() 
	 {
		 return this.lockTable;
	 }

	 public int getPreviousFailtime() 
	 {
	     return this.previousFailtime;
	 }

	 public int getId() 
	 {
	    return siteID;
	 }

	 public int read(int id) {
	        return indexes.get(id).getValue();
	 }
	 
	 public int read(String id) 
	 {
	     return read(getWithoutStartingX(id));
	 }
	 
	 /**
	     * writes the value of the index to the server
	     * 
	     * @param id
	     *            - index id
	     * @param value
	     *            - value to be committed
	     *            
	     */
	 
	    public void write(String id, int value) {
	        if (status.equals(ServerStatus.RECOVERING)) {
	        	IndexRecovered.add(id);
	            if (IndexRecovered.size() == indexes.size()) {
	                setStatus(ServerStatus.UP);
	            }
	        }
	        addIndexToSite(getWithoutStartingX(id), value);
	    }
	 /**
	     * handles the failure of the site.
	     * 
	     * @param timestamp - the time at which failure happened
	     * 
	     */
	 
	 public void failure(int timestamp) 
	 {
		 status = ServerStatus.DOWN;
	     lockTable.clear();
	     IndexRecovered.clear();
	     previousFailtime = timestamp;
	 }
	 
	 /**
	 * handles recovery of the site
	 * 
	 */
	 public void recover() 
	 {
	    int id = this.getId();
	    if (id % 2 == 0) {
	    	IndexRecovered.add("x" + (id - 1));
	        IndexRecovered.add("x" + (id - 1 + 10));
	     }
	     status = ServerStatus.RECOVERING;
	 }
	 /**
	     * returns all the indexes and their value at the site
	     * 
	     * @return String containing all the data
	     * 
	     */
	    public String getIndexes() 
	    {

	        StringBuilder str = new StringBuilder();
	        for (int i = 1; i <= TransactionManager.totalIndexes; i++) 
	        {
	            if (indexes.containsKey(i)) 
	            {
	                str.append("x" + i + ":" + indexes.get(i).getValue() + ", ");
	            }
	        }

	        str.replace(str.lastIndexOf(","), str.length(), "");

	        return str.toString();
	    }
	    
	    public String getindexString(int indexID) 
	    {
	        if (!indexes.containsKey(indexID)) 
	        {
	            return "ignore";
	        }

	        return "x" + indexID + ":" + indexes.get(indexID).getValue()
	                + " ";
	    }	 
	 
	    
	 private int getWithoutStartingX(String id) 
	 {
	     if (id.startsWith("x")) 
	     {
	         int idWithoutX = Integer.parseInt(id.substring(1));
	         return idWithoutX;
	     }
	     // ID was not properly provided (not in the form x1, x2, ..., x20)
	     return -1;
	 }
	 
	    /**
	     * checks if a transaction should wait for the other transaction to finish
	     * @param transaction - the transaction
	     * @param index -the index on which it wants lock
	     * @return -true or false
	     *  
	     */
	    public boolean transactionWaits(Transaction transaction, String index, List<WaitOperation> waitingOperations) {
	    	boolean waits = true;
	    	if (!(this.status == ServerStatus.DOWN)) {
	            ArrayList<Lock> locks = lockTable.get(index);
	            Transaction transHoldingLock = locks.get(0).getTransaction();
	            
	            if(transactionAbortsOnWrite(transaction, index, waitingOperations ))
	            {
	            	waits = false;
	            }
	            else
	            {
	            	System.out.println("Transaction " + transaction.getID()
	                        + " waits because " + transHoldingLock.getID()
	                        + " has  lock on " + index);
	            	waits = true;
	            }
	        }
	        return waits;
	    }
	    /**
	     * checks if a transaction should wait for the other transaction to finish or it should abort
	     * 
	     * @param transaction  - the transaction
	     * @param index  - the index on which it wants lock
	     * @return -true or false
	     * 
	     */
	    public boolean transactionAbortsOnWrite(Transaction transaction, String index, List<WaitOperation> waitingOperations) {
	        if (!(this.status == ServerStatus.DOWN)) {
	            ArrayList<Lock> locks = lockTable.get(index);
	            Transaction transHoldingLock = locks.get(0).getTransaction();
	            for(WaitOperation wo : waitingOperations)
	            {
	            	//System.out.println("-----" + transHoldingLock.getID() + " --------- "+ wo.getWaitingTransaction().getID());
	            	if(transHoldingLock.getID() == wo.getWaitingTransaction().getID())
	            	{
	            		System.out.println("Transaction " + transaction.getID()
		                        + " aborts because " + transHoldingLock.getID()
		                        + " has  lock on " + index);
	            		return true;
	            	}
	            }
	            
	        }
	        return false;
	    }
	    
	    boolean detectCycle(Transaction thl, Transaction twl, List<WaitOperation> waitingOperations, String index)
	    {
	    	Boolean isCycle = false; 
	    	for(WaitOperation wo : waitingOperations)
	    	{
	    		isCycle = wo.isEquals(thl, index);
	    	}
	    	return isCycle;
	    }

	    /**
	     * checks if read lock can be acquired on the index
	     * 
	     * @param index  -the index on which read lock is required
	     * @return - true or false
	     * 
	     */
	    public boolean isReadLockAvailable(String index, Transaction transaction) {
	        if (status.equals(ServerStatus.RECOVERING)
	                && !IndexRecovered.contains(index)) {
	            return false;
	        }
	        if (!lockTable.containsKey(index)) {
	            return true;
	        } else {
	            ArrayList<Lock> locks = lockTable.get(index);
	            if (locks.get(0).getType() == LockType.READ) { // if the locks are
	                                                           // read locks give it
	                return true;
	            } else {
	            	 if (locks.get(0).getTransaction().equals(transaction)) { 
	 	            	// if the same transaction holds the lock then we should be able to read 
	 	                return true;
	 	            }
	                return false;
	            }
	        }
	    }

	    /**
	     * the transaction takes the read lock on the index
	     * 
	     * @param trans -the transaction
	     * @param index  -the index on which read lock is taken
	     */
	    public void getReadLock(Transaction trans, String index) {
	        if (lockTable.containsKey(index)) {
	            Lock lock = new Lock(trans, LockType.READ);
	            ArrayList<Lock> locks = lockTable.get(index);
	            locks.add(lock);
	            lockTable.put(index, locks);
	        } else {
	            Lock lock = new Lock(trans, LockType.READ);
	            ArrayList<Lock> locks = new ArrayList<Lock>();
	            locks.add(lock);
	            lockTable.put(index, locks);
	        }
	    }

	    /**
	     * checks if a write lock can be taken by the transaction on the index
	     * 
	     * @param transaction   - the transaction
	     * @param index   -the index
	     * @return true or false
	     */
	    public boolean isWriteLockAvailable(Transaction transaction, String index) {
	        if (!lockTable.containsKey(index)) {
	            return true;
	        }
	        if (lockTable.get(index).size() == 1) {
	            Lock lock = lockTable.get(index).get(0);
	            if (lock.getTransaction().equals(transaction)) { 
	            	// there is only one lock and the same transaction holds it.
	                return true;
	            }
	        }
	        return false;
	    }

	    /**
	     * transaction takes the write lock on the index
	     * 
	     * @param transaction  - the transaction
	     * @param index - the index
	    
	     */
	    public void getWriteLock(Transaction transaction, String index) {
	        if (!lockTable.containsKey(index)) {
	            Lock lock = new Lock(transaction, LockType.WRITE);
	            ArrayList<Lock> locks = new ArrayList<Lock>();
	            locks.add(lock);
	            lockTable.put(index, locks);
	        } else {
	            Lock lock = lockTable.get(index).get(0);
	            lock.setType(LockType.WRITE); 
	            // convert read lock for the transaction to write lock
	        }
	    }

	    /**
	     * returns true if transaction already has a write lock on the index false otherwise
	     * 
	     * @param transaction
	     *            -the transaction
	     * @param index
	     *            -the index
	     * @return true or false
	     */
	    public boolean isWriteLockTaken(Transaction transaction, String index) {
	        if (lockTable.containsKey(index)) {
	            Lock lock = lockTable.get(index).get(0);
	            if (lock.getTransaction().equals(transaction)
	                    && lock.getType().equals(LockType.WRITE)) {
	                return true;
	            }
	        }
	        return false;
	    }
	 
}
