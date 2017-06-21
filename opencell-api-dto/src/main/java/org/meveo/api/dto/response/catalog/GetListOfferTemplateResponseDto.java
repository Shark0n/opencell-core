package org.meveo.api.dto.response.catalog;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.catalog.OfferTemplateDto;
import org.meveo.api.dto.response.BaseResponse;

@XmlRootElement(name = "GetListOfferTemplateResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetListOfferTemplateResponseDto extends BaseResponse {

    private static final long serialVersionUID = 5535571034571826093L;

    @XmlElementWrapper(name = "offerTemplates")
    @XmlElement(name = "offerTemplate")
    private List<OfferTemplateDto> offerTemplates;

    public GetListOfferTemplateResponseDto() {

    }

    public List<OfferTemplateDto> getOfferTemplates() {
        return offerTemplates;
    }

    public void setOfferTemplates(List<OfferTemplateDto> offerTemplates) {
        this.offerTemplates = offerTemplates;
    }

    @Override
    public String toString() {
        return "GetListOfferTemplateResponseDto [offerTemplates=" + offerTemplates + "]";
    }
}