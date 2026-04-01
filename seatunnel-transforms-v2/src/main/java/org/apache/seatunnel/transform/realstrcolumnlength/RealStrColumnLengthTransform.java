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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.table.catalog.*;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SqlType;
import org.apache.seatunnel.transform.common.AbstractCatalogSupportMapTransform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class RealStrColumnLengthTransform extends AbstractCatalogSupportMapTransform {

    public static final String PLUGIN_NAME = "RealStrColumnLength";

    private String sourceCharset;
    private String sinkCharset;
    private double lengthRatio;
    private Pattern columnExcludeRegexPattern;
    public RealStrColumnLengthTransform(
            @NonNull ReadonlyConfig config,  @NonNull CatalogTable inputCatalogTable) {
        super(inputCatalogTable);
        initConfig(config);
    }


    private void initConfig(ReadonlyConfig config) {
        sourceCharset = config.get(RealStrColumnLengthTransformConfig.SOURCE_CHARSET);
        sinkCharset = config.get(RealStrColumnLengthTransformConfig.SINK_CHARSET);
        if(StringUtils.isNotBlank(config.get(RealStrColumnLengthTransformConfig.COLUMN_EXCLUDE_REGEX))){
            columnExcludeRegexPattern = Pattern.compile(config.get(RealStrColumnLengthTransformConfig.COLUMN_EXCLUDE_REGEX), Pattern.CASE_INSENSITIVE);
        }
        if (sourceCharset.equalsIgnoreCase(sinkCharset)) {
            lengthRatio = 1.0;
        } else if (RealStrColumnLengthTransformConfig.UTF8.displayName().equalsIgnoreCase(sourceCharset)) {
            lengthRatio = 0.5;
        } else {
            lengthRatio = 2.0;
        }
        log.info("sourceCharset:{},sinkCharset:{},lengthRatio:{}",sourceCharset,sinkCharset,lengthRatio);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    protected TableIdentifier transformTableIdentifier() {
        return inputCatalogTable.getTableId();
    }

    @Override
    protected SeaTunnelRow transformRow(SeaTunnelRow inputRow) {
        return inputRow;
    }

    @Override
    protected TableSchema transformTableSchema() {
        log.info("transformTableSchema:{} is started by RealStrFieldLength",inputCatalogTable.getTableId());
        return TableSchema.builder()
                .primaryKey(inputCatalogTable.getTableSchema().getPrimaryKey())
                .constraintKey(inputCatalogTable.getTableSchema().getConstraintKeys())
                .columns(
                        inputCatalogTable.getTableSchema().getColumns().stream()
                                .map(
                                        column -> {
                                            if (!column.getDataType().getSqlType().equals(SqlType.STRING)
                                                    ||column.getColumnLength() == null
                                                    || column.getColumnLength() <= 0
                                                    ||StringUtils.isBlank(column.getSourceType())) {
                                                return column;
                                            }
                                            return getNewColumn(column);
                                        })
                                .collect(Collectors.toList()))
                .build();
    }

    private Column getNewColumn(Column column) {
        String sourceType=column.getSourceType();
        log.info("column:{},sourceType:{}",column.getName(),sourceType);
        Long columnLength=column.getColumnLength();
        String sourceTypeLength = sourceType.replaceAll(".*\\((\\d+)\\)", "$1");
        if(StringUtils.isBlank(sourceTypeLength)||sourceType.equalsIgnoreCase(sourceTypeLength)){
            return column;
        }
        log.info("column:{},sourceTypeLength:{}",column.getName(),sourceTypeLength);
        double currentRatio = lengthRatio;
        if ((columnExcludeRegexPattern!=null&&columnExcludeRegexPattern.matcher(column.getName()).find())){
            currentRatio = 1.0;
        }
        Long finalLength = (long) (Math.min(columnLength, Long.parseLong(sourceTypeLength))*currentRatio);
        PhysicalColumn.PhysicalColumnBuilder physicalColumnBuilder = PhysicalColumn.builder()
                .name(column.getName())
                .dataType(column.getDataType())
                .columnLength(finalLength)
                .nullable(column.isNullable())
                .defaultValue(column.getDefaultValue())
                .comment(column.getComment()).sourceType(sourceType).options(column.getOptions());
        return physicalColumnBuilder.build();
    }

}
