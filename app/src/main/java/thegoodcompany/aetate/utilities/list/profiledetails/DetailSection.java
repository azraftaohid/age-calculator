package thegoodcompany.aetate.utilities.list.profiledetails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import thegoodkid.common.utils.recyclerview.HeaderItem;
import thegoodkid.common.utils.recyclerview.Section;

public class DetailSection extends Section<HeaderItem, DetailItem> {
    public DetailSection(@Nullable HeaderItem sectionHeader, @NonNull ArrayList<DetailItem> items) {
        super(sectionHeader, items);
    }
}
