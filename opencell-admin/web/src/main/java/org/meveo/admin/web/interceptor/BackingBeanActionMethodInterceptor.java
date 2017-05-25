package org.meveo.admin.web.interceptor;

import java.io.Serializable;
import java.sql.SQLException;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commits transaction and handles exceptions of backing bean action methods
 *
 * @author Andrius Karpavicius
 */
@ActionMethod
@Interceptor
public class BackingBeanActionMethodInterceptor implements Serializable {

    private static final long serialVersionUID = -8361765042326423662L;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    protected Messages messages;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {

        Object result = null;
        try {
            // Call a backing bean method and flush persistence
            result = invocationContext.proceed();
            return result;

        } catch (TransactionRequiredException e) {
            log.error("Transaction must have been rollbacked already (probably by exception thown in service and caught in backing bean): {}", e.getMessage());
            if (result != null) {
                return result;
            }

        } catch (ConstraintViolationException e) {
            log.error("Failed to execute {}.{} method due to validation errors ", invocationContext.getMethod().getDeclaringClass().getName(),
                invocationContext.getMethod().getName(), e);

            StringBuilder builder = new StringBuilder();
            builder.append("Invalid values passed: ");
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                builder.append(String.format("    %s.%s: value '%s' - %s;", violation.getRootBeanClass().getSimpleName(), violation.getPropertyPath().toString(),
                    violation.getInvalidValue(), violation.getMessage()));
            }

            messages.clear();
            messages.error(new BundleKey("messages", "error.action.failed"), builder.toString());
            FacesContext.getCurrentInstance().validationFailed();

        } catch (PersistenceException e) {
            messages.clear();
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                log.error("Delete was unsuccessful because entity is already in use.", e.getCause());
                messages.error(new BundleKey("messages", "error.delete.entityUsed"));
            } else {
                log.error("Failed to execute {}.{} method due to database errors. ", invocationContext.getMethod().getDeclaringClass().getName(),
                    invocationContext.getMethod().getName(), e);
                messages.error(new BundleKey("messages", "error.action.failed"), e.getMessage());
            }
            FacesContext.getCurrentInstance().validationFailed();

        } catch (Exception e) {
            log.error("Failed to execute {}.{} method due to errors ", invocationContext.getMethod().getDeclaringClass().getName(), invocationContext.getMethod().getName(), e);

            // See if can get to the root of the exception cause
            String message = e.getMessage();
            boolean validation = false;
            Throwable cause = e.getCause();
            while (cause != null) {

                if (cause instanceof SQLException || cause instanceof BusinessException) {
                    message = cause.getMessage();
                    validation = cause instanceof ValidationException;
                    break;
                }
                cause = cause.getCause();
            }

            messages.clear();
            if (validation && message != null) {
                messages.error(message);
            } else {
                messages.error(new BundleKey("messages", "error.action.failed"), message == null ? e.getClass().getSimpleName() : message);
            }
            FacesContext.getCurrentInstance().validationFailed();
        }

        return null;
    }
}
