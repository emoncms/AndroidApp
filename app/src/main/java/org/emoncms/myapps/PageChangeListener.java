package org.emoncms.myapps;

import org.emoncms.myapps.myelectric.MyElectricSettings;

/**
 * Interface for things which want to know about changes to pages
 */
public interface PageChangeListener {

    void onAddPage(MyElectricSettings settings);

    void onDeletePage(MyElectricSettings settings);

    void onUpdatePage(MyElectricSettings settings);
}
