package org.motechproject.sms.templates;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * todo
 */
public class Request {
    private String urlPath;
    private String recipientsSeparator;
    private Map<String, String> queryParameters = new HashMap<String, String>();
    private Map<String, String> bodyParameters = new HashMap<String, String>();
    private HttpMethodType type;
    private Map<String, String> props = null;
    private String processedUrlPath = null;

    public String getUrlPath(Map<String, String> props) {
        if (!props.equals(this.props)) {
            StringBuffer sb = new StringBuffer();
            Pattern p = Pattern.compile("(\\[[^\\]]+\\])");
            Matcher m = p.matcher(urlPath);

            while (m.find())
            {
                String repString = props.get(m.group(1).substring(1, m.group(1).length()-1));
                if (repString != null)
                    m.appendReplacement(sb, repString);
            }
            m.appendTail(sb);
            processedUrlPath = sb.toString();
            this.props = props;
        }
        return processedUrlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getRecipientsSeparator() {
        return recipientsSeparator;
    }

    public void setRecipientsSeparator(String recipientsSeparator) {
        this.recipientsSeparator = recipientsSeparator;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        if (queryParameters != null) {
            this.queryParameters = queryParameters;
        }
    }

    public HttpMethodType getType() {
        return type;
    }

    public void setType(HttpMethodType type) {
        this.type = type;
    }

    public Map<String, String> getBodyParameters() {
        return bodyParameters;
    }

    public void setBodyParameters(Map<String, String> bodyParameters) {
        this.bodyParameters = bodyParameters;
    }
}