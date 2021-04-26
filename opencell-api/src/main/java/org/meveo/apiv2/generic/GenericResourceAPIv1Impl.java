package org.meveo.apiv2.generic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.meveo.api.MeveoApiErrorCodeEnum;
import org.meveo.api.dto.*;
import org.meveo.api.dto.account.AccessDto;
import org.meveo.api.dto.account.AccountHierarchyDto;
import org.meveo.api.dto.billing.*;
import org.meveo.api.dto.invoice.CancelInvoiceRequestDto;
import org.meveo.api.dto.invoice.ValidateInvoiceRequestDto;
import org.meveo.apiv2.GenericOpencellRestfulAPIv1;
import org.meveo.apiv2.generic.core.GenericHelper;
import org.meveo.commons.utils.StringUtils;
import org.meveo.util.Inflector;
import org.meveo.util.Version;

import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thang Nguyen
 */
@RequestScoped
@Interceptors({ AuthenticationFilter.class })
public class GenericResourceAPIv1Impl implements GenericResourceAPIv1 {

    private static final String METHOD_GET_ALL = "/list";
    private static final String METHOD_GET_ALL_BIS = "/listGetAll";
    private static final String METHOD_CREATE = "/";
    private static final String METHOD_CREATE_BIS = "/create";
    private static final String METHOD_UPDATE = "/";
    private static final String METHOD_DELETE = "/";
    private static final String METHOD_CREATE_OR_UPDATE = "/createOrUpdate";

    private static final String FORWARD_SLASH = "/";
    private static final String QUERY_PARAM_SEPARATOR = "?";
    private static final String QUERY_PARAM_VALUE_SEPARATOR = "=";
    private static final String PAIR_QUERY_PARAM_SEPARATOR = "&";
    private static final String DTO_SUFFIX = "dto";

    // static final string for services
    private static final String ENABLE_SERVICE = "enable";
    private static final String DISABLE_SERVICE = "disable";

    // business final string variables
    private static final String WALLET_OPERATION = "/billing/wallet/operation";
    private static final String PRICE_PLAN = "/catalog/pricePlan";
    private static final String COUNTRY_ISO = "/countryIso";
    private static final String CURRENCY_ISO = "/currencyIso";
    private static final String LANGUAGE_ISO = "/languageIso";
    private static final String CUSTOMER = "/account/customer";
    private static final String USER = "/user";
    private static final String INVOICE = "/invoice";
    private static final String ACCOUNTING_CODE = "/billing/accountingCode";
    private static final String CONTACT = "/contact";
    private static final String TAX_CATEGORY = "/taxCategory";
    private static final String TAX_CLASS = "/taxClass";
    private static final String TAX_MAPPING = "/taxMapping";
    private static final String PAYMENT_METHOD = "/payment/paymentMethod";
    private static final String FILE_FORMAT = "/admin/fileFormat";
    private static final String OSC_TEMPLATE = "/catalog/oneShotChargeTemplate";
    private static final String RC_TEMPLATE = "/catalog/recurringChargeTemplate";
    private static final String UC_TEMPLATE = "/catalog/usageChargeTemplate";
    private static final String SUBSCRIPTION = "/billing/subscription";
    private static final String RATED_TRANSACTION = "/billing/ratedTransaction";
    private static final String WALLET = "/billing/wallet";
    private static final String OFFER_TEMPLATE = "/catalog/offerTemplate";
    private static final String CALENDAR = "/calendar";
    private static final String UNIT_MEASURE = "/catalog/unitOfMeasure";
    private static final String TAX = "/tax";
    private static final String CREDIT_CATEGORY = "/payment/creditCategory";
    private static final String CUSTOMER_ACCOUNT = "/account/customerAccount";
    private static final String TITLE = "/account/title";
    private static final String BILLING_ACCOUNT = "/account/billingAccount";
    private static final String USER_ACCOUNT = "/account/userAccount";
    private static final String SERVICE_TEMPLATE = "/catalog/serviceTemplate";
    private static final String BUSINESS_ACCOUNT_MODEL = "/account/businessAccountModel";
    private static final String BUSINESS_PRODUCT_MODEL = "/catalog/businessProductModel";

    private static final String API_REST = "api/rest";

    private List<PathSegment> segmentsOfPathAPIv2;
    private String entityCode;
    private String pathIBaseRS;
    private String entityClassName;
    private StringBuilder queryParams;
    private MultivaluedMap<String, String> queryParamsMap;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders headers;

