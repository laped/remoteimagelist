package dk.remoteimagelist.activity;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import dk.remoteimagelist.R;
import dk.remoteimagelist.RemoteImageView;

public class RemoteimagelistActivity extends ListActivity {
    
    private ArrayList<ProfileItem> profiles = null;
    private ProfileAdapter adapter;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        profiles = new ArrayList<RemoteimagelistActivity.ProfileItem>();
        profiles.add(new ProfileItem("username 1", ""));
        profiles.add(new ProfileItem("username 2", "http://dl.dropbox.com/u/4811285/Pictures/android/Thumbnail_634508342213746514.jpg"));
        profiles.add(new ProfileItem("username 3", "http://dl.dropbox.com/u/4811285/Pictures/android/Thumbnail_634508363812036553.jpg"));
        
        adapter = new ProfileAdapter(this, R.layout.user_list_item, profiles);        
        setListAdapter(adapter);
    }
    
    static class ViewHolder {
        RemoteImageView profileThumb;
        TextView status;
        TextView username;
        ImageView onlineImg;
        TextView distance;
    }

    private class ProfileAdapter extends ArrayAdapter<ProfileItem> {
        private ArrayList<ProfileItem> items;
        private LayoutInflater pInflater;

        public ProfileAdapter(Context context, int textViewResourceId, ArrayList<ProfileItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
            this.pInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                v = pInflater.inflate(R.layout.user_list_item, null);
                holder = new ViewHolder();
                holder.profileThumb = (RemoteImageView) v.findViewById(R.id.uli_profileThumb);
                holder.status = (TextView) v.findViewById(R.id.uli_status);
                holder.username = (TextView) v.findViewById(R.id.uli_Username);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            ProfileItem p = items.get(position);

            if (p != null) {
                int defaultImageResId = R.drawable.noimage_male_66x88;
                holder.profileThumb.setDefaultDrawable(defaultImageResId);
                if (TextUtils.isEmpty(p.thumbnailUrl)) {
                    holder.profileThumb.setNoImageDrawable(defaultImageResId);
                } else {
                    holder.profileThumb.setImageUrl(p.thumbnailUrl);
                    holder.profileThumb.loadImage();
                }
                holder.username.setText(p.username);
            }

            return v;
        }
    }
    
    class ProfileItem {
        public ProfileItem(String username, String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            this.username = username;
        }
        public String thumbnailUrl;
        public String username;
    }
}