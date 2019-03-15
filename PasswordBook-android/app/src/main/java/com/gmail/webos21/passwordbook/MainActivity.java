package com.gmail.webos21.passwordbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import android.widget.ListView;

import com.gmail.webos21.android.patch.PRNGFixes;
import com.gmail.webos21.crypto.Base64WebSafe;
import com.gmail.webos21.crypto.CryptoHelper;
import com.gmail.webos21.passwordbook.db.PbRow;
import com.gmail.webos21.passwordbook.db.PbRowAdapter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private boolean loginFlag;

    private NavigationView navigationView;
    private ListView pblist;
    private PbRowAdapter pbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        FloatingActionButton fabInputMass = (FloatingActionButton) findViewById(R.id.fab_input_mass);
        fabInputMass.setOnClickListener(this);

        // Set Main-ListView
        pbAdapter = new PbRowAdapter(this);
        pblist = (ListView) findViewById(R.id.lv_container);
        pblist.setAdapter(pbAdapter);
        pblist.setOnItemClickListener(new PbRowClickedListener());

        // Check App Key
        SharedPreferences pref = getSharedPreferences(Consts.PREF_FILE, MODE_PRIVATE);
        String appkey = pref.getString(Consts.PREF_APPKEY, "");
        if (appkey == null || appkey.length() == 0) {
            SecretKey sk = CryptoHelper.genAESKey();
            Log.i(TAG, "sk = " + sk.toString());

            String savekey = Base64WebSafe.encode(sk.getEncoded());
            Log.i(TAG, "savekey = " + savekey);
            SharedPreferences.Editor shEdit = pref.edit();
            shEdit.putString(Consts.PREF_APPKEY, savekey);
            shEdit.commit();
        } else {
            Log.i(TAG, "appkey = " + appkey);
            byte[] decKey = Base64WebSafe.decode(appkey);
            SecretKey sk = new SecretKeySpec(decKey, 0, decKey.length, "AES");
            Log.i(TAG, "sk = " + sk.toString());
        }
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
    }

    @Override
    protected void onStop() {
        loginFlag = false;
        super.onStop();
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
            case R.id.fab_input_mass:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                MainActivity.this.pblist.invalidate();
            }
        }
        if (requestCode == Consts.ACTION_MODIFY) {
            if (resultCode == RESULT_OK) {
                MainActivity.this.pblist.invalidate();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class NavItemSelected implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.nav_cur_pos:
                    break;
                default:
                    break;
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            navigationView.getMenu().getItem(0).setChecked(true);

            return true;
        }

    }

    private class PbRowClickedListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object o = parent.getItemAtPosition(position);
            if (o instanceof PbRow) {
                Log.i(TAG, "o is PbRow!!!!!!");
                PbRow pbrow = (PbRow) o;

                Log.i(TAG, "name = " + pbrow.getSiteName());
                Log.i(TAG, "url = " + pbrow.getSiteUrl());

                Intent i = new Intent(MainActivity.this, PbEditActivity.class);
                i.putExtra(Consts.EXTRA_ID, pbrow.getId());
                startActivityForResult(i, Consts.ACTION_MODIFY);
            } else {
                Log.i(TAG, "o is not PbRow!!!!!!");
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
            MainActivity.this.pblist.invalidate();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText == null || newText.length() == 0) {
                this.myAdapter.searchAll();
                MainActivity.this.pblist.invalidate();
            }
            return false;
        }
    }

}
