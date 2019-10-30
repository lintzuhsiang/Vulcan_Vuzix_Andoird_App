//on subline
//
//
//import android.Manifest;
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.ImageFormat;
//import android.graphics.Paint;
//import android.graphics.SurfaceTexture;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.CameraMetadata;
//import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.TotalCaptureResult;
//import android.hardware.camera2.params.StreamConfigurationMap;
//import android.media.Image;
//import android.media.ImageReader;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.util.Size;
//import android.util.SparseIntArray;
//import android.view.Surface;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//
//import com.example.myapplication.R;
//import com.microsoft.projectoxford.face.FaceServiceClient;
//import com.microsoft.projectoxford.face.FaceServiceRestClient;
//import com.microsoft.projectoxford.face.contract.Emotion;
//import com.microsoft.projectoxford.face.contract.Face;
//import com.microsoft.projectoxford.face.contract.FaceRectangle;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//public class AndroidCameraApi extends AppCompatActivity {
//    private static final String TAG = "AndroidCameraApi";
//    private Button takePictureButton;
//    private TextureView textureView;
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//    private String cameraId;
//    protected CameraDevice cameraDevice;
//    protected CameraCaptureSession cameraCaptureSessions;
//    protected CaptureRequest captureRequest;
//    protected CaptureRequest.Builder captureRequestBuilder;
//    private Size imageDimension;
//    private ImageReader imageReader;
//    private File file;
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//    private boolean mFlashSupported;
//    private Handler mBackgroundHandler;
//    private HandlerThread mBackgroundThread;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_android_camera_api);
//        textureView = (TextureView) findViewById(R.id.texture);
//        assert textureView != null;
//        textureView.setSurfaceTextureListener(textureListener);
//
//        takePictureButton = (Button) findViewById(R.id.btn_takepicture);
//        assert takePictureButton != null;
//        takePictureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                takePicture();
//            }
//        });
//    }
//    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            //open your camera here
//            openCamera();
//        }
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            // Transform you image captured size according to the surface width and height
//        }
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//            return false;
//        }
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        }
//    };
//    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(CameraDevice camera) {
//            //This is called when the camera is open
//            Log.e(TAG, "onOpened");
//            cameraDevice = camera;
//            createCameraPreview();
//        }
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            cameraDevice.close();
//        }
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//    };
//    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
//        @Override
//        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//            super.onCaptureCompleted(session, request, result);
//            Toast.makeText(AndroidCameraApi.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
//            createCameraPreview();
//        }
//    };
//
//    final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
//        @Override
//        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//            super.onCaptureCompleted(session, request, result);
//            Toast.makeText(AndroidCameraApi.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
//            createCameraPreview();
//        }
//    };
//
//    protected void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread("Camera Background");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//    protected void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try {
//            mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//    protected void takePicture() {
//        if(null == cameraDevice) {
//            Log.e(TAG, "cameraDevice is null");
//            return;
//        }
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
//            Size[] jpegSizes = null;
//            if (characteristics != null) {
//                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
//            }
//            int width = 640;
//            int height = 480;
//            if (jpegSizes != null && 0 < jpegSizes.length) {
//                width = jpegSizes[0].getWidth();
//                height = jpegSizes[0].getHeight();
//            }
//
//            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
//            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
//            outputSurfaces.add(reader.getSurface());
//            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
//            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//            // Orientation
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//
//            final File file = new File(Environment.getExternalStorageDirectory()+"/pic.jpg");
//
//            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    Image image = null;
//                    try {
//                        image = reader.acquireLatestImage();
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                        byte[] bytes = new byte[buffer.capacity()];
//                        buffer.get(bytes);
//                        detectAndFrame(bytes)
//                        save(bytes);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (image != null) {
//                            image.close();
//                        }
//                    }
//                }
//                private void save(byte[] bytes) throws IOException {
//                    OutputStream output = null;
//                    try {
//                        output = new FileOutputStream(file);
//                        output.write(bytes);
//                    } finally {
//                        if (null != output) {
//                            output.close();
//                        }
//                    }
//                }
//
//
//
//
//            };
//
//            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
//
//            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
//                @Override
//                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(AndroidCameraApi.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
//                    createCameraPreview();
//                }
//            };
//            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(CameraCaptureSession session) {
//                    try {
//                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//                @Override
//                public void onConfigureFailed(CameraCaptureSession session) {
//                }
//            }, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//    protected void createCameraPreview() {
//        try {
//            SurfaceTexture texture = textureView.getSurfaceTexture();
//            assert texture != null;
//            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
//            Surface surface = new Surface(texture);
//            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            captureRequestBuilder.addTarget(surface);
//            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    //The camera is already closed
//                    if (null == cameraDevice) {
//                        return;
//                    }
//                    // When the session is ready, we start displaying the preview.
//                    cameraCaptureSessions = cameraCaptureSession;
//                    updatePreview();
//                }
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Toast.makeText(AndroidCameraApi.this, "Configuration change", Toast.LENGTH_SHORT).show();
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//    private void openCamera() {
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        Log.e(TAG, "is camera open");
//        try {
//            cameraId = manager.getCameraIdList()[0];
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            assert map != null;
//            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
//            // Add permission for camera and let user grant the permission
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(AndroidCameraApi.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
//                return;
//            }
//            manager.openCamera(cameraId, stateCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        Log.e(TAG, "openCamera X");
//    }
//    protected void updatePreview() {
//        if(null == cameraDevice) {
//            Log.e(TAG, "updatePreview error, return");
//        }
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        try {
//            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//    private void closeCamera() {
//        if (null != cameraDevice) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//        if (null != imageReader) {
//            imageReader.close();
//            imageReader = null;
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                // close the app
//                Toast.makeText(AndroidCameraApi.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }
//    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.e(TAG, "onResume");
//        startBackgroundThread();
//        if (textureView.isAvailable()) {
//            openCamera();
//        } else {
//            textureView.setSurfaceTextureListener(textureListener);
//        }
//    }
//    @Override
//    protected void onPause() {
//        Log.e(TAG, "onPause");
//        //closeCamera();
//        stopBackgroundThread();
//        super.onPause();
//    }
//}
//
//
//
//    private void detectAndFrame(final Bitmap imageBitmap) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//        ByteArrayInputStream inputStream =
//                new ByteArrayInputStream(outputStream.toByteArray());
//
//        AsyncTask<InputStream, String, Face[]> detectTask =
//                new AsyncTask<InputStream, String, Face[]>() {
//                    String exceptionMessage = "";
//
//                    @Override
//                    protected Face[] doInBackground(InputStream... params) {
//                        try {
//                            publishProgress("Detecting...");
//                            Face[] result = faceServiceClient.detect(
//                                    params[0],
//                                    true,         // returnFaceId
//                                    false,        // returnFaceLandmarks
//                                    null          // returnFaceAttributes:
//                                    /* new FaceServiceClient.FaceAttributeType[] {
//                                        FaceServiceClient.FaceAttributeType.Age,
//                                        FaceServiceClient.FaceAttributeType.Gender }
//                                    */
//                            );
//                            if (result == null){
//                                publishProgress(
//                                        "Detection Finished. Nothing detected");
//                                return null;
//                            }
//                            publishProgress(String.format(
//                                    "Detection Finished. %d face(s) detected",
//                                    result.length));
//                            return result;
//                        } catch (Exception e) {
//                            exceptionMessage = String.format(
//                                    "Detection failed: %s", e.getMessage());
//                            return null;
//                        }
//                    }
//
//                    @Override
//                    protected void onPreExecute() {
//                        //TODO: show progress dialog
//                        detectionProgressDialog.show();
//                    }
//                    @Override
//                    protected void onProgressUpdate(String... progress) {
//                        //TODO: update progress
//                        detectionProgressDialog.setMessage(progress[0]);
//                    }
//                    @Override
//                    protected void onPostExecute(Face[] result) {
//                        //TODO: update face frames
//                        detectionProgressDialog.dismiss();
//
//                        if(!exceptionMessage.equals("")){
//                            showError(exceptionMessage);
//                        }
//                        if (result == null) return;
//
//                        ImageView imageView = findViewById(R.id.imageView1);
//                        imageView.setImageBitmap(
//                                drawFaceRectanglesOnBitmap(imageBitmap, result));
//                        imageBitmap.recycle();
//                    }
//                };
//
//        detectTask.execute(inputStream);
//    }






































///on android

//
//
//
//    package com.example.myapplication;
//
//            import androidx.annotation.NonNull;
//            import androidx.appcompat.app.AppCompatActivity;
//            import androidx.core.app.ActivityCompat;
//
//            import android.Manifest;
//            import android.app.AlertDialog;
//            import android.app.ProgressDialog;
//            import android.content.Context;
//            import android.content.DialogInterface;
//            import android.content.Intent;
//            import android.content.pm.PackageManager;
//            import android.graphics.Bitmap;
//            import android.graphics.BitmapFactory;
//            import android.graphics.Canvas;
//            import android.graphics.Color;
//            import android.graphics.ImageFormat;
//            import android.graphics.Paint;
//            import android.graphics.SurfaceTexture;
//            import android.graphics.drawable.BitmapDrawable;
//            import android.hardware.camera2.params.StreamConfigurationMap;
//            import android.media.Image;
//            import android.media.ImageReader;
//            import android.os.AsyncTask;
//            import android.os.Bundle;
//            import android.os.Environment;
//            import android.os.HandlerThread;
//            import android.os.Handler;
//            import android.provider.MediaStore;
//            import android.util.Log;
//            import android.util.Size;
//            import android.util.SparseIntArray;
//            import android.view.Surface;
//            import android.view.TextureView;
//            import android.view.View;
//            import android.widget.Button;
//            import android.widget.ImageView;
//            import android.hardware.camera2.*;
//            import android.widget.Toast;
//
//            import java.io.ByteArrayInputStream;
//            import java.io.ByteArrayOutputStream;
//            import java.io.File;
//            import java.io.FileNotFoundException;
//            import java.io.FileOutputStream;
//            import java.io.IOException;
//            import java.io.InputStream;
//            import java.io.OutputStream;
//            import java.nio.ByteBuffer;
//            import java.util.ArrayList;
//            import java.util.Arrays;
//            import java.util.List;
//
//            import com.microsoft.projectoxford.face.*;
//            import com.microsoft.projectoxford.face.contract.*;
//
//public class MainActivity<textureView> extends AppCompatActivity {
//    private ImageView imageView;
//    private static final int REQUEST_IMAGE_CAPTURE = 101;
//
//
//    private String cameraId;
//    protected CameraDevice cameraDevice;
//    protected CameraCaptureSession cameraCaptureSessions;
//    protected CaptureRequest captureRequest;
//    protected CaptureRequest.Builder captureRequestBuilder;
//    private Size imageDimension;
//    private ImageReader imageReader;
//    private File file;
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//    //    private boolean mFlashSupported;
//    private Handler mBackgroundHandler;
//    private HandlerThread mBackgroundThread;
//    protected static final int CAMERA_CALIBRATION_DELAY = 500;
//    protected static long cameraCaptureStartTime;
//
//
//
//    private static final String TAG = "MainActivity";
//    private Button takePictureButton;
//    private TextureView textureView;
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//    static{
//        ORIENTATIONS.append(Surface.ROTATION_0,90);
//        ORIENTATIONS.append(Surface.ROTATION_90,0);
//        ORIENTATIONS.append(Surface.ROTATION_180,270);
//        ORIENTATIONS.append(Surface.ROTATION_270,180);
//    }
//
//    //Face Detection API
//    Context mContext;
//    Bitmap bitmap_;
//    String FACE_SUBSCRIPTION_KEY = "bc027dc227484433a77d7b613807d230";
//    String FACE_ENDPOINT = "https://empthetic.cognitiveservices.azure.com/face/v1.0";
//    private final String apiEndpoint =  FACE_ENDPOINT;
//    private final String subscriptionKey = FACE_SUBSCRIPTION_KEY;
//    private final FaceServiceClient faceServiceClient = new FaceServiceRestClient(apiEndpoint, subscriptionKey);
//    private ProgressDialog detectionProgressDialog;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
////        imageView = findViewById(R.id.imageView);
//        textureView = (TextureView)findViewById(R.id.textureView);
//        assert textureView != null;
//        textureView.setSurfaceTextureListener(textureListener);
//        takePictureButton = (Button) findViewById(R.id.btn_takepicture);
//        System.out.println("on Create function here");
//
//
//        assert takePictureButton != null;
//        takePictureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                takePicture();
//            }
//        });
//        detectionProgressDialog = new ProgressDialog(this);
//
//    }
//
//    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            openCamera();
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//            return false;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//        }
//    };
//
//    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(CameraDevice camera) {
//            //This is called when the camera is open
//            Log.e(TAG, "onOpened");
//            cameraDevice = camera;
//            createCameraPreview();
//        }
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            cameraDevice.close();
//        }
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//    };
//
//    final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
//        @Override
//        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//            super.onCaptureCompleted(session, request, result);
//            Toast.makeText(com.example.myapplication.MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
//            createCameraPreview();
//        }
//    };
//
//    protected void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread("Camera Background");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//    protected void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try {
//            mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//    protected void takePicture() {
//        Log.d("tag","take Picture function here");
//        if(null == cameraDevice) {
//            Log.e(TAG, "cameraDevice is null");
//            return;
//        }
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//
//        try {
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
//            Size[] jpegSizes = null;
//            if (characteristics != null) {
//                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
//            }
//            int width = 640;
//            int height = 480;
//            if (jpegSizes != null && 0 < jpegSizes.length) {
//                width = jpegSizes[0].getWidth();
//                height = jpegSizes[0].getHeight();
//            }
//
//            //this.mImageReader = ImageReader.newInstance(width, height, ImageFormat.RAW_SENSOR, /*maxImages*/ 1);
//            //2. Surface surface = this.mImageReader.getSurface();
//            //2. final List<Surface> surfaces = Arrays.asList(surface);
//            //  3. this.mCamera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
//            // Callback methods here
//            //}, null);
//            //4.CaptureRequest.Builder captureRequestBuilder;
//            //4.captureRequestBuilder = this.mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            //4.captureRequestBuilder.addTarget(surface);
//            //4.this.mCaptureRequest = captureRequestBuilder.build();
////            5.this.mCaptureSession.setRepeatingRequest(mCaptureRequest, null, null);
//
//            //this.mImageReader = ImageReader.newInstance(width, height, ImageFormat.RAW_SENSOR, /*maxImages*/ 1);
//            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1  );
//            Log.d("Surface","surface");
//
//            //2. Surface surface = this.mImageReader.getSurface();
//            //2. final List<Surface> surfaces = Arrays.asList(surface);
//            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
//            outputSurfaces.add(reader.getSurface());
//            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
//
//
//            //4.CaptureRequest.Builder captureBuilder;
//            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
////            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//
//
//
//            Log.d("captureRequest","capture request");
//            //4.captureRequestBuilder.addTarget(surface);
//            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//
//
//            //4.this.mCaptureRequest = captureRequestBuilder.build();
////            5.this.mCaptureSession.setRepeatingRequest(mCaptureRequest, null, null);
//
//            // Orientation
//            Log.d("rotation","rotation");
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//
//
////            cameraCaptureSessions.setRepeatingRequest(captureBuilder.build(), null, null);
//
//
//            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
//                @Override
//                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(com.example.myapplication.MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
//                    createCameraPreview(); //unlock focus
////                    updatePreview();
//                }
//            };
//            cameraCaptureSessions.stopRepeating();
//            cameraCaptureSessions.setRepeatingRequest(captureBuilder.build(), captureListener, null);
//            //3
////            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
////                @Override
////                public void onConfigured(CameraCaptureSession session) {
////                    try {
////                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
////                        session.setRepeatingRequest(captureBuilder.build(), captureListener, mBackgroundHandler);
////                    } catch (CameraAccessException e) {
////                        e.printStackTrace();
////                    }
////                }
////                @Override
////                public void onConfigureFailed(CameraCaptureSession session) {
////                }
////            }, mBackgroundHandler);
//
//            Log.d("file","file");
//            final File file = new File(Environment.getExternalStorageDirectory()+"/pic.jpg");
//            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    Image image = null;
//                    try {
//                        Log.d("image reader","image reader");
////                        image = reader.acquireLatestImage();
//                        image = reader.acquireNextImage();
//                        if(System.currentTimeMillis() > cameraCaptureStartTime + CAMERA_CALIBRATION_DELAY){
//                            int img_width = image.getWidth();
//                            int img_height = image.getHeight();
//                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                            byte[] bytes = new byte[buffer.capacity()];
//                            buffer.get(bytes);
//
////                        bitmap_ = Bitmap.createBitmap(img_width,img_height,Bitmap.Config.ARGB_8888);
////                        ((BitmapDrawable) image.getDrawable()).getBitmap();
//                            detectAndFrame(bytes);
//
//                            save(bytes);
//                        }
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (image != null) {
//                            image.close();
//                        }
//                    }
//                }
//                private void save(byte[] bytes) throws IOException {
//                    OutputStream output = null;
//                    try {
//                        output = new FileOutputStream(file);
//                        output.write(bytes);
////                        bitmap_ = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//                    } finally {
//                        if (null != output) {
//                            output.close();
//                        }
//                    }
//                }
//            };
//
//            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
//
//
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        System.out.println("end of take picture function");
//    }
//
//    protected void createCameraPreview() {
//        try {
//            System.out.println("createCameraPreview");
//            SurfaceTexture texture = textureView.getSurfaceTexture();
//            assert texture != null;
//            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
//            Surface surface = new Surface(texture);
//
//            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            captureRequestBuilder.addTarget(surface);
//            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession session) {
//                    //The camera is already closed
//                    if (null == cameraDevice) {
//                        return;
//                    }
//                    try{
//                        captureRequest = captureRequestBuilder.build();
//
//                        // When the session is ready, we start displaying the preview.
//                        cameraCaptureSessions = session;
//                        cameraCaptureSessions.setRepeatingRequest(captureRequest,null,null);
//                        //updatePreview();
//                    }catch(CameraAccessException e){
//                        e.printStackTrace();
//                    }
//
//
//
//                    //
//                }
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Toast.makeText(com.example.myapplication.MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void openCamera() {
//        System.out.println("openCamera");
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        Log.e(TAG, "is camera open");
//        try {
//            cameraId = manager.getCameraIdList()[0];
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            assert map != null;
//            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
//            // Add permission for camera and let user grant the permission
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(com.example.myapplication.MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
//                return;
//            }
//            manager.openCamera(cameraId, stateCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        Log.e(TAG, "openCamera X");
//    }
//
//    protected void updatePreview() {   //restartPreview
//        System.out.println("updataPreview");
//        if(null == cameraDevice) {
//            Log.e(TAG, "updatePreview error, return");
//        }
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        try {
//            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
//////            cameraCaptureStartTime = System.currentTimeMillis();
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //    private void closeCamera() {
////        if (null != cameraDevice) {
////            cameraDevice.close();
////            cameraDevice = null;
////        }
////        if (null != imageReader) {
////            imageReader.close();
////            imageReader = null;
////        }
////    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                // close the app
//                Toast.makeText(com.example.myapplication.MainActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.e(TAG, "onResume");
//        startBackgroundThread();
//        if (textureView.isAvailable()) {
//            openCamera();
//        } else {
//            textureView.setSurfaceTextureListener(textureListener);
//        }
//    }
//    @Override
//    protected void onPause() {
//        Log.e(TAG, "onPause");
//        //closeCamera();
//        stopBackgroundThread();
//        super.onPause();
//    }
//
//
//    //    private void detectAndFrame(final Bitmap bitmap) {
//    private void detectAndFrame(final byte[] bytes) {
//        Log.d("detectFrame","Face API called");
////        System.out.println(bytes.getClass());
////        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
////        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
////
//
////        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
////
//        AsyncTask<InputStream, String, Face[]> detectTask =
//                new AsyncTask<InputStream, String, Face[]>() {
//                    String exceptionMessage = "";
//
//                    @Override
//                    protected Face[] doInBackground(InputStream... params) {
//
//                        try {
//                            publishProgress("Detecting...");
//                            Log.d("detectFrame","Inside doInBackground");
//
//                            Face[] result = faceServiceClient.detect(
//                                    params[0],
//                                    true,         // returnFaceId
//                                    false,        // returnFaceLandmarks
//                                    new FaceServiceClient.FaceAttributeType[]{
//                                            FaceServiceClient.FaceAttributeType.Age,
//                                            FaceServiceClient.FaceAttributeType.Gender, FaceServiceClient.FaceAttributeType.Emotion
//                                    }
//                            );
//                            Log.d("detectFrame","Finished Face result");
//                            if (result == null){
//                                publishProgress(
//                                        "Detection Finished. Nothing detected");
//                                return null;
//                            }
//                            publishProgress(String.format(
//                                    "Detection Finished. %d face(s) detected",
//                                    result.length));
//                            return result;
//                        } catch (Exception e) {
//                            exceptionMessage = String.format(
//                                    "Detection failed: %s", e.getMessage());
//                            return null;
//                        }
////                        Log.d("detectFrame","Inside doInBackground");
//
//                    }
//
//
//                    //                    @Override
////                    protected void onPreExecute() {
////                        //TODO: show progress dialog
//////                        detectionProgressDialog.show();
////                    }
////                    @Override
////                    protected void onProgressUpdate(String... progress) {
////                        //TODO: update progress
////                        detectionProgressDialog.setMessage(progress[0]);
////                    }
//                    @Override
//                    protected void onPostExecute(Face[] result) {
//                        //TODO: update face frames
//                        detectionProgressDialog.dismiss();
//
//                        if(!exceptionMessage.equals("")){
////                            showError(exceptionMessage);
//                            System.out.println(exceptionMessage);
//                        }
//                        if (result == null) return;
////                        Bitmap mBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
//
//                        showFaceResult(result);
////                        ImageView imageView = findViewById(R.id.imageView1);
//
////                        imageView.setImageBitmap(mBitmap);
////                                drawFaceRectanglesOnBitmap(imageBitmap, result));
////                        imageBitmap.recycle();
////                        mBitmap.recycle();
//                    }
//                };
//
//        detectTask.execute(inputStream);
//    }
//    private void showFaceResult(Face[] faces) {
//        Log.d("showFaceResult", "Inside showFaceResult function");
//        Canvas canvas = new Canvas();
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        //paint.setStyle(Paint.Style.STROKE);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.RED);
//        paint.setStrokeWidth(10);
//        String emo;
//        if (faces != null) {
//            Log.d("emotion","face not null");
//            System.out.println(faces.length);
//            for (Face face : faces) {
//                Log.d("emotion","in for loop");
//                FaceRectangle faceRectangle = face.faceRectangle;
//                Emotion faceEmotion = face.faceAttributes.emotion;
//                //System.out.print(faceEmotion);
//                double anger = faceEmotion.anger;
//                double contempt = faceEmotion.contempt;
//                double disgust = faceEmotion.disgust;
//                double fear = faceEmotion.fear;
//                double happiness = faceEmotion.happiness;
//                double neutral = faceEmotion.neutral;
//                double sadness = faceEmotion.sadness;
//                double surprise = faceEmotion.surprise;
//                double[] emoArrVal = {anger, contempt, disgust, fear, happiness, neutral, sadness, surprise};
//                String[] emoArr = {"anger", "contempt", "disgust", "fear", "happiness", "neutral", "sadness", "surprise"};
//                int maxEmo = getMaxValue(emoArrVal);
//                emo = emoArr[maxEmo];
//                paint.setTextSize(400);
//                canvas.drawText(emo, 75, 385, paint);
//
//                canvas.translate(0, 200);
//                Log.d("emotion",emo);
//
//            }
//        }else{
//            paint.setTextSize(400);
//            canvas.drawText("no face detected",75,385,paint);
//            canvas.translate(0,200);
//        }
////        return emoArrVal;
//    }
//
//    private void showError(String message) {
//        new AlertDialog.Builder(this)
//                .setTitle("Error")
//                .setMessage(message)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                    }})
//                .create().show();
//    };
//
//    public static int getMaxValue(double[] numbers){
//        double maxValue = numbers[0];
//        int maxIndex = 0;
//
//        for(int i=0 ;i < numbers.length;i++){
//            if(numbers[i] > maxValue){
//                maxValue = numbers[i];
//                maxIndex = i;
//            }
//        }
//        return maxIndex;
//    }
//
//
//}









