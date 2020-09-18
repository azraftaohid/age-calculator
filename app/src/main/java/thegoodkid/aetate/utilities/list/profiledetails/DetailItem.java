package thegoodkid.aetate.utilities.list.profiledetails;

import android.view.View;

import androidx.annotation.Nullable;

import thegoodcompany.common.utils.recyclerview.BaseItem;

public class DetailItem implements BaseItem {
    private String mTitle;
    @Nullable
    private String mSubtitle;
    @Nullable
    private View mCustomView;

    public DetailItem(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getSubtitle() {
        return mSubtitle;
    }

    public DetailItem setSubtitle(String subtitle) {
        mSubtitle = subtitle;

        return this;
    }

    @Nullable
    public View getCustomView() {
        return mCustomView;
    }

    public DetailItem setCustomView(View customView) {
        mCustomView = customView;

        return this;
    }
}
