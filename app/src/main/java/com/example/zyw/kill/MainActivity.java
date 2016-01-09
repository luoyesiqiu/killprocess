package com.example.zyw.kill;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sp;
    Button bnClear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bnClear=(Button)findViewById(R.id.bn_clear);
        sp=getSharedPreferences("data",MODE_PRIVATE);
        bnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //如果安卓版本大于4.4
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {

                    if(sp.getBoolean("isFirst",true)==true)
                    {
                        toast(getResources().getString(R.string.open));
                        Intent usageAccessIntent = new Intent( Settings.ACTION_USAGE_ACCESS_SETTINGS );
                        usageAccessIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                        startActivity( usageAccessIntent );
                        sp.edit().putBoolean("isFirst",false).commit();
                    }else {
                        Intent intent = new Intent(MainActivity.this, ClearService.class);
                        startService(intent);
                    }
                }
                else
                {
                    Intent intent=new Intent(MainActivity.this,ClearService.class);
                    startService(intent);
                }


            }
        });
    }
    public void toast(CharSequence text)
    {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

}
