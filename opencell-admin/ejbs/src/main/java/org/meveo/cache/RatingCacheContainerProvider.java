package org.meveo.cache;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.meveo.model.CounterValueChangeInfo;
import org.meveo.model.billing.CounterInstance;
import org.meveo.model.billing.CounterPeriod;
import org.meveo.model.billing.UsageChargeInstance;
import org.meveo.model.catalog.Calendar;
import org.meveo.model.catalog.PricePlanMatrix;
import org.meveo.model.catalog.TriggeredEDRTemplate;
import org.meveo.model.catalog.UsageChargeTemplate;
import org.meveo.service.billing.impl.UsageChargeInstanceService;
import org.meveo.service.catalog.impl.CalendarService;
import org.meveo.service.catalog.impl.PricePlanMatrixService;
import org.meveo.service.catalog.impl.UsageChargeTemplateService;
import org.slf4j.Logger;

/**
 * Provides cache related services (loading, update) for rating related operations
 * 
 * @author Andrius Karpavicius
 * 
 */
@Startup
@Singleton
@Lock(LockType.READ)
public class RatingCacheContainerProvider implements Serializable { // CacheContainerProvider, Serializable {

    private static final long serialVersionUID = -8177539254337953136L;

    public static String COUNTER_CACHE = "counterCache";

    @Inject
    protected Logger log;

    @EJB
    private PricePlanMatrixService pricePlanMatrixService;

    @EJB
    private UsageChargeInstanceService usageChargeInstanceService;

    @EJB
    private UsageChargeTemplateService usageChargeTemplateService;

    @Inject
    private CalendarService calendarService;

    /**
     * Contains association between charge code and price plans. Key format: <charge template code, which is pricePlanMatrix.eventCode>, value: List of <PricePlanMatrix entity>
     */
    @Resource(lookup = "java:jboss/infinispan/cache/opencell/opencell-price-plan")
    private Cache<String, List<PricePlanMatrix>> pricePlanCache;

    /**
     * Contains association between usage charge template ID and cached usage charge template information. Key format: UsageChargeTemplate.id, value: <DTO version of
     * UsageChargeTemplate>
     */
    @Resource(lookup = "java:jboss/infinispan/cache/opencell/opencell-usage-charge-template-cache")
    private Cache<Long, CachedUsageChargeTemplate> usageChargeTemplateCache;

    /**
     * Contains association between subscription and usage charge instances. Key format: Subscription.id, value: List of <DTO version of UsageChargeInstance>
     */
    @Resource(lookup = "java:jboss/infinispan/cache/opencell/opencell-charge-instance-cache")
    private Cache<Long, List<CachedUsageChargeInstance>> usageChargeInstanceCache;

    /**
     * Contains association between counter instance ID and cached counter information. Key format: CounterInstance.id, value: <DTO version of CounterInstance>
     */
    @Resource(lookup = "java:jboss/infinispan/cache/opencell/opencell-counter-cache")
    private Cache<Long, CachedCounterInstance> counterCache;

    // @Resource(name = "java:jboss/infinispan/container/meveo")
    // private CacheContainer meveoContainer;

    @PostConstruct
    private void init() {
        try {
            log.debug("RatingCacheContainerProvider initializing...");
            // pricePlanCache = meveoContainer.getCache("meveo-price-plan");
            // usageChargeTemplateCache = meveoContainer.getCache("meveo-usage-charge-template-cache-cache");
            // usageChargeInstanceCache = meveoContainer.getCache("meveo-charge-instance-cache");
            // counterCache = meveoContainer.getCache("meveo-counter-cache");

            refreshCache(System.getProperty(CacheContainerProvider.SYSTEM_PROPERTY_CACHES_TO_LOAD));

            log.info("RatingCacheContainerProvider initialized");

        } catch (Exception e) {
            log.error("RatingCacheContainerProvider init() error", e);
            throw e;
        }
    }

