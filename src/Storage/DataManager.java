/**
 * 
 */
package Storage;

import java.util.ArrayList;
import java.util.List;

import Storage.Site;
import Storage.Site.ServerStatus;


/**
 * @author mkunaparaju
 *
 */
public class DataManager {
	 private List<Site> siteList;

	 
	 public DataManager() 
	 {
	     setUp();
	 }

	 public List<Site> getSiteList() 
	 {
		 return siteList;
	 }
	 
	 /**
	     * sets up the initial database: sites with variables and their initial value
	     */
	    public void setUp() {
	        // creating variables
	        List<Index> varList = new ArrayList<Index>();
	        for (int i = 1; i <= 20; i++) {
	            Index v = new Index(i, i * 10);
	            varList.add(v);
	        }
	        // creating sites
	        siteList = new ArrayList<Site>();
	        for (int i = 1; i <= 10; i++) 
	        {
	            Site site = new Site(i);
	            site.setStatus(ServerStatus.UP);
	            siteList.add(site);
	        }

	        // adding indexs to site
	        for (int i = 1; i <= 20; i++)
	        {
	            // var is odd
	            if (i % 2 != 0) 
	            {
	                siteList.get(i % 10).addIndexToSite(varList.get(i - 1));
	            } 
	            else 
	            { 
	            	// add to all sites
	                for (int j = 0; j < 10; j++) {
	                    siteList.get(j).addIndexToSite(varList.get(i - 1));
	                }
	            }
	        }
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
		  Site site = getSiteList().get(siteID - 1);
	        if (site != null) 
	        {
	            site.recover();
	        }
	    }	
}
