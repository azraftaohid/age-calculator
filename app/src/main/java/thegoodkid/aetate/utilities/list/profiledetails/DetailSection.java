package thegoodkid.aetate.utilities.list.profiledetails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import thegoodcompany.common.utils.recyclerview.HeaderItem;
import thegoodcompany.common.utils.recyclerview.Section;

public class DetailSection extends Section<HeaderItem, DetailItem> {
    public DetailSection(@Nullable HeaderItem sectionHeader, @NonNull ArrayList<DetailItem> items) {
        super(sectionHeader, items);
    }

    void replaceItems(List<DetailItem> items) {
        clearItems();
        addItems(items);
    }
}
