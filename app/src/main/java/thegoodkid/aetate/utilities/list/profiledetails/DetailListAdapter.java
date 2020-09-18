package thegoodkid.aetate.utilities.list.profiledetails;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.microsoft.fluentui.listitem.ListItemView;
import com.microsoft.fluentui.listitem.ListSubHeaderView;
import com.microsoft.fluentui.snackbar.Snackbar;
import com.microsoft.fluentui.util.ViewUtilsKt;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;

import thegoodcompany.common.utils.recyclerview.BaseItem;
import thegoodcompany.common.utils.recyclerview.BaseListAdapter;
import thegoodcompany.common.utils.recyclerview.HeaderItem;
import thegoodkid.aetate.R;

public class DetailListAdapter extends BaseListAdapter<DetailListAdapter.Section, DetailSection, HeaderItem, DetailItem, RecyclerView.ViewHolder> {
    @NonNull
    Context context;
    @Nullable
    View root;

    public DetailListAdapter(@NonNull Context context, @Nullable View root, LinkedHashMap<Section, DetailSection> sectionMap) {
        super(sectionMap);

        this.context = context;
        this.root = root;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        switch (ViewType.values()[viewType]) {
            case ITEM:
                ListItemView itemView = new ListItemView(context);
                itemView.setLayoutParams(layoutParams);
                itemView.setCustomViewSize(ListItemView.CustomViewSize.SMALL);
                return new DetailHolder(itemView);
            case HEADER:
                ListSubHeaderView headerView = new ListSubHeaderView(context);
                headerView.setLayoutParams(layoutParams);
                return new HeaderHolder(headerView);
        }

        throw new IllegalStateException("View holder not implemented (View Type: " + ViewType.values()[viewType] + ")");
    }

    @Override
    protected void onBindViewHolder(@NonNull AfterWards afterWards, @NonNull RecyclerView.ViewHolder viewHolder, int i) {
        BaseItem baseItem = getItem(i);

        if (viewHolder instanceof DetailHolder) {
            DetailHolder holder = (DetailHolder) viewHolder;
            DetailItem item = (DetailItem) baseItem;

            holder.itemView.setTitle(item.getTitle());
            holder.itemView.setSubtitle(item.getSubtitle() != null ? item.getSubtitle() : "");
            holder.itemView.setCustomView(item.getCustomView());

            holder.itemView.setOnClickListener(view -> onDetailClick((ListItemView) view));
            holder.itemView.setOnLongClickListener(view -> onDetailClick((ListItemView) view));
        } else if (viewHolder instanceof HeaderHolder) {
            HeaderHolder holder = (HeaderHolder) viewHolder;
            HeaderItem item = (HeaderItem) baseItem;

            holder.headerView.setTitle(item.getTitle());
        }

        afterWards.run();
    }

    private boolean onDetailClick(@NotNull ListItemView v) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText(null, v.getTitle());

        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(data);

            if (root != null) {
                View check = ViewUtilsKt.createImageView(context, R.drawable.ic_fluent_checkmark_24_regular, ContextCompat.getColor(context, R.color.snackbar_drawable));

                Snackbar.Companion.make(root, context.getString(R.string.copied), Snackbar.LENGTH_SHORT, Snackbar.Style.REGULAR)
                        .setCustomView(check, Snackbar.CustomViewSize.SMALL)
                        .show();
            } else {
                Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    public void replaceSectionItems(@NonNull Section sectionKey, List<DetailItem> items) {
        DetailSection section = getSectionMap().get(sectionKey);
        if (section == null)
            throw new NullPointerException("Section not present; couldn't complete action (provided section: " + sectionKey +
                    "; available sections: " + getSectionMap().keySet() + ")");

        int itemCountThen = section.getItemCount();
        int itemCountNow = items.size();
        int startPos = getSectionStartPosition(sectionKey) + (section.hasHeader() ? 1 : 0);

        section.replaceItems(items);

        if (itemCountThen == itemCountNow) {
            notifyItemRangeChanged(startPos, itemCountNow);
        } else if (itemCountThen > itemCountNow) {
            notifyItemRangeRemoved(startPos + itemCountNow, itemCountThen - itemCountNow);
            notifyItemRangeChanged(startPos, itemCountNow);
        } else {
            notifyItemRangeInserted(startPos + itemCountThen, itemCountNow - itemCountThen);
            notifyItemRangeChanged(startPos, itemCountThen);
        }
    }

    public enum Section {
        BASIC, AGE
    }

    private static class DetailHolder extends RecyclerView.ViewHolder {
        private ListItemView itemView;

        public DetailHolder(@NonNull ListItemView itemView) {
            super(itemView);

            this.itemView = itemView;
        }
    }

    private static class HeaderHolder extends RecyclerView.ViewHolder {
        private ListSubHeaderView headerView;

        public HeaderHolder(@NonNull ListSubHeaderView itemView) {
            super(itemView);

            headerView = itemView;
        }
    }
}
