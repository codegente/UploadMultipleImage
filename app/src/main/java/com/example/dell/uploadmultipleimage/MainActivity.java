package com.example.dell.uploadmultipleimage;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nileshp.multiphotopicker.photopicker.activity.PickImageActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


// CODE CREATED BY CODEGENTE.COM ( PANKAZ KUMAR MITTAL )

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView messageText, noImage;
    private Button uploadButton, btnselectpic;
    private EditText etxtUpload;
    private ProgressDialog dialog = null;
    private JSONObject jsonObject;
    private int PICK_IMAGE_REQUEST = 1;
    ArrayList<Uri> imagesUriList;
    ArrayList<String> encodedImageList;

    ArrayList<String> pathList;
    String imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadButton = (Button)findViewById(R.id.uploadButton);
        btnselectpic = (Button)findViewById(R.id.button_selectpic);
        messageText  = (TextView)findViewById(R.id.messageText);
        noImage  = (TextView)findViewById(R.id.noImage);
        etxtUpload = (EditText)findViewById(R.id.etxtUpload);

        btnselectpic.setOnClickListener(this);
        uploadButton.setOnClickListener(this);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        jsonObject = new JSONObject();
        encodedImageList = new ArrayList<>();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.button_selectpic:

                Intent mIntent = new Intent(this, PickImageActivity.class);
                mIntent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 60);
                mIntent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 1);
                startActivityForResult(mIntent, PickImageActivity.PICKER_REQUEST_CODE);

                break;
            case R.id.uploadButton:
                dialog.show();

                JSONArray jsonArray = new JSONArray();

                if (encodedImageList.isEmpty()){
                    Toast.makeText(this, "Please select some images first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (String encoded: encodedImageList){
                    jsonArray.put(encoded);
                }
                try {
                    jsonObject.put(Utils.imageName, etxtUpload.getText().toString().trim());
                    jsonObject.put(Utils.imageList, jsonArray);
                } catch (JSONException e) {
                    Log.e("JSONObject Here", e.toString());
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Utils.urlUpload, jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                Log.e("Message from server", jsonObject.toString());
                                dialog.dismiss();
                                messageText.setText("Images Uploaded Successfully");
                                Toast.makeText(getApplicationContext(), "Images Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("Message from server", volleyError.toString());
                        Toast.makeText(getApplicationContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy( 200*30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                Volley.newRequestQueue(this).add(jsonObjectRequest);
                break;
        }

    }


  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
      super.onActivityResult(requestCode, resultCode, intent);
      if (resultCode != RESULT_OK) {
          return;
      }
      if (resultCode == -1 && requestCode == PickImageActivity.PICKER_REQUEST_CODE) {
          this.pathList = intent.getExtras().getStringArrayList(PickImageActivity.KEY_DATA_RESULT);
          if (this.pathList != null && !this.pathList.isEmpty()) {
              StringBuilder sb=new StringBuilder("");
              for(int i=0;i<pathList.size();i++) {

                  encodedImageList.clear();

                  Bitmap bitmap = null;
                  try {
                      bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),  Uri.fromFile(new File(pathList.get(i))));
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
                  ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                              bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                              String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

                              encodedImageList.add(encodedImage);


                  sb.append("Photo"+(i+1)+":"+pathList.get(i));
                  sb.append("\n");
                      }
                  }
              }
          }
      }


