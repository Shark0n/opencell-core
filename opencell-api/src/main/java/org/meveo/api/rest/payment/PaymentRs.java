package org.meveo.api.rest.payment;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.payment.CardTokenRequestDto;
import org.meveo.api.dto.payment.CardTokenResponseDto;
import org.meveo.api.dto.payment.PaymentDto;
import org.meveo.api.dto.response.CustomerPaymentsResponse;
import org.meveo.api.rest.IBaseRs;

@Path("/payment")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface PaymentRs extends IBaseRs {

    /**
     * Creates automated payment. It also process if a payment is matching or not
     * 
     * @param postData Payment's data
     * @return
     */
    @POST
    @Path("/create")
    public ActionStatus create(PaymentDto postData);

    /**
     * Returns a list of account operations along with the balance of a customer
     * 
     * @param customerAccountCode
     * @return
     */
    @GET
    @Path("/customerPayment")
    public CustomerPaymentsResponse list(@QueryParam("customerAccountCode") String customerAccountCode);
    
    /**
     * Tokenize payment card details
     * 
     * @param cardTokenRequestDto
     * @return
     */
    @POST
    @Path("/createCardToken")
    public CardTokenResponseDto createCardToken(CardTokenRequestDto postData);

}
