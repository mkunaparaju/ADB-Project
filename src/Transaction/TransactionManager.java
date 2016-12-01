/**
 * 
 */
package Transaction;
import java.util.*;

import Storage.AccessedSites;
import Storage.DataManager;
import Storage.Lock;
import Storage.Site;
import Storage.Site.*;
import Transaction.Transaction.Status;
import Transaction.WaitOperation.OPERATION;

/**
 * @author mkunaparaju
 *
 */
public class TransactionManager {

	 public static final int totalSites = 10;
     public static final int totalIndexes = 20;

	DataManager dm;
	int currentTime;
	Map<String, Transaction> transMap;
	private List<WaitOperation> waitingOperations;
	public TransactionManager() {
		dm = new DataManager();
		currentTime =1;
		transMap = new HashMap<String, Transaction>();
		waitingOperations = new ArrayList<WaitOperation>();
	}
	
	
	/**
	 * this method begins a read-write transaction
	 * @param timeStamp - transaction starting time
	 * @param operInput - the transaction that is beginning
	 * 
	 */
	public void begin(int timeStamp, String transactionID)
	{
	//	System.out.println("BEGIN : timestamp = " + timeStamp + ", transaction = " + transactionID);
		Transaction newTransaction = new Transaction(transactionID, timeStamp, false);
        transMap.put(transactionID, newTransaction);
		
	}
	/**
	 * @param timeStamp
	 * @param operInput
	 */
	public void beginro(int timeStamp, String transactionID)
	{
		//System.out.println("BEGIN : timestamp = " + timeStamp + ", transaction = " + transactionID);
		Transaction newTransaction = new Transaction(transactionID, timeStamp, true);
		newTransaction.setPresentStateRO(presentProgState());
        transMap.put(transactionID, newTransaction);
		
	}
	/**
     * transaction ends, checks whether it can commit or not
     * 
     * @param timeStamp - the timestamp of the ending of the Transaction
     * @param transaction  - The transaction that is ending
     */
    public void end(int timeStamp, String transactionID) {

        Transaction transaction = transMap.get(transactionID);
        if (transaction == null) 
        {
            System.out.println("Incorrect transaction name " + "or transaction has already ended");
            return;
        }

        if (transaction.getTransactionStatus() == Status.ABORTED) 
        {
            System.out.println("Transaction " + transactionID + " already aborted");
            return;
        }

        Set<AccessedSites> setOfSitesAccessed = transaction.getSitesAccessed();

        if (!transaction.getIsReadOnly()) 
        {
            int transactionTimestamp = transaction.getTimeStamp();
            for (AccessedSites s : setOfSitesAccessed) 
            {
                if (s.getTimeOfAccess() <= s.getSiteAccessed().getPreviousFailtime() || s.getSiteAccessed().getStatus().compareTo(ServerStatus.DOWN) == 0) 
                {
                    System.out.println("Transaction " + transactionID   + " aborted because Site "  + s.getSiteAccessed().getId() + " was down!");
                    transaction.abort(timeStamp);
                    clearLocksAndUnblock( transaction);
                    return;
                }
            }
            System.out.println("Transaction " + transactionID + " commits");
            commitRequest(transaction, transactionTimestamp);
            transaction.commit(timeStamp);
        }

        else {
            System.out.println("Transaction " + transactionID + " commits");
            transaction.commit(timeStamp);
        }
        clearLocksAndUnblock(transaction);
    }
	
	
	public void write(int timeStamp,String transactionID, String index, String val )
	{
		int value = Integer.parseInt(val);
        Transaction transaction = transMap.get(transactionID);
        int indexNum = Integer.parseInt(index.substring(1));

        // if index is odd
        if (indexNum % 2 != 0) 
        {
            int siteNum = indexNum % 10;
            Site site = dm.getSiteList().get(siteNum);
            if (site.getStatus() == ServerStatus.UP|| site.getStatus() == ServerStatus.RECOVERING) 
            {
                // take lock if not then wait or die
                if (site.isWriteLockAvailable(transaction, index)) 
                {
                    site.getWriteLock(transaction, index);
                    transaction.setTransactionStatus(Status.RUNNING);
                    AccessedSites siteAccessed = new AccessedSites(site,currentTime);
                    transaction.addToSitesAccessed(siteAccessed);
                    transaction.addToUncommitedindexes(index, value);
                } 
                else 
                { 
                	// lock not available
                    if (site.transactionAbortsOnWrite(transaction, index)) 
                    {
                        transaction.setTransactionStatus(Status.WAITING);
                        WaitOperation waitOperation = new WaitOperation(transaction, OPERATION.WRITE, index, site,value);
                        waitingOperations.add(waitOperation);

                    } 
                    else 
                    {
                        transaction.setTransactionStatus(Status.ABORTED);
                        clearLocksAndUnblock(transaction);
                    }
                }

            } 
            else 
            { 
            	// the site is down. It waits
                transaction.setTransactionStatus(Status.WAITING);
                WaitOperation waitOperation = new WaitOperation(transaction, OPERATION.WRITE, index, site, value);
                waitingOperations.add(waitOperation);
            }
        }
        // index is even
        else {
        	
        	boolean allLocksAcquired = true;

            for (int i = 0; i < 10; i++) 
            {
                Site site = dm.getSiteList().get(i);
                if (site.getStatus() == ServerStatus.UP  || site.getStatus() == ServerStatus.RECOVERING) 
                {
                    // if lock can be taken
                    if (site.isWriteLockAvailable(transaction, index)) 
                    {
                        site.getWriteLock(transaction, index);
                        AccessedSites siteAccessed = new AccessedSites(site,  currentTime);
                        transaction.addToSitesAccessed(siteAccessed);
                    } 
                    else 
                    { 
                    	// either the transaction waits or gets aborted
                        if (site.transactionAbortsOnWrite(transaction, index)) 
                        {
                            transaction.setTransactionStatus(Status.WAITING);
                            WaitOperation waitOperation = new WaitOperation(transaction, OPERATION.WRITE, index, site,value);
                            waitingOperations.add(waitOperation);
                            allLocksAcquired = false;
                        } else {
                            transaction.setTransactionStatus(Status.ABORTED);
                           
                            clearLocksAndUnblock( transaction);
                            return;
                        }
                    }
                }
            }

            if (allLocksAcquired) {
                transaction.addToUncommitedindexes(index, value);
            }
        }
	}
	public void read(int timeStamp,String transactionID, String index)
	{

        Transaction transaction = transMap.get(transactionID);
        int indexNum = Integer.parseInt(index.substring(1));

        if (transaction.getIsReadOnly()) 
        {
            readOnlyRequest(transaction, index);
            return;
        }

        // if index is odd
        if (indexNum % 2 != 0) {
            int siteNum = indexNum % 10;
            Site site = dm.getSiteList().get(siteNum);
            if (site.getStatus() == ServerStatus.UP|| site.getStatus() == ServerStatus.RECOVERING) 
            {

                if (site.isReadLockAvailable(index, transaction)) 
                {
                    site.getReadLock(transaction, index);
                    
//                    HashMap<String, Integer> uncommit = transaction.getUncommitedindexes();
//                    if (!uncommit.isEmpty()) {
//                    	Integer uncommitval = uncommit.get(index);
//                    	if (uncommitval != null) {
//                    		int indexval = site.read(index);
//                    		if(uncommitval == indexval)
//                    		{    
//                    			System.out.println(transactionID + " reads " + index + " value: " + indexval);
//                    		}
//                    		else {
//                    			System.out.println(transactionID + " reads " + index + " value: " + uncommitval);
//                    		}
//                          }
//                    }
//                    else 
//                    {
//                    	System.out.println(transactionID + " reads " + index + " value: " + site.read(index));
//                    }
                    System.out.println(transactionID + " reads " + index + " value: " + site.read(index));
                    transaction.setTransactionStatus(Status.RUNNING);
                    AccessedSites siteAccessed = new AccessedSites(site, currentTime);
                    transaction.addToSitesAccessed(siteAccessed);
                } 
                else 
                {
                    if (site.transactionWaits(transaction, index)) 
                    {
                        transaction.setTransactionStatus(Status.WAITING);
                        WaitOperation waitOperation = new WaitOperation(transaction, OPERATION.READ, index);
                        waitingOperations.add(waitOperation);
                    } 
                    else 
                    {
                        transaction.setTransactionStatus(Status.ABORTED);
                        clearLocksAndUnblock(transaction);
                        return;
                    }
                }

            } else 
            {
                transaction.setTransactionStatus(Status.WAITING);
                WaitOperation waitOperation = new WaitOperation(transaction, OPERATION.READ, index);
                waitingOperations.add(waitOperation);

            }
        } 
        else
        {
        	// index is even
            Boolean valueRead = false;
            for (Site site : dm.getSiteList()) 
            {
                if (site.getStatus() == ServerStatus.UP) 
                {
                    if (site.isReadLockAvailable(index, transaction)) 
                    {
                        site.getReadLock(transaction, index);
//                        
//                        HashMap<String, Integer> uncommit = transaction.getUncommitedindexes();
//                        if (!uncommit.isEmpty()) {
//                        	Integer uncommitval = uncommit.get(index);
//                        	if (uncommitval != null) {
//                        		int indexval = site.read(index);
//                        		if(uncommitval == indexval)
//                        		{    
//                        			System.out.println(transactionID + " reads " + index + " value: " + indexval);
//                        		}
//                        		else {
//                        			System.out.println(transactionID + " reads " + index + " value: " + uncommitval);
//                        		}
//                              }
//                        }
//                        else 
//                        {
//                        	System.out.println(transactionID + " reads " + index + " value: " + site.read(index));
//                        }
                      	System.out.println(transactionID + " reads " + index + " value: " + site.read(index));
                        valueRead = true;
                        AccessedSites siteAccessed = new AccessedSites(site,currentTime);
                        transaction.addToSitesAccessed(siteAccessed);
                        break;
                    }
                }
            }
            if (!valueRead) 
            {
            	// either all servers are down or there is a write lock.
                for (int i = 0; i < 10; i++) {
                    if (dm.getSiteList().get(i).getStatus() == ServerStatus.UP) 
                    {
                        if (!dm.getSiteList().get(i).transactionWaits(transaction, index)) 
                        {
                            transaction.setTransactionStatus(Status.ABORTED);
                            clearLocksAndUnblock(transaction);
                            return;
                        }
                        break;
                    }
                }
                WaitOperation waitOperation = new WaitOperation(transaction, OPERATION.READ, index);
                waitingOperations.add(waitOperation);
            }

        }
		
	}
	
