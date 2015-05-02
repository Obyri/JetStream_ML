/*
 * JetStream ML
 * ListViewController.java
 *     Copyright (C) 2015  Reice Robinson
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package obyriasura.jetstreamml.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import obyriasura.jetstreamml.R;
import obyriasura.jetstreamml.models.item.AbstractItemModel;
import obyriasura.jetstreamml.models.listAdapter.RowViewModel;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link ListViewController.FragmentEventListener}
 * interface.
 */
public class ListViewController extends Fragment implements AbsListView.OnItemClickListener {

    /**
     * Object listening for callbacks from this one, generally the main Activity.
     */
    private FragmentEventListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private WeakReference<AbsListView> mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<RowViewModel> mAdapter;

    /**
     * The ArrayList which is used to set and reset the ArrayAdapter items.
     */
    private ArrayList<RowViewModel> mArrayList = new ArrayList<>();

    /**
     * The parent item to items in the list.
     */
    private AbstractItemModel parentItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListViewController() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // set this FragmentEventListener
            mListener = (FragmentEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain instances of the fragments for easy config change.
        setRetainInstance(true);
        if (savedInstanceState == null) {
            mAdapter = new ListAdapterController<>(getActivity(),
                    R.layout.row_layout, mArrayList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_item, container, false);

        // Set the adapter
        mListView = new WeakReference<AbsListView>((ListView) view.findViewById(android.R.id.list));
        mListView.get().setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.get().setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onDetach() {
        mListener = null;
        Log.d(getString(R.string.app_name), "onDetach");
        super.onDetach();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // About to browse this item. show loading anim.
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onItemSelectListener(mAdapter.getItem(position).getItemModel());
        }
    }

    /**
     * Returns the Array Adapter for this fragment.
     *
     * @return the ArrayAdapter.
     */
    public ArrayAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Set and refresh the Array Adapter for this fragment using and ArrayList of AbstractItemModel objects.
     *
     * @param arrayList the arrayList containing objects to set the list.
     */
    public void setAdapter(ArrayList<AbstractItemModel> arrayList) {
        if (arrayList.size() <= 0) {
            if (mAdapter != null)
                mAdapter.clear();
            return;
        }

        // Put Found Item into viewmodel
        ArrayList<RowViewModel> viewModels = new ArrayList<>();
        for (AbstractItemModel item : arrayList) {
            viewModels.add(new RowViewModel(item.toString(), item.getDescription(), this.getActivity(), item, item.getItemType()));
        }
        if (mAdapter != null) {
            mAdapter.clear();
            mArrayList = viewModels;
            mAdapter.addAll(mArrayList);
        } else {
            mArrayList = viewModels;
        }
    }

    public AbstractItemModel getParentItem() {
        return parentItem;
    }

    public void setParentItem(AbstractItemModel parentItem) {
        this.parentItem = parentItem;
    }

    /**
     * Interface for content holder to implement for callbacks
     */
    public interface FragmentEventListener {
        void onItemSelectListener(Object object);
    }

}
