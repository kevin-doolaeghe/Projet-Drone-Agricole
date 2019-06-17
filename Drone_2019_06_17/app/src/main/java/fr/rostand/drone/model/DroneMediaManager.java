package fr.rostand.drone.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import dji.sdk.media.DownloadListener;
import dji.sdk.media.FetchMediaTask;
import dji.sdk.media.FetchMediaTaskContent;
import dji.sdk.media.FetchMediaTaskScheduler;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.thirdparty.afinal.core.AsyncTask;
import fr.rostand.drone.R;

public class DroneMediaManager {
    private static final String TAG = "DroneMediaManager";

    private Context mContext;
    private static DroneMediaManager instance;

    // Drone variables
    private MediaManager mMediaManager;
    private List<MediaFile> mMediaFileList = new ArrayList<>();

    private MediaManager.FileListState mCurrentFileListState = MediaManager.FileListState.UNKNOWN;
    private FetchMediaTaskScheduler mFetchMediaTaskScheduler;

    private MediaManager.FileListStateListener mUpdateFileListStateListener = state -> {
        mCurrentFileListState = state;
    };

    private MutableLiveData<List<Bitmap>> mImageList = new MutableLiveData<>();

    /**
     * Constructor
     */
    private DroneMediaManager(Context context) {
        this.mContext = context;
    }

    /**
     * Singleton pattern
     */
    public static DroneMediaManager getInstance(Context context) {
        if (instance == null) {
            instance = new DroneMediaManager(context);
        }
        return instance;
    }

    /**
     * Getters and setters
     */
    public LiveData<List<Bitmap>> getImageList() {
        return mImageList;
    }

    /**
     * Init drone's variables
     */
    public void initComponents() {
        mImageList.postValue(new ArrayList<>());

        if (Drone.getProductInstance() == null) {
            mMediaFileList.clear();
        } else {
            if (null != Drone.getCameraInstance() && Drone.getCameraInstance().isMediaDownloadModeSupported()) {
                mMediaManager = Drone.getCameraInstance().getMediaManager();
                if (null != mMediaManager) {
                    mMediaManager.addUpdateFileListStateListener(this.mUpdateFileListStateListener);
                    Drone.getCameraInstance().setMode(SettingsDefinitions.CameraMode.MEDIA_DOWNLOAD, djiError -> {
                        if (djiError == null) {
                            Log.d(TAG, "Set camera mode success");
                            getFileList();
                        } else {
                            Log.d(TAG, "Set camera mode failed");
                        }
                    });

                    mFetchMediaTaskScheduler = mMediaManager.getScheduler();
                }
            }
        }
    }

    /**
     * Destroy drone's variables
     */
    public void destroyComponents() {
        if (mMediaManager != null) {
            mMediaManager.stop(null);
            mMediaManager.removeFileListStateCallback(this.mUpdateFileListStateListener);
            mMediaManager.exitMediaDownloading();
            if (mFetchMediaTaskScheduler != null) {
                mFetchMediaTaskScheduler.removeAllTasks();
            }
        }

        if (Drone.getCameraInstance() != null) {
            Drone.getCameraInstance().setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, error -> {
                if (error != null){
                    Log.d(TAG, "Set shoot photo mode failed : " + error.getDescription());
                }
            });
        }

        if (mMediaFileList != null) {
            mMediaFileList.clear();
        }
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT).show());
    }

    /**
     * Get file list from drone
     */
    public void getFileList() {
        if (null != Drone.getCameraInstance()) {
            mMediaManager = Drone.getCameraInstance().getMediaManager();
        }

        if (mMediaManager != null) {
            if ((mCurrentFileListState == MediaManager.FileListState.SYNCING) || (mCurrentFileListState == MediaManager.FileListState.DELETING)){
                Log.d(TAG, "Media Manager is busy.");
            } else {
                mMediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, djiError -> {
                    if (null == djiError) {
                        //Reset data
                        if (mCurrentFileListState != MediaManager.FileListState.INCOMPLETE) {
                            mMediaFileList.clear();
                        }

                        mMediaFileList = mMediaManager.getSDCardFileListSnapshot();
                        Log.d(TAG, mMediaFileList.size() + " file(s) loaded !");

                        // Trie dans l'ordre chronologique
                        Collections.sort(mMediaFileList, new Comparator<MediaFile>() {
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

                        // Get icon list and image list
                        getThumbnailList();
                        getPreviewList();
                    } else {
                        Log.d(TAG, "Get media file list failed : " + djiError.getDescription());
                    }
                });
            }
        }
    }

    /**
     * Get icon list
     */
    private void getThumbnailList() {
        for (MediaFile mediaFile : mMediaFileList) {
            getThumbnail(mediaFile);
        }
    }

    private void getThumbnail(MediaFile mediaFile) {
        FetchMediaTask task = new FetchMediaTask(mediaFile, FetchMediaTaskContent.THUMBNAIL, new FetchMediaTask.Callback() {
            @Override
            public void onUpdate(MediaFile file, FetchMediaTaskContent fetchMediaTaskContent, DJIError error) {
                if (error != null) {
                    Log.d(TAG, "Fetch thumbnail image failed : " + error.getDescription());
                }
            }
        });

        mFetchMediaTaskScheduler.resume(error -> {
            if (error == null) {
                mFetchMediaTaskScheduler.moveTaskToEnd(task);
            } else {
                Log.d(TAG, "Resume scheduler failed : " + error.getDescription());
            }
        });
    }

    /**
     * Get image list
     */
    public void getPreviewList() {
        for (MediaFile mediaFile : mMediaFileList) {
            mImageList.postValue(new ArrayList<>());
            getPreview(mediaFile);
        }
    }

    private void getPreview(MediaFile mediaFile) {
        FetchMediaTask task = new FetchMediaTask(mediaFile, FetchMediaTaskContent.PREVIEW, new FetchMediaTask.Callback() {
            @Override
            public void onUpdate(MediaFile file, FetchMediaTaskContent fetchMediaTaskContent, DJIError error) {
                if (error != null) {
                    Log.d(TAG, "Fetch preview image failed : " + error.getDescription());
                } else {
                    if (file.getPreview() != null) {
                        Bitmap preview = file.getPreview();

                        List<Bitmap> imageList = mImageList.getValue();
                        imageList.add(preview);
                        mImageList.postValue(imageList);
                    } else {
                        Log.d(TAG, "Null bitmap !");
                    }
                }
            }
        });

        mFetchMediaTaskScheduler.resume(error -> {
            if (error == null) {
                mFetchMediaTaskScheduler.moveTaskToNext(task);
            } else {
                Log.d(TAG, "Resume scheduler failed : " + error.getDescription());
            }
        });
    }

    /**
     * Delete drone file list
     */
    public void deleteFileList() {
        if (mMediaFileList.size() > 0) {
            mMediaManager.deleteFiles(mMediaFileList, new CommonCallbacks.CompletionCallbackWithTwoParam<List<MediaFile>, DJICameraError>() {
                @Override
                public void onSuccess(List<MediaFile> mediaFiles, DJICameraError djiCameraError) {
                    showToast(mContext.getString(R.string.msg_files_deleted));
                    mMediaFileList.clear();
                }

                @Override
                public void onFailure(DJIError djiError) {
                    showToast(mContext.getString(R.string.error_delete_file_list));
                }
            });
        }
    }
}
