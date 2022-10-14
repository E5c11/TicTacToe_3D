package com.esc.test.apps.common.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;

import com.esc.test.apps.R;
import com.esc.test.apps.databinding.FriendListBinding;
import com.esc.test.apps.data.objects.pojos.UserInfo;

public class FriendRequestAdapter extends
        ListAdapter<UserInfo, RequestHolder> {

    private final Context context;
    private final ActiveFriendsAdapter.OnClickListener listener;
    public static final String REQUEST_LIST = "request";

    public FriendRequestAdapter(Context context, ActiveFriendsAdapter.OnClickListener listener) {
        super(ActiveFriendsAdapter.diffCallback);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FriendListBinding binding = FriendListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestHolder holder, int pos) {
        UserInfo user = getItem(pos);
        holder.bind(user, REQUEST_LIST);
        holder.bindButton(context.getString(R.string.accept));
    }
}
