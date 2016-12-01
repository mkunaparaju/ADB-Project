/**
 * 
 */
package Transaction;
import java.util.*;

import Storage.DataManager;
import Storage.Site.*;

/**
 * @author mkunaparaju
 *
 */
public class TransactionManager {

	 public static final int totalSites = 10;
     public static final int totalIndexes = 20;

	DataManager dm;
	int count;
	Map<String, Transaction> transMap;
	public TransactionManager() {
		dm = new DataManager();
		count =1;
		transMap = new HashMap<String, Transaction>();
	}
	
	
	/**
	 * this method begins a read-write transaction
	 * @param timeStamp - transaction starting time
	 * @param operInput - the transaction that is beginning
	 * 
	 */
	public void begin(int timeStamp, String transactionID)
	{
		System.out.println("BEGIN : timestamp = " + timeStamp + ", transaction = " + transactionID);
		Transaction newTransaction = new Transaction(transactionID, timeStamp, false);
        transMap.put(transactionID, newTransaction);
		
	}
	/**
	 * @param timeStamp
	 * @param operInput
	 */
	public void beginro(int timeStamp, String transactionID)
	{
		System.out.println("BEGIN : timestamp = " + timeStamp + ", transaction = " + transactionID);
		Transaction newTransaction = new Transaction(transactionID, timeStamp, true);
		newTransaction.setPresentStateRO(presentProgState());
        transMap.put(transactionID, newTransaction);
		
	}
	public void end(int timeStamp, String operInput)
	{
		
	}public void write(int timeStamp,String transaction, String index, String value )
	{
		
	}
	public void read(int timeStamp,String transaction, String index)
	{
		
	}

	public void dump()
	{
		
	}
	public void dump(String index)
	{
		
	}
	public void dump(int siteID)
	{
		
	}
	
	public DataManager getDM()
	{
		return dm;
	}
	
	public void counter()
	{
		count++;
	}
	
	HashMap<String, Integer> presentProgState()
	{
		 HashMap<String, Integer> presentStateMap = new HashMap<String, Integer>();

	        // adding even variables
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
	        // adding odd variables
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
}
