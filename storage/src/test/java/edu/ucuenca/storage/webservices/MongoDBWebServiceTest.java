/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ucuenca.storage.webservices;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.http.ContentType;
import org.apache.marmotta.platform.core.test.base.JettyMarmotta;
import static org.hamcrest.Matchers.containsString;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class MongoDBWebServiceTest {

    private static JettyMarmotta marmotta;

    @BeforeClass
    public static void beforeClass() {
        marmotta = new JettyMarmotta("/redi-test", 9090, MongoDBWebService.class);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 9090;
        RestAssured.basePath = "/redi-test";
        RestAssured.config = RestAssuredConfig.newConfig().decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"));
    }

    @AfterClass
    public static void afterClass() {
        if (marmotta != null) {
            marmotta.shutdown();
        }
    }

    @Test
    @Ignore("TODO")
    public void testHello() {
        /*
         * GET ?name=<xxx>
         */
        RestAssured.given()
                .param("name", "Steve")
                .expect()
                .content(containsString("Hello Steve"))
                .when()
                .get("/mongo");

        RestAssured.expect()
                .statusCode(400)
                .when()
                .get("/mongo");
    }

    @Test
    @Ignore("TODO")
    public void testNonAsciiHello() {
        /*
         * GET ?name=<xxx>
         */
        RestAssured.given()
                .param("name", "Jürgen")
                .expect()
                .contentType(ContentType.TEXT)
                .content(containsString("Hello Jürgen"))
                .when()
                .get("/mongo");

        RestAssured.expect()
                .statusCode(400)
                .when()
                .get("/mongo");
    }

    @Test
    @Ignore("TODO")
    public void testDoThis() {
        /*
         * POST ?turns=i default 2
         */
        RestAssured.given()
                .param("turns", 1)
                .expect()
                .statusCode(204)
                .when()
                .post("/mongo");

        RestAssured.given()
                .param("turns", 10)
                .expect()
                .statusCode(204)
                .when()
                .post("/mongo");

        RestAssured.expect()
                .statusCode(204)
                .when()
                .post("/mongo");

        RestAssured.given()
                .param("turns", 123)
                .expect()
                .statusCode(500)
                .when()
                .post("/mongo");
    }

}
