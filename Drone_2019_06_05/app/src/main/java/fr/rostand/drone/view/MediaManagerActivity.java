package fr.rostand.drone.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJICameraError;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.log.DJILog;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.FetchMediaTask;
import dji.sdk.media.FetchMediaTaskContent;
import dji.sdk.media.FetchMediaTaskScheduler;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;

import fr.rostand.drone.R;
import fr.rostand.drone.model.Drone;

public class MediaManagerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MediaManagerActivity";

    private Button mBackBtn, mDeleteBtn, mReloadBtn, mDownloadBtn;
    private RecyclerView listView;
    private FileListAdapter mListAdapter;
    private List<MediaFile> mediaFileList = new ArrayList<>();
    private MediaManager mMediaManager;
    private MediaManager.FileListState currentFileListState = MediaManager.FileListState.UNKNOWN;
    private FetchMediaTaskScheduler scheduler;
    private ProgressDialog mLoadingDialog;
    private ProgressDialog mDownloadDialog;
    File destDir = new File(Environment.getExternalStorageDirectory().getPath() + "/MediaManagerDemo/");
    private int currentProgress = -1;
    private ImageView mDisplayImageView;
    private int lastClickViewIndex =-1;
    private View lastClickView;

    //Listeners
    private View.OnClickListener itemViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            lastClickViewIndex = (int) (v.getTag());

            if (lastClickView != null && lastClickView != v) {
                lastClickView.setSelected(false);
            }
            v.setSelected(true);
            lastClickView = v;
        }
    };

    private View.OnClickListener ImgOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MediaFile selectedMedia = (MediaFile) v.getTag();
            if (selectedMedia != null && mMediaManager != null) {
                addMediaTask(selectedMedia);
            }
        }
    };

    private MediaManager.FileListStateListener updateFileListStateListener = state -> {
        currentFileListState = state;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_manager);

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initMediaManager();
    }

    @Override
    protected void onDestroy() {
        lastClickView = null;
        if (mMediaManager != null) {
            mMediaManager.stop(null);
            mMediaManager.removeFileListStateCallback(this.updateFileListStateListener);
            mMediaManager.exitMediaDownloading();
            if (scheduler!=null) {
                scheduler.removeAllTasks();
            }
        }

        if (Drone.getCameraInstance() != null) {
            Drone.getCameraInstance().setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, error -> {
                if (error != null){
                    setResultToToast("Set Shoot Photo Mode Failed" + error.getDescription());
                }
            });
        }

        if (mediaFileList != null) {
            mediaFileList.clear();
        }
        super.onDestroy();
    }

    void initUI() {
        //Init RecyclerView
        listView = findViewById(R.id.filelistView);
        listView.setLayoutManager(new LinearLayoutManager(this));

        //Init FileListAdapter
        mListAdapter = new FileListAdapter();
        listView.setAdapter(mListAdapter);

        //Init Loading Dialog
        mLoadingDialog = new ProgressDialog(MediaManagerActivity.this);
        mLoadingDialog.setMessage("Please wait");
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);

        //Init Download Dialog
        mDownloadDialog = new ProgressDialog(MediaManagerActivity.this);
        mDownloadDialog.setTitle("Downloading file");
        mDownloadDialog.setIcon(android.R.drawable.ic_dialog_info);
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setCancelable(true);
        mDownloadDialog.setOnCancelListener(dialog -> {
            if (mMediaManager != null) {
                mMediaManager.exitMediaDownloading();
            }
        });

        mBackBtn = findViewById(R.id.back_btn);
        mDeleteBtn = findViewById(R.id.delete_btn);
        mDownloadBtn = findViewById(R.id.download_btn);
        mReloadBtn = findViewById(R.id.reload_btn);
        mDisplayImageView = findViewById(R.id.imageView);
        mDisplayImageView.setVisibility(View.VISIBLE);

        mBackBtn.setOnClickListener(this);
        mDeleteBtn.setOnClickListener(this);
        mDownloadBtn.setOnClickListener(this);
        mReloadBtn.setOnClickListener(this);
        mDownloadBtn.setOnClickListener(this);
    }

    private void initMediaManager() {
        if (Drone.getProductInstance() == null) {
            mediaFileList.clear();
            mListAdapter.notifyDataSetChanged();
            Log.d(TAG, "Product disconnected");
            return;
        } else {
            if (null != Drone.getCameraInstance() && Drone.getCameraInstance().isMediaDownloadModeSupported()) {
                mMediaManager = Drone.getCameraInstance().getMediaManager();
                if (null != mMediaManager) {
                    mMediaManager.addUpdateFileListStateListener(this.updateFileListStateListener);
                    Drone.getCameraInstance().setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, error -> {
                        if (error == null) {
                            DJILog.e(TAG, "Set cameraMode success");
                            showProgressDialog();
                            getFileList();
                        } else {
                            setResultToToast("Set cameraMode failed");
                        }
                    });
                    if (mMediaManager.isVideoPlaybackSupported()) {
                        DJILog.e(TAG, "Camera support video playback!");
                    } else {
                        setResultToToast("Camera does not support video playback!");
                    }
                    scheduler = mMediaManager.getScheduler();
                }

            } else if (null != Drone.getCameraInstance()
                    && !Drone.getCameraInstance().isMediaDownloadModeSupported()) {
                setResultToToast("Media Download Mode not Supported");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn: {
                this.finish();
                break;
            }
            case R.id.delete_btn: {
                deleteFileByIndex(lastClickViewIndex);
                break;
            }
            case R.id.reload_btn: {
                getFileList();
                break;
            }
            case R.id.download_btn: {
                downloadFileByIndex(lastClickViewIndex);
                break;
            }
            default:
                break;
        }
    }

    private void showProgressDialog() {
        runOnUiThread(() -> {
            if (mLoadingDialog != null) {
                mLoadingDialog.show();
            }
        });
    }

    private void hideProgressDialog() {
        runOnUiThread(() -> {
            if (null != mLoadingDialog && mLoadingDialog.isShowing()) {
                mLoadingDialog.dismiss();
            }
        });
    }

    private void showDownloadProgressDialog() {
        if (mDownloadDialog != null) {
            runOnUiThread(() -> {
                    mDownloadDialog.incrementProgressBy(-mDownloadDialog.getProgress());
                    mDownloadDialog.show();
            });
        }
    }

    private void hideDownloadProgressDialog() {
        if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
            runOnUiThread(() -> {
                mDownloadDialog.dismiss();
            });
        }
    }

    private void setResultToToast(final String result) {
        runOnUiThread(() -> {
            Toast.makeText(MediaManagerActivity.this, result, Toast.LENGTH_SHORT).show();
        });
    }

    private void getFileList() {
        if (null != Drone.getCameraInstance()) {
            mMediaManager = Drone.getCameraInstance().getMediaManager();
        }
        if (mMediaManager != null) {
            if ((currentFileListState == MediaManager.FileListState.SYNCING) || (currentFileListState == MediaManager.FileListState.DELETING)){
                DJILog.e(TAG, "Media Manager is busy.");
            } else {
                mMediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, djiError -> {
                    if (null == djiError) {
                        hideProgressDialog();

                        //Reset data
                        if (currentFileListState != MediaManager.FileListState.INCOMPLETE) {
                            mediaFileList.clear();
                            lastClickViewIndex = -1;
                            lastClickView = null;
                        }

                        mediaFileList = mMediaManager.getSDCardFileListSnapshot();
                        Collections.sort(mediaFileList, new Comparator<MediaFile>() {
                            @Override
                            public int compare(MediaFile lhs, MediaFile rhs) {
                                if (lhs.getTimeCreated() < rhs.getTimeCreated()) {
                                    return 1;
                                } else if (lhs.getTimeCreated() > rhs.getTimeCreated()) {
                                    return -1;
                                }
                                return 0;
                            }
                        });
                        scheduler.resume(error -> {
                            if (error == null) {
                                getThumbnails();
                            }
                        });
                    } else {
                        hideProgressDialog();
                        setResultToToast("Get Media File List Failed:" + djiError.getDescription());
                    }
                });
            }
        }
    }

    private void getThumbnails() {
        if (mediaFileList.size() <= 0) {
            setResultToToast("No File info for downloading thumbnails");
            return;
        }
        for (int i = 0; i < mediaFileList.size(); i++) {
            getThumbnailByIndex(i);
        }
    }

    private FetchMediaTask.Callback taskCallback = new FetchMediaTask.Callback() {
        @Override
        public void onUpdate(MediaFile file, FetchMediaTaskContent option, DJIError error) {
            if (null == error) {
                if (option == FetchMediaTaskContent.PREVIEW) {
                    runOnUiThread(() -> {
                        mListAdapter.notifyDataSetChanged();
                    });
                }
                if (option == FetchMediaTaskContent.THUMBNAIL) {
                    runOnUiThread(() -> {
                        mListAdapter.notifyDataSetChanged();
                    });
                }
            } else {
                DJILog.e(TAG, "Fetch Media Task Failed" + error.getDescription());
            }
        }
    };

    private void getThumbnailByIndex(final int index) {
        FetchMediaTask task = new FetchMediaTask(mediaFileList.get(index), FetchMediaTaskContent.THUMBNAIL, taskCallback);
        scheduler.moveTaskToEnd(task);
    }

    private void addMediaTask(final MediaFile mediaFile) {
        final FetchMediaTaskScheduler scheduler = mMediaManager.getScheduler();
        final FetchMediaTask task =
                new FetchMediaTask(mediaFile, FetchMediaTaskContent.PREVIEW, new FetchMediaTask.Callback() {
                    @Override
                    public void onUpdate(final MediaFile mediaFile, FetchMediaTaskContent fetchMediaTaskContent, DJIError error) {
                        if (null == error) {
                            if (mediaFile.getPreview() != null) {
                                runOnUiThread(() -> {
                                    final Bitmap previewBitmap = mediaFile.getPreview();
                                    mDisplayImageView.setVisibility(View.VISIBLE);
                                    mDisplayImageView.setImageBitmap(previewBitmap);
                                });
                            } else {
                                setResultToToast("null bitmap!");
                            }
                        } else {
                            setResultToToast("fetch preview image failed: " + error.getDescription());
                        }
                    }
                });

        scheduler.resume(error -> {
            if (error == null) {
                scheduler.moveTaskToNext(task);
            } else {
                setResultToToast("resume scheduler failed: " + error.getDescription());
            }
        });
    }

    private void downloadFileByIndex(final int index){
        if (mediaFileList.size() <= index || index < 0) {
            return;
        }

        if ((mediaFileList.get(index).getMediaType() == MediaFile.MediaType.PANORAMA)
                || (mediaFileList.get(index).getMediaType() == MediaFile.MediaType.SHALLOW_FOCUS)) {
            return;
        }

        mediaFileList.get(index).fetchFileData(destDir, null, new DownloadListener<String>() {
            @Override
            public void onFailure(DJIError error) {
                hideDownloadProgressDialog();
                setResultToToast("Download File Failed" + error.getDescription());
                currentProgress = -1;
            }

            @Override
            public void onProgress(long total, long current) {
            }

            @Override
            public void onRateUpdate(long total, long current, long persize) {
                int tmpProgress = (int) (1.0 * current / total * 100);
                if (tmpProgress != currentProgress) {
                    mDownloadDialog.setProgress(tmpProgress);
                    currentProgress = tmpProgress;
                }
            }

            @Override
            public void onStart() {
                currentProgress = -1;
                showDownloadProgressDialog();
            }

            @Override
            public void onSuccess(String filePath) {
                hideDownloadProgressDialog();
                setResultToToast("Download File Success" + ":" + filePath);
                currentProgress = -1;
            }
        });
    }

    private void deleteFileByIndex(final int index) {
        ArrayList<MediaFile> fileToDelete = new ArrayList<>();
        if (mediaFileList.size() > index && index >= 0) {
            fileToDelete.add(mediaFileList.get(index));
            mMediaManager.deleteFiles(fileToDelete, new CommonCallbacks.CompletionCallbackWithTwoParam<List<MediaFile>, DJICameraError>() {
                @Override
                public void onSuccess(List<MediaFile> x, DJICameraError y) {
                    Log.d(TAG, "Delete file success");
                    runOnUiThread(() -> {
                        MediaFile file = mediaFileList.remove(index);

                        //Reset select view
                        lastClickViewIndex = -1;
                        lastClickView = null;

                        //Update recyclerView
                        mListAdapter.notifyItemRemoved(index);
                    });
                }

                @Override
                public void onFailure(DJIError error) {
                    setResultToToast("Delete file failed");
                }
            });
        }
    }

    /**
     * Inner classes
     */
    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail_img;
        TextView file_name;
        TextView file_type;
        TextView file_size;
        TextView file_time;

        public ItemHolder(View itemView) {
            super(itemView);
            this.thumbnail_img = itemView.findViewById(R.id.filethumbnail);
            this.file_name = itemView.findViewById(R.id.filename);
            this.file_type = itemView.findViewById(R.id.filetype);
            this.file_size = itemView.findViewById(R.id.fileSize);
            this.file_time = itemView.findViewById(R.id.filetime);
        }
    }

    private class FileListAdapter extends RecyclerView.Adapter<ItemHolder> {
        @Override
        public int getItemCount() {
            if (mediaFileList != null) {
                return mediaFileList.size();
            }
            return 0;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_file, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder mItemHolder, final int index) {
            final MediaFile mediaFile = mediaFileList.get(index);
            if (mediaFile != null) {
                if (mediaFile.getMediaType() != MediaFile.MediaType.MOV && mediaFile.getMediaType() != MediaFile.MediaType.MP4) {
                    mItemHolder.file_time.setVisibility(View.GONE);
                } else {
                    mItemHolder.file_time.setVisibility(View.VISIBLE);
                    mItemHolder.file_time.setText(mediaFile.getDurationInSeconds() + " s");
                }
                mItemHolder.file_name.setText(mediaFile.getFileName());
                mItemHolder.file_type.setText(mediaFile.getMediaType().name());
                mItemHolder.file_size.setText(mediaFile.getFileSize() + " Bytes");
                mItemHolder.thumbnail_img.setImageBitmap(mediaFile.getThumbnail());
                mItemHolder.thumbnail_img.setOnClickListener(ImgOnClickListener);
                mItemHolder.thumbnail_img.setTag(mediaFile);
                mItemHolder.itemView.setTag(index);

                if (lastClickViewIndex == index) {
                    mItemHolder.itemView.setSelected(true);
                } else {
                    mItemHolder.itemView.setSelected(false);
                }
                mItemHolder.itemView.setOnClickListener(itemViewOnClickListener);
            }
        }
    }
}
