/**
 * Copyright 2017 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onap.holmes.rulemgt.dcae;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.onap.holmes.common.dcae.DcaeConfigurationQuery;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dcae.entity.Rule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.GsonUtil;
import org.onap.holmes.common.utils.HttpsUtils;
import org.onap.holmes.common.utils.Md5Util;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;

@Slf4j
public class DcaeConfigurationPolling implements Runnable {

    public static long POLLING_PERIOD = 30 * 1000L;

    private String hostname;

    private String url = "https://127.0.0.1:9101/api/holmes-rule-mgmt/v1/rule";

    public DcaeConfigurationPolling(String hostname) {
        this.hostname = hostname;
    }

    private String prevConfigMd5 = Md5Util.md5(null);

    private boolean prevResult = false;

    @Override
    public void run() {
        DcaeConfigurations dcaeConfigurations = null;
        try {
            dcaeConfigurations = DcaeConfigurationQuery.getDcaeConfigurations(hostname);
            String md5 = Md5Util.md5(dcaeConfigurations);
            if (prevResult && prevConfigMd5.equals(md5)){
                log.info("Operation aborted due to identical Configurations.");
                return;
            }
            prevConfigMd5 = md5;
            prevResult = false;
        } catch (CorrelationException e) {
            log.error("Failed to fetch DCAE configurations. " + e.getMessage(), e);
        } catch (Exception e) {
            log.info("Failed to generate the MD5 information for new configurations.", e);
        }
        RuleQueryListResponse ruleQueryListResponse = null;
        if (dcaeConfigurations != null) {
            try {
                ruleQueryListResponse = getAllCorrelationRules();
            } catch (CorrelationException e) {
                log.error("Failed to get right response!" + e.getMessage(), e);
            } catch (IOException e) {
                log.error("Failed to extract response entity. " + e.getMessage(), e);
            }
        }
        if (ruleQueryListResponse != null) {
            List<RuleResult4API> ruleResult4APIs = ruleQueryListResponse.getCorrelationRules();
            deleteAllCorrelationRules(ruleResult4APIs);
            try {
                prevResult = addAllCorrelationRules(dcaeConfigurations);
            } catch (CorrelationException e) {
                log.error("Failed to add rules. " + e.getMessage(), e);
                prevResult = false;
            }
        }
    }

    public RuleQueryListResponse getAllCorrelationRules() throws CorrelationException, IOException {
              HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON);
        HttpResponse httpResponse = HttpsUtils.get(url, headers);
        String response = HttpsUtils.extractResponseEntity(httpResponse);
        return JSON.parseObject(response,RuleQueryListResponse.class);
    }

    private boolean addAllCorrelationRules(DcaeConfigurations dcaeConfigurations) throws CorrelationException {
        boolean suc = false;
        for (Rule rule : dcaeConfigurations.getDefaultRules()) {
            RuleCreateRequest ruleCreateRequest = getRuleCreateRequest(rule);
            String content = "";
            try {
                content = GsonUtil.beanToJson(ruleCreateRequest);
            } catch (Exception e) {
                throw new CorrelationException("Failed to convert the message object to a json string.", e);
            }
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON);
            headers.put("Accept", MediaType.APPLICATION_JSON);
            HttpResponse httpResponse;
            try {
                httpResponse = HttpsUtils
                        .put(url, headers, new HashMap<>(), new StringEntity(content));
            } catch (UnsupportedEncodingException e) {
                throw new CorrelationException("Failed to create https entity.", e);
            } catch (Exception e) {
                throw new CorrelationException(e.getMessage());
            }
            if (httpResponse != null) {
                suc = httpResponse.getStatusLine().getStatusCode() == 200;
            }
            if (!suc) {
                break;
            }
        }
        return suc;
    }

    private void deleteAllCorrelationRules(List<RuleResult4API> ruleResult4APIs){
        ruleResult4APIs.forEach(correlationRule ->{
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON);
            try {
                HttpsUtils.delete(url + "/" + correlationRule.getRuleId(), headers);
            } catch (Exception e) {
                log.warn("Failed to delete rule, the rule id is : " + correlationRule.getRuleId()
                        + " exception messge is : " + e.getMessage(), e);
            }
        });
    }

    private RuleCreateRequest getRuleCreateRequest(Rule rule) {
        RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        ruleCreateRequest.setLoopControlName(rule.getLoopControlName());
        ruleCreateRequest.setRuleName(rule.getName());
        ruleCreateRequest.setContent(rule.getContents());
        ruleCreateRequest.setDescription("");
        ruleCreateRequest.setEnabled(1);
        return ruleCreateRequest;
    }
}
