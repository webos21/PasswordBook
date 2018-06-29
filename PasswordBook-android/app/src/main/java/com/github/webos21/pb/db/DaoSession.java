package com.github.webos21.pb.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.github.webos21.pb.db.Account;
import com.github.webos21.pb.db.Category;
import com.github.webos21.pb.db.AcctType;

import com.github.webos21.pb.db.AccountDao;
import com.github.webos21.pb.db.CategoryDao;
import com.github.webos21.pb.db.AcctTypeDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig accountDaoConfig;
    private final DaoConfig categoryDaoConfig;
    private final DaoConfig acctTypeDaoConfig;

    private final AccountDao accountDao;
    private final CategoryDao categoryDao;
    private final AcctTypeDao acctTypeDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        accountDaoConfig = daoConfigMap.get(AccountDao.class).clone();
        accountDaoConfig.initIdentityScope(type);

        categoryDaoConfig = daoConfigMap.get(CategoryDao.class).clone();
        categoryDaoConfig.initIdentityScope(type);

        acctTypeDaoConfig = daoConfigMap.get(AcctTypeDao.class).clone();
        acctTypeDaoConfig.initIdentityScope(type);

        accountDao = new AccountDao(accountDaoConfig, this);
        categoryDao = new CategoryDao(categoryDaoConfig, this);
        acctTypeDao = new AcctTypeDao(acctTypeDaoConfig, this);

        registerDao(Account.class, accountDao);
        registerDao(Category.class, categoryDao);
        registerDao(AcctType.class, acctTypeDao);
    }
    
    public void clear() {
        accountDaoConfig.getIdentityScope().clear();
        categoryDaoConfig.getIdentityScope().clear();
        acctTypeDaoConfig.getIdentityScope().clear();
    }

    public AccountDao getAccountDao() {
        return accountDao;
    }

    public CategoryDao getCategoryDao() {
        return categoryDao;
    }

    public AcctTypeDao getAcctTypeDao() {
        return acctTypeDao;
    }

}
