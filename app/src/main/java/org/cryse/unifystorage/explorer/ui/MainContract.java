package org.cryse.unifystorage.explorer.ui;

import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.cryse.unifystorage.explorer.base.BasePresenter;
import org.cryse.unifystorage.explorer.base.BaseView;

public interface MainContract {
    interface View extends BaseView<Presenter> {
        void onDrawerItemsChanged(IDrawerItem[] drawerItems);
        void onNavigateTo(IDrawerItem drawerItem);
    }

    interface Presenter extends BasePresenter {
        void updateDrawerItems();

        void onNavigationSelected(IDrawerItem drawerItem);

        void addNewProvider(String displayName, String userName, int providerType, String credentialData, String extraData);
    }
}