    /*
     * This request is used to retrieve all entities, or also a particular entity
     */
    @Override
    public Response getAllEntitiesOrGetAnEntity() throws URISyntaxException, IOException {
        String aGetPath = GenericOpencellRestfulAPIv1.API_VERSION + uriInfo.getPath();

        segmentsOfPathAPIv2 = uriInfo.getPathSegments();
        StringBuilder suffixPathBuilder = new StringBuilder();
        for (int i = 0; i < segmentsOfPathAPIv2.size() - 1; i++ )
            suffixPathBuilder.append( FORWARD_SLASH + segmentsOfPathAPIv2.get(i).getPath() );
        String getAnEntityPath = GenericOpencellRestfulAPIv1.API_VERSION + suffixPathBuilder.toString();

        URI redirectURI;

        // to get all entities
        if ( GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.containsKey( aGetPath ) ) {
            pathIBaseRS = GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.get( aGetPath );
            if ( pathIBaseRS.equals( WALLET_OPERATION ) )
                entityClassName = "WalletOperation";
            else if ( pathIBaseRS.equals( PRICE_PLAN ) )
                entityClassName = "pricePlanMatrix";
            else if ( pathIBaseRS.equals( COUNTRY_ISO ) )
                entityClassName = "country";
            else if ( pathIBaseRS.equals( CURRENCY_ISO ) )
                entityClassName = "currency";
            else if ( pathIBaseRS.equals( LANGUAGE_ISO ) )
                entityClassName = "language";
            else if ( pathIBaseRS.equals( "/job/jobReport" ) )
                entityClassName = "Job";
            else
                entityClassName = pathIBaseRS.split( FORWARD_SLASH )[ pathIBaseRS.split( FORWARD_SLASH ).length - 1 ];

            queryParamsMap = uriInfo.getQueryParameters();
            GenericPagingAndFilteringUtils.getInstance().constructPagingAndFiltering(queryParamsMap);
            Class entityClass = GenericHelper.getEntityClass( Inflector.getInstance().singularize(entityClassName) );
            GenericPagingAndFilteringUtils.getInstance().generatePagingConfig(entityClass);

            if ( ! queryParamsMap.isEmpty() ) {
                queryParams = new StringBuilder( QUERY_PARAM_SEPARATOR );
                for( String aKey : queryParamsMap.keySet() ){
                    queryParams.append( aKey + QUERY_PARAM_VALUE_SEPARATOR
                            + queryParamsMap.get( aKey ).get(0).replace( GenericPagingAndFilteringUtils.BLANK_SPACE, GenericPagingAndFilteringUtils.BLANK_SPACE_ENCODED )
                            + PAIR_QUERY_PARAM_SEPARATOR );
                }

                if ( pathIBaseRS.equals( OSC_TEMPLATE ) || pathIBaseRS.equals( RC_TEMPLATE )
                    || pathIBaseRS.equals( UC_TEMPLATE ) || pathIBaseRS.equals( CUSTOMER )
                    || pathIBaseRS.equals( SUBSCRIPTION ) || pathIBaseRS.equals( RATED_TRANSACTION )
                    || pathIBaseRS.equals( WALLET ) || pathIBaseRS.equals( OFFER_TEMPLATE )
                    || pathIBaseRS.equals( USER ) || pathIBaseRS.equals( INVOICE )
                    || pathIBaseRS.equals( ACCOUNTING_CODE ) || pathIBaseRS.equals( CALENDAR )
                    || pathIBaseRS.equals( UNIT_MEASURE ) || pathIBaseRS.equals( CONTACT )
                    || pathIBaseRS.equals( COUNTRY_ISO ) || pathIBaseRS.equals( CURRENCY_ISO )
                    || pathIBaseRS.equals( LANGUAGE_ISO ) || pathIBaseRS.equals( TAX )
                    || pathIBaseRS.equals( TAX_CATEGORY ) || pathIBaseRS.equals( TAX_CLASS )
                    || pathIBaseRS.equals( TAX_MAPPING ) || pathIBaseRS.equals( CREDIT_CATEGORY )
                    || pathIBaseRS.equals( PAYMENT_METHOD ) || pathIBaseRS.equals( TITLE )
                    || pathIBaseRS.equals( CUSTOMER_ACCOUNT ) || pathIBaseRS.equals( BILLING_ACCOUNT )
                    || pathIBaseRS.equals( USER_ACCOUNT ) || pathIBaseRS.equals( SERVICE_TEMPLATE )
                    || pathIBaseRS.equals( PRICE_PLAN ) || pathIBaseRS.equals( WALLET_OPERATION )
                    || pathIBaseRS.equals( FILE_FORMAT ) || pathIBaseRS.equals( BUSINESS_ACCOUNT_MODEL )
                    || pathIBaseRS.equals( BUSINESS_PRODUCT_MODEL ) )
                    redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                            + API_REST + pathIBaseRS + METHOD_GET_ALL_BIS
                            + queryParams.substring( 0, queryParams.length() - 1 )
                            .replace( GenericPagingAndFilteringUtils.BLANK_SPACE, GenericPagingAndFilteringUtils.BLANK_SPACE_ENCODED )
                            .replace( GenericPagingAndFilteringUtils.QUOTE, GenericPagingAndFilteringUtils.QUOTE_ENCODED ) );
                else
                    redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                            + API_REST + pathIBaseRS + METHOD_GET_ALL
                            + queryParams.substring( 0, queryParams.length() - 1 )
                            .replace( GenericPagingAndFilteringUtils.BLANK_SPACE, GenericPagingAndFilteringUtils.BLANK_SPACE_ENCODED )
                            .replace( GenericPagingAndFilteringUtils.QUOTE, GenericPagingAndFilteringUtils.QUOTE_ENCODED ) );
            }
            else {
                if ( pathIBaseRS.equals( OSC_TEMPLATE ) || pathIBaseRS.equals( RC_TEMPLATE )
                    || pathIBaseRS.equals( UC_TEMPLATE ) || pathIBaseRS.equals( CUSTOMER )
                    || pathIBaseRS.equals( SUBSCRIPTION ) || pathIBaseRS.equals( RATED_TRANSACTION )
                    || pathIBaseRS.equals( WALLET ) || pathIBaseRS.equals( OFFER_TEMPLATE )
                    || pathIBaseRS.equals( USER ) || pathIBaseRS.equals( INVOICE )
                    || pathIBaseRS.equals( ACCOUNTING_CODE ) || pathIBaseRS.equals( CALENDAR )
                    || pathIBaseRS.equals( UNIT_MEASURE ) || pathIBaseRS.equals( CONTACT )
                    || pathIBaseRS.equals( COUNTRY_ISO ) || pathIBaseRS.equals( CURRENCY_ISO )
                    || pathIBaseRS.equals( LANGUAGE_ISO ) || pathIBaseRS.equals( TAX )
                    || pathIBaseRS.equals( TAX_CATEGORY ) || pathIBaseRS.equals( TAX_CLASS )
                    || pathIBaseRS.equals( TAX_MAPPING ) || pathIBaseRS.equals( CREDIT_CATEGORY )
                    || pathIBaseRS.equals( PAYMENT_METHOD ) || pathIBaseRS.equals( TITLE )
                    || pathIBaseRS.equals( CUSTOMER_ACCOUNT ) || pathIBaseRS.equals( BILLING_ACCOUNT )
                    || pathIBaseRS.equals( USER_ACCOUNT ) || pathIBaseRS.equals( SERVICE_TEMPLATE )
                    || pathIBaseRS.equals( PRICE_PLAN ) || pathIBaseRS.equals( WALLET_OPERATION )
                    || pathIBaseRS.equals( FILE_FORMAT ) || pathIBaseRS.equals( BUSINESS_ACCOUNT_MODEL )
                    || pathIBaseRS.equals( BUSINESS_PRODUCT_MODEL ) )
                    redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                            + API_REST + pathIBaseRS + METHOD_GET_ALL_BIS );
                else
                    redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                            + API_REST + pathIBaseRS + METHOD_GET_ALL );
            }

            Response getResponse = AuthenticationFilter.httpClient.target( redirectURI ).request().get();

            return Response.ok().entity(customizeResponse(getResponse, entityClassName)).build();
        }
        else if ( GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.containsKey( getAnEntityPath ) ) {
            pathIBaseRS = GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.get( getAnEntityPath );
            entityCode = segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 1 ).getPath();

            // special handle for customerCategory
            if ( pathIBaseRS.equals(CUSTOMER) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + "/category" + FORWARD_SLASH + entityCode);
            }
            // special handle for user
            else if ( pathIBaseRS.equals(USER) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "username=" + entityCode);
            }
            // special handle for job
            else if ( pathIBaseRS.equals("/job") ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "jobInstanceCode=" + entityCode);
            }
            // special handle for jobReport, contact, taxCategory, taxClass
            else if ( pathIBaseRS.equals("/job/jobReport") || pathIBaseRS.equals(CONTACT)
                    || pathIBaseRS.equals(TAX_CATEGORY) || pathIBaseRS.equals(TAX_CLASS)
                    || pathIBaseRS.equals(FILE_FORMAT) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "code=" + entityCode);
            }
            // special handle for invoice
            else if ( pathIBaseRS.equals(INVOICE) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "invoiceNumber=" + entityCode);
            }
            // special handle for accountingCode
            else if ( pathIBaseRS.equals(ACCOUNTING_CODE) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "accountingCode=" + entityCode);
            }
            // special handle for countryIso
            else if ( pathIBaseRS.equals(COUNTRY_ISO) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "countryCode=" + entityCode);
            }
            // special handle for currencyIso
            else if ( pathIBaseRS.equals(CURRENCY_ISO) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "currencyCode=" + entityCode);
            }
            // special handle for languageIso
            else if ( pathIBaseRS.equals(LANGUAGE_ISO) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "languageCode=" + entityCode);
            }
            // special handle for taxMapping, paymentMethod
            else if ( pathIBaseRS.equals(TAX_MAPPING) || pathIBaseRS.equals(PAYMENT_METHOD) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "id=" + entityCode);
            }
            else {
                entityClassName = pathIBaseRS.split( FORWARD_SLASH )[ pathIBaseRS.split( FORWARD_SLASH ).length - 1 ];
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + entityClassName + "Code=" + entityCode);
            }
            return Response.temporaryRedirect( redirectURI ).build();
        }
        // to handle get requests containing regular expressions
        else if ( GenericOpencellRestfulAPIv1.MAP_NEW_REGEX_PATH_AND_IBASE_RS_PATH.containsKey( aGetPath ) ) {
            pathIBaseRS = GenericOpencellRestfulAPIv1.MAP_NEW_REGEX_PATH_AND_IBASE_RS_PATH.get( aGetPath );
            queryParams = new StringBuilder( QUERY_PARAM_SEPARATOR );

            String originalPattern = GenericOpencellRestfulAPIv1.MAP_NEW_REGEX_PATH_AND_IBASE_RS_PATH.getPattern().toString();
            int indexCodeRegex = originalPattern.indexOf( GenericOpencellRestfulAPIv1.CODE_REGEX );
            String aSmallPattern;
            String smallString = null;

            if ( indexCodeRegex >= 0 ) {
                while ( indexCodeRegex >= 0 ) {
                    aSmallPattern = originalPattern.substring( 0,
                            indexCodeRegex + GenericOpencellRestfulAPIv1.CODE_REGEX.length() );

                    Matcher matcher = Pattern.compile( aSmallPattern ).matcher( aGetPath );
                    // get the first occurrence matching smallStringPattern
                    if ( matcher.find() ) {
                        smallString = matcher.group(0);

                        String[] matches = Pattern.compile( GenericOpencellRestfulAPIv1.CODE_REGEX )
                                .matcher( smallString )
                                .results()
                                .map(MatchResult::group)
                                .toArray(String[]::new);

                        if ( pathIBaseRS.equals( "/catalog/pricePlan/list" ) )
                            queryParams.append( "eventCode=" + matches[matches.length - 1] + PAIR_QUERY_PARAM_SEPARATOR );
                        else
                            queryParams.append( Inflector.getInstance().singularize( matches[matches.length - 2] ) + "Code="
                                    + matches[matches.length - 1] + PAIR_QUERY_PARAM_SEPARATOR );
                    }

                    indexCodeRegex = originalPattern.indexOf( GenericOpencellRestfulAPIv1.CODE_REGEX, indexCodeRegex + 1 );
                }

                // If smallString differs from the string aGetPath, the request is to retrieve all entities, so we add paging and filtering
                // Otherwise, if smallString is exactly the string aGetPath, the request is to retrieve a particular entity
                if ( ! smallString.equals( aGetPath ) ) {
                    queryParamsMap = uriInfo.getQueryParameters();
                    GenericPagingAndFilteringUtils.getInstance().constructPagingAndFiltering(queryParamsMap);

                    if ( pathIBaseRS.equals( "/catalog/pricePlan/list" ) )
                        entityClassName = "PricePlanMatrix";
                    else
                        entityClassName = aGetPath.split( FORWARD_SLASH )[ aGetPath.split( FORWARD_SLASH ).length - 1 ];

                    Class entityClass = GenericHelper.getEntityClass( Inflector.getInstance().singularize( entityClassName ) );
                    GenericPagingAndFilteringUtils.getInstance().generatePagingConfig(entityClass);
                }

                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + queryParams.substring( 0, queryParams.length() - 1 ) );
            }
            else {
                queryParams.append( uriInfo.getRequestUri().getQuery() );

                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + queryParams );
            }

            Response getResponse = AuthenticationFilter.httpClient.target( redirectURI ).request().get();

            return Response.ok().entity(customizeResponse(getResponse, entityClassName)).build();
        }
        else {
            if ( aGetPath.matches( "/v1/invoices/pdfInvoices/" + GenericOpencellRestfulAPIv1.CODE_REGEX ) ) {
                entityCode = segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 1 ).getPath();
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + "/invoice/getPdfInvoice" + QUERY_PARAM_SEPARATOR + "invoiceNumber=" + entityCode );
                return Response.temporaryRedirect( redirectURI ).build();
            }
            else if ( aGetPath.matches( "/v1/invoices/xmlInvoices/" + GenericOpencellRestfulAPIv1.CODE_REGEX ) ) {
                entityCode = segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 1 ).getPath();
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + "/invoice/getXMLInvoice" + QUERY_PARAM_SEPARATOR + "invoiceNumber=" + entityCode );
                return Response.temporaryRedirect( redirectURI ).build();
            }

            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // Concerns only responses for requests get all entities
    public Map<String, Object> customizeResponse( Response getResponse, String entityName ) throws IOException {
        Map<String, Object> customResponse = new LinkedHashMap<>();
        if ( getResponse.hasEntity() ) {
            Object aResponse2 = getResponse.getEntity();
            Map<String, Object> origResponse = new ObjectMapper().readValue( (InputStream) aResponse2, Map.class );

            for (Map.Entry<String,Object> entry : origResponse.entrySet()) {
                if ( entry.getKey().equals("actionStatus") || entry.getKey().equals("paging") )
                    customResponse.put(entry.getKey(), entry.getValue());
                else if ( entry.getKey().equals( "pricePlanMatrixes" ) ) {
                    if ( entry.getValue() instanceof Map ) {
                        Map mapEntities = (Map) entry.getValue();
                        for (Object aKey : mapEntities.keySet()) {
                            customResponse.put( "pricePlanMatrices", mapEntities.get(aKey) );
                        }
                    }
                }
                else if ( entry.getKey().equals( "list" + StringUtils.capitalizeFirstLetter(entityName) )
                        || entry.getKey().equals( entityName ) || entry.getKey().equals( "dto" )
                        || entry.getKey().equals( Inflector.getInstance().pluralize(entityName) )
                        || entry.getKey().equals( Inflector.getInstance().pluralize(entityName) + "Dto" ) ) {
                    if ( entry.getValue() instanceof Map ) {
                        Map mapEntities = (Map) entry.getValue();
                        for (Object aKey : mapEntities.keySet()) {
                            if ( aKey.equals( Inflector.getInstance().singularize(entityName) ) ||
                                aKey.equals( Inflector.getInstance().pluralize(entityName) ) ||
                                aKey.equals( entityName ) )
                                if ( CollectionUtils.isNotEmpty((List) mapEntities.get(aKey)) )
                                    customResponse.put( Inflector.getInstance().pluralize(entityName), mapEntities.get(aKey) );
                        }
                    }
                    else if ( entry.getValue() instanceof List )
                        if ( CollectionUtils.isNotEmpty((List) entry.getValue() ) )
                            customResponse.put( Inflector.getInstance().pluralize(entityName), entry.getValue() );
                }
                else
                    customResponse.put(entry.getKey(), entry.getValue());
            }
        }

        return customResponse;
    }

    @Override
    public Response postRequest( String jsonDto ) throws URISyntaxException {
        String postPath = GenericOpencellRestfulAPIv1.API_VERSION + uriInfo.getPath();
        URI redirectURI = null;
        segmentsOfPathAPIv2 = uriInfo.getPathSegments();
        queryParamsMap = uriInfo.getQueryParameters();
        queryParams = new StringBuilder( QUERY_PARAM_SEPARATOR );
        for( String aKey : queryParamsMap.keySet() ){
            queryParams.append( aKey + QUERY_PARAM_VALUE_SEPARATOR
                    + queryParamsMap.get( aKey ).get(0).replace( GenericPagingAndFilteringUtils.BLANK_SPACE, GenericPagingAndFilteringUtils.BLANK_SPACE_ENCODED )
                    + PAIR_QUERY_PARAM_SEPARATOR );
        }

        if ( GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.containsKey( postPath ) ) {
            pathIBaseRS = GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.get( postPath );

            if ( pathIBaseRS.equals( "/jobInstance" ) || pathIBaseRS.equals( "/job" ) )
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + METHOD_CREATE_BIS );
            else if ( pathIBaseRS.equals( "/invoice/sendByEmail" ) )
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS );
            else
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + METHOD_CREATE );

            return Response.temporaryRedirect( redirectURI )
                    .entity( Entity.entity(jsonDto, MediaType.APPLICATION_JSON) ).build();
        }
        else if ( GenericOpencellRestfulAPIv1.MAP_NEW_REGEX_PATH_AND_IBASE_RS_PATH.containsKey( postPath ) ) {
            pathIBaseRS = GenericOpencellRestfulAPIv1.MAP_NEW_REGEX_PATH_AND_IBASE_RS_PATH.get( postPath );

            // Handle the generic special endpoint: enable a service
            if ( segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 1 ).getPath().equals(ENABLE_SERVICE) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS
                        + FORWARD_SLASH + segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 2 ).getPath()
                        + FORWARD_SLASH + ENABLE_SERVICE + queryParams.substring( 0, queryParams.length() - 1 ) );
            }
            // Handle the generic special endpoint: disable a service
            else if ( segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 1 ).getPath().equals(DISABLE_SERVICE) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS
                        + FORWARD_SLASH + segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 2 ).getPath()
                        + FORWARD_SLASH + DISABLE_SERVICE + queryParams.substring( 0, queryParams.length() - 1 ) );
            }

            return Response.temporaryRedirect( redirectURI )
                    .entity( Entity.entity(jsonDto, MediaType.APPLICATION_JSON) ).build();
        }
        else {
            if ( postPath.equals( "/v1/invoices/pdfInvoices" ) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + "/invoice/fetchPdfInvoice" );
                return Response.temporaryRedirect( redirectURI )
                        .entity( Entity.entity(jsonDto, MediaType.APPLICATION_JSON) ).build();
            }
            else if ( postPath.equals( "/v1/invoices/xmlInvoices" ) ) {
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + "/invoice/fetchXMLInvoice" );
                return Response.temporaryRedirect( redirectURI )
                        .entity( Entity.entity(jsonDto, MediaType.APPLICATION_JSON) ).build();
            }

            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Override
    public Response putRequest( String jsonDto ) throws URISyntaxException, IOException {
        String putPath = GenericOpencellRestfulAPIv1.API_VERSION + uriInfo.getPath();
        URI redirectURI;
        segmentsOfPathAPIv2 = uriInfo.getPathSegments();
        StringBuilder suffixPathBuilder = new StringBuilder();
        for ( int i = 0; i < segmentsOfPathAPIv2.size() - 1; i++ )
            suffixPathBuilder.append( FORWARD_SLASH + segmentsOfPathAPIv2.get(i).getPath() );
        String pathUpdateAnEntity = GenericOpencellRestfulAPIv1.API_VERSION + suffixPathBuilder.toString();

        if ( GenericOpencellRestfulAPIv1.MAP_NEW_REGEX_PATH_AND_IBASE_RS_PATH.containsKey( putPath ) ) {
            pathIBaseRS = GenericOpencellRestfulAPIv1.MAP_NEW_REGEX_PATH_AND_IBASE_RS_PATH.get( putPath );

            Class entityDtoClass = GenericOpencellRestfulAPIv1.MAP_SPECIAL_IBASE_RS_PATH_AND_DTO_CLASS.get( pathIBaseRS );
            Object aDto = null;

            if ( entityDtoClass != null ) {
                aDto = new ObjectMapper().readValue( jsonDto, entityDtoClass );

                // Handle the special endpoint, such as: activation, suspension, termination, update services of a subscription,
                // cancel/validate an existing invoice, cancel/validate a billing run, send an existing invoice by email
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS );

                if ( aDto instanceof ActivateSubscriptionRequestDto )
                    ((ActivateSubscriptionRequestDto) aDto).setSubscriptionCode( segmentsOfPathAPIv2.get(segmentsOfPathAPIv2.size() - 2).toString() );
                else if ( aDto instanceof OperationSubscriptionRequestDto )
                    ((OperationSubscriptionRequestDto) aDto).setSubscriptionCode( segmentsOfPathAPIv2.get(segmentsOfPathAPIv2.size() - 2).toString() );
                else if ( aDto instanceof TerminateSubscriptionRequestDto )
                    ((TerminateSubscriptionRequestDto) aDto).setSubscriptionCode( segmentsOfPathAPIv2.get(segmentsOfPathAPIv2.size() - 2).toString() );
                else if ( aDto instanceof UpdateServicesRequestDto )
                    ((UpdateServicesRequestDto) aDto).setSubscriptionCode( segmentsOfPathAPIv2.get(segmentsOfPathAPIv2.size() - 2).toString() );
                else if ( aDto instanceof CancelInvoiceRequestDto )
                    ((CancelInvoiceRequestDto) aDto).setInvoiceId( Long.parseLong(segmentsOfPathAPIv2.get(segmentsOfPathAPIv2.size() - 2).toString()) );
                else if ( aDto instanceof CancelBillingRunRequestDto )
                    ((CancelBillingRunRequestDto) aDto).setBillingRunId( Long.parseLong(segmentsOfPathAPIv2.get(segmentsOfPathAPIv2.size() - 2).toString()) );
                else if ( aDto instanceof ValidateInvoiceRequestDto )
                    ((ValidateInvoiceRequestDto) aDto).setInvoiceId( Long.parseLong(segmentsOfPathAPIv2.get(segmentsOfPathAPIv2.size() - 2).toString()) );
                else if ( aDto instanceof ValidateBillingRunRequestDto )
                    ((ValidateBillingRunRequestDto) aDto).setBillingRunId( Long.parseLong(segmentsOfPathAPIv2.get(segmentsOfPathAPIv2.size() - 2).toString()) );
            }
            else {
                // Handle the special endpoint, such as: stop a job
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + FORWARD_SLASH + segmentsOfPathAPIv2.get(segmentsOfPathAPIv2.size() - 2).toString() );
            }

            return AuthenticationFilter.httpClient.target( redirectURI ).request()
                    .put( Entity.entity(aDto, MediaType.APPLICATION_JSON) );
        }
        else if ( GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.containsKey( pathUpdateAnEntity ) ) {
            pathIBaseRS = GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.get( pathUpdateAnEntity );
            entityClassName = pathIBaseRS.split( FORWARD_SLASH )[ pathIBaseRS.split( FORWARD_SLASH ).length - 1 ];

            if ( entityClassName.equals( "job" ) )
                entityClassName = "jobInstance";
            else if ( entityClassName.equals( "timer" ) )
                entityClassName = "timerEntity";

            Class entityDtoClass = GenericHelper.getEntityDtoClass( entityClassName.toLowerCase() + DTO_SUFFIX );
            entityCode = segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 1 ).getPath();
            Object aDto = new ObjectMapper().readValue( jsonDto, entityDtoClass );
            if ( aDto instanceof BusinessEntityDto )
                ((BusinessEntityDto) aDto).setCode(entityCode);
            else if ( aDto instanceof IEntityDto )
                ((IEntityDto) aDto).setId(Long.parseLong(entityCode));
            else if ( aDto instanceof AccountHierarchyDto )
                ((AccountHierarchyDto) aDto).setCustomerCode(entityCode);
            else if ( aDto instanceof AccessDto )
                ((AccessDto) aDto).setCode(entityCode);
            else if ( aDto instanceof LanguageDto )
                ((LanguageDto) aDto).setCode(entityCode);
            else if ( aDto instanceof CountryDto )
                ((CountryDto) aDto).setCountryCode(entityCode);
            else if ( aDto instanceof CurrencyDto )
                ((CurrencyDto) aDto).setCode(entityCode);
            else if ( aDto instanceof UserDto )
                ((UserDto) aDto).setUsername(entityCode);

            redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                    + API_REST + pathIBaseRS + METHOD_UPDATE );

            return AuthenticationFilter.httpClient.target( redirectURI ).request()
                    .put( Entity.entity( aDto, MediaType.APPLICATION_JSON ) );
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response deleteAnEntity() throws URISyntaxException {
        segmentsOfPathAPIv2 = uriInfo.getPathSegments();
        URI redirectURI;
        StringBuilder suffixPathBuilder = new StringBuilder();
        for (int i = 0; i < segmentsOfPathAPIv2.size() - 1; i++ )
            suffixPathBuilder.append( FORWARD_SLASH + segmentsOfPathAPIv2.get(i).getPath() );
        String deletePath = GenericOpencellRestfulAPIv1.API_VERSION + suffixPathBuilder.toString();

        if ( GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.containsKey( deletePath ) ) {
            pathIBaseRS = GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.get( deletePath );
            entityCode = segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 1 ).getPath();

            if ( pathIBaseRS.equals( PAYMENT_METHOD ) )
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + QUERY_PARAM_SEPARATOR + "id=" + entityCode);
            else
                redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                        + API_REST + pathIBaseRS + METHOD_DELETE + entityCode);
            return Response.temporaryRedirect( redirectURI ).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response postCreationOrUpdate( String jsonDto ) throws JsonProcessingException, URISyntaxException {
        URI redirectURI;
        StringBuilder aPathBd = new StringBuilder(GenericOpencellRestfulAPIv1.API_VERSION);
        segmentsOfPathAPIv2 = uriInfo.getPathSegments();

        entityCode = segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 2 ).toString();

        if ( segmentsOfPathAPIv2.size() >= 3 ) {
            entityClassName = Inflector.getInstance().singularize(segmentsOfPathAPIv2.get( segmentsOfPathAPIv2.size() - 3 ));
            Class entityDtoClass = GenericHelper.getEntityDtoClass( entityClassName.toLowerCase() + DTO_SUFFIX );
            Object aDto = new ObjectMapper().readValue( jsonDto, entityDtoClass );
            if ( aDto instanceof BusinessEntityDto )
                ((BusinessEntityDto) aDto).setCode(entityCode);
            else if ( aDto instanceof AccountHierarchyDto )
                ((AccountHierarchyDto) aDto).setCustomerCode(entityCode);
            else if ( aDto instanceof AccessDto )
                ((AccessDto) aDto).setCode(entityCode);

            for ( int i = 0; i <= segmentsOfPathAPIv2.size() - 3; i++ )
                aPathBd.append( FORWARD_SLASH + segmentsOfPathAPIv2.get(i) );
            String aPath = aPathBd.toString();
            if ( GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.containsKey(aPath) ) {
                pathIBaseRS = GenericOpencellRestfulAPIv1.MAP_NEW_PATH_AND_IBASE_RS_PATH.get(aPath);

                if ( aPath.equals("/v1/accountManagement/customerCategories") )
                    redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                            + API_REST + pathIBaseRS + "/createOrUpdateCategory" );
                else if ( aPath.equals("/v1/accountManagement/customerBrands") )
                    redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                            + API_REST + pathIBaseRS + "/createOrUpdateBrand" );
                else
                    redirectURI = new URI( uriInfo.getBaseUri().toString().substring(0, uriInfo.getBaseUri().toString().length() - 3 )
                            + API_REST + pathIBaseRS + METHOD_CREATE_OR_UPDATE );

                return AuthenticationFilter.httpClient.target( redirectURI ).request()
                        .post( Entity.entity( aDto, MediaType.APPLICATION_JSON ) );
            }
            else {
                ActionStatus notFoundStatus = new ActionStatus(ActionStatusEnum.FAIL,
                        MeveoApiErrorCodeEnum.URL_NOT_FOUND, "The specified URL cannot be found");

                return Response.status(Response.Status.NOT_FOUND)
                        .entity(notFoundStatus).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }
        else {
            ActionStatus notFoundStatus = new ActionStatus(ActionStatusEnum.FAIL,
                    MeveoApiErrorCodeEnum.URL_NOT_FOUND, "The specified URL cannot be found");

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(notFoundStatus).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    @Override
    public Response getListRestEndpoints() {
        return Response.ok().entity(GenericOpencellRestfulAPIv1.RESTFUL_ENTITIES_MAP).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response getListRestEndpointsForEntity(String entityName) {
        if ( GenericOpencellRestfulAPIv1.RESTFUL_ENTITIES_MAP.containsKey( StringUtils.capitalizeFirstLetter(entityName) ) ) {
            entityName = StringUtils.capitalizeFirstLetter(entityName);
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put( entityName, GenericOpencellRestfulAPIv1.RESTFUL_ENTITIES_MAP.get( entityName ) );
            return Response.ok().entity(responseMap).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        else {
            ActionStatus notFoundStatus = new ActionStatus(ActionStatusEnum.FAIL,
                    MeveoApiErrorCodeEnum.ENTITY_DOES_NOT_EXISTS_EXCEPTION, "Entity " + entityName + " cannot be found");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(notFoundStatus).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    @Override
    public Response getApiVersion() {
        ActionStatus successfulStatus = new ActionStatus(ActionStatusEnum.SUCCESS,
                "Opencell core version " + Version.appVersion + ", Opencell Rest API version " + GenericOpencellRestfulAPIv1.API_VERSION.substring(1)
                        + ", commit " + Version.buildNumber + " , build at " + Version.build_time );

        return Response.status(Response.Status.OK).entity(successfulStatus).build();
    }

    @PreDestroy
    public void destroy() {
        AuthenticationFilter.httpClient.close();
    }
}
