package org.emoncms.myapps.chart;

/**
 * Created by tamsin on 10/10/16.
 */
public interface MyElectricDataManager {

    void setFeedIds(int flowId, int useId);

    void setCurrentValues(float powerNowW, float totalUsagekWh);

    float getTotalUsagekWh();

    void setUseToYesterday(float useToYesterdaykWh);

    void loadFeeds(int delay);

    void loadPowerNow(int delay);

    void loadPowerHistory(int delay);

    /**
     * Return true if time to load it.
     * @return
     */
    boolean loadUseHistory(int delay);

    void showMessage(String message);

    void showMessage(int message);

    void clearMessage();

    String getEmonCmsUrl();

    String getEmoncmsApikey();

    /**
     * The tag to use for all threads relating to this page
     * @return
     */
    String getPageTag();



}
