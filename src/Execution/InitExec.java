/**
 * 
 */
package Execution;

/**
 * @author mkunaparaju
 *
 */
class InitExec {

	/**
	 * @param args
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
		
	}

}
