package gun0912.tedbottompicker;

import android.net.Uri;
import android.support.v4.app.FragmentManager;
import io.reactivex.Single;

import java.util.List;

public class TedRxBottomPicker extends TedBottomSheetDialogFragment {

    public static Builder with(FragmentManager manager, String title, String done, String empty) {
        return new Builder(manager, title, done, empty);
    }

    public static class Builder extends BaseBuilder<Builder> {

        private Builder(FragmentManager manager, String title, String done, String empty) {
            super(manager, title, done, empty);
        }

        public Single<Uri> show() {
            return Single.create(emitter -> {
                onImageSelectedListener = emitter::onSuccess;
                onErrorListener = message -> emitter.onError(new Exception(message));
                create().show(fragmentManager);
            });
        }

        public Single<List<Uri>> showMultiImage() {
            return Single.create(emitter -> {
                onMultiImageSelectedListener = emitter::onSuccess;
                onErrorListener = message -> emitter.onError(new Exception(message));
                create().show(fragmentManager);
            });
        }
    }

}
