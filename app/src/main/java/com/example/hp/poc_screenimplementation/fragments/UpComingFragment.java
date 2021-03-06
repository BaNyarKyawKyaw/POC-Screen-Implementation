package com.example.hp.poc_screenimplementation.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hp.poc_screenimplementation.R;
import com.example.hp.poc_screenimplementation.adapters.UpComingAdapter;
import com.example.hp.poc_screenimplementation.components.SmartRecyclerView;
import com.example.hp.poc_screenimplementation.components.SmartScrollListener;
import com.example.hp.poc_screenimplementation.data.model.PopularMovieModel;
import com.example.hp.poc_screenimplementation.data.persistence.PopularMovieContract;
import com.example.hp.poc_screenimplementation.data.vo.PopularMovieVO;
import com.example.hp.poc_screenimplementation.events.PopularMoviesEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by HP on 11/10/2017.
 */

public class UpComingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.rv_movie_list_upcoming)
    SmartRecyclerView rvMovieList;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private UpComingAdapter upComingAdapter;
    private SmartScrollListener mSmartScrollListener;

    private static final int POPULAR_MOVIE_LOADER_ID = 1001;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);
        ButterKnife.bind(this, view);

        upComingAdapter = new UpComingAdapter(getContext());
        rvMovieList.setAdapter(upComingAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvMovieList.setLayoutManager(linearLayoutManager);

        mSmartScrollListener = new SmartScrollListener(new SmartScrollListener.OnSmartScrollListener() {
            @Override
            public void onListEndReach() {
                PopularMovieModel.getObjInstance().loadMoreMovies(getContext());
            }
        });
        rvMovieList.addOnScrollListener(mSmartScrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PopularMovieModel.getObjInstance().forceRefreshNews(getContext());
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(POPULAR_MOVIE_LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        List<PopularMovieVO> newsList = PopularMovieModel.getObjInstance().getPopularMovie();
        if (newsList != null) {
            upComingAdapter.setNewData(newsList);
        } else {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPopularMovieLoaded(PopularMoviesEvent.PopularMoviesLoaded event) {
//        upComingAdapter.appendNewData(event.getPopularMovie());
//        swipeRefreshLayout.setRefreshing(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorInvokingAPI(PopularMoviesEvent.ErrorInvokingAPIEvent event) {
        Snackbar.make(rvMovieList, event.getErrorMsg(), Snackbar.LENGTH_INDEFINITE).show();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                PopularMovieContract.PopularMovieEntry.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            List<PopularMovieVO> popularMovieList = new ArrayList<>();

            do {
                PopularMovieVO popularMovie = PopularMovieVO.parseFromCursor(data);
                popularMovieList.add(popularMovie);
            } while (data.moveToNext());
            upComingAdapter.appendNewData(popularMovieList);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
