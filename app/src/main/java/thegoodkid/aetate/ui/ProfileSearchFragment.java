package thegoodkid.aetate.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.microsoft.fluentui.listitem.ListItemDivider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import thegoodkid.aetate.databinding.FragmentProfileSearchBinding;
import thegoodkid.aetate.utilities.Avatar;
import thegoodkid.aetate.utilities.Birthday;
import thegoodkid.aetate.utilities.list.profile.ProfileListAdapter;
import thegoodkid.aetate.utilities.list.profile.ProfilesGroup;
import thegoodkid.aetate.utilities.profilemanagement.Profile;
import thegoodkid.aetate.utilities.profilemanagement.ProfileManager;
import thegoodkid.aetate.utilities.profilemanagement.ProfileManagerInterface;
import thegoodkid.aetate.utilities.tagmanagement.Tag;

public class ProfileSearchFragment extends Fragment implements
        ProfileManagerInterface.OnProfileUpdatedListener, ProfileManagerInterface.OnProfileRemovedListener {
    private static final String QUERY_TEXT = "query_text";

    @SuppressWarnings("FieldCanBeLocal")
    private FragmentProfileSearchBinding binding;
    private ProfileListAdapter<Group> adapter;
    private ProfileManager profileManager;
    private String query = "";

    public ProfileSearchFragment() {

    }

    @NonNull
    public static ProfileSearchFragment newInstance() {
        return new ProfileSearchFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinkedHashMap<Group, ProfilesGroup> map = new LinkedHashMap<>();
        map.put(Group.DEFAULT, new ProfilesGroup(null, new ArrayList<>()));

        profileManager = ProfileManager.getInstance(getContext());
        adapter = new ProfileListAdapter<>(requireContext(), map);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileSearchBinding.inflate(inflater, container, false);

        binding.resultContainer.addItemDecoration(new ListItemDivider(requireContext(), DividerItemDecoration.VERTICAL));
        binding.resultContainer.setAdapter(adapter);

        if (savedInstanceState != null) {
            setQueryText(savedInstanceState.getString(QUERY_TEXT));
        }

        profileManager.addOnProfileUpdatedListener(this);
        profileManager.addOnProfileRemovedListener(this);

        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(QUERY_TEXT, query);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        profileManager.removeOnProfileUpdatedListener(this);
        profileManager.removeOnProfileRemovedListener(this);
    }

    void setQueryText(String s) {
        query = s;
        updateResult();
    }

    @Override
    public void onProfileAvatarChanged(int profileId, @Nullable Avatar newAvatar, @Nullable Avatar previousAvatar) {
        adapter.refreshAvatar(profileId);
    }

    @Override
    public void onProfileNameChanged(int profileId, @NonNull String newName, String previousName) {
        adapter.refreshName(profileId);
    }

    @Override
    public void onProfileDateOfBirthUpdated(int profileId, int newBirthYear, int newBirthMonth, int newBirthDay, Birthday previousBirthDay) {
        adapter.refreshAge(profileId);
    }

    @Override
    public void onProfileRemoved(@NonNull Profile profile, List<Tag> removedTags) {
        adapter.removeItem(profile);
    }

    private void updateResult() {
        if (query == null || query.length() == 0) return;

        adapter.clearSectionItems(Group.DEFAULT);
        ArrayList<Profile> result = profileManager.query(query);

        for (Profile profile : result) {
            adapter.addItem(Group.DEFAULT, profile);
        }
    }

    private enum Group {
        DEFAULT
    }
}
