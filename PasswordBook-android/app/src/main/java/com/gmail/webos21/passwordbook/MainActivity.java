package com.gmail.webos21.passwordbook;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.webos21.android.patch.PRNGFixes;
import com.gmail.webos21.android.widget.ChooseFileDialog;
import com.gmail.webos21.crypto.Base64WebSafe;
import com.gmail.webos21.crypto.CryptoHelper;
import com.gmail.webos21.passwordbook.db.PbDbInterface;
import com.gmail.webos21.passwordbook.db.PbDbManager;
import com.gmail.webos21.passwordbook.db.PbExporter;
import com.gmail.webos21.passwordbook.db.PbImporter;
import com.gmail.webos21.passwordbook.db.PbRow;
import com.gmail.webos21.passwordbook.db.PbRowAdapter;

import java.io.File;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private boolean loginFlag;

    private NavigationView navigationView;

    private CheckBox cbIcon;
    private TextView tvTotalSite;

    private ListView pblist;
    private PbRowAdapter pbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Consts.DEBUG) {
            Log.i(TAG, "onCreate!!!!!!!!!!!!!");
        }

        // Android SecureRandom Fix!!! (No Dependency)
        PRNGFixes.apply();

        // Set Tool-Bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set Drawer-Layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            //noinspection deprecation
            drawer.setDrawerListener(toggle);
            toggle.syncState();
        }

        // Set Navigation-View
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavItemSelected());
        }

        // Set FloatingActionButton
        FloatingActionButton fabInputOne = (FloatingActionButton) findViewById(R.id.fab_input_one);
        fabInputOne.setOnClickListener(this);
        FloatingActionButton fabImportCsv = (FloatingActionButton) findViewById(R.id.fab_import_csv);
        fabImportCsv.setOnClickListener(this);
        FloatingActionButton fabExportCsv = (FloatingActionButton) findViewById(R.id.fab_export_csv);
        fabExportCsv.setOnClickListener(this);

        // Check App Key
        SharedPreferences pref = getSharedPreferences(Consts.PREF_FILE, MODE_PRIVATE);
        String appkey = pref.getString(Consts.PREF_APPKEY, "");
        if (appkey == null || appkey.length() == 0) {
            SecretKey sk = CryptoHelper.genAESKey();
            if (Consts.DEBUG) {
                Log.i(TAG, "sk = " + sk.toString());
            }
            String savekey = Base64WebSafe.encode(sk.getEncoded());
            if (Consts.DEBUG) {
                Log.i(TAG, "savekey = " + savekey);
            }
            SharedPreferences.Editor shEdit = pref.edit();
            shEdit.putString(Consts.PREF_APPKEY, savekey);
            shEdit.commit();
        } else {
            if (Consts.DEBUG) {
                Log.i(TAG, "appkey = " + appkey);
            }
            byte[] decKey = Base64WebSafe.decode(appkey);
            SecretKey sk = new SecretKeySpec(decKey, 0, decKey.length, "AES");
            if (Consts.DEBUG) {
                Log.i(TAG, "sk = " + sk.toString());
            }
        }

        // Set Views
        boolean bIconShow = pref.getBoolean(Consts.PREF_SHOW_ICON, false);
        cbIcon = (CheckBox) findViewById(R.id.chk_icon_show);
        cbIcon.setChecked(bIconShow);
        cbIcon.setOnCheckedChangeListener(new ShowConfigListener());

        tvTotalSite = (TextView) findViewById(R.id.tv_total_site);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchAdapter(pbAdapter));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Consts.DEBUG) {
            Log.i(TAG, "onStart!!!!!!!!!!!!!");
        }

        SharedPreferences pref = getSharedPreferences(Consts.PREF_FILE, MODE_PRIVATE);
        String passkey = pref.getString(Consts.PREF_PASSKEY, "");
        if (passkey == null || passkey.length() == 0) {
            Intent i = new Intent(this, AuthConfigActivity.class);
            startActivityForResult(i, Consts.ACTION_PASS_CFG);
            return;
        }

        if (!loginFlag) {
            Intent i = new Intent(this, AuthActivity.class);
            startActivityForResult(i, Consts.ACTION_LOGIN);
            return;
        }

        // Request Permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Consts.PERM_REQ_EXTERNAL_STORAGE);
            return;
        }

        // Set Main-ListView
        pbAdapter = new PbRowAdapter(this, pref.getBoolean(Consts.PREF_SHOW_ICON, false));
        pblist = (ListView) findViewById(R.id.lv_container);
        pblist.setAdapter(pbAdapter);
        pblist.setOnItemClickListener(new PbRowClickedListener());

        // Set Views
        tvTotalSite.setText(getResources().getString(R.string.cfg_total_item) + pbAdapter.getCount());
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (Consts.DEBUG) {
            Log.i(TAG, "onStop!!!!!!!!!!!!!");
        }
        loginFlag = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (Consts.DEBUG) {
            Log.i(TAG, "onDestroy!!!!!!!!!!!!!");
        }
        loginFlag = false;
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        switch (vId) {
            case R.id.fab_input_one: {
                Intent i = new Intent(this, PbAddActivity.class);
                startActivityForResult(i, Consts.ACTION_ADD);
                break;
            }
            case R.id.fab_import_csv:
                showFileDialog();
                break;
            case R.id.fab_export_csv:
                exportCsv();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (Consts.DEBUG) {
            Log.i(TAG, "onRequestPermissionsResult!!!!!!!!!!!!!");
        }
        if (requestCode == Consts.PERM_REQ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // OK, nothing to do
            } else {
                Toast.makeText(this, getResources().getString(R.string.err_perm_exflah), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Consts.DEBUG) {
            Log.i(TAG, "onActivityResult!!!!!!!!!!!!!");
        }
        if (requestCode == Consts.ACTION_PASS_CFG) {
            if (resultCode == RESULT_OK) {
                // Nothing to do
            } else {
                finish();
            }
        }
        if (requestCode == Consts.ACTION_LOGIN) {
            if (resultCode == RESULT_OK) {
                loginFlag = true;
            } else {
                finish();
            }
        }
        if (requestCode == Consts.ACTION_ADD) {
            if (resultCode == RESULT_OK) {
                loginFlag = true;
            }
        }
        if (requestCode == Consts.ACTION_MODIFY) {
            if (resultCode == RESULT_OK) {
                loginFlag = true;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showFileDialog() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Consts.PERM_REQ_EXTERNAL_STORAGE);
            return;
        }

        String mountPoint = Environment.getExternalStorageDirectory().toString();
        ChooseFileDialog cfd = new ChooseFileDialog(this, mountPoint, "csv");
        cfd.setOnFileChosenListener(new CsvFileSelectedListener());
        cfd.show();
    }

    private void exportCsv() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Consts.PERM_REQ_EXTERNAL_STORAGE);
            return;
        }

        String mountPoint = Environment.getExternalStorageDirectory().toString();
        File csvFile = new File(mountPoint + "/Download", "exp.csv");
        PbDbInterface pdi = PbDbManager.getInstance().getPbDbInterface();

        new PbExporter(pdi, csvFile, new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "File is exported!!", Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    private class NavItemSelected implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.nav_cur_pos:
                    break;

                case R.id.nav_settings: {
                    Intent i = new Intent(MainActivity.this, AuthConfigActivity.class);
                    MainActivity.this.startActivityForResult(i, Consts.ACTION_PASS_CFG);
                    break;
                }
                default:
                    break;
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            navigationView.getMenu().getItem(0).setChecked(true);

            return true;
        }
    }

    private class ShowConfigListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int vId = buttonView.getId();
            switch (vId) {
                case R.id.chk_icon_show: {
                    SharedPreferences pref = getSharedPreferences(Consts.PREF_FILE, MODE_PRIVATE);
                    pref.edit().putBoolean(Consts.PREF_SHOW_ICON, isChecked).commit();
                    MainActivity.this.pbAdapter.setShowIcon(isChecked);
                    break;
                }
                default:
                    break;
            }
        }
    }

    private class PbRowClickedListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object o = parent.getItemAtPosition(position);
            if (o instanceof PbRow) {
                final PbRow pbrow = (PbRow) o;
                if (Consts.DEBUG) {
                    Log.i(TAG, "o is PbRow!!!!!!");
                    Log.i(TAG, "id = " + pbrow.getId());
                    Log.i(TAG, "name = " + pbrow.getSiteName());
                    Log.i(TAG, "url = " + pbrow.getSiteUrl());
                }
                Intent i = new Intent(MainActivity.this, PbEditActivity.class);
                i.putExtra(Consts.EXTRA_ID, pbrow.getId());
                MainActivity.this.startActivityForResult(i, Consts.ACTION_MODIFY);
            } else {
                if (Consts.DEBUG) {
                    Log.i(TAG, "o is not PbRow!!!!!!");
                }
            }
        }
    }

    private class SearchAdapter implements SearchView.OnQueryTextListener {

        private PbRowAdapter myAdapter;

        public SearchAdapter(PbRowAdapter pbAdapter) {
            this.myAdapter = pbAdapter;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            this.myAdapter.searchItems(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText == null || newText.length() == 0) {
                this.myAdapter.searchAll();
            }
            return false;
        }
    }

    private class CsvFileSelectedListener implements ChooseFileDialog.FileChosenListener {
        @Override
        public void onFileChosen(File chosenFile) {
            PbDbInterface pdi = PbDbManager.getInstance().getPbDbInterface();
            new PbImporter(pdi, chosenFile, new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.pbAdapter.notifyDataSetChanged();
                }
            }).execute();
        }
    }

}