    /**
     * Populate price plan cache from db
     */
    private void populatePricePlanCache() {

        log.debug("Start to populate price plan cache");

        pricePlanCache.startBatch();
        pricePlanCache.clear();

        String lastEventCode = null;
        List<PricePlanMatrix> chargePriceListSameEventCode = null;

        // Retrieve price plans ordered by event code
        List<PricePlanMatrix> activePricePlans = pricePlanMatrixService.getPricePlansForCache();

        for (PricePlanMatrix pricePlan : activePricePlans) {
            if (lastEventCode == null) {
                chargePriceListSameEventCode = new ArrayList<PricePlanMatrix>();
                lastEventCode = pricePlan.getEventCode();

            } else if (!lastEventCode.equals(pricePlan.getEventCode())) {
                Collections.sort(chargePriceListSameEventCode);
                pricePlanCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(lastEventCode, chargePriceListSameEventCode);
                chargePriceListSameEventCode = new ArrayList<PricePlanMatrix>();
                lastEventCode = pricePlan.getEventCode();
            }

            if (pricePlan.getCriteria1Value() != null && pricePlan.getCriteria1Value().length() == 0) {
                pricePlan.setCriteria1Value(null);
            }
            if (pricePlan.getCriteria2Value() != null && pricePlan.getCriteria2Value().length() == 0) {
                pricePlan.setCriteria2Value(null);
            }
            if (pricePlan.getCriteria3Value() != null && pricePlan.getCriteria3Value().length() == 0) {
                pricePlan.setCriteria3Value(null);
            }
            if (pricePlan.getCriteriaEL() != null && pricePlan.getCriteriaEL().length() == 0) {
                pricePlan.setCriteriaEL(null);
            }

            // Lazy loading workaround
            if (pricePlan.getOfferTemplate() != null) {
                pricePlan.getOfferTemplate().getCode();
            }
            if (pricePlan.getScriptInstance() != null) {
                pricePlan.getScriptInstance().getCode();
            }
            if (pricePlan.getValidityCalendar() != null) {
                preloadCache(pricePlan.getValidityCalendar());
            }

            chargePriceListSameEventCode.add(pricePlan);

            // log.trace("Added pricePlan to cache chargeCode {} ; priceplan {}", pricePlan.getEventCode(), pricePlan);
        }

        if (chargePriceListSameEventCode != null && !chargePriceListSameEventCode.isEmpty()) {
            Collections.sort(chargePriceListSameEventCode);
            pricePlanCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(lastEventCode, chargePriceListSameEventCode);
        }

        pricePlanCache.endBatch(true);

        log.info("Price plan cache populated with {} price plans", activePricePlans.size());
    }

    private void preloadCache(Calendar calendar) {
        if (calendar != null) {
            calendar = calendarService.refreshOrRetrieve(calendar);
            calendar.nextCalendarDate(new Date());
        }
    }

    /**
     * Add price plan to a cache
     * 
     * @param pricePlan Price plan to add
     */
    // @Lock(LockType.WRITE)
    public void addPricePlanToCache(PricePlanMatrix pricePlan) {

        String cacheKey = pricePlan.getEventCode();

        log.trace("Adding pricePlan {} to pricePlan cache under key {}", pricePlan.getId(), cacheKey);

        if (pricePlan.getCriteria1Value() != null && pricePlan.getCriteria1Value().length() == 0) {
            pricePlan.setCriteria1Value(null);
        }
        if (pricePlan.getCriteria2Value() != null && pricePlan.getCriteria2Value().length() == 0) {
            pricePlan.setCriteria2Value(null);
        }
        if (pricePlan.getCriteria3Value() != null && pricePlan.getCriteria3Value().length() == 0) {
            pricePlan.setCriteria3Value(null);
        }
        if (pricePlan.getCriteriaEL() != null && pricePlan.getCriteriaEL().length() == 0) {
            pricePlan.setCriteriaEL(null);
        }
        if (pricePlan.getAmountWithoutTaxEL() != null && pricePlan.getAmountWithoutTaxEL().length() == 0) {
            pricePlan.setAmountWithoutTaxEL(null);
        }
        if (pricePlan.getAmountWithTaxEL() != null && pricePlan.getAmountWithTaxEL().length() == 0) {
            pricePlan.setAmountWithTaxEL(null);
        }

        // Lazy loading workaround
        if (pricePlan.getOfferTemplate() != null) {
            pricePlan.getOfferTemplate().getCode();
        }

        if (pricePlan.getScriptInstance() != null) {
            pricePlan.getScriptInstance().getCode();
        }

        if (pricePlan.getValidityCalendar() != null) {
            preloadCache(pricePlan.getValidityCalendar());
        }

        List<PricePlanMatrix> chargePriceList = pricePlanCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(cacheKey);
        if (chargePriceList == null) {
            chargePriceList = new ArrayList<PricePlanMatrix>();
        }

        chargePriceList.add(pricePlan);
        Collections.sort(chargePriceList);

        pricePlanCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(cacheKey, chargePriceList);
    }

