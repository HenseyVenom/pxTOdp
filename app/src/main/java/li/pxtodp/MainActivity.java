package li.pxtodp;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by li wen hao on 2016/7/26.
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner              displaySpinner;
    EditText             pxEdit;
    TextView             convertValue;
    List<DisplayMetrics> standardScreenDensityList;
    ProgressDialog       loadDialog;
    boolean              zeroStart;

    private static final int LOAD_ASSETS = 0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (LOAD_ASSETS == msg.what) {
                loadFinish();
            }
        }
    };

    private void loadFinish() {
        loadDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displaySpinner = (Spinner) findViewById(R.id.display_metrics);
        pxEdit = (EditText) findViewById(R.id.px_value);
        convertValue = (TextView) findViewById(R.id.dp_value);
        loadDialog = new ProgressDialog(this);
        loadDialog.setMessage(getString(R.string.loading));
        loadDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream stream = getAssets().open("display_list");
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(stream));
                    String densityString = "";
                    String line;
                    while ((line = br.readLine()) != null) {
                        densityString += line;
                    }
                    Gson gson = new Gson();
                    standardScreenDensityList = gson.fromJson(densityString, new TypeToken<List<DisplayMetrics>>() {
                    }.getType());
                    handler.sendEmptyMessage(LOAD_ASSETS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        displaySpinner.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item,
                getResources().getStringArray(R.array.mobile_display_metrics_array)));
        displaySpinner.setOnItemSelectedListener(this);

        pxEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                zeroStart = "0".equals(s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int dpValue = 0;
                if (!TextUtils.isEmpty(s)) {
                    if (!zeroStart) {
                        if (standardScreenDensityList != null && standardScreenDensityList.size() > 0) {
                            int position = displaySpinner.getSelectedItemPosition();
                            int pxValue  = Integer.parseInt(s.toString());
                            dpValue = getDipValue(pxValue, standardScreenDensityList.get(position).density);
                        }
                    } else {
                        if (s.length() > 1) {
                            CharSequence zeroBehind = s.subSequence(1, 2);
                            if (!"0".equals(zeroBehind)) {
                                setEditText(zeroBehind.toString());
                            } else {
                                setEditText("0");
                            }
                        }
                    }
                } else {
                    setEditText("0");
                }
                convertValue.setText(getString(R.string.dp_valus_with_unit, dpValue + ""));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        pxEdit.setText("0");
    }

    private void setEditText(String text) {
        pxEdit.setText(text);
        pxEdit.setSelection(pxEdit.getText().length());
    }

    private int getDipValue(int px, float density) {
        return (int) (px / density + 0.5f);
    }

    private int getSpValue(int px, float scaledDensity) {
        return (int) (px / scaledDensity + 0.5f);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setEditText(pxEdit.getText().toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