	 /**
     * handles requests for ReadOnly transactions
     * 
     * @param transaction
     *            - The Transaction that is making the read request
     * @param var
     *            - The variable that we are reading
     */
    private void readOnlyRequest(Transaction transaction, String var) 
    {
        HashMap<String, Integer> snapshot = transaction.getPresentStateRO();
        if (snapshot.containsKey(var)) 
        {
            System.out.println(transaction.getID() + " reads " + var+ " value: " + snapshot.get(var));
        } 
        else 
        {
            // value was not present at the time of snapshot get value now
            int varNum = Integer.parseInt(var.substring(1));
            int siteNum = varNum % 10;
            Site site = dm.getSiteList().get(siteNum);
            if (site.getStatus() == ServerStatus.UP) 
            {
                System.out.println(transaction.getID() + " reads " + var + " value: " + site.read(var));
            } 
            else if ((site.getStatus() == ServerStatus.RECOVERING)  && site.isReadLockAvailable(var, transaction)) 
            {
                System.out.println(transaction.getID() + " reads " + var + " value: " + site.read(var));
            } 
            else 
            {
            	// create wait operation
                WaitOperation waitOperation = new WaitOperation(transaction, OPERATION.READ, var);
                waitingOperations.add(waitOperation);
            }
        }
    }

