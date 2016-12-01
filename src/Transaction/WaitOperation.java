/**
 * 
 */
package Transaction;
import Storage.Site;

/**
 * @author mkunaparaju
 *
 */
public class WaitOperation {

	 public enum OPERATION {
	        READ, WRITE;
	    }

	    private Transaction waitingTransaction;
	    private OPERATION operationType;
	    private String index;
	    private Site waitSite;
	    private int value;

	    public WaitOperation(Transaction t, OPERATION o, String var) {
	        this.waitingTransaction = t;
	        this.operationType = o;
	        this.index = var;
	    }

	    public WaitOperation(Transaction t, OPERATION o, String var, Site site, int val) {
	        this.waitingTransaction = t;
	        this.operationType = o;
	        this.index = var;
	        this.waitSite = site;
	        this.value = val;
	    }

	    public Transaction getWaitingTransaction() {
	        return waitingTransaction;
	    }

	    public OPERATION getWaitOperation() {
	        return operationType;
	    }

	    public String getindex() {
	        return index;
	    }

	    public Site getWaitSite() {
	        return waitSite;
	    }

	    public int getValue() {
	        return value;
	    }
}
