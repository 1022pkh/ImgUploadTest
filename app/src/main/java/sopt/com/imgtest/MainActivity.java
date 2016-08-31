package sopt.com.imgtest;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private TextView getIP;
    private Button getImgBtn;
    private Button uploadImg;
    String getServerURL = "";
    String getImgURL="";
    String getImgName="";

    final int REQ_CODE_SELECT_IMAGE=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIP = (TextView)findViewById(R.id.getIP);

        Intent intent = getIntent();
        getServerURL = intent.getExtras().get("url").toString();
        getIP.setText(intent.getExtras().get("url").toString());


        getImgBtn = (Button)findViewById(R.id.getImg);
        getImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 사진 갤러리 호출
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
            }
        });

        uploadImg = (Button)findViewById(R.id.uploadImg);
        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),getImgURL,Toast.LENGTH_SHORT).show();
                /**
                 * getImgBtn 버튼 클릭을 통해, 업로드할 사진의 절대경로를 가져옴
                 * 이를 어떻게 서버로 보내야할까??
                 */


                uploadFile(getImgURL , getImgName);
            }
        });


    }

    // 선택된 이미지 가져오기
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        Toast.makeText(getBaseContext(), "resultCode : "+resultCode,Toast.LENGTH_SHORT).show();

        if(requestCode == REQ_CODE_SELECT_IMAGE)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                try {
                    //Uri에서 이미지 이름을 얻어온다.
                    String name_Str = getImageNameToUri(data.getData());
                    getImgURL = name_Str;

                    Log.i("myTag",name_Str);

                    //이미지 데이터를 비트맵으로 받아온다.
                    Bitmap image_bitmap 	= MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    ImageView image = (ImageView)findViewById(R.id.imageView1);

                    //배치해놓은 ImageView에 set
                    image.setImageBitmap(image_bitmap);

                    //Toast.makeText(getBaseContext(), "name_Str : "+name_Str , Toast.LENGTH_SHORT).show();

                }
                catch (FileNotFoundException e) { 		e.printStackTrace(); 			}
                catch (IOException e)                 {		e.printStackTrace(); 			}
                catch (Exception e)		         {             e.printStackTrace();			}
            }
        }
    }


    // 선택된 이미지 파일명 가져오기
    public String getImageNameToUri(Uri data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        getImgURL = imgPath;
        getImgName = imgName;

        return "success";
    }


    /**
     * Upload Image Client Code
     */

    private void uploadFile(String ImgURL, String ImgName) {

        //IP주소 변경필요!
        String url = getServerURL;

        //file_name이 이름 or 절대경로 말하는 것인지??
        String file_name = ImgName;

        // create upload service client
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .build();

        NetworkService service = retrofit.create(NetworkService.class);


        File file = new File(Environment.getExternalStorageDirectory().toString(), file_name);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", file.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);

        // finally, execute the request
        Call<ResponseBody> call = service.upload(description, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }


}
