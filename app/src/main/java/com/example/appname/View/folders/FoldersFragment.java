package com.example.appname.View.folders;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.appname.Model.Image;
import com.example.appname.R;
import com.example.appname.View.dialogs.ConfirmDeleteDialog;
import com.example.appname.View.dialogs.NewFolderDialog;
import com.example.appname.View.dialogs.RenameDialog;
import com.example.appname.View.dialogs.UpdateItemDialog;
import com.example.appname.View.main.MainActivity;
import com.example.appname.Model.Explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FoldersFragment extends Fragment implements FolderAdapter.FolderListener,
        ImageAdapter.ImageListener,
        MainActivity.BackPressedListener,
        NewFolderDialog.DialogListener,
        UpdateItemDialog.DialogListener,
        ConfirmDeleteDialog.ConfirmListener,
        RenameDialog.RenameListener {

    //==============================================================================================
    //  ATTRIBUTES
    //==============================================================================================

    private Explorer mExplorer;
    private RecyclerView mFolderRecyclerView;
    private RecyclerView mImageRecyclerView;
    private FolderAdapter mFolderAdapter;
    private ImageAdapter mImageAdapter;
    private List<Image> mSelectedImages;
    private ActionMode mActionMode;


    //==============================================================================================
    //  CONSTRUCTORS
    //==============================================================================================

    public FoldersFragment() {
        // Required empty public constructor
    }

    //==============================================================================================
    //  STATE FUNCTIONS
    //==============================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_folders, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mExplorer = new Explorer(getContext());
        mSelectedImages = new ArrayList<>();
        initRecyclers();
        ((MainActivity)getActivity()).setBackListener(this);
    }



    //==============================================================================================
    //  INIT FUNCTIONS
    //==============================================================================================

    private void initRecyclers() {
        //folders recycler
        mFolderRecyclerView = getView().findViewById(R.id.folderRecyclerView);
        mFolderAdapter = new FolderAdapter(getContext(), mExplorer.getFolders(), this);
        mFolderRecyclerView.setAdapter(mFolderAdapter);
        int spanCount = getResources().getDisplayMetrics().widthPixels / (300);
        mFolderRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        //image recycler
        mImageRecyclerView = getView().findViewById(R.id.imageRecyclerView);
        mImageAdapter = new ImageAdapter(getContext(), mExplorer.getImages(), this);
        mImageRecyclerView.setAdapter(mImageAdapter);
        mImageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
    }

    //==============================================================================================
    //  FUNCTIONS
    //==============================================================================================


    //==============================================================================================
    //  LISTENERS FUNCTIONS
    //==============================================================================================

    //click on a folder
    @Override
    public void onClick(File file) {
        mExplorer.openFolder(file);
        mFolderAdapter.updateFolders(mExplorer.getFolders());
        mImageAdapter.updateImageList(mExplorer.getImages());
    }

    //long click on a folder
    @Override
    public void onLongClick(File file) {
        UpdateItemDialog.newInstance(file.getAbsolutePath(), this).show(getFragmentManager(), "update_item");
    }

    //click on the add button
    @Override
    public void onClickAdd() {
        NewFolderDialog dialog = new NewFolderDialog(this);
        dialog.show(getFragmentManager(), "new folder dialog");
    }

    //long click on an image
    @Override
    public boolean onLongClickImage(Image image) {
        if (mImageAdapter.isSelectionMode()) {
            mImageAdapter.setSelectionMode(false);
            mActionMode.finish();
        } else {
            mSelectedImages.clear();
            mImageAdapter.setSelectionMode(true);
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mCallback);
        }
        return true;
    }

    //select an image in multiple selection mode
    @Override
    public void onChecked(Image image, boolean isChecked) {
        if (isChecked) {
            if (!mSelectedImages.contains(image)){
                mSelectedImages.add(image);
            }
        } else {
            mSelectedImages.remove(image);
        }
        mActionMode.setTitle(mSelectedImages.size() + "/" + mImageAdapter.getItemCount() + " selected");
    }

    //click back button
    @Override
    public void onBackPressed() {
        if (mExplorer.goBack()) {
            mFolderAdapter.updateFolders(mExplorer.getFolders());
            mImageAdapter.updateImageList(mExplorer.getImages());
        }
    }

    @Override
    public void onNewFolder(String name) {
        mExplorer.newFolder(name);
        mFolderAdapter.addFolder(mExplorer.getCurrentPath() + File.separator + name);
    }

    //click on a folder option
    @Override
    public void onOptionClick(int which, String path) {
        switch (which) {
            case R.id.delete:
                ConfirmDeleteDialog.newInstance(path, this).show(getFragmentManager(), "confirm_delete");
                break;
            case R.id.rename:
                RenameDialog.newInstance(path, this).show(getFragmentManager(), "rename");
                break;
        }
    }

    @Override
    public void onConfirmDelete(String path) {
        mExplorer.deleteFolder(path);
        mFolderAdapter.removeFolder(path);
    }

    @Override
    public void onRename(String fromPath, String toPath) {
        mExplorer.renameFolder(fromPath, toPath);
        mFolderAdapter.renameFolder(fromPath, toPath);
    }

    //==============================================================================================
    //  ACTION MODE
    //==============================================================================================

    private ActionMode.Callback mCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.menu_multiple_image_selection, menu);
            actionMode.setTitle(mSelectedImages.size() + "/" + mImageAdapter.getItemCount() + " selected");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.move_option:
                    actionMode.finish();
                    break;
                case R.id.delete_option:
                    //Delete Selection
                    actionMode.finish();
                    break;
                case R.id.select_all_option:
                    mImageAdapter.setAllChecked(true);
                    break;
                default:
                    actionMode.finish();
                    break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mImageAdapter.setSelectionMode(false);
            mImageAdapter.setAllChecked(false);
            mActionMode = null;
        }
    };
}
