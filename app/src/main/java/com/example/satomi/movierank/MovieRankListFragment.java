package com.example.satomi.movierank;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
        mMovieRankAdapter = new MovieRankAdapter(getActivity(), new ArrayList<String>());
        setListAdapter(mMovieRankAdapter);
    }

    private void updateRank() {
        FetchRankTask getRankTask = new FetchRankTask();
        getRankTask.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRank();
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

            ImageView imageView = (ImageView) view.findViewById(R.id.list_item_movie_rank_imageview);

            String url = "http://image.tmdb.org/t/p/w500/" + getItem(position);
            Picasso.with(getContext()).load(url).into(imageView);

            return view;
        }
    }

    public class FetchRankTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchRankTask.class.getSimpleName();

        private String[] getMovieRankDataFromJson(String movieRankStr)
                throws JSONException {

            final String OWM_RESULTS = "results";
            final String OWM_TITLE = "title";
            final String OWM_OVERVIEW = "overview";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_VOTE_AVERAGE = "vote_average";

            JSONObject movieRankJson = new JSONObject(movieRankStr);
            JSONArray movieRankArray = movieRankJson.getJSONArray(OWM_RESULTS);

            String[] resultStrs = new String[20];

            for(int i = 0; i < 20; i++) {
                JSONObject movieInfo = movieRankArray.getJSONObject(i);

                String title = movieInfo.getString(OWM_TITLE);
                String release_date = movieInfo.getString(OWM_RELEASE_DATE);
                String vote_ave = movieInfo.getString(OWM_VOTE_AVERAGE);
                String synopsis  = movieInfo.getString(OWM_OVERVIEW);
                String poster_path = movieInfo.getString(OWM_POSTER_PATH);

//                resultStrs[i] = "title: " + title + "\nrelease date: " + release_date + "\nvote average: " + vote_ave + "\nsynopsis: " + synopsis + "\nposter: " + poster_path;
                resultStrs[i] = poster_path;
            }

            return resultStrs;
        }


        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieRankStr = null;

            String rank_popurality = "popularity.desc";
            String rank_rate = "vote_average.desc";
            String sort_parameter = rank_popurality;

            try {
                final String TMD_BASE_URL_RANK = "https://api.themoviedb.org/3/discover/movie";
                final String SORT_PARAM = "sort_by";
                final String API_KEY = "api_key";

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String rank_type = prefs.getString(getString(R.string.pref_rank_key), getString(R.string.pref_rank_popular));

                if (rank_type.equals(getString(R.string.pref_rank_rate))) {
                    sort_parameter = rank_rate;
                }

                Uri builtUri = Uri.parse(TMD_BASE_URL_RANK).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sort_parameter)
                        .appendQueryParameter(API_KEY, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                movieRankStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieRankDataFromJson(movieRankStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String[] result) {
            if (result != null) {
                mMovieRankAdapter.clear();

                for(String movieSummary : result) {
                    mMovieRankAdapter.add(movieSummary);
                }
            }
        }
    }
}

