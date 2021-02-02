package org.ar.ar_audiomic.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.ar.ar_audiomic.fragment.InfoFragment;
import org.ar.ar_audiomic.fragment.MainFragment;

import static org.ar.ar_audiomic.activity.MainActivity.INDEX_CONN_MIC;
import static org.ar.ar_audiomic.activity.MainActivity.INDEX_INFO;

public class MainPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;
    public MainPagerAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
        mContext =context;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        return getTabFragment(position);
    }

    @Override
    public int getCount() {
        return 2;
    }

    private Fragment getTabFragment(int index){
        Fragment fragment =null;
        switch (index){
            case INDEX_CONN_MIC:
                fragment = MainFragment.getInstance(mContext);
                break;
            case INDEX_INFO:
                fragment = InfoFragment.getInstance(mContext);
                break;
        }
        return fragment;
    }
}
