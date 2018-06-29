package com.github.webos21.pb;

import android.app.Application;

import com.github.webos21.pb.db.AccountHelper;
import com.github.webos21.pb.db.CategoryHelper;
import com.github.webos21.pb.db.TypeHelper;
import com.github.webos21.pb.utils.CategoryUtil;
import com.github.webos21.pb.utils.EnvUtil;
import com.github.webos21.pb.utils.ResUtil;
import com.github.webos21.pb.utils.UserDefault;

/**
 * Created by bob.sun on 16/3/19.
 */
public class App extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        AccountHelper.getInstance(getApplicationContext());
        CategoryHelper categoryHelper = CategoryHelper.getInstance(getApplicationContext());
        TypeHelper typeHelper = TypeHelper.getInstance(getApplicationContext());

        if (categoryHelper.getAllCategory() == null || categoryHelper.getAllCategory().size() == 0) {
            CategoryUtil.getInstance(getApplicationContext()).initBuiltInCategories();
        }

        if (typeHelper.getAllTypes() == null || typeHelper.getAllTypes().size() == 0) {
            CategoryUtil.getInstance(getApplicationContext()).initBuiltInTypes();
        }

        ResUtil.getInstance(getApplicationContext());
        CategoryUtil.getInstance(getApplicationContext());
        EnvUtil.getInstance(getApplicationContext());
        UserDefault.getInstance(getApplicationContext());

    }
}
