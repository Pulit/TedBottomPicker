package gun0912.tedbottompicker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import gun0912.tedbottompicker.adapter.ImageGalleryAdapter;

public class CustomBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ted";
    static final int REQ_CODE_CAMERA = 1;
    static final int REQ_CODE_GALLERY = 2;
    ImageGalleryAdapter imageGalleryAdapter;
    Builder builder;
    private RecyclerView rc_gallery;
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {

            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };
    private Uri cameraImageUri;

    public void show(FragmentManager fragmentManager) {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(this, getTag());
        ft.commitAllowingStateLoss();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onViewCreated(View contentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(contentView, savedInstanceState);


    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_dialog_content_view, null);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            if(builder.peekHeight>0){
                // ((BottomSheetBehavior) behavior).setPeekHeight(1500);
                ((BottomSheetBehavior) behavior).setPeekHeight(builder.peekHeight);
            }

        }

        rc_gallery = (RecyclerView) contentView.findViewById(R.id.rc_gallery);
        setRecyclerView();
    }

    private void setRecyclerView() {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rc_gallery.setLayoutManager(gridLayoutManager);


        rc_gallery.addItemDecoration(new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(), builder.spacing, false));

        imageGalleryAdapter = new ImageGalleryAdapter(
                getActivity()
                , builder.maxCount
                , builder.cameraTileDrawable
                , builder.galleryTileDrawable
                , builder.showCamera
                , builder.showGallery);
        rc_gallery.setAdapter(imageGalleryAdapter);
        imageGalleryAdapter.setOnItemClickListener(new ImageGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                ImageGalleryAdapter.PickerTile pickerTile = imageGalleryAdapter.getItem(position);

                switch (pickerTile.getTileType()) {
                    case ImageGalleryAdapter.PickerTile.CAMERA:
                        startCameraIntent();
                        break;
                    case ImageGalleryAdapter.PickerTile.GALLERY:
                        startGalleryIntent();
                        break;
                    case ImageGalleryAdapter.PickerTile.IMAGE:
                        complete(pickerTile.getImageUri());

                        break;

                    default:
                        errorMessage();
                }

            }
        });
    }

    private void complete(Uri uri) {
        //uri = Uri.parse(uri.toString());
        builder.onImageSelectedListener.onImageSelected(uri);
        dismiss();
    }

    private void startCameraIntent() {
        Intent cameraInent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraInent.resolveActivity(getActivity().getPackageManager()) == null) {
            errorMessage("This Application do not have Camera Application");
            return;
        }

        File imageFile = getImageFile();
        cameraInent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(cameraInent, REQ_CODE_CAMERA);

    }

    private void startGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(getActivity().getPackageManager()) == null) {
            errorMessage("This Application do not have Gallery Application");
            return;
        }

        startActivityForResult(galleryIntent, REQ_CODE_GALLERY);

    }

    private File getImageFile() {
        // Create an image file name
        File imageFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);


            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );


            // Save a file: path for use with ACTION_VIEW intents
            cameraImageUri = Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage("Could not create imageFile for camera");
        }


        return imageFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImageUri = null;
            if (requestCode == REQ_CODE_GALLERY && data != null) {
                selectedImageUri = data.getData();
                if (selectedImageUri == null) {
                    errorMessage();
                }
            } else if (requestCode == REQ_CODE_CAMERA) {
                // Do something with imagePath
                selectedImageUri = cameraImageUri;
            }

            if (selectedImageUri != null) {
                complete(selectedImageUri);
            } else {
                errorMessage();
            }
        }

    }

    private void errorMessage() {
        errorMessage(null);
    }

    private void errorMessage(String message) {
        String errorMessage = message == null ? "Something wrong." : message;

        if (builder.onErrorListener == null) {
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            builder.onErrorListener.onError(errorMessage);
        }
    }


    public interface OnImageSelectedListener {
        void onImageSelected(Uri uri);
    }

    public interface OnErrorListener {
        void onError(String message);
    }

    public static class Builder {

        Context context;
        int maxCount = 25;
        Drawable cameraTileDrawable;
        Drawable galleryTileDrawable;

        int spacing = 1;
        OnImageSelectedListener onImageSelectedListener;
        OnErrorListener onErrorListener;
        boolean showCamera = true;
        boolean showGallery = true;
        int peekHeight=-1;

        public Builder(@NonNull Context context) {

            this.context = context;

            setCameraTile(R.drawable.ic_camera);
            setGalleryTile(R.drawable.ic_gallery);
            setSpacingResId(R.dimen.grid_layout_margin);
        }

        public Builder setMaxCount(int maxCount) {
            this.maxCount = maxCount;
            return this;
        }

        public Builder setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
            this.onImageSelectedListener = onImageSelectedListener;
            return this;
        }

        public Builder setOnErrorListener(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
            return this;
        }

        public Builder showCameraTile(boolean showCamera) {
            this.showCamera = showCamera;
            return this;
        }

        public Builder setCameraTile(@DrawableRes int cameraTileResId) {
            setCameraTile(ContextCompat.getDrawable(context, cameraTileResId));
            return this;
        }

        public Builder setCameraTile(Drawable cameraTileDrawable) {
            this.cameraTileDrawable = cameraTileDrawable;
            return this;
        }

        public Builder showGalleryTile(boolean showGallery) {
            this.showGallery = showGallery;
            return this;
        }

        public Builder setGalleryTile(@DrawableRes int galleryTileResId) {
            setGalleryTile(ContextCompat.getDrawable(context, galleryTileResId));
            return this;
        }

        public Builder setGalleryTile(Drawable galleryTileDrawable) {
            this.galleryTileDrawable = galleryTileDrawable;
            return this;
        }

        public Builder setSpacing(int spacing) {
            this.spacing = spacing;
            return this;
        }

        public Builder setSpacingResId(@DimenRes int dimenResId) {
            this.spacing = context.getResources().getDimensionPixelSize(dimenResId);
            return this;
        }

        public Builder setPeekHeight(int peekHeight) {
            this.peekHeight = peekHeight;
            return this;
        }

        public Builder setPeekHeightResId(@DimenRes int dimenResId) {
            this.peekHeight = context.getResources().getDimensionPixelSize(dimenResId);
            return this;
        }

        public CustomBottomSheetDialogFragment create() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission. Did you remember to request it first?");
            }

            if (onImageSelectedListener == null) {
                throw new RuntimeException("You have to setOnImageSelectedListener() for receive selected Uri");
            }

            CustomBottomSheetDialogFragment customBottomSheetDialogFragment = new CustomBottomSheetDialogFragment();

            customBottomSheetDialogFragment.builder = this;
            return customBottomSheetDialogFragment;
        }

    }


}