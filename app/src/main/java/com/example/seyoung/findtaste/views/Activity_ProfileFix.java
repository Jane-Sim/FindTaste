package com.example.seyoung.findtaste.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by seyoung on 2017-12-09.
 */

public class Activity_ProfileFix extends AppCompatActivity {
    ImageView p_image,setting,back;
    //TextView ;
    EditText name,email,number;
    ArrayList<String> imageList = new ArrayList<>();

    public static final String INFO_SEQ = "INFO_SEQ";
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;

    File imageFile;
    String imageFilename,remove;

    ProgressDialog dialog;

    Button next;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profilefix);

        p_image = findViewById(R.id.p_image);
        setting = findViewById(R.id.setting);
        back = findViewById(R.id.back);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        number = findViewById(R.id.number);
        next = findViewById(R.id.next);

        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        email.setText(intent.getStringExtra("email"));
        number.setText(intent.getStringExtra("phone"));
        remove = intent.getStringExtra("imagename");
        Glide.with(this)                         //글라이드로 빠르게 사진을 넣는다.
                .load(intent.getStringExtra("image"))
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .fitCenter()
                        .centerCrop()
                        .circleCrop()
                )
                .into(p_image);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDialog();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name1=name.getText().toString();
                String tel=number.getText().toString();
                String mail=email.getText().toString();

                if(!name1.equals("")&!name1.equals(" ")&!name1.equals("  ")&!name1.equals("\n")&!name1.equals("\n\n")) {
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    String g = pref.getString("ing", "");
                    ImageAsyncTask lDB = new ImageAsyncTask();
                    lDB.execute(name1, mail, tel, g);
                } else
                    Toast.makeText(Activity_ProfileFix.this,"이름을 적어주세요",Toast.LENGTH_SHORT).show();
            }
        });


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        5);
            }
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        6);
            }
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        7);
            }
        }

        imageFilename = "tmp_" + String.valueOf(System.currentTimeMillis());
        imageFile = FileLib.getInstance().getImageFile(getApplicationContext(), imageFilename);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    showNoPermissionToastAndFinish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case 6: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
            case 7: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }

        }
    }

    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void showImageDialog() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(R.array.camera_album_category, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    getImageFromCamera();
                                } else {
                                    imageList.clear();
                                    getImageFromAlbum();
                                }

                                dialog.dismiss();
                            }
                        }).show();
    }

    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    /**
     * 앨범으로부터 이미지를 선택할 수 있는 액티비티를 시작한다.
     */
    @TargetApi(18)
    private void getImageFromAlbum() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    @TargetApi(16)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FROM_CAMERA) {
                imageList.clear();
                Glide.with(this)                         //글라이드로 빠르게 사진을 넣는다.
                        .load(imageFile)
                        .apply(new RequestOptions()
                                .error(R.drawable.fbnull)
                                .override(200,200)
                                .fitCenter()
                                .centerCrop()
                                .circleCrop()
                        )
                        .into(p_image);
                imageList.add(String.valueOf(imageFile));
            } else if (requestCode == PICK_FROM_ALBUM) {
                imageList.clear();
                imageList.add(String.valueOf(data.getData()));
                Glide.with(this)                         //글라이드로 빠르게 사진을 넣는다.
                        .load(data.getData())
                        .apply(new RequestOptions()
                                .error(R.drawable.fbnull)
                                .override(300,300)
                                .fitCenter()
                                .centerCrop()
                                .circleCrop()
                        )
                        .into(p_image);

                Bitmap bitmap2 = null;
                try {
                    bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else {
                 Log.e("이미지패스", "비었다");
                }


                }
            }




    public class ImageAsyncTask extends AsyncTask<String, Void, String> {

        String Add = "http://findtaste.vps.phps.kr/user_signup/userEdit.php";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int maxBufferSize = 1 * 1024 * 1024;
        public static final int MAX_READ_TIME = 10000;
        public static final int MAX_CONNECT_TIME = 15000;
        int bufferSize;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 작업을 시작하기 전 할일
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
            dialog = new ProgressDialog(Activity_ProfileFix.this);
            dialog.setTitle("저장 중");
            dialog.setMessage("잠시만 기다려주세요...");
            dialog.setIndeterminate(true);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            if (s != null) {
              //  Toast.makeText(Activity_ProfileFix.this,s,Toast.LENGTH_SHORT).show();
                Log.d("업로드 완료 후","POST response  - " + s);
                finish();
            } else {
                Toast.makeText(Activity_ProfileFix.this,"실패"+s,Toast.LENGTH_SHORT).show();
            }

            //Log.d(TAG, "POST response  - " + s);
        }



        @Override
        protected String doInBackground(String... params) {
            ArrayList<String> imageList7 = new ArrayList<>();
            InputStream is = null;
            if(imageList.size()==1) {
                String name_Str = getImageNameToUri(imageList.get(0));
                Cursor c = getContentResolver().query(Uri.parse(imageList.get(0)), null, null, null, null);
                c.moveToNext();
                try {
                    String absolutePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));

                    Bitmap image_bitmap = null;

                    image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageList.get(0)));

                    ///리사이징
                    int height = image_bitmap.getHeight();
                    int width = image_bitmap.getWidth();

                    BitmapFactory.Options options = new BitmapFactory.Options();

                    Bitmap src = BitmapFactory.decodeFile(absolutePath, options);
                    Bitmap resized = Bitmap.createScaledBitmap(src, width/2, height/2, true);

                    absolutePath = saveBitmaptoJpeg(resized, "seatdot", name_Str);
                    imageList7.add(absolutePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Log.d("TEST", "file Path=>" + imageList);
                c.close();
            }

            String name = (String)params[0];
            String mail = (String)params[1];
            String tel = (String)params[2];
            String g = (String)params[3];

            String postParameters = "name=" + name + "&mail=" + mail
                    + "&tel=" + tel + "&username=" + g;

            try {
                URL connectUrl = new URL(Add);
                FileInputStream mFileInputStream ;
                HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();
                conn.setConnectTimeout(MAX_CONNECT_TIME);
                conn.setReadTimeout(MAX_READ_TIME);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects( false ) ;
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("cache-control", "no-cache");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty("cache-length", "length");
                if(imageList7.size()==1)
                conn.setRequestProperty("image0", imageList7.get(0));
                conn.setRequestProperty("user-agent", "test");
                conn.connect();


                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                if(imageList7.size()==1){
                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"remove\"\r\n\r\n" + URLEncoder.encode(remove,"utf-8"));

                }

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"list\"\r\n\r\n" + URLEncoder.encode(String.valueOf(imageList7.size()),"utf-8"));

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"\r\n\r\n" + URLEncoder.encode(name,"utf-8"));
                //   dos.writeUTF(name);
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"mail\"\r\n\r\n"+URLEncoder.encode(mail,"utf-8"));
                //   dos.writeUTF(address);
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"tel\"\r\n\r\n"+ URLEncoder.encode(tel, "UTF-8"));

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"username\"\r\n\r\n"+ URLEncoder.encode(g,"utf-8"));
                if(imageList7.size()==1) {
                    mFileInputStream = new FileInputStream(imageList7.get(0));
                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"image0\";filename=\"" + imageList7.get(0) + "\"" + lineEnd);

                    dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                    dos.writeBytes(lineEnd);
                    int bytesAvailable = mFileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = mFileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                    }
                    mFileInputStream.close();
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 //   Log.d(TAG, "POST response code - " + conn.getResponseCode());
                    is = conn.getInputStream();
                } else {//실패
                    // Toast.makeText(AddtasteActivity.this,"실패",Toast.LENGTH_SHORT).show();
                    is = conn.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {

             //   Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }


    }


    public String getImageNameToUri(String data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(Uri.parse(data), proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);

        return imgName;
    }

    public static String saveBitmaptoJpeg(Bitmap bitmap,String folder, String name){
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg";
        String string_path = ex_storage+foler_name;
        String UploadImgPath = string_path+file_name;


        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
        return UploadImgPath;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
