package gun0912.tedbottompicker;

import android.support.v4.app.FragmentManager;

public class TedBottomPicker extends TedBottomSheetDialogFragment {

    public static Builder with(FragmentManager manager, String title, String done, String empty) {
        return new Builder(manager, title, done, empty);
    }

    public static class Builder extends BaseBuilder<Builder> {

        private Builder(FragmentManager manager, String title, String done, String empty) {
            super(manager, title, done, empty);
        }

        public void show(OnImageSelectedListener onImageSelectedListener) {
            this.onImageSelectedListener = onImageSelectedListener;
            create().show(fragmentManager);
        }

        public void showMultiImage(OnMultiImageSelectedListener onMultiImageSelectedListener) {
            this.onMultiImageSelectedListener = onMultiImageSelectedListener;
            create().show(fragmentManager);
        }
    }


}
