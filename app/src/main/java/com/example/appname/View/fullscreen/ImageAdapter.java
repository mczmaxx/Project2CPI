package com.example.appname.View.fullscreen;

import android.content.Context;
import android.transition.TransitionManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.appname.Model.Image;
import com.example.appname.Model.Note;
import com.example.appname.R;
import com.example.appname.ViewModel.ImageViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends PagerAdapter
        implements View.OnTouchListener,
        GestureDetector.OnGestureListener {

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

    private AppCompatActivity mContext;
    private List<Image> mImages;
    private ImageListener mListener;
    private GestureDetector mGestureDetector;
    private ConstraintLayout mLayout;
    private ImageViewModel mViewModel;
    private List<Note> mNotes;

    private ConstraintLayout mNoteLayout;
    private ConstraintSet showText = new ConstraintSet();
    private ConstraintSet hideText = new ConstraintSet();
    private ConstraintSet showBubbles = new ConstraintSet();
    private boolean mBubblesVisible = false;

    public ImageAdapter(AppCompatActivity context, List<Image> images, ImageListener listener) {
        mContext = context;
        mImages = images;
        mListener = listener;
        mGestureDetector = new GestureDetector(mContext, this);
        mLayout = context.findViewById(R.id.display_parent);
        mNotes = new ArrayList<>();
        mViewModel = ViewModelProviders.of(mContext).get(ImageViewModel.class);
    }

    public void setImages(List<Image> images) {
        mImages = images;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (mImages == null) ? 0 : mImages.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        String path = mImages.get(position).getPath();
        mContext.setTitle(path.substring(path.lastIndexOf(File.separator) + 1));
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Glide
                .with(mContext)
                .load(mImages.get(position).getPath())
                .into(imageView);
        mViewModel.getNotes(mImages.get(position).getRowId()).observe(mContext, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                mNotes = notes;
            }
        });
        if (!mNotes.isEmpty()) {
            int noteIndice = 1;
            for (final Note note : mNotes) {
                View viewNote = mContext.getLayoutInflater().inflate(R.layout.note_view_0, null);
                ImageView bubble = viewNote.findViewById(R.id.bubble);
                TextView noteTexte = viewNote.findViewById(R.id.noteText);
                noteTexte.setText(note.getText());
                mNoteLayout = viewNote.findViewById(R.id.note_layout_0);
                final ConstraintLayout noteLayout = viewNote.findViewById(R.id.note_layout_0);
                hideText.clone(noteLayout);
                showText.clone(mContext, R.layout.note_view_1);
                showBubbles.clone(mContext, R.layout.note_view_2);
                bubble.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TransitionManager.beginDelayedTransition(noteLayout);
                        if (!note.isTextVisible()) {
                            showText.applyTo(noteLayout);
                            note.setTextVisible(true);
                        } else {
                            hideText.applyTo(noteLayout);
                            note.setTextVisible(false);
                        }
                    }
                });
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "show notes", Toast.LENGTH_SHORT).show();
                        TransitionManager.beginDelayedTransition(noteLayout);
                        if (!mBubblesVisible) {
                            showBubbles.applyTo(noteLayout);
                            mBubblesVisible = true;
                        } else {
                            hideText.applyTo(noteLayout);
                            mBubblesVisible = false;
                        }
                    }
                });
                viewNote.setX(note.getX());
                viewNote.setY(note.getY());
                mLayout.addView(viewNote);
                noteIndice++;
            }
        }
        imageView.setOnTouchListener(this);
        container.addView(imageView, 0);
        return imageView;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


    public void removeImage(Image image) {
        mImages.remove(image);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    //==============================================================================================
    //  LISTENERS FUNCTIONS
    //==============================================================================================

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.setOnDoubleTapListener(mDoubleTapListener);
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        mListener.onLongClickImage(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private GestureDetector.OnDoubleTapListener mDoubleTapListener = new GestureDetector.OnDoubleTapListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mListener.onClickImage();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //TransitionManager.beginDelayedTransition(noteLayout);
            //if (!mBubblesVisible) {
            //    showBubbles.applyTo(noteLayout);
            //    mBubblesVisible = true;
            //} else {
            //    hideText.applyTo(noteLayout);
            //    mBubblesVisible = false;
            //}
            mListener.onDoubleClick();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    };

    //==============================================================================================
    //  INIT FUNCTIONS
    //==============================================================================================

    public interface ImageListener {

        void onClickImage();

        void onDoubleClick();

        void onLongClickImage(MotionEvent event);
    }
}
