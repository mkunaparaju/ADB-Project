package Storage;

import Transaction.TransactionManager;

public class Index {
	 private int id;
	 private int value;

	 public Index(int id, int value) {
	        if (id < 1 || id > TransactionManager.totalIndexes) {
	            throw new NullPointerException("Index ID is not in bounds!!");
	        }

	        this.id = id;
	        this.value = value;
	    }

	    public int getId() {
	        return id;
	    }

	    public void setId(int id) {
	        this.id = id;
	    }

	    public int getValue() {
	        return value;
	    }

	    public void setValue(int value) {
	        this.value = value;
	    }
}