    /**
     * Remove price plan from a cache
     * 
     * @param pricePlan Price plan to remove
     */
    public void removePricePlanFromCache(PricePlanMatrix pricePlan) {

        String cacheKey = pricePlan.getEventCode();

        log.trace("Removing pricePlan {} from priceplan cache under key {}", pricePlan.getId(), cacheKey);

        List<PricePlanMatrix> chargePriceList = pricePlanCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(cacheKey);

        if (chargePriceList != null && !chargePriceList.isEmpty()) {
            boolean removed = chargePriceList.remove(pricePlan);
            if (removed) {
                // Remove cached value altogether if no value are left in the list
                if (chargePriceList.isEmpty()) {
                    pricePlanCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).remove(cacheKey);
                } else {
                    pricePlanCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(cacheKey, chargePriceList);
                }
                log.trace("Removed pricePlan {} from priceplan cache under key {}", pricePlan.getId(), cacheKey);
            }
        }
    }

    public void updatePricePlanInCache(PricePlanMatrix pricePlan) {
        removePricePlanFromCache(pricePlan);
        addPricePlanToCache(pricePlan);
    }

    /**
     * Get applicable price plans for a given charge code
     * 
     * @param chargeCode Charge code
     * @return A list of applicable price plans
     */
    public List<PricePlanMatrix> getPricePlansByChargeCode(String chargeCode) {
        return pricePlanCache.get(chargeCode);
    }

    /**
     * Populate usage charge related caches
     */
    private void populateUsageChargeCache() {
        log.debug("Loading usage charge cache");

        usageChargeTemplateCache.startBatch();
        usageChargeInstanceCache.startBatch();
        counterCache.startBatch();

        usageChargeTemplateCache.clear();
        usageChargeInstanceCache.clear();
        counterCache.clear();

        List<UsageChargeInstance> usageChargeInstances = usageChargeInstanceService.getAllUsageChargeInstancesForCache();
        if (usageChargeInstances != null) {
            log.debug("Loading cache for {} usage charges", usageChargeInstances.size());
            for (UsageChargeInstance usageChargeInstance : usageChargeInstances) {
                updateUsageChargeInstanceInCache(usageChargeInstance);
            }
        }

        usageChargeTemplateCache.endBatch(true);
        usageChargeInstanceCache.endBatch(true);
        counterCache.endBatch(true);

        log.info("Usage charge cache populated with {} usage charge instances", usageChargeInstances.size());
    }

    /**
     * Updated cached usageChangeInstance and related info
     * 
     * @param usageChargeInstance Usage charge instance
     */
    public void updateUsageChargeInstanceInCache(UsageChargeInstance usageChargeInstance) {

        if (usageChargeInstance == null) {
            return;
        }

        Long subscriptionId = usageChargeInstance.getServiceInstance().getSubscription().getId();

        log.debug("Updating usageChargeInstance cache with usageChargeInstance: subscription Id: {}, charge id={}, usageChargeTemplate id: {}", subscriptionId,
            usageChargeInstance.getId(), usageChargeInstance.getChargeTemplate().getId());

        CachedUsageChargeTemplate cachedChargeTemplate = usageChargeTemplateCache.get(usageChargeInstance.getChargeTemplate().getId());
        if (cachedChargeTemplate == null) {
            UsageChargeTemplate usageChargeTemplate = usageChargeTemplateService.findById(usageChargeInstance.getChargeTemplate().getId());
            cachedChargeTemplate = createOrUpdateUsageChargeTemplateInCache(usageChargeTemplate, subscriptionId);

        } else {
            cachedChargeTemplate = associateUsageChargeTemplateWithSubscription(cachedChargeTemplate, subscriptionId);
        }

        boolean cachedSubscriptionContainsCharge = false;

        CachedUsageChargeInstance cachedCharge = null;
        List<CachedUsageChargeInstance> cachedSubscriptionCharges = usageChargeInstanceCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(subscriptionId);
        if (cachedSubscriptionCharges != null) {
            for (CachedUsageChargeInstance charge : cachedSubscriptionCharges) {
                if (charge.getId() == usageChargeInstance.getId()) {
                    cachedCharge = charge;
                    cachedSubscriptionContainsCharge = true;
                }
            }
        } else {
            cachedSubscriptionCharges = new ArrayList<CachedUsageChargeInstance>();
        }

        if (cachedCharge == null) {
            cachedCharge = new CachedUsageChargeInstance();
        }

        CachedCounterInstance cachedCounterInstance = null;
        if (usageChargeInstance.getCounter() != null) {
            cachedCounterInstance = getOrAddCounterInstanceToCache(usageChargeInstance.getCounter());
        }

        cachedCharge.populateFromUsageChargeInstance(usageChargeInstance, cachedChargeTemplate, cachedCounterInstance);

        if (!cachedSubscriptionContainsCharge) {
            cachedSubscriptionCharges.add(cachedCharge);
            Collections.sort(cachedSubscriptionCharges);
        }

        usageChargeInstanceCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(subscriptionId, cachedSubscriptionCharges);

        log.debug("UsageChargeInstance " + (!cachedSubscriptionContainsCharge ? "added" : "updated") + " in usageChargeInstanceCache: subscription id {}, charge id {}",
            subscriptionId, usageChargeInstance.getId());

    }

    /**
     * Add relation between subscription and usage charge template
     * 
     * @param cachedChargeTemplate Cached usage charge template to update
     * @param subscriptionId Subscription identifier
     */
    private CachedUsageChargeTemplate associateUsageChargeTemplateWithSubscription(CachedUsageChargeTemplate cachedChargeTemplate, Long subscriptionId) {

        if (!cachedChargeTemplate.getSubscriptionIds().contains(subscriptionId)) {

            log.debug("Associating subscription {} to cached UsageChargeTemplate {}/{} ", subscriptionId, cachedChargeTemplate.getId(), cachedChargeTemplate.getCode());

            cachedChargeTemplate = usageChargeTemplateCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(cachedChargeTemplate.getId());
            cachedChargeTemplate.getSubscriptionIds().add(subscriptionId);

            usageChargeTemplateCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(cachedChargeTemplate.getId(), cachedChargeTemplate);
        }

        return cachedChargeTemplate;
    }

    /**
     * Update usage charge template in cache
     * 
     * @param usageChargeTemplate Usage charge template to update
     * @param subscriptionId Id of subscription to relate template to
     * @return Cached usage charge template
     */
    public CachedUsageChargeTemplate createOrUpdateUsageChargeTemplateInCache(UsageChargeTemplate usageChargeTemplate, Long subscriptionId) {

        if (usageChargeTemplate == null) {
            return null;
        }

        CachedUsageChargeTemplate cachedTemplate = null;
        boolean priorityChanged = false;
        boolean codeChanged = false;
        boolean addToCache = false;
        if (usageChargeTemplateCache.containsKey(usageChargeTemplate.getId())) {
            cachedTemplate = usageChargeTemplateCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(usageChargeTemplate.getId());
            priorityChanged = cachedTemplate.getPriority() != usageChargeTemplate.getPriority();
            codeChanged = !cachedTemplate.getCode().equals(usageChargeTemplate.getCode());
            cachedTemplate.populateFromUsageChargeTemplate(usageChargeTemplate);

        } else {
            cachedTemplate = new CachedUsageChargeTemplate(usageChargeTemplate);
            addToCache = true;
        }

        // Update priority and chargeTemplateCode fields in CachedUsageChargeInstance class if code or priority has changed
        // If priority has changed then reorder all cacheInstance associated to this template. Priority can change only when chargeTemplate was cached earlier, as for new
        // chargeTemplates, no subscriptions are linked yet, thus nothing to reorder.
        if (codeChanged || priorityChanged) {
            for (Long subscId : cachedTemplate.getSubscriptionIds()) {
                updateAndReorderUserChargeInstancesInCache(subscId, cachedTemplate, priorityChanged);
            }
        }

        // Covers a case when new subscription is added. Usage charge ordering in that subscription is happening in updateUsageChargeInstanceInCache() method
        if (subscriptionId != null) {
            boolean newSubscription = cachedTemplate.getSubscriptionIds().add(subscriptionId);
            if (newSubscription) {
                log.debug("Subscription {} associated to cached UsageChargeTemplate {}/{} ", subscriptionId, usageChargeTemplate.getId(), usageChargeTemplate.getCode());
            }
        }

        usageChargeTemplateCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(usageChargeTemplate.getId(), cachedTemplate);

        log.debug("UsageChargeTemplate " + (addToCache ? "added" : "updated") + " in usageChargeTemplateCache: template {}", usageChargeTemplate.getCode());

        return cachedTemplate;
    }

    /**
     * Update cache with usage charge templates that are associated to triggered EDR template
     * 
     * @param triggeredEDRTemplate Triggered EDR template
     */
    public void updateUsageChargeTemplateInCache(TriggeredEDRTemplate triggeredEDRTemplate) {
        log.debug("Updating charge template for triggeredEDR {} in usage charge template cache", triggeredEDRTemplate.toString());

        List<UsageChargeTemplate> charges = usageChargeTemplateService.findAssociatedToEDRTemplate(triggeredEDRTemplate);

        for (UsageChargeTemplate charge : charges) {
            createOrUpdateUsageChargeTemplateInCache(charge, null);
        }
    }

    /**
     * Update cached usage charge instances with info from charge template and reorder cached usage charge instances associated to a given subscription if priority has changed
     * 
     * @param subscriptionId Subscription ID
     * @param chargeTemplate Charge template
     * @param priorityChanged Priority has changed?
     */
    private void updateAndReorderUserChargeInstancesInCache(Long subscriptionId, CachedUsageChargeTemplate chargeTemplate, boolean priorityChanged) {

        log.debug("Updating and sorted cached subscription {} usage charges", subscriptionId);

        BiFunction<? super Long, ? super List<CachedUsageChargeInstance>, ? extends List<CachedUsageChargeInstance>> remappingFunction = (subId, charges) -> {
            if (charges == null) {
                return null;
            }

            boolean anyUpdated = false;
            for (CachedUsageChargeInstance cachedUsageChargeInstance : charges) {
                anyUpdated = anyUpdated || cachedUsageChargeInstance.updateChargeTemplateInfo(chargeTemplate);
            }

            if (anyUpdated && priorityChanged) {
                Collections.sort(charges);
            }

            return charges;
        };

        usageChargeInstanceCache.compute(subscriptionId, remappingFunction);

    }

    /**
     * Add (or overwrite) counter instance to cache
     * 
     * @param counterInstance Counter instance to add
     * @return Cached counter instance value
     */
    private CachedCounterInstance addCounterInstanceToCache(CounterInstance counterInstance) {

        log.debug("Adding counter to the counter cache counter: {}", counterInstance);

        CachedCounterInstance counterCacheValue = new CachedCounterInstance(counterInstance);
        counterCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(counterInstance.getId(), counterCacheValue);

        return counterCacheValue;
    }

    /**
     * Add counter instance to cache if not cached yet and return cached counter instance
     * 
     * @param counterInstance Counter instance to add
     * @return Cached counter instance
     */
    private CachedCounterInstance getOrAddCounterInstanceToCache(CounterInstance counterInstance) {
        if (counterCache.containsKey(counterInstance.getId())) {
            return counterCache.get(counterInstance.getId());
        } else {
            return addCounterInstanceToCache(counterInstance);
        }
    }

    /**
     * Retrieve cached counter instance
     * 
     * @param counterId Counter instance ID
     * @return Cached counter instance
     */
    public CachedCounterInstance getCounterInstance(Long counterId) {
        return counterCache.get(counterId);
    }

    /**
     * Retrieve cached usage charge template
     * 
     * @param chargeTemplateId Usage charge template ID
     * @return Cached usage charge template
     */
    public CachedUsageChargeTemplate getUsageChargeTemplate(Long chargeTemplateId) {
        return usageChargeTemplateCache.get(chargeTemplateId);
    }

    /**
     * Are usage charge instances cached for a given subscription
     * 
     * @param subscriptionId Subscription id
     * @return True if usage charge instances cached
     */
    public boolean isUsageChargeInstancesCached(Long subscriptionId) {
        return usageChargeInstanceCache.containsKey(subscriptionId);
    }

    /**
     * Get a list of usage charge instances associated to subscription
     * 
     * @param subscriptionId Subsription id
     * @return A list of usage charge instances
     */
    public List<CachedUsageChargeInstance> getUsageChargeInstances(Long subscriptionId) {
        return usageChargeInstanceCache.get(subscriptionId);
    }

    /**
     * Get a summary of cached information
     * 
     * @return A list of a map containing cache information with cache name as a key and cache as a value
     */
    // @Override
    @SuppressWarnings("rawtypes")
    public Map<String, Cache> getCaches() {
        Map<String, Cache> summaryOfCaches = new HashMap<String, Cache>();
        summaryOfCaches.put(pricePlanCache.getName(), pricePlanCache);
        summaryOfCaches.put(usageChargeTemplateCache.getName(), usageChargeTemplateCache);
        summaryOfCaches.put(usageChargeInstanceCache.getName(), usageChargeInstanceCache);
        summaryOfCaches.put(counterCache.getName(), counterCache);

        return summaryOfCaches;
    }

    /**
     * Refresh cache by name
     * 
     * @param cacheName Name of cache to refresh or null to refresh all caches
     */
    // @Override
    @Asynchronous
    public void refreshCache(String cacheName) {

        if (cacheName == null || cacheName.equals(pricePlanCache.getName()) || cacheName.contains(pricePlanCache.getName())) {
            populatePricePlanCache();
        }
        if (cacheName == null || cacheName.equals(usageChargeTemplateCache.getName()) || cacheName.equals(usageChargeInstanceCache.getName())
                || cacheName.equals(counterCache.getName()) || cacheName.equals(COUNTER_CACHE) || cacheName.contains(usageChargeTemplateCache.getName())
                || cacheName.contains(usageChargeInstanceCache.getName()) || cacheName.contains(counterCache.getName())) {
            populateUsageChargeCache();
        }
    }

    /**
     * Add counterPeriodToCache
     * 
     * @param counterPeriod Counter period
     * @return
     */
    public CachedCounterPeriod addCounterPeriodToCache(CounterPeriod counterPeriod) {

        log.debug("Adding counter period to the counter cache counter: {}", counterPeriod);

        CachedCounterInstance cachedCounterInstance = counterCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(counterPeriod.getCounterInstance().getId());
        if (cachedCounterInstance == null) {
            cachedCounterInstance = new CachedCounterInstance(counterPeriod.getCounterInstance());
        }
        CachedCounterPeriod cachedCounterPeriod = cachedCounterInstance.addCounterPeriod(counterPeriod);

        counterCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(cachedCounterInstance.getId(), cachedCounterInstance);

        return cachedCounterPeriod;
    }

    /**
     * Retrieve cached counter period by counter instance and counter period ids
     * 
     * @param counterInstanceId Counter instance id
     * @param counterPeriodId Counter period id
     * @return Cached counter period
     */
    public CachedCounterPeriod getCounterPeriod(Long counterInstanceId, Long counterPeriodId) {

        CachedCounterInstance cachedCounterInstance = counterCache.get(counterInstanceId);
        if (cachedCounterInstance == null) {
            return null;
        }

        return cachedCounterInstance.getCounterPeriod(counterPeriodId);
    }

    /**
     * Retrieve cached counter period by counter instance and date
     * 
     * @param counterInstanceId Counter instance id
     * @param date Date to match
     * @return Cached counter period
     */
    public CachedCounterPeriod getCounterPeriod(Long counterInstanceId, Date date) {

        CachedCounterInstance cachedCounterInstance = counterCache.get(counterInstanceId);
        if (cachedCounterInstance == null) {
            return null;
        }

        return cachedCounterInstance.getCounterPeriod(date);
    }

    /**
     * Deduce current counterPeriod's value by a given amount. If given amount exceeds current value, only partial amount will be deduced
     * 
     * @param counterInstanceId Counter instance id to update
     * @param counterPeriodId Counter period id to update
     * @param deduceBy Amount to deduce by
     * @return Previous, the actual deduced value and new counter value. or NULL if value is not tracked (initial counter value is not set)
     */
    public CounterValueChangeInfo deduceCounterValue(Long counterInstanceId, Long counterPeriodId, BigDecimal deduceBy) {

        CachedCounterInstance cachedCounterInstance = counterCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(counterInstanceId);
        CachedCounterPeriod counterPeriodToUpdate = cachedCounterInstance.getCounterPeriod(counterPeriodId);

        BigDecimal previousValue = null;
        BigDecimal deducedQuantity = null;

        // No initial value, so no need to track present value (will always be able to deduce by any amount) and thus no need to update cache
        if (counterPeriodToUpdate.getLevel() == null) {
            return null;

        } else if (counterPeriodToUpdate.getValue().compareTo(BigDecimal.ZERO) == 0) {
            return new CounterValueChangeInfo(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        } else if (counterPeriodToUpdate.getValue().compareTo(deduceBy) < 0) {
            previousValue = counterPeriodToUpdate.getValue();
            deducedQuantity = counterPeriodToUpdate.getValue();
            counterPeriodToUpdate.setValue(BigDecimal.ZERO);

        } else {
            previousValue = counterPeriodToUpdate.getValue();
            deducedQuantity = deduceBy;
            counterPeriodToUpdate.setValue(counterPeriodToUpdate.getValue().subtract(deduceBy));
        }

        counterCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(cachedCounterInstance.getId(), cachedCounterInstance);

        log.debug("Deduced cached {}/{} counter period by {} to a new value {}", counterInstanceId, counterPeriodId, deducedQuantity, counterPeriodToUpdate.getValue());

        return new CounterValueChangeInfo(previousValue, deducedQuantity, counterPeriodToUpdate.getValue());
    }

    /**
     * Increment current counterPeriod's value by a given amount.
     * 
     * @param counterInstanceId Counter instance id to update
     * @param counterPeriodId Counter period id to update
     * @param incrementBy Amount to increment by
     * @return The new value, or NULL if value is not tracked (initial value is not set)
     */
    public BigDecimal incrementCounterValue(Long counterInstanceId, Long counterPeriodId, BigDecimal incrementBy) {

        CachedCounterInstance cachedCounterInstance = counterCache.getAdvancedCache().withFlags(Flag.FORCE_WRITE_LOCK).get(counterInstanceId);
        CachedCounterPeriod counterPeriodToUpdate = cachedCounterInstance.getCounterPeriod(counterPeriodId);

        // No initial value, so no need to track present value (will always be able to deduce by any amount) and thus no need to update cache
        if (counterPeriodToUpdate.getLevel() == null) {
            return null;

        } else {
            counterPeriodToUpdate.setValue(counterPeriodToUpdate.getValue().add(incrementBy));
        }

        counterCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES).put(cachedCounterInstance.getId(), cachedCounterInstance);

        log.debug("Incremented cached {}/{} counter period by {} to a new value {}", counterInstanceId, counterPeriodId, incrementBy, counterPeriodToUpdate.getValue());

        return counterPeriodToUpdate.getValue();
    }
}
