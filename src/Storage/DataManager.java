/**
 * 
 */
package Storage;

import java.util.List;

/**
 * @author mkunaparaju
 *
 */
public class DataManager {
	 private List<Site> siteList;

	 public List<Site> getSiteList() 
	 {
		 return siteList;
	 }
	
	public void fail(int timeStamp, int siteID)
	{
	
		 Site site = getSiteList().get(siteID - 1);

	        if (site != null) 
	        {
	        	//System.out.println("FAIL : timestamp = " + timeStamp + ", siteID = " + siteID);
	            site.failure(timeStamp);
	        }
	}
	
	public void recover(int siteID)
	{
		
	}

}
