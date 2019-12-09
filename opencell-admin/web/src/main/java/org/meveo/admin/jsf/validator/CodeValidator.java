package org.meveo.admin.jsf.validator;

import java.text.MessageFormat;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import org.meveo.admin.util.ResourceBundle;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.StringUtils;

/**
 * @author Edward P. Legaspi
 * @author Wassim Drira
 * @lastModifiedVersion 5.0
 * 
 **/
@FacesValidator(value = "codeValidator", managed = true)
public class CodeValidator implements Validator {

    @Inject
    private ResourceBundle resourceMessages;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !StringUtils.isMatch(value.toString(), ParamBeanFactory.getAppScopeInstance().getProperty("meveo.code.pattern", StringUtils.CODE_REGEX))) {
            FacesMessage facesMessage = new FacesMessage();
            String message = resourceMessages.getString("message.validation.code.pattern");
            message = MessageFormat.format(message, getLabel(context, component), ParamBeanFactory.getAppScopeInstance().getProperty("meveo.code.pattern", StringUtils.CODE_REGEX));
            facesMessage.setDetail(message);
            facesMessage.setSummary(message);
            facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);

            throw new ValidatorException(facesMessage);
        }
    }

    private Object getLabel(FacesContext context, UIComponent component) {
        Object o = component.getAttributes().get("label");
        if (o == null || (o instanceof String && ((String) o).length() == 0)) {
            o = component.getValueExpression("label");
        }
        // Use the "clientId" if there was no label specified.
        if (o == null) {
            o = component.getClientId(context);
        }
        return o;
    }

}
