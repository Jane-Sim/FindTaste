package com.example.seyoung.findtaste.views.AddTasteInfor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.views.FileLib;

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
 * Created by seyoung on 2017-10-20.
 * 사용자가 원하는 맛집을 추가하는 화면입니다.
 * 사용자가 직접 이름과 주소를 적거나, 지도화면에서 원하는 맛집을 클릭 해 추가할 수 있습니다.
 * 이때 원하는 맛집의 이름과 주소, 전화번호와 상세내용, 원하는 사진을 추가할 수 있게 해줍니다.
 * 등록한 맛집의 위도와 경도를 받아와 해당 맛집을 리스트나 구글맵으로 보여줄 수 있게 만들어줍니다.
 */

public class AddtasteActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();     //서버에서 데이터를 받아왔을 때 어디 화면인지 알려주는 tag입니다.
    ProgressDialog dialog;          //맛집이 저장될 동안 띄워줄 다이얼로그
    ArrayList<String> imageList = new ArrayList<>();    //맛집의 사진경로를 담을 이미지 리스트입니다.
    Bitmap bitmap2;         //사용자가 앨범이나 카메라로 찍은 사진을 이미지뷰에 보여주기 위해 사용할 비트맵입니다.
    Uri picUrl;             //사용자가 앨범에서 사진을 한 장 가져올 때 그 사진의 경로를 담을 url입니다.
    EditText nameEdit;      //맛집의 이름을 적을 에딧
    EditText telEdit;       //맛집의 전화번호
    EditText addressEdit;   //맛집의 주소
    EditText descriptionEdit;//맛집상세정보
    TextView currentLength; //상세정보의 글자 갯수를 보여준다
    TextView imagepick;     //사진의 앨범이나 카메라를 선택하게 해주는 이미지뷰
    ImageView mappick ;      //사용자를 구글맵으로 이동시킬 맵 이미지
    TextView MapText;       //
    Button uploadButton;    //사용자가 입력한 맛집의 정보들을 서버에 저장시키는 버튼입니다.
    String userid;          //현재 사용자의 아이디입니다. 서버에 맛집을 저장할 때 등록시킨 사용자도 같이 저장시켜줍니다.
    private static final int PICK_FROM_CAMERA = 0;  // 카메라에서 현재화면으로 돌아왔을 때, 결과값을 실행시킬 값입니다.
    private static final int PICK_FROM_ALBUM = 1;   // 앨범에서 현재화면으로 돌아왔을 때, 결과값을 실행시킬 값입니다.

    File imageFile;         //사진을 찍을 경우, 사진을 기기에 저장하게 만들어준다. 그 저장한 사진의 경로를 서버에 보낸다.
    String imageFilename, lati, logi; //찍은 사진의 이름을 정해주고, 맛집의 위도와 경도를 저장할 값이다.
    ImageView infoImage1, infoImage2, infoImage3, infoImage4, infoImage5;   //사용자가 이미지를 불러올 경우, 보여줄 이미지뷰들이다
    String imagePath;       //앨범에서 사진을 가져올 경우 경로를 저장할 값이다

    boolean pass = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtaste);
        //맛집의 정보를 입력할 에딧들을 불러온다
        currentLength = findViewById(R.id.current_length);
        nameEdit = findViewById(R.id.foodname);
        telEdit = findViewById(R.id.foodtel);
        addressEdit = findViewById(R.id.foodaddress);
        descriptionEdit = findViewById(R.id.fooddescription);
        //현재 사용자의 아이디를 가져온다
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        userid = pref.getString("ing", "");
        //상세정보에 입력할 때마다 입력한 글자의 숫자를 가져온다.
        descriptionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentLength.setText(String.valueOf(s.length()));  //상세정보에입력한 글자수를 보여준다.
                if(nameEdit.getText().length()>0&&addressEdit.getText().length()>0&&s.length()>0) {   //모든 입력값이 빈 값이 아닐 때 추가하기 버튼을 빨갛게 만든다.
                    uploadButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    pass = true;    //맛집을 추가할 수 있게 만든다
                }
                else if(s.length()==0) {   //상세정보가 없을 경우 추가하기 버튼을 회색으로 처리한다.
                    uploadButton.setBackgroundColor(getResources().getColor(R.color.com_facebook_button_border_color_focused));
                    pass = false;   //맛집을 추가하지 못하게 한다
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //카메라를 사용하기 위해 유저에게 카메라 권한을 물어본다. 없을 경우 권한설정하라고 해줌
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        5); //카메라 권한을 물어보고 난 뒤 확인해주는 값.
            }
        }
        //앨범,파일을 사용하기 위해 유저에게 앨범,파일 가져오기 권한을 물어본다. 없을 경우 권한설정하라고 해줌(카메라나 앨범을 가져올 경우, 파일을 갖고오기 위해 필요)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        6);
            }
        }
        //앨범,파일을 사용하기 위해 유저에게 파일과 사진 쓰기 권한을 물어본다. 없을 경우 권한설정하라고 해줌(카메라를 찍었을 경우, 찍은 사진을 기기에 저장하기 위해 필요)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        7);
            }
        }

        uploadButton = findViewById(R.id.complete);  //맛집 추가하기 버튼
        uploadButton.setOnClickListener(this);

        imagepick = findViewById(R.id.pick);    //앨범과 카메라 사용하기 버튼
        mappick = findViewById(R.id.Map); //지도맵으로 이동하는 버튼
        MapText = findViewById(R.id.MapText);   //지도맵으로 이동하는 버튼

        imageFilename = "tmp_" + String.valueOf(System.currentTimeMillis());    //카메라로 찍었을 경우, 현재 시간으로 이름을 정해준다.
        imageFile = FileLib.getInstance().getImageFile(getApplicationContext(), imageFilename); //카메라로 저장된 경로에서, 현재시간으로 저장한
                                                                                                //사진을 가져오도록 한다.
        //사용자가 카메라나 앨범으로 사진을 가져왔을 때, 바로 보여줄 이미지뷰다
        infoImage1 = findViewById(R.id.image1);
        infoImage2 = findViewById(R.id.image2);
        infoImage3 = findViewById(R.id.image3);
        infoImage4 = findViewById(R.id.image4);
        infoImage5 = findViewById(R.id.image5);
       // imageMemoEdit = (EditText) findViewById(R.id.register_image_memo);

        ImageView imagechoice = findViewById(R.id.image_choice);    //앨범과 카메라 선택형 다이얼로그를 띄우게 하는 이미지뷰다
        imagechoice.setOnClickListener(this);
        imagepick.setOnClickListener(this);                                      //앨범과 카메라 선택형 다이얼로그를 띄우게 하는 텍스트
        ImageView Mapfind = findViewById(R.id.Map);                              //지도화면으로 넘어가게 만드는 이미지뷰
        Mapfind.setOnClickListener(this);
        MapText.setOnClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5: {
                //카메라 권한설정을 했을 때,
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    //권한을 안 줬을 경우, 어플을 종료시킨다.
                    showNoPermissionToastAndFinish();
                }
                return;
            }
            case 6: {
                if (grantResults.length > 0
                        //파일 가져오기 권한을 줬을 때,
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    //권한을 안 줬을 경우, 어플을 종료시킨다.
                    showNoPermissionToastAndFinish();
                }
                return;
            }
            case 7: {
                if (grantResults.length > 0
                        //파일 쓰기 권한을 줬을 때,
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    //권한을 안 줬을 경우, 어플을 종료시킨다.
                    showNoPermissionToastAndFinish();
                   }
                return;
            }

        }
    }
    //사용자가 권한이 없을 때 어플을 종료시킨다
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    //카메라를 실행시키며 결과를 받아오게 한다
    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    //업로드 후 현재 화면이 종료될 때, 다이얼로그가 띄어져 있으면 오류가 나므로 꼭
    //화면이 제거될 때 다이얼로그도 종료시켜야한다
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    //앨범을 실행시키며 결과값을 가져온다
    //사진을 여러개로 가져올 수 있게 했다.
    @TargetApi(18)
    private void getImageFromAlbum() {
        Intent intent = new Intent();
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setAction(Intent.ACTION_PICK);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent.createChooser(intent, "다중 선택은 포토클릭"), PICK_FROM_ALBUM);
    }

    //카메라와 앨범, 지도화면에서 돌아왔을 때 결과값을 받아온다
    @Override
    @TargetApi(16)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            //카메라에서 가져왔을 경우,
            if (requestCode == PICK_FROM_CAMERA) {
                Glide.with(this).load(imageFile).into(infoImage1);  //글라이드로 빠르게 사진을 추가시켜준다.
                infoImage1.setVisibility(View.VISIBLE);                     //비트맵으로 바꿀 필요x
                imageList.add(String.valueOf(imageFile));                   //그리고 서버에 보낼 사진경로 리스트에 현재 사진경로를 추가한다.
            } else if (requestCode == PICK_FROM_ALBUM && data != null) {    //앨범에서 사진을 가져올 때
                //한 장의 사진만 가져왔을 경우,
                if (data.getClipData() == null) {
                    imageList.add(String.valueOf(data.getData()));  //한장의 데이터를 받아오도록 만들어준다
                    try {
                        //가져온 이미지의 경로를 통해서 해당 사진을 비트맵으로 만든다. 그러면 이미지뷰로 볼 수 있음
                        bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        Uri selectedImageUri = data.getData();
                        picUrl = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                        if (cursor != null) {
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imagePath = cursor.getString(columnIndex);

// Picasso.with(mContext).load(new File(imagePath))
// .into(imageView); // 피카소 라이브러를 이용하여 선택한 이미지를 imageView에  전달.
                            cursor.close();

                        } else {
                            Log.e("이미지패스", "비었다");

                        }
                        infoImage1.setImageBitmap(bitmap2);
                        infoImage1.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //이미지를 여러장 갖고 왔을 때
                    ClipData clibData = data.getClipData(); //여러장의 데이터를 가져오게 한다
                    Log.i("clipdata", String.valueOf(clibData.getItemCount()));
                    //만약 5장보다 더 많이 들고왔을 경우,
                    if (clibData.getItemCount() > 5) {
                        Toast.makeText(this, "사진은 5개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (clibData.getItemCount() == 1) {  //포토에서 한 장만 가져왔을 경우,
                        String dataStr = String.valueOf(clibData.getItemAt(0).getUri());
                        Log.i("2.clipdata choice", String.valueOf(clibData.getItemAt(0).getUri()));
                        Log.i("2.single choice", clibData.getItemAt(0).getUri().getPath());
                        //사진경로를 볼 수 있게 로그로 남겨준다
                        imageList.add(dataStr); //그리고 서버에 보낼 사진경로리스트에 추가
                        try {
                            Glide.with(this).load(clibData.getItemAt(0).getUri()).into(infoImage1);
                            bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), clibData.getItemAt(0).getUri());
                            infoImage1.setImageBitmap(bitmap2);
                            infoImage1.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (clibData.getItemCount() > 1 && clibData.getItemCount() < 6) {
                        //1장에서 5장 이내로 사진을 들고 왔을 때.
                        for (int i = 0; i < clibData.getItemCount(); i++) {
                            //사진의 갯수만큼 리스트에 추가한 뒤, 이미지뷰에 넣어준다.
                            Log.i("3. single choice", String.valueOf(clibData.getItemAt(i).getUri()));
                            imageList.add(String.valueOf(clibData.getItemAt(i).getUri()));
                            try {
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = 8;
                                bitmap2 = BitmapFactory.decodeFile(String.valueOf(MediaStore.Images.Media.getBitmap(getContentResolver(), clibData.getItemAt(i).getUri())),options);
                              //  bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), clibData.getItemAt(i).getUri());
                                if (i == 0) {
                                    Glide.with(this).load(clibData.getItemAt(i).getUri()).into(infoImage1);
                                    infoImage1.setImageBitmap(bitmap2);
                                    infoImage1.setVisibility(View.VISIBLE);
                                } else if (i == 1) {
                                    Glide.with(this).load(clibData.getItemAt(i).getUri()).into(infoImage2);
                                    infoImage2.setImageBitmap(bitmap2);
                                    infoImage2.setVisibility(View.VISIBLE);
                                } else if (i == 2) {
                                    Glide.with(this).load(clibData.getItemAt(i).getUri()).into(infoImage3);
                                    infoImage3.setImageBitmap(bitmap2);
                                    infoImage3.setVisibility(View.VISIBLE);
                                } else if (i == 3) {
                                    Glide.with(this).load(clibData.getItemAt(i).getUri()).into(infoImage4);
                                    infoImage4.setImageBitmap(bitmap2);
                                    infoImage4.setVisibility(View.VISIBLE);
                                } else if (i == 4) {
                                    Glide.with(this).load(clibData.getItemAt(i).getUri()).into(infoImage5);
                                    infoImage5.setImageBitmap(bitmap2);
                                    infoImage5.setVisibility(View.VISIBLE);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            // 지도화면에서 맛집을 선택하고 온 경우, 선택한 맛집의 이름과 주소, 위도 경도를 가지고 온다
            else if (requestCode == 200) {
                nameEdit.setText(data.getStringExtra("title"));
                addressEdit.setText(data.getStringExtra("adress"));
                lati = data.getStringExtra("lati");
                logi = data.getStringExtra("logi");
            }
        }
    }


    /**
     * 이미지를 어떤 방식으로 선택할지에 대해 다이얼로그를 보여준다.
     * //  * @param context 컨텍스트 객체
     */
    public void showImageDialog() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(R.array.camera_album_category, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //카메라르 선택한 경우 카메라를 실행시킨다
                                if (which == 0) {
                                    getImageFromCamera();
                                } else {
                                    //앨범을 선택한 경우, 사진 경로들을 지우고, 이미지뷰를 다시 숨긴다.
                                    imageList.clear();
                                    infoImage1.setVisibility(View.GONE);
                                    infoImage2.setVisibility(View.GONE);
                                    infoImage3.setVisibility(View.GONE);
                                    infoImage4.setVisibility(View.GONE);
                                    infoImage5.setVisibility(View.GONE);
                                    //앨범을 실행시킨다
                                    getImageFromAlbum();
                                }

                                dialog.dismiss();
                            }
                        }).show();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.image_choice) {
            //사진 가져오기
            showImageDialog();
        } else if (view.getId() == R.id.pick) {
            //사진 가져오기
            showImageDialog();
        } else if (view.getId() == R.id.Map|| view.getId() == R.id.MapText) {
            //지도화면으로 가기
            Intent intent = new Intent(this, MapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, 200);
        } else if (view.getId() == R.id.complete) {
            //맛집 추가하기 버튼을 눌렀을 때
            //사용자가 모든 정보를 입력했을 경우 서버에 저장시킨다
            if(pass) {
                ImageAsyncTask lDB = new ImageAsyncTask();
                String name = nameEdit.getText().toString();
                String tel = telEdit.getText().toString();
                String address = addressEdit.getText().toString();
                String description = descriptionEdit.getText().toString();
                // lDB.execute();
                lDB.execute(name, address, tel, description, lati, logi, userid);
                //Toast.makeText(this,"등록 완료",Toast.LENGTH_SHORT).show();
                // finish();
            }else {
                //상세정보를 적으라고 알림창으로 알려준다.
                Toast.makeText(AddtasteActivity.this,"맛집의 상세정보를 적어주세요",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //맛집의 이미지와 이름, 주소, 번호, 상세정보를 서버에 보낸다
    @SuppressLint("StaticFieldLeak")
    public class ImageAsyncTask extends AsyncTask<String, Void, String> {
        //서버의 주소
        String Add = "http://findtaste.vps.phps.kr/user_signup/upload.php";
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
            // 저장 할 때 다이얼로그로 사용자에게 저장 중을 보여준다
            dialog = new ProgressDialog(AddtasteActivity.this);
            dialog.setTitle("저장 중");
            dialog.setMessage("잠시만 기다려주세요...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //서버에서 결과를 받아오면 다이얼로그를 지운다
            dialog.dismiss();
            if (s != null) {
                //제대로 저장을 햇으면 화면을 꺼준다
               Toast.makeText(AddtasteActivity.this,s,Toast.LENGTH_SHORT).show();
                finish();
            } else {
                //실패할 경우 알림창을 통해 알려준다
                Toast.makeText(AddtasteActivity.this,"실패"+s,Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "POST response  - " + s);
        }



        @Override
        protected String doInBackground(String... params) {
            ArrayList<String> imageList7 = new ArrayList<>();
            InputStream is = null;
            //저장된 사진의경로를 가져와서 경로를 기기의 디비에 저장한다
            for(int i=0; i<imageList.size();i++) {
                    //현재 사진의 경로에서 사진 이름을 가져옵니다
                String name_Str = getImageNameToUri(imageList.get(i));
                //기기의 데이터베이스에서 현재 선택한 이미지 경로의 사진을 들고옵니다
                Cursor c = getContentResolver().query(Uri.parse(imageList.get(i)), null, null, null, null);
                c.moveToNext();
            try {
                // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
                //해당 사진의 경로를 안드로이드 베이터베이스에서 가져옵니다
                String absolutePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));

                Bitmap image_bitmap = null;
                //해당 사진의 경로를 통해 사진을 비트맵으로 가져옵니다.
                image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageList.get(i)));

                //이때 사진의 용량을 줄이기 위해
                ///해당 사진의 크기를 절반으로 압축시킵니다.
                int height = image_bitmap.getHeight();
                int width = image_bitmap.getWidth();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                //압축시킨 사진을 다시 절반의 크기로 줄입니다.
                Bitmap src = BitmapFactory.decodeFile(absolutePath,options);
                Bitmap resized = Bitmap.createScaledBitmap( src, width/2, height/2, true );
                //리사이징한 사진을 핸드폰에 저장합니다.
                absolutePath = saveBitmaptoJpeg(resized, "seatdot", name_Str);
                    imageList7.add(absolutePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Log.d("TEST", "file Path=>" + imageList7);
                c.close();
            }
            //서버에 보낼 맛집정보
            String name = (String)params[0];        //이름
            String address = (String)params[1];     //주소
            String tel = (String)params[2];         //전화번호
            String description = (String)params[3]; //상세정보
            String lati = (String)params[4];        //위도
            String logi = (String)params[5];        //경도
            String userId = (String)params[6];           //현재 유저의 이름

      /*      String postParameters = "name=" + name + "&address=" + address
                    + "&tel=" + tel + "&description=" + description + "&lati=" + lati  + "&logi=" + logi
                    + "&username=" + g;*/

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
                //서버에 보낼 사진 이름과, 경로를 꼭 지정해줘야한다
                for(int i=0; i<imageList7.size();i++) {
                    conn.setRequestProperty("image"+i, imageList7.get(i));
                }
                conn.setRequestProperty("user-agent", "test");
                conn.connect();

                //데이터를 서버로 보내 줄 스트림을 불러온다
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                //사진의 갯수를 서버에 보내고
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"list\"\r\n\r\n" + imageList7.size());
                //맛집이름
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"\r\n\r\n" +URLEncoder.encode(name,"utf-8"));
                //맛집주소
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"address\"\r\n\r\n"+URLEncoder.encode(address,"utf-8"));
                //맛집 번호
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"tel\"\r\n\r\n"+ URLEncoder.encode(tel, "UTF-8"));
                 //상세정보
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"description\"\r\n\r\n"+ URLEncoder.encode(description,"utf-8"));
                //위도,경도
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"lati\"\r\n\r\n" +  URLEncoder.encode(lati,"utf-8"));
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"logi\"\r\n\r\n" +  URLEncoder.encode(logi,"utf-8"));
                //사용자 이름
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"username\"\r\n\r\n"+ URLEncoder.encode(userId,"utf-8"));
                //즐겨찾기의 기본 값을 0으로 지정한다
                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"favorites\"\r\n\r\n" + "0");

                //만약 사진을 한장만 보낼경우,
                if(imageList7.size()==1) {
                    //해당 사진을 가져온다
                    mFileInputStream = new FileInputStream(imageList7.get(0));
                    //서버에 사진이름과 경로를 보낸다
                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"image0\";filename=\"" + imageList7.get(0) + "\"" + lineEnd);
                    //어플리케이션의 형식을 지정하지 않고 8비트로 된 일련의 데이터를 보내준다.
                    dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                    dos.writeBytes(lineEnd);
                    //현재 사진의 바이트 수를 읽어온다
                    int bytesAvailable = mFileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    byte[] buffer = new byte[bufferSize];
                    //데이터를 바이트 배열로 읽어들입니다.
                    int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = mFileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                    }
                    mFileInputStream.close();           //파일을 보내고 난 후 꼭 닫아준다
                }
                    if(imageList7.size()>1) {
                        for (int i = 0; i < imageList7.size(); i++) {
                            mFileInputStream = new FileInputStream(imageList7.get(i));

                            dos.writeBytes("\r\n--" + boundary + "\r\n");
                            dos.writeBytes("Content-Disposition: form-data; name=\"image"+i+"\";filename=\"" + imageList7.get(i) + "\"" + lineEnd);
                            dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                            dos.writeBytes(lineEnd);
                            Log.d("image"+i,imageList7.get(i));
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
                    }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "POST response code - " + conn.getResponseCode());
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

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }


    }

    //선택한 사진의 이름을 반환시켜준다
    public String getImageNameToUri(String data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(Uri.parse(data), proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();
        //사진의 경로를 기기의 데이터베이스에서 가져온다
        String imgPath = cursor.getString(column_index);
        // 맨 마지막 커서에서 / 뒤에 있는 이름을 가져온다
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);
        //이름을 반환
        return imgName;
    }

    public static String saveBitmaptoJpeg(Bitmap bitmap,String folder, String name){
        // 기기의 저장되는 경로를 가져온다
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        //사진을 저장할 폴더경로를 만들고
        String foler_name = "/"+folder+"/";
        //사진의 이름을 jpg와 시킨다.
        String file_name = name+".jpg";
        //저장되는 곳에 폴더와 이미지 이름을 넣어서 저장시킨다.
        String string_path = ex_storage+foler_name;
        String UploadImgPath = string_path+file_name;


        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                //만약 지정한 폴더가 없을 경우, 폴더를 만든다
                file_path.mkdirs();
            }
            //해당 폴더로 저장시키는 스트림을 불러온다
            FileOutputStream out = new FileOutputStream(string_path+file_name);
            // 비트맵을 사진으로 만들어서 해당 경로로 보내준다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
        return UploadImgPath;   //해당 폴더에 저장된 사진의 경로를 반환한다
    }
}

