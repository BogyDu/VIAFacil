package com.example.imageclassificationdemo;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;






import android.speech.tts.TextToSpeech;



import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;




import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import java.util.Locale;
import java.util.Map;




import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f;
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f;
    public String js;
    public FloatingActionButton fab;
    public FloatingActionButton fab1;
    public ArrayList<String> n = new ArrayList<>();
    public ArrayList<String> d = new ArrayList<>();
    protected Interpreter tflite;
    TextToSpeech t1;
    ImageView imageView;
    Uri imageuri;
    TextView classitext;
    TextView descr;
    Dialog mydialog;
    String ruta;
    private TensorImage inputImageBuffer;
    private int imageSizeX;
    private int imageSizeY;
    private TensorBuffer outputProbabilityBuffer;
    private TensorProcessor probabilityProcessor;
    private Bitmap bitmap;
    private List<String> labels;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageView = findViewById(R.id.image);
        ImageButton btnG = findViewById(R.id.gal);
        ImageButton btnC = findViewById(R.id.cam);
        classitext = findViewById(R.id.classifytext);
        descr = findViewById(R.id.descripcion);

        fab = findViewById(R.id.pa);
        fab1 = findViewById(R.id.pl);



        validarpermisos();


        btnG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 12);
            }
        });

        btnC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara();
            }
        });


        fab.setElevation(99);
        fab1.setElevation(90);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t1.stop();

                fab.setElevation(90);
                fab1.setElevation(99);
            }
        });



        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setElevation(99);
                fab1.setElevation(90);

                t1.speak(descr.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });




        try {
            tflite = new Interpreter(loadmodelfile(this));
        } catch (Exception e) {
            e.printStackTrace();
        }



        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(new Locale("spa", "ESP"));
                }
            }
        });





        mydialog = new Dialog(this);


    }




    private boolean validarpermisos() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if ((checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }
        if ((shouldShowRequestPermissionRationale(CAMERA)) ||
                (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))) {
            cargardialogo();
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }
    private void cargardialogo() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Permisos no concedidos :D");
        dialog.setMessage("Acepta porfa los permisos para poder utilizar el maximo de la aplcicacion");
        dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
            }
        });

        dialog.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.informacion) {
            showPopUp();
        }


        return super.onOptionsItemSelected(item);
    }
    public void showPopUp() {
        mydialog.setContentView(R.layout.info);
        mydialog.show();
    }
    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {

            File imagenArch = null;

            try {
                imagenArch = crearImagen();
            } catch (IOException exception) {

            }

            if (imagenArch != null) {
                Uri fotoUri = FileProvider.getUriForFile(this, "com.example.imageclassificationdemo", imagenArch);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);

                startActivityForResult(intent, 1);
            }

        }
    }
    private TensorImage loadImage(final Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);
        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(getPreprocessNormalizeOp())
                        .build();

        return imageProcessor.process(inputImageBuffer);
    }
    private MappedByteBuffer loadmodelfile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startoffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength);
    }
    private TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }
    private TensorOperator getPostprocessNormalizeOp() {
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }


    private void showresult() throws IOException, JSONException {

        String dat = null;
        try {
            labels = FileUtil.loadLabels(this, "labels.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                        .getMapWithFloatValue();
        float maxValueInMap = (Collections.max(labeledProbability.values()));
        float prob = 0.0f;
        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
            if (entry.getValue() == maxValueInMap) {
                classitext.setText(entry.getKey());
                prob = entry.getValue();
                dat = entry.getKey();
            }
        }
        float probabilidad = (prob * 100000) - 300;

        try {
            InputStream is = getAssets().open("values.json");
            int size = is.available();
            byte[] bfr = new byte[size];
            is.read(bfr);
            is.close();
            js = new String(bfr, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        //LECTURA DE JSON PARA COPROBAR SI EL ENCONTRADO CONTIENE DESCRIPCION
        JSONObject jsonObject = new JSONObject(js);
        JSONArray jsonArray = jsonObject.getJSONArray("datos");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject nom = jsonArray.getJSONObject(i);
            n.add(nom.getString("n"));
            d.add(nom.getString("d"));
        }


        for (int j = 0; j < n.size(); j++) {
            if (n.get(j).equals(dat)) {
                descr.setText(d.get(j));
                fab.setElevation(99);
                fab1.setElevation(90);
            }
        }


        t1.setLanguage(new Locale("spa", "ESP"));
        if (probabilidad < 60.00f) {
            mydialog.setContentView(R.layout.adver);
            mydialog.show();

            fab.setElevation(90);
            fab1.setElevation(99);
        }else{
                t1.speak(descr.getText().toString(),TextToSpeech.QUEUE_ADD, null);
        }

    }


    private void cc(Bitmap btmp) throws IOException, JSONException {


        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape();
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();

        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape();
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        inputImageBuffer = new TensorImage(imageDataType);
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();


        inputImageBuffer = loadImage(btmp);

        tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());
        showresult();

    }


    private File crearImagen() throws IOException {
        String nombre = "Img_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File img = File.createTempFile(nombre, ".jpg", dir);
        ruta = img.getAbsolutePath();

        return img;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12 && resultCode == RESULT_OK && data != null) {

            imageuri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri);
                imageView.setImageBitmap(bitmap);

                cc(bitmap);


            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }


        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            Bitmap imgBitmap = BitmapFactory.decodeFile(ruta);
            imageView.setImageBitmap(imgBitmap);
            try {
                cc(imgBitmap);
            } catch (IOException exception) {
                exception.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}

