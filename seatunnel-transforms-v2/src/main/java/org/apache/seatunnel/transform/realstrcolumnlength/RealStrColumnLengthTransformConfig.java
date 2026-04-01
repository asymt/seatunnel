/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.transform.realstrcolumnlength;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Data
@AllArgsConstructor
public class RealStrColumnLengthTransformConfig implements Serializable {
    public static final Charset UTF8 = StandardCharsets.UTF_8;
    public static final Charset GBK = Charset.forName("GBK");

    public static final Option<String> SOURCE_CHARSET =
            Options.key("source_charset")
                    .stringType()
                    .defaultValue(UTF8.displayName())
                    .withDescription(
                            "source_charset represents the character set of the source database, with a default value of UTF8");

    public static final Option<String> SINK_CHARSET =
            Options.key("sink_charset")
                    .stringType()
                    .defaultValue(UTF8.displayName())
                    .withDescription("sink_charset represents the character set of the sink database, with a default value of UTF8");


    public static final Option<String> COLUMN_EXCLUDE_REGEX =
            Options.key("column_exclude_regex")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The regex to match the column name to exclude");


}