    /**
     * gives the committed values of all copies of all variables at all sites,
     * sorted per site.
     */
    public void dump() 
    {
        System.out.println("DUMP ALL:");
        for (int i = 1; i <= 10; i++) 
        {
            dump(i);
        }
    }

    /**
     * gives the committed values of all copies of all variables at site siteNUm
     * 
     * @param siteNum - identifier of the site that we are dumping from
     * 
     */
    public void dump(int siteNum) {
        System.out.println("DUMP : siteNum = " + siteNum);
        ServerStatus status = dm.getSiteList().get(siteNum - 1).getStatus();

        if (status == ServerStatus.UP || status == ServerStatus.RECOVERING) {
            System.out.println(dm.getSiteList().get(siteNum - 1).getIndexes());
        } 
        else if (status == ServerStatus.DOWN) 
        {
            System.out.println("Site "+ dm.getSiteList().get(siteNum - 1).getId()+ ": Down");
        }
    }

    /**
     * gives the committed values of all copies of variable var at all sites.
     * 
     * @param index
     *            - the index that we are dumping from all sites 
     */
    public void dump(String index) {
        int indexID = Integer.parseInt(index.substring(1));

        for (Site s : dm.getSiteList()) {
            ServerStatus status = s.getStatus();

            if (status == ServerStatus.UP || status == ServerStatus.RECOVERING) 
            {
                String val = s.getindexString(indexID);
                if (!val.equalsIgnoreCase("ignore")) 
                {
                    System.out.println("SITE " + s.getId() + " : " + s.getindexString(indexID));
                }
            } 
            else if (status == ServerStatus.DOWN) 
            {
            	System.out.println("Server is Down!");
            }
        }
    }
	
