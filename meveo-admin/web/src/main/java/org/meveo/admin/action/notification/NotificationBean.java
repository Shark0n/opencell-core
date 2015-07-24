package org.meveo.admin.action.notification;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Entity;

import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.RejectedImportException;
import org.meveo.commons.utils.CsvBuilder;
import org.meveo.commons.utils.CsvReader;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.ObservableEntity;
import org.meveo.model.notification.Notification;
import org.meveo.model.notification.NotificationEventTypeEnum;
import org.meveo.model.notification.StrategyImportTypeEnum;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.notification.NotificationService;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@Named
@ViewScoped
public class NotificationBean extends BaseBean<Notification> {

	private static final long serialVersionUID = 6473465285480945644L;

	@Inject
	private NotificationService notificationService;

	ParamBean paramBean = ParamBean.getInstance();

	CsvReader csvReader = null;
	private UploadedFile file;

	private static final int CODE = 0;
	private static final int CLASS_NAME_FILTER = 1;
	private static final int EL_FILTER = 2;
	private static final int ACTIVE = 3;
	private static final int EL_ACTION = 4;
	private static final int EVENT_TYPE_FILTER = 5;

	private StrategyImportTypeEnum strategyImportType;

	CsvBuilder csv = null;
	private String providerDir = paramBean.getProperty("providers.rootDir", "/tmp/meveo_integr");
	private String existingEntitiesCsvFile = null;

	public NotificationBean() {
		super(Notification.class);
	}

	@Override
	protected IPersistenceService<Notification> getPersistenceService() {
		return notificationService;
	}

	@Override
	protected List<String> getFormFieldsToFetch() {
		return Arrays.asList("provider");
	}

	@Override
	protected List<String> getListFieldsToFetch() {
		return Arrays.asList("provider");
	}



    public void handleFileUpload(FileUploadEvent event) throws Exception {
        try {
            file = event.getFile();
            log.debug("File uploaded " + file.getFileName());
            upload();
            messages.info(new BundleKey("messages", "import.csv.successful"));
        } catch (Exception e) {
            log.error("Failed to handle uploaded file {}", event.getFile().getFileName(), e);
            messages.error(new BundleKey("messages", "import.csv.failed"), e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    private void upload() throws IOException, BusinessException {
        if (file == null) {
            return;
        }
        csvReader = new CsvReader(file.getInputstream(), ';', Charset.forName("ISO-8859-1"));
        csvReader.readHeaders();

        String existingEntitiesCSV = paramBean.getProperty("existingEntities.csv.dir", "existingEntitiesCSV");
        File dir = new File(providerDir + File.separator + getCurrentProvider().getCode() + File.separator + existingEntitiesCSV);
        dir.mkdirs();
        existingEntitiesCsvFile = dir.getAbsolutePath() + File.separator + "Notifications_" + new SimpleDateFormat("ddMMyyyyHHmmSS").format(new Date()) + ".csv";
        csv = new CsvBuilder();
        boolean isEntityAlreadyExist = false;
        while (csvReader.readRecord()) {
            String[] values = csvReader.getValues();
            Notification existingEntity = notificationService.findByCode(values[CODE], getCurrentProvider());
            if (existingEntity != null) {
                checkSelectedStrategy(values, existingEntity, isEntityAlreadyExist);
                isEntityAlreadyExist = true;
            } else {
                Notification notif = new Notification();
                notif.setCode(values[CODE]);
                notif.setClassNameFilter(values[CLASS_NAME_FILTER]);
                notif.setElFilter(values[EL_FILTER]);
                notif.setDisabled(Boolean.parseBoolean(values[ACTIVE]));
                notif.setElAction(values[EL_ACTION]);
                notif.setEventTypeFilter(NotificationEventTypeEnum.valueOf(values[EVENT_TYPE_FILTER]));
                notificationService.create(notif);
            }
        }
        if (isEntityAlreadyExist && strategyImportType.equals(StrategyImportTypeEnum.REJECT_EXISTING_RECORDS)) {
            csv.writeFile(csv.toString().getBytes(), existingEntitiesCsvFile);
        }
    }

    public void checkSelectedStrategy(String[] values, Notification existingEntity, boolean isEntityAlreadyExist) throws RejectedImportException {
		if (strategyImportType.equals(StrategyImportTypeEnum.UPDATED)) {
			existingEntity.setClassNameFilter(values[CLASS_NAME_FILTER]);
			existingEntity.setElFilter(values[EL_FILTER]);
			existingEntity.setDisabled(Boolean.parseBoolean(values[ACTIVE]));
			existingEntity.setElAction(values[EL_ACTION]);
			existingEntity.setEventTypeFilter(NotificationEventTypeEnum.valueOf(values[EVENT_TYPE_FILTER]));
			notificationService.update(existingEntity);
		} else if (strategyImportType.equals(StrategyImportTypeEnum.REJECTE_IMPORT)) {
			throw new RejectedImportException("notification.rejectImport");
		} else if (strategyImportType.equals(StrategyImportTypeEnum.REJECT_EXISTING_RECORDS)) {
			if (!isEntityAlreadyExist) {
				csv.appendValue("Code");
				csv.appendValue("Classename filter");
				csv.appendValue("El filter");
				csv.appendValue("Active");
				csv.appendValue("El action");
				csv.appendValue("Event type filter");
			}
			csv.startNewLine();
			csv.appendValue(values[CODE]);
			csv.appendValue(values[CLASS_NAME_FILTER]);
			csv.appendValue(values[EL_FILTER]);
			csv.appendValue(values[ACTIVE]);
			csv.appendValue(values[EL_ACTION]);
			csv.appendValue(values[EVENT_TYPE_FILTER]);
		}

	}

	public StrategyImportTypeEnum getStrategyImportType() {
		return strategyImportType;
	}

	public void setStrategyImportType(StrategyImportTypeEnum strategyImportType) {
		this.strategyImportType = strategyImportType;
	}

    /**
     * Autocomplete method for class filter field - search entity type classes with @ObservableEntity annotation
     * 
     * @param query A partial class name (including a package)
     * @return A list of classnames
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<String> autocompleteClassNames(String query) {

        List<Class> classes = null;
        try {
            classes = ReflectionUtils.getClasses("org.meveo.model");
        } catch (Exception e) {
            log.error("Failed to get a list of classes for a model package", e);
            return null;
        }

        String queryLc = query.toLowerCase();
        List<String> classNames = new ArrayList<String>();
        for (Class clazz : classes) {
            if (clazz.isAnnotationPresent(Entity.class) && clazz.isAnnotationPresent(ObservableEntity.class) && clazz.getName().toLowerCase().contains(queryLc)) {
                classNames.add(clazz.getName());
            }
        }

        Collections.sort(classNames);
        return classNames;
    }

}