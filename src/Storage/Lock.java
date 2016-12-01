/**
 * 
 */
package Storage;

import Transaction.Transaction;

/**
 * @author mkunaparaju
 *
 */
public class Lock {
	 public enum LockType {
	        READ, WRITE;
	    }

	    LockType type;
	    Transaction transaction;

	    public Lock(Transaction trans, LockType type) {
	        this.type = type;
	        this.transaction = trans;
	    }

	    public Transaction getTransaction() {
	        return transaction;
	    }

	    public LockType getType() {
	        return type;
	    }

	    public void setType(LockType type) {
	        this.type = type;
	    }
}
