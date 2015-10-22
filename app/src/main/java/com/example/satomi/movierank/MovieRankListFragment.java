package com.example.satomi.movierank;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Satomi on 10/21/15.
 */
public class MovieRankListFragment extends ListFragment {

    private ArrayAdapter<String> mMovieRankAdapter;

    public MovieRankListFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMovieRankAdapter = new MovieRankAdapter(getActivity(), MovieSet.list);
        setListAdapter(mMovieRankAdapter);
    }

    public class MovieRankAdapter extends ArrayAdapter<String> {

        public MovieRankAdapter(Context context, List<String> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(R.layout.list_item_movie_rank, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(R.id.list_item_movie_rank_textview);
            textView.setText(getItem(position));

            return view;
        }
    }
}

