/**
 * 
 */
package Execution;
import Transaction.TransactionManager;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author manasa & iqra
 *
 */
class InitExec {

	/**
	 * @param args - provides the input file path. Only 1 argument
	 */	
	
	public static void main(String[] args) 
	{
		if(args.length != 1)
		{
			System.out.println("Please provide input file path");
			return;
		}
		String inFile = args[0];
		InitExec execution = new InitExec();
		execution.parseInput(inFile);
	}
	
	void parseInput(String inFilePath)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(inFilePath));
	        String nextLine = br.readLine();
	        int timestamp = 1;
	        TransactionManager tm = new TransactionManager();
	        System.out.println("------------------TESTCASE-------------------");
	        while (nextLine != null) {
                // System.out.println(nextLine);
            
                 if (nextLine.startsWith("//") || nextLine.isEmpty()) {
                    nextLine = br.readLine();
                    continue;
                } else if (nextLine.startsWith("=")) {
                    break;
                }

                String[] operations = nextLine.split(";");
                
                for (String s: operations) {

                    String singleOper = s.trim();
                    String typeOfOper = singleOper.substring(0, singleOper.indexOf("(")).trim().toLowerCase();
                    String inputToOperation = singleOper.substring(singleOper.indexOf("(") + 1, singleOper.indexOf(")")).trim().toLowerCase();

                    if (typeOfOper.equals("begin")) 
                    {
                        tm.begin(timestamp, inputToOperation);
                    } 
                    else if (typeOfOper.equals("beginro")) 
                    {
                        tm.beginro(timestamp, inputToOperation);
                    } 
                    else if (typeOfOper.equals("end")) 
                    {
                        tm.end(timestamp, inputToOperation);
                    } 
                    else if (typeOfOper.equals("fail")) 
                    {
                        int siteID = Integer.parseInt(inputToOperation);
                        tm.getDM().fail(timestamp,siteID);
                    } 
                    else if (typeOfOper.equals("recover")) {
                        int siteID = Integer.parseInt(inputToOperation);
                        tm.getDM().recover(siteID);
                    } 
                    else if (typeOfOper.equals("w")) {
                        String T = inputToOperation.split(",")[0].trim();
                        String var = inputToOperation.split(",")[1].trim();
                        String val = inputToOperation.split(",")[2].trim();
                        tm.write(timestamp, T, var, val);
                    } 
                    else if (typeOfOper.equals("r")) {
                        String T = inputToOperation.split(",")[0].trim();
                        String var = inputToOperation.split(",")[1].trim();
                        tm.read(timestamp, T, var);
                    } 
                    else if (typeOfOper.equals("dump")) {
                        if (inputToOperation.equals("")) {
                            tm.dump();
                        } else if (inputToOperation.toLowerCase()
                                .startsWith("x")) {
                            tm.dump(inputToOperation);
                        } else {
                            int siteID = Integer.parseInt(inputToOperation);
                            tm.dump(siteID);
                        }
                    }
                }

                timestamp++;
                tm.counter();
                nextLine = br.readLine();
            }
            br.close();

	         
	         
		}
		catch(Exception e)
		{
			System.out.println("Input file issue");
			e.printStackTrace();
		}
		
	}
}
