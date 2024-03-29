/**
 * Copyright 2021-2022 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.holmes.rulemgt.dcae;

import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.onap.holmes.common.ConfigFileScanner;
import org.onap.holmes.common.utils.FileUtils;
import org.onap.holmes.common.utils.JerseyClient;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JerseyClient.class})
@SuppressStaticInitializationFor({"org.onap.holmes.common.utils.JerseyClient"})
public class ConfigFileScanningTaskTest {

    @Rule
    public final SystemOutRule systemOut = new SystemOutRule().enableLog();

    @Test
    public void run_failed_to_get_existing_rules() throws Exception {
        System.setProperty("ENABLE_ENCRYPT", "true");

        String indexPath = getFilePath("index-add.json");

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(null);
        Whitebox.setInternalState(cfst, "configFile", indexPath);

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andThrow(new RuntimeException());

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        assertThat(systemOut.getLog(), containsString("Failed to get existing rules for comparison."));
    }

    @Test
    public void run_add_rules() throws Exception {
        System.setProperty("ENABLE_ENCRYPT", "true");

        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String indexPath = getFilePath("index-add.json");
        String contents = FileUtils.readTextFile(indexPath);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(null);
        Whitebox.setInternalState(cfst, "configFile", indexPath);

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        // mock for deployRule
        EasyMock.expect(jcMock.header(EasyMock.anyString(), EasyMock.anyObject())).andReturn(jcMock).times(2);
        EasyMock.expect(jcMock.put(EasyMock.anyString(), EasyMock.anyObject())).andReturn("");

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        assertThat(systemOut.getLog(), containsString("Rule 'ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b' has been deployed."));

        System.clearProperty("ENABLE_ENCRYPT");
    }

    @Test
    public void run_remove_rules_normal() throws Exception {
        System.setProperty("ENABLE_ENCRYPT", "false");

        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String indexPath = getFilePath("index-add.json");
        String contents = FileUtils.readTextFile(indexPath);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(new ConfigFileScanner());
        Whitebox.setInternalState(cfst, "configFile", getFilePath("index-empty.json"));

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API(clName, contents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        // mock for deleteRule
        EasyMock.expect(jcMock.delete(EasyMock.anyString())).andReturn("");

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        assertThat(systemOut.getLog(), containsString("Rule 'ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b' has been removed."));

        System.clearProperty("ENABLE_ENCRYPT");
    }

    @Test
    public void run_remove_rules_api_calling_returning_null() throws Exception {
        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String indexPath = getFilePath("index-add.json");
        String contents = FileUtils.readTextFile(indexPath);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(new ConfigFileScanner());
        Whitebox.setInternalState(cfst, "configFile", indexPath);

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API(clName, contents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        // mock for deleteRule
        EasyMock.expect(jcMock.delete(EasyMock.anyString())).andReturn(null);

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        assertThat(systemOut.getLog(), containsString("Failed to delete rule, the rule id is: ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b"));
    }

    @Test
    public void run_change_rules_normal() throws Exception {
        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String oldDrlPath = getFilePath("ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b.drl");
        String oldDrlContents = FileUtils.readTextFile(oldDrlPath);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(new ConfigFileScanner());
        Whitebox.setInternalState(cfst, "configFile", getFilePath("index-rule-changed.json"));

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API(clName, oldDrlContents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        // mock for deleteRule
        EasyMock.expect(jcMock.delete(EasyMock.anyString())).andReturn("");

        // mock for deployRule
        EasyMock.expect(jcMock.header(EasyMock.anyString(), EasyMock.anyObject())).andReturn(jcMock).times(2);
        EasyMock.expect(jcMock.put(EasyMock.anyString(), EasyMock.anyObject())).andReturn("");

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        assertThat(systemOut.getLog(), containsString("Rule 'ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b' has been updated."));
    }

    @Test
    public void run_change_rules_no_change_except_for_spaces() throws Exception {
        String clName = "ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b";
        String oldDrlPath = getFilePath("ControlLoop-VOLTE-2179b738-fd36-4843-a71a-a8c24c70c55b.drl");
        String oldDrlContents = FileUtils.readTextFile(oldDrlPath);

        ConfigFileScanningTask cfst = new ConfigFileScanningTask(new ConfigFileScanner());
        Whitebox.setInternalState(cfst, "configFile", getFilePath("index-rule-spaces-test.json"));

        // mock for getExistingRules
        JerseyClient jcMock = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(jcMock).anyTimes();
        RuleQueryListResponse rqlr = new RuleQueryListResponse();
        rqlr.getCorrelationRules().add(getRuleResult4API(clName, oldDrlContents));
        EasyMock.expect(jcMock.get(EasyMock.anyString(), EasyMock.anyObject())).andReturn(rqlr);

        PowerMock.replayAll();
        cfst.run();
        PowerMock.verifyAll();

        assertThat(systemOut.getLog(), not(containsString("has been updated.")));
    }

    private String getFilePath(String fileName) {
        return ConfigFileScanningTaskTest.class.getResource("/" + fileName).getFile();
    }

    private RuleResult4API getRuleResult4API(String clName, String contents) {
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setRuleId(clName);
        ruleResult4API.setRuleName(clName);
        ruleResult4API.setLoopControlName(clName);
        ruleResult4API.setContent(contents);
        ruleResult4API.setDescription("");
        ruleResult4API.setEnabled(1);
        ruleResult4API.setCreator("__SYSTEM__DEFAULT__");
        return ruleResult4API;
    }
}