	public DataManager getDM()
	{
		return dm;
	}
	
	public void counter()
	{
		currentTime++;
	}
	
	HashMap<String, Integer> presentProgState()
	{
		 HashMap<String, Integer> presentStateMap = new HashMap<String, Integer>();

	        // adding even indexs
	        for (int i = 0; i < totalSites; i = i + 2) 
	        {
	            if (dm.getSiteList().get(i).getStatus() == ServerStatus.UP) 
	            {
	                for (int var = 2; var <= 20; var = var + 2) 
	                {
	                	presentStateMap.put("x" + var, dm.getSiteList().get(i).read(var));
	                }
	                break;
	            }

	        }
	        // adding odd indexs
	        for (int i = 1; i < 10; i = i + 2) 
	        {
	            if (dm.getSiteList().get(i).getStatus() == ServerStatus.UP) 
	            {
	            	presentStateMap.put("x" + i, dm.getSiteList().get(i).read(i));
	            	presentStateMap.put("x" + (i + 10), dm.getSiteList().get(i)
	                        .read(i + 10));
	            }
	        }
	        return presentStateMap;
	}
	
	/**
     * Clears the locks held by the transaction and unblocks waiting operations
     * 
     * @param timeStamp - the time moment that the Transaction comes to this method
     * @param transaction  - The transaction that we need to clear from the sites lock table
     * 
     */
    public void clearLocksAndUnblock(Transaction transaction) 
    {
        transMap.remove(transaction.getID());
        for (Site s : dm.getSiteList()) 
        {
            Map<String, ArrayList<Lock>> siteLockTable = s.getLockTable();
            ArrayList<String> auxSiteLockTable = new ArrayList<String>();
            for (String index : siteLockTable.keySet()) 
            {
                ArrayList<Lock> lockArrayList = siteLockTable.get(index);
                ArrayList<Lock> auxLockList = new ArrayList<Lock>();
                for (Lock lock : lockArrayList) 
                {
                    if (lock.getTransaction().equals(transaction)) 
                    {
                       auxLockList.add(lock);
                    }
                }
                for (Lock lock : auxLockList) 
                {
                    siteLockTable.get(index).remove(lock);
                }

                if (siteLockTable.get(index).size() == 0) 
                {
                    auxSiteLockTable.add(index);
                }
            }

            for (String index : auxSiteLockTable) 
            {
                siteLockTable.remove(index);
            }
        }

        checkWaitingOperations();
    }

