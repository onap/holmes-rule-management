/*
 * Copyright 2017-2022 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.holmes.rulemgt.resources;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;

@RestController
@RequestMapping("/swagger.json")
@Slf4j
public class SwaggerResource {

    @GetMapping
    @Produces(MediaType.APPLICATION_JSON)
    public String getSwaggerJson() {
        URL url = SwaggerResource.class.getResource("/swagger.json");
        String ret = "{}";

        File file;
        try {
            System.out.println(URLDecoder.decode(url.getPath(), "UTF-8"));
            file = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
        } catch(IOException e) {
            log.warn("An error occurred while reading swagger.json.", e);
            return ret;
        }
        try(BufferedReader br = new BufferedReader(new FileReader(file));)  {
            StringBuffer buffer = new StringBuffer();
            String line = " ";
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            ret = buffer.toString();
        } catch (FileNotFoundException e) {
            log.warn("Failed to read the API description file.", e);
        } catch (IOException e) {
            log.warn("An error occurred while reading swagger.json.", e);
        }
        return ret;
    }
}
