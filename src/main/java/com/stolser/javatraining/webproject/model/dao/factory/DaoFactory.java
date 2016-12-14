package com.stolser.javatraining.webproject.model.dao.factory;

import com.stolser.javatraining.webproject.model.dao.invoice.InvoiceDao;
import com.stolser.javatraining.webproject.model.dao.credential.CredentialDao;
import com.stolser.javatraining.webproject.model.dao.periodical.PeriodicalDao;
import com.stolser.javatraining.webproject.model.dao.role.RoleDao;
import com.stolser.javatraining.webproject.model.dao.subscription.SubscriptionDao;
import com.stolser.javatraining.webproject.model.dao.user.UserDao;

import java.sql.Connection;

public abstract class DaoFactory {
    public abstract PeriodicalDao getPeriodicalDao(Connection conn);
    public abstract CredentialDao getCredentialDao(Connection conn);
    public abstract UserDao getUserDao(Connection conn);
    public abstract RoleDao getRoleDao(Connection conn);
    public abstract SubscriptionDao getSubscriptionDao(Connection conn);
    public abstract InvoiceDao getInvoiceDao(Connection conn);

    public static DaoFactory getMysqlDaoFactory() {
        return new MysqlDaoFactory();
    }
}
