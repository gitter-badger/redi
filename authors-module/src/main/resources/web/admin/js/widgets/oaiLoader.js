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


function extractAuthorsOAI () {
    var host = _SERVER_URL;
    var name = $("#oai_endpoint_name").val();
    var endpoint = $("#endpoint").val();
    alert (name);
    
    
    $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/oaiExtraction?name="+name+"&endpoint="+endpoint,
        success: function (Result) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Correcto: " + Result);
        },
        error: function (data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });
    
}