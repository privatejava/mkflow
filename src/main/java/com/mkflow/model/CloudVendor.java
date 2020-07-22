/*
 * Copyright 2020 Mkflow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.mkflow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat
public enum CloudVendor {
    AMAZON("aws"), AZURE("azure"), GOOGLE("google");

    private String shortName;

    CloudVendor(String shortName) {
        this.shortName = shortName;
    }

    @JsonCreator
    public static CloudVendor parse(String shortName) {
        for (CloudVendor t : values()) {
            if (t.shortName.equalsIgnoreCase(shortName)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid cloud type provided");
    }

    @JsonValue
    public String getShortName() {
        return shortName;
    }
}

