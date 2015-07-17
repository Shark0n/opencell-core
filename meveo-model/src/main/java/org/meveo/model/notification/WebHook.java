package org.meveo.model.notification;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ADM_NOTIF_WEBHOOKS")
public class WebHook extends Notification {

    private static final long serialVersionUID = -2527123286118840886L;

    @Column(name = "HTTP_HOST", length = 255, nullable = false)
    @NotNull
    @Size(max = 255)
    private String host;

    @Column(name = "HTTP_PORT")
    @NotNull
    @Max(65535)
    private int port;

    @Column(name = "HTTP_PAGE", length = 255)
    @NotNull
    @Size(max = 255)
    private String page;

    @Column(name = "HTTP_METHOD", nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private WebHookMethodEnum httpMethod;

    @Column(name = "USERNAME", length = 255)
    @Size(max = 255)
    private String username;

    @Column(name = "PASSWORD", length = 255)
    @Size(max = 255)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ADM_NOTIF_WEBHOOK_HEADER")
    private Map<String, String> headers = new HashMap<String, String>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ADM_NOTIF_WEBHOOK_PARAM")
    private Map<String, String> params = new HashMap<String, String>();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public WebHookMethodEnum getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(WebHookMethodEnum httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return String.format("WebHook [host=%s, port=%s, page=%s, httpMethod=%s, username=%s, password=%s, headers=%s, params=%s, notification=%s]", host, port, page, httpMethod,
            username, password, headers != null ? toString(headers.entrySet(), maxLen) : null, params != null ? toString(params.entrySet(), maxLen) : null, super.toString());
    }

    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }
}