    /**
     * Checks if waiting operations can be started
     * 
     */
    public void checkWaitingOperations() 
    {
        int count = waitingOperations.size();

        List<WaitOperation> auxOperations = new ArrayList<WaitOperation>();

        for (int i = 0; i < waitingOperations.size(); i++) 
        {
            auxOperations.add(waitingOperations.get(i));
        }

        for (int i = 0; i < count; i++) 
        {
            WaitOperation waitTask = auxOperations.get(i);
            waitingOperations.remove(waitTask);

            if (!transMap.containsValue(waitTask.getWaitingTransaction())) 
            {
                continue;
            }
            
            if (waitTask.getWaitOperation() == OPERATION.READ) 
            {
                read(waitTask.getWaitingTransaction().getTimeStamp(), waitTask.getWaitingTransaction().getID(), waitTask.getindex());
            } 
            else 
            {
                // check if write lock is available on the site:
                Site site = waitTask.getWaitSite();
                Transaction transaction = waitTask.getWaitingTransaction();
                String index = waitTask.getindex();
                if (site.getStatus() == ServerStatus.UP || site.getStatus() == ServerStatus.RECOVERING) 
                {
                    // take lock if not then wait or die

                    if (site.isWriteLockAvailable(transaction, index)) 
                    {
                        site.getWriteLock(transaction, index);
                        AccessedSites siteAccessed = new AccessedSites(site, currentTime);
                        transaction.addToSitesAccessed(siteAccessed);

                        // check if transaction is waiting for more locks

//                        for (int j = i; j < count; j++) 
//                        {
//                            if (auxOperations.get(j).getWaitingTransaction() == transaction) 
//                            {
//                            }
//                        }

                        transaction.addToUncommitedindexes(index, waitTask.getValue());
                    }

                    else 
                    { 
                    	// lock not available
                        if (site.transactionWaits(transaction, index)) 
                        {
                            transaction.setTransactionStatus(Status.WAITING);
                            WaitOperation waitOperation = new WaitOperation(transaction, OPERATION.WRITE, index, site, waitTask.getValue());
                            waitingOperations.add(waitOperation);

                        }
                        else 
                        {
                            transaction.setTransactionStatus(Status.ABORTED);
                            clearLocksAndUnblock(transaction);
                        }

                    }
                } else {
                    transaction.setTransactionStatus(Status.ABORTED);
                    System.out.println("Transaction "  + transaction.getID() + " Aborted because it was waiting for a lock but site "+ site + "failed");
                    clearLocksAndUnblock(transaction);
                }
            }

        }
    }

    /**
     * transaction makes a commit request
     * 
     * @param transaction   - The transaction that is trying to commit
     * @param timeStamp  - the timestamp of the commit request

     */
    public void commitRequest(Transaction transaction, int timeStamp) {
        
        HashMap<String, Integer> uncommitted = transaction.getUncommitedindexes();
        for (String index : uncommitted.keySet()) 
        {
            for (Site s : dm.getSiteList()) 
            {
                if (s.doesIndexExistOnSite(index)) 
                {
                    if ((s.getStatus().compareTo(ServerStatus.UP) == 0 || s.getStatus().compareTo(ServerStatus.RECOVERING) == 0) && s.isWriteLockTaken(transaction, index)) 
                    {
                        s.write(index, uncommitted.get(index));
                    }
                }
            }
        }

        transaction.getUncommitedindexes().clear();
    }
}