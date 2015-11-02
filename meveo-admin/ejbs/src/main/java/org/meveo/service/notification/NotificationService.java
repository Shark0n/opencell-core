package org.meveo.service.notification;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.cache.NotificationCacheContainerProvider;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.admin.User;
import org.meveo.model.crm.Provider;
import org.meveo.model.notification.Notification;
import org.meveo.model.notification.ScriptNotification;
import org.meveo.service.base.BusinessService;

@Stateless
public class NotificationService extends BusinessService<ScriptNotification> {

	@SuppressWarnings("unchecked")
	public List<ScriptNotification> listAll() {
		QueryBuilder qb = new QueryBuilder(Notification.class, "d");
		qb.addBooleanCriterion("disabled", false);
		return qb.getQuery(getEntityManager()).getResultList();
	}

    /**
     * Get a list of notifications to populate a cache
     * 
     * @return A list of active notifications
     */
    public List<Notification> getNotificationsForCache() {
        return getEntityManager().createNamedQuery("Notification.getNotificationsForCache", Notification.class).getResultList();
    }
    
    @Inject
    private NotificationCacheContainerProvider notificationCacheContainerProvider;

    @Override
    public void create(ScriptNotification scriptNotification, User creator, Provider provider) {
        super.create(scriptNotification, creator, provider);
        notificationCacheContainerProvider.addNotificationToCache(scriptNotification);
    }

    @Override
    public ScriptNotification update(ScriptNotification scriptNotification, User updater) {
    	scriptNotification = super.update(scriptNotification, updater);
        notificationCacheContainerProvider.updateNotificationInCache(scriptNotification);
        return scriptNotification;
    }

    @Override
    public void remove(ScriptNotification scriptNotification) {
        super.remove(scriptNotification);
        notificationCacheContainerProvider.removeNotificationFromCache(scriptNotification);
    }

    @Override
    public ScriptNotification disable(ScriptNotification scriptNotification) {
    	scriptNotification = super.disable(scriptNotification);
        notificationCacheContainerProvider.removeNotificationFromCache(scriptNotification);
        return scriptNotification;
    }

    @Override
    public ScriptNotification enable(ScriptNotification scriptNotification) {
    	scriptNotification = super.enable(scriptNotification);
        notificationCacheContainerProvider.addNotificationToCache(scriptNotification);
        return scriptNotification;
    }
}
