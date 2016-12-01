/**
 * 
 */
package Storage;

/**
 * @author mkunaparaju
 *
 */

public class AccessedSites {

	private Site site;
    private int timeOfAccess;

    public AccessedSites(Site siteAccessed, int timeOfAccess) {
        super();
        this.site = siteAccessed;
        this.timeOfAccess = timeOfAccess;
    }

    public Site getSiteAccessed() {
        return site;
    }

    public int getTimeOfAccess() {
        return timeOfAccess;
    }
}
