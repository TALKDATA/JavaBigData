/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.flume.sink.hbase;

import com.google.common.base.Charsets;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.conf.ComponentConfiguration;
import org.apache.flume.sink.hbase.SimpleHbaseEventSerializer.KeyType;
import org.hbase.async.AtomicIncrementRequest;
import org.hbase.async.PutRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple serializer to be used with the AsyncHBaseSink
 * that returns puts from an event, by writing the event
 * body into it. The headers are discarded. It also updates a row in hbase
 * which acts as an event counter.
 *
 * Takes optional parameters:<p>
 * <tt>rowPrefix:</tt> The prefix to be used. Default: <i>default</i><p>
 * <tt>incrementRow</tt> The row to increment. Default: <i>incRow</i><p>
 * <tt>suffix:</tt> <i>uuid/random/timestamp.</i>Default: <i>uuid</i><p>
 *
 * Mandatory parameters: <p>
 * <tt>cf:</tt>Column family.<p>
 * Components that have no defaults and will not be used if absent:
 * <tt>payloadColumn:</tt> Which column to put payload in. If it is not present,
 * event data will not be written.<p>
 * <tt>incrementColumn:</tt> Which column to increment. If this is absent, it
 *  means no column is incremented.
 */
public class KfkAsyncHbaseEventSerializer implements AsyncHbaseEventSerializer {
    private byte[] table;
    private byte[] cf;
    private byte[] payload;
    private byte[] payloadColumn;
    private byte[] incrementColumn;
    private String rowPrefix;
    private byte[] incrementRow;
    private KeyType keyType;

    @Override
    public void initialize(byte[] table, byte[] cf) {
        this.table = table;
        this.cf = cf;
    }

    @Override
    public List<PutRequest> getActions() {
        List<PutRequest> actions = new ArrayList<>();
        if (payloadColumn != null) {
            byte[] rowKey;
            try {
                /*---------------------------代码修改开始---------------------------------*/
                //解析列字段
                String[] columns = new String(this.payloadColumn).split(",");
                //解析flume采集过来的每行的值
                String[] values = new String(this.payload).split(",");
                for(int i=0;i < columns.length;i++) {
                    byte[] colColumn = columns[i].getBytes();
                    byte[] colValue = values[i].getBytes(Charsets.UTF_8);

                    //数据校验：字段和值是否对应
                    if (colColumn.length != colValue.length) break;

                    //时间
                    String datetime = values[0].toString();
                    //用户id
                    String userid = values[1].toString();
                    //根据业务自定义Rowkey
                    rowKey = SimpleRowKeyGenerator.getKfkRowKey(userid, datetime);
                    //插入数据
                    PutRequest putRequest = new PutRequest(table, rowKey, cf,
                            colColumn, colValue);
                    actions.add(putRequest);
                    /*---------------------------代码修改结束---------------------------------*/
                }
            } catch (Exception e) {
                throw new FlumeException("Could not get row key!", e);
            }
        }
        return actions;
    }

    public List<AtomicIncrementRequest> getIncrements() {
        List<AtomicIncrementRequest> actions = new ArrayList<AtomicIncrementRequest>();
        if (incrementColumn != null) {
            AtomicIncrementRequest inc = new AtomicIncrementRequest(table,
                    incrementRow, cf, incrementColumn);
            actions.add(inc);
        }
        return actions;
    }

    @Override
    public void cleanUp() {
        // TODO Auto-generated method stub

    }

    @Override
    public void configure(Context context) {
        String pCol = context.getString("payloadColumn", "pCol");
        String iCol = context.getString("incrementColumn", "iCol");
        rowPrefix = context.getString("rowPrefix", "default");
        String suffix = context.getString("suffix", "uuid");
        if (pCol != null && !pCol.isEmpty()) {
            if (suffix.equals("timestamp")) {
                keyType = KeyType.TS;
            } else if (suffix.equals("random")) {
                keyType = KeyType.RANDOM;
            } else if (suffix.equals("nano")) {
                keyType = KeyType.TSNANO;
            } else {
                keyType = KeyType.UUID;
            }
            payloadColumn = pCol.getBytes(Charsets.UTF_8);
        }
        if (iCol != null && !iCol.isEmpty()) {
            incrementColumn = iCol.getBytes(Charsets.UTF_8);
        }
        incrementRow = context.getString("incrementRow", "incRow").getBytes(Charsets.UTF_8);
    }

    @Override
    public void setEvent(Event event) {
        this.payload = event.getBody();
    }

    @Override
    public void configure(ComponentConfiguration conf) {
        // TODO Auto-generated method stub
    }

